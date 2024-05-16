package me.char321.chunkcacher.cache;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class CachedChunk {
    public ChunkPos pos;
    @Nullable
    public BiomeArray biomes;
    public Map<Heightmap.Type, PackedIntegerArray> heightmaps;
    public ChunkStatus status;
    public NbtList blockEntities;
    public CachedChunkSection[] sections;
    public List<NbtCompound> entities;
    public List<BlockPos> lightSources;
    public ShortList[] postProcessingLists = new ShortList[16];
    public Map<StructureFeature<?>, NbtCompound> structureStarts;
    public Map<StructureFeature<?>, LongSet> structureReferences;
    public UpgradeData upgradeData;
    public ChunkTickScheduler<Block> blockTickScheduler;
    public ChunkTickScheduler<Fluid> fluidTickScheduler;
    public long inhabitedTime;
    public Map<GenerationStep.Carver, BitSet> carvingMasks = new Object2ObjectArrayMap<>();
    public boolean lightOn;

}
