package me.char321.chunkcacher.mixin.access;

import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Heightmap.class)
public interface HeightmapAccessor {
    @Accessor
    PackedIntegerArray getStorage();

    @Mutable
    @Accessor
    void setStorage(PackedIntegerArray storage);
}
