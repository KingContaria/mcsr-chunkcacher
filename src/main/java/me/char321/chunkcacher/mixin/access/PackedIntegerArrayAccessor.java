package me.char321.chunkcacher.mixin.access;

import net.minecraft.util.collection.PackedIntegerArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackedIntegerArray.class)
public interface PackedIntegerArrayAccessor {
    @Accessor
    int getElementBits();
}
