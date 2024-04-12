package me.char321.chunkcacher.cache;

import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.PalettedContainer;

public class CachedChunkSection {
    public int yOffset;
    public short nonEmptyBlockCount;
    public short randomTickableBlockCount;
    public short nonEmptyFluidCount;
    public PacketByteBuf container;
}
