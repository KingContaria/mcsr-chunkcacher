package me.char321.chunkcacher;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GeneratorOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldCache {
    public static boolean isGenerating = false;
    private static GeneratorOptions lastGeneratorOptions;
    private static final Map<RegistryKey<World>, List<Long2ObjectLinkedOpenHashMap<NbtCompound>>> cache = new HashMap<>();
    public static List<ChunkPos> strongholdCache;

    public static void addChunk(ChunkPos chunkPos, ChunkStatus status, Chunk chunk, ServerWorld world) {
        cache.computeIfAbsent(world.getRegistryKey(), k -> {
            List<Long2ObjectLinkedOpenHashMap<NbtCompound>> list = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                list.add(new Long2ObjectLinkedOpenHashMap<>());
            }
            return list;
        }).get(status.getIndex()).put(chunkPos.toLong(), ChunkSerializer.serialize(world, chunk));
    }

    public static boolean shouldCache() {
        return isGenerating && Atum.isRunning;
    }

    public static NbtCompound getChunkNbt(ChunkPos chunkPos, ChunkStatus status, ServerWorld world) {
        List<Long2ObjectLinkedOpenHashMap<NbtCompound>> list = cache.get(world.getRegistryKey());
        if (list == null) return null;
        return list.get(status.getIndex()).get(chunkPos.toLong());
    }

    /**
     * Checks if the generator options have changed, if so, clear the cache
     * dude github copilot is so cool it auto generated these comments

     * kept as fallback just in case some Atum update messes anything up
     * not perfect but good enough for that purpose
     */
    public static void checkGeneratorOptions(GeneratorOptions generatorOptions) {
        if (lastGeneratorOptions == null ||
                lastGeneratorOptions.getSeed() != generatorOptions.getSeed() ||
                lastGeneratorOptions.shouldGenerateStructures() != generatorOptions.shouldGenerateStructures() ||
                lastGeneratorOptions.isFlatWorld() != generatorOptions.isFlatWorld()
        ) {
            clearCache();
            lastGeneratorOptions = generatorOptions;
        }
    }

    public static void clearCache() {
        cache.clear();
        strongholdCache = null;
    }
}