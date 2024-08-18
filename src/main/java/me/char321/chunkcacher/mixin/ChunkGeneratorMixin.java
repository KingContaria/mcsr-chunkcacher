package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Shadow
    @Final
    private List<ChunkPos> strongholdPositions;

    @Shadow
    @Final
    private StructuresConfig structuresConfig;

    @Inject(method = "generateStrongholdPositions", at = @At("HEAD"))
    private void applyCachedStrongholds(CallbackInfo ci) {
        // this.structuresConfig.getStronghold() == StructuresConfig.DEFAULT_STRONGHOLD works for vanilla world generation because only the overworld will use DEFAULT_STRONGHOLD,
        // but it may fail with datapacks, mods or 20w14∞ adding new dimensions
        if (WorldCache.shouldCache() && this.structuresConfig.getStronghold() == StructuresConfig.DEFAULT_STRONGHOLD && WorldCache.strongholdCache != null && this.strongholdPositions.isEmpty()) {
            this.strongholdPositions.addAll(WorldCache.strongholdCache);
        }
    }

    @Inject(method = "generateStrongholdPositions", at = @At("TAIL"))
    private void cacheStrongholds(CallbackInfo ci) {
        if (WorldCache.shouldCache() && this.structuresConfig.getStronghold() == StructuresConfig.DEFAULT_STRONGHOLD && WorldCache.strongholdCache == null) {
            WorldCache.strongholdCache = Collections.unmodifiableList(new ArrayList<>(this.strongholdPositions));
        }
    }
}