package me.char321.chunkcacher.mixin.access;

import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor {
    @Accessor short getNonEmptyBlockCount();
    @Accessor short getRandomTickableBlockCount();
    @Accessor short getNonEmptyFluidCount();
}
