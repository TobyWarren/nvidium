package me.cortex.nvidium.mixin.sodium;

import me.cortex.nvidium.Nvidium;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = ChunkBuilder.class, remap = false)
public class MixinChunkBuilder {

    // unused code - will readd if needed..
//    @Redirect(method = "getSchedulingBudget", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int moreSchedulingBudget(List<Thread> threads) {
        int budget = threads.size();
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            budget *= 3;
        }
        return budget;
    }
}
