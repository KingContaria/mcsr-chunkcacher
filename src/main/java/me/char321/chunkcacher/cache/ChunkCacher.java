package me.char321.chunkcacher.cache;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import me.char321.chunkcacher.mixin.access.*;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("UnreachableCode")
public class ChunkCacher {
    public static CachedChunk cache(ProtoChunk chunk, ServerWorld world) {
        /*
        pos immutable
        biomes immutable
        lightingProvider chunks aren't lit yet, is null
        heightmaps copied
        upgradeData should be NO_UPGRADE_DATA
         */
        CachedChunk res = new CachedChunk();

        res.biomes = chunk.getBiomeArray();

        if (chunk.getUpgradeData() != UpgradeData.NO_UPGRADE_DATA) {
            throw new UnsupportedOperationException("caching upgrade data is not supported");
        }
        res.upgradeData = chunk.getUpgradeData();

        ChunkPos pos = chunk.getPos();
        res.pos = pos;

        CachedChunkSection[] sections = new CachedChunkSection[16];
        for (int i = 0; i < 16; i++) {
            sections[i] = cacheChunkSection(chunk.getSectionArray()[i]);
        }
        res.sections = sections;

        res.blockTickScheduler = copy(chunk.getBlockTickScheduler(), world);
        res.fluidTickScheduler = copy(chunk.getFluidTickScheduler(), world);

        res.heightmaps = cacheHeightmaps(chunk.getHeightmaps());

        res.status = chunk.getStatus();

        res.blockEntities = cacheBlockEntities(chunk);

        res.entities = copy(chunk.getEntities());

        res.lightSources = chunk.getLightSourcesStream().collect(Collectors.toList());

        for (int i = 0; i < 16; i++) {
            if (chunk.getPostProcessingLists()[i] != null) {
                Chunk.getList(res.postProcessingLists, i).addAll(chunk.getPostProcessingLists()[i]);
            }
        }

        res.structureStarts = cacheStructureStarts(chunk.getStructureStarts(), pos, world);
        res.structureReferences = copyStructureReferences(chunk.getStructureReferences());

        res.inhabitedTime = chunk.getInhabitedTime();

        res.carvingMasks = copyCarvingMasks(((ProtoChunkAccessor) chunk).getCarvingMasks());

        res.lightOn = chunk.isLightOn();

        return res;
    }

    public static ProtoChunk retrieve(CachedChunk chunk, ServerWorld world) {
        if (chunk == null) return null;
        ChunkPos pos = chunk.pos;
        ChunkSection[] sections = new ChunkSection[16];
        for (int i = 0; i < 16; i++) {
            sections[i] = retrieveChunkSection(chunk.sections[i]);
        }
        if (chunk.upgradeData != UpgradeData.NO_UPGRADE_DATA) {
            throw new UnsupportedOperationException("caching upgrade data is not supported");
        }
        ProtoChunk res = new ProtoChunk(pos, chunk.upgradeData, sections, copy(chunk.blockTickScheduler, world), copy(chunk.fluidTickScheduler, world), world);

        res.setBiomes(chunk.biomes);

        ((ProtoChunkAccessor) res).setHeightmaps(retrieveHeightmaps(chunk.heightmaps, res));

        res.setStatus(chunk.status);

        retrieveBlockEntities(chunk.blockEntities, res);

        res.getEntities().addAll(copy(chunk.entities));

        ((ProtoChunkAccessor) res).getLightSources().addAll(chunk.lightSources);

        for (int i = 0; i < 16; i++) {
            if (chunk.postProcessingLists[i] != null) {
                Chunk.getList(res.getPostProcessingLists(), i).addAll(chunk.postProcessingLists[i]);
            }
        }

        res.setStructureStarts(retrieveStructureStarts(chunk.structureStarts, world, world.getSeed()));
        res.setStructureReferences(copyStructureReferences(chunk.structureReferences));

        res.setInhabitedTime(chunk.inhabitedTime);

        res.setLightOn(chunk.lightOn);

        return res;
    }

    private static @NotNull NbtList cacheBlockEntities(ProtoChunk chunk) {
        NbtList blockEntityList = new NbtList();
        for(BlockPos blockPos : chunk.getBlockEntityPositions()) {
            NbtCompound nbt = chunk.getPackedBlockEntityNbt(blockPos);
            if (nbt == null) {
                throw new IllegalArgumentException("invalid block entity nbt");
            }
            blockEntityList.add(nbt);
        }
        return blockEntityList;
    }

    private static void retrieveBlockEntities(NbtList blockEntities, ProtoChunk chunk) {
        for(int o = 0; o < blockEntities.size(); ++o) {
            NbtCompound blockEntityNbt = blockEntities.getCompound(o);
            chunk.addPendingBlockEntityNbt(blockEntityNbt);
        }
    }

    private static List<NbtCompound> copy(List<NbtCompound> src) {
        List<NbtCompound> res = new ArrayList<>(src.size());
        for(NbtCompound nbtCompound : src) {
            res.add(nbtCompound.copy());
        }
        return res;
    }

    private static <T> ChunkTickScheduler<T> copy(ChunkTickScheduler<T> src, ServerWorld world) {
        Predicate<T> shouldExclude = ((ChunkTickSchedulerAccessor<T>) src).getShouldExclude();
        ChunkPos pos = ((ChunkTickSchedulerAccessor<?>) src).getPos();
        ChunkTickScheduler<T> res = new ChunkTickScheduler<>(shouldExclude, pos, world);
        ShortList[] reslists = ((ChunkTickSchedulerAccessor<T>) res).getScheduledPositions();
        ShortList[] lists = ((ChunkTickSchedulerAccessor<T>) src).getScheduledPositions();

        for (int i = 0; i < lists.length; i++) {
            if (lists[i] != null) {
                Chunk.getList(reslists, i).addAll(lists[i]);
            }
        }
        return res;
    }

