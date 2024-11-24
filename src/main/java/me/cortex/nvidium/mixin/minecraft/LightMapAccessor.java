package me.cortex.nvidium.mixin.minecraft;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LightmapTextureManager.class)
public interface LightMapAccessor {

    @Unique
    NativeImageBackedTexture getTexture();
}
