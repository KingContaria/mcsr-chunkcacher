package me.char321.chunkcacher;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldCache {
    private static final Map<RegistryKey<World>, Long2ObjectLinkedOpenHashMap<CompoundTag>> cache = new HashMap<>();
    public static List<ChunkPos> strongholdCache;

    public static void addChunk(ChunkPos chunkPos, Chunk chunk, ServerWorld world) {
        cache.computeIfAbsent(world.getRegistryKey(), k -> new Long2ObjectLinkedOpenHashMap<>()).put(chunkPos.toLong(), ChunkSerializer.serialize(world, chunk));
    }

    public static boolean shouldCache() {
        return Atum.isRunning() && Atum.config.isSetSeed();
    }

    public static CompoundTag getChunkNbt(ChunkPos chunkPos, ServerWorld world) {
        Long2ObjectLinkedOpenHashMap<CompoundTag> map = cache.get(world.getRegistryKey());
        if (map == null) return null;
        return map.get(chunkPos.toLong());
    }

    public static void clearCache() {
        cache.clear();
        strongholdCache = null;
    }
}