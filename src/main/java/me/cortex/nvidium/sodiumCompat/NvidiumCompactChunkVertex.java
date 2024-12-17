package me.cortex.nvidium.sodiumCompat;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorU8;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttribute;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.system.MemoryUtil;

import java.util.Collections;

public class NvidiumCompactChunkVertex implements ChunkVertexType {
    // Adjust this to match the new GlVertexFormat constructor:
    public static final GlVertexFormat VERTEX_FORMAT = new GlVertexFormat(16, Collections.emptyMap());
    
    public static final int STRIDE = 16;
    public static final NvidiumCompactChunkVertex INSTANCE = new NvidiumCompactChunkVertex();

    private static final int POSITION_MAX_VALUE = 65536;
    public static final int TEXTURE_MAX_VALUE = 32768;

    private static final float MODEL_ORIGIN = 8.0f;
    private static final float MODEL_RANGE = 32.0f;
    private static final float MODEL_SCALE = MODEL_RANGE / POSITION_MAX_VALUE;
    private static final float MODEL_SCALE_INV = POSITION_MAX_VALUE / MODEL_RANGE;
    private static final float TEXTURE_SCALE = (1.0f / TEXTURE_MAX_VALUE);

    @Override
    public float getTextureScale() {
        return TEXTURE_SCALE;
    }

    @Override
    public float getPositionScale() {
        return MODEL_SCALE;
    }

    @Override
    public float getPositionOffset() {
        return -MODEL_ORIGIN;
    }

    @Override
    public GlVertexFormat getVertexFormat() {
        return VERTEX_FORMAT;
    }

    @Override
    public ChunkVertexEncoder getEncoder() {
        // Ensure that 'vertex' provides getters like getX(), getY(), getZ(), getLight(), getColor(), getU(), getV().
        // This depends on the current definition of vertex in the API you are using.
        return (ptr, material, vertex, sectionIndex) -> {
            int light = compactLight(vertex.getLight());

            MemoryUtil.memPutInt(ptr,     (encodePosition(vertex.getX()) << 0) | (encodePosition(vertex.getY()) << 16));
            MemoryUtil.memPutInt(ptr + 4, (encodePosition(vertex.getZ()) << 0) | (encodeDrawParameters(material) << 16) | ((light & 0xFF) << 24));
            MemoryUtil.memPutInt(ptr + 8, (encodeColor(vertex.getColor()) << 0) | (((light >> 8) & 0xFF) << 24));
            MemoryUtil.memPutInt(ptr + 12, encodeTexture(vertex.getU(), vertex.getV()));

            return ptr + STRIDE;
        };
    }

    private static int compactLight(int light) {
        // Assuming light is still packed with sky and block in upper/lower bytes
        int sky = MathHelper.clamp((light >>> 16) & 0xFF, 8, 248);
        int block = MathHelper.clamp((light >>> 0) & 0xFF, 8, 248);

        return (block << 0) | (sky << 8);
    }

    private static int encodePosition(float v) {
        return (int) ((MODEL_ORIGIN + v) * MODEL_SCALE_INV);
    }

    private static int encodeDrawParameters(Material material) {
        return (material.bits() & 0xFF);
    }

    private static int encodeColor(int color) {
        float brightness = ColorU8.byteToNormalizedFloat(ColorABGR.unpackAlpha(color));

        int r = ColorU8.normalizedFloatToByte(ColorU8.byteToNormalizedFloat(ColorABGR.unpackRed(color)) * brightness);
        int g = ColorU8.normalizedFloatToByte(ColorU8.byteToNormalizedFloat(ColorABGR.unpackGreen(color)) * brightness);
        int b = ColorU8.normalizedFloatToByte(ColorU8.byteToNormalizedFloat(ColorABGR.unpackBlue(color)) * brightness);

        return ColorABGR.pack(r, g, b, 0x00);
    }

    private static int encodeTexture(float u, float v) {
        return ((Math.round(u * TEXTURE_MAX_VALUE) & 0xFFFF) << 0) |
               ((Math.round(v * TEXTURE_MAX_VALUE) & 0xFFFF) << 16);
    }
}
