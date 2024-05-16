package me.char321.chunkcacher.mixin.access;

import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(ChunkTickScheduler.class)
public interface ChunkTickSchedulerAccessor<T> {
    @Accessor
    ShortList[] getScheduledPositions();

    @Accessor
    Predicate<T> getShouldExclude();

    @Accessor
    ChunkPos getPos();
}