    private static Map<Heightmap.Type, PackedIntegerArray> cacheHeightmaps(Collection<Map.Entry<Heightmap.Type, Heightmap>> heightmaps) {
        EnumMap<Heightmap.Type, PackedIntegerArray> res = new EnumMap<>(Heightmap.Type.class);
        for (Map.Entry<Heightmap.Type, Heightmap> entry : heightmaps) {
            res.put(entry.getKey(), cacheHeightmap(entry.getValue()));
        }
        return res;
    }

    private static Map<Heightmap.Type, Heightmap> retrieveHeightmaps(Map<Heightmap.Type, PackedIntegerArray> heightmaps, Chunk chunk) {
        EnumMap<Heightmap.Type, Heightmap> res = new EnumMap<>(Heightmap.Type.class);
        for (Map.Entry<Heightmap.Type, PackedIntegerArray> entry : heightmaps.entrySet()) {
            res.put(entry.getKey(), retrieveHeightmap(entry.getValue(), entry.getKey(), chunk));
        }
        return res;
    }

    private static PackedIntegerArray cacheHeightmap(Heightmap heightmap) {
        return copy(((HeightmapAccessor) heightmap).getStorage());
    }

    private static Heightmap retrieveHeightmap(PackedIntegerArray heightmap, Heightmap.Type type, Chunk chunk) {
        Heightmap res = new Heightmap(chunk, type);
        ((HeightmapAccessor) res).setStorage(copy(heightmap));
        return res;
    }

    private static PackedIntegerArray copy(PackedIntegerArray src) {
        int elementBits = ((PackedIntegerArrayAccessor) src).getElementBits();
        int size = src.getSize();
        long[] storage = src.getStorage();
        long[] newstorage = Arrays.copyOf(storage, storage.length);
        return new PackedIntegerArray(elementBits, size, newstorage);
    }

    private static CachedChunkSection cacheChunkSection(ChunkSection src) {
        if (src == null) return null;
        CachedChunkSection res = new CachedChunkSection();
        res.nonEmptyBlockCount = ((ChunkSectionAccessor) src).getNonEmptyBlockCount();
        res.randomTickableBlockCount = ((ChunkSectionAccessor) src).getRandomTickableBlockCount();
        res.nonEmptyFluidCount = ((ChunkSectionAccessor) src).getNonEmptyFluidCount();
        res.yOffset = src.getYOffset();
        PalettedContainer<BlockState> container = src.getContainer();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        container.toPacket(buf);
        res.container = buf;
        return res;
    }

    private static ChunkSection retrieveChunkSection(CachedChunkSection src) {
        if (src == null) return null;
        ChunkSection res = new ChunkSection(src.yOffset, src.nonEmptyBlockCount, src.randomTickableBlockCount, src.nonEmptyFluidCount);
        src.container.resetReaderIndex();
        res.getContainer().fromPacket(src.container);
        return res;
    }

    private static Map<StructureFeature<?>, NbtCompound> cacheStructureStarts(Map<StructureFeature<?>, StructureStart<?>> structureStarts, ChunkPos pos, ServerWorld world) {
        Map<StructureFeature<?>, NbtCompound> res = new HashMap<>();
        for (Map.Entry<StructureFeature<?>, StructureStart<?>> entry : structureStarts.entrySet()) {
            res.put(entry.getKey(), entry.getValue().toNbt(world, pos));
        }
        return res;
    }

    private static Map<StructureFeature<?>, StructureStart<?>> retrieveStructureStarts(Map<StructureFeature<?>, NbtCompound> structureStarts, ServerWorld world, long worldSeed) {
        Map<StructureFeature<?>, StructureStart<?>> map = Maps.newHashMap();

        for(Map.Entry<StructureFeature<?>, NbtCompound> entry : structureStarts.entrySet()) {
            StructureFeature<?> structureFeature = entry.getKey();
            StructureStart<?> structureStart = StructureFeature.readStructureStart(world, entry.getValue(), worldSeed);
            if (structureStart == null) {
                throw new IllegalArgumentException();
            }
            map.put(structureFeature, structureStart);
        }

        return map;
    }

    private static Map<StructureFeature<?>, LongSet> copyStructureReferences(Map<StructureFeature<?>, LongSet> src) {
        Map<StructureFeature<?>, LongSet> res = new HashMap<>();
        for (Map.Entry<StructureFeature<?>, LongSet> entry : src.entrySet()) {
            LongSet longSet = entry.getValue();
            res.put(entry.getKey(), new LongOpenHashSet(longSet));
        }
        return res;
    }

    private static Map<GenerationStep.Carver, BitSet> copyCarvingMasks(Map<GenerationStep.Carver, BitSet> src) {
        Map<GenerationStep.Carver, BitSet> res = new Object2ObjectArrayMap<>();
        for (Map.Entry<GenerationStep.Carver, BitSet> entry : src.entrySet()) {
            GenerationStep.Carver carver = entry.getKey();
            BitSet bitSet = entry.getValue();
            res.put(carver, (BitSet) bitSet.clone());
        }
        return res;
    }
}
