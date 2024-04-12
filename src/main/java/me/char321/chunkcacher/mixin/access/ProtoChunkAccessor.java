package me.char321.chunkcacher.mixin.access;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

@Mixin(ProtoChunk.class)
public interface ProtoChunkAccessor {
    @Mutable
    @Accessor
    void setHeightmaps(Map<Heightmap.Type, Heightmap> heightmaps);

    @Accessor
    Map<GenerationStep.Carver, BitSet> getCarvingMasks();

    @Accessor
    List<BlockPos> getLightSources();
}
