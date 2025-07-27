package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @WrapMethod(method = "renderLevel")
    public void wrapRenderLevel(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline, Camera camera,
            GameRenderer gameRenderer,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Operation<Void> original
    ) {
        CoreFeature.setRenderingLevel();
        original.call(graphicsResourceAllocator, deltaTracker, renderBlockOutline, camera, gameRenderer, frustumMatrix, projectionMatrix);
        CoreFeature.resetRenderingLevel();
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    public void endOutlineBatches(
            FogParameters fogParameters,
            DeltaTracker deltaTracker,
            Camera camera,
            ProfilerFiller profilerFiller,
            Matrix4f matrix4f,
            Matrix4f matrix4f2,
            ResourceHandle resourceHandle,
            ResourceHandle resourceHandle2,
            ResourceHandle resourceHandle3,
            ResourceHandle resourceHandle4,
            boolean bl, Frustum frustum,
            ResourceHandle resourceHandle5,
            CallbackInfo ci
    ) {
        CoreBuffers.POS_TEX_COLOR_OUTLINE.drawBuffers();
        CoreBuffers.POS_TEX_COLOR_OUTLINE.clearBuffers();
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 0))
    public void endAllEntityBatches(
            FogParameters fogParameters,
            DeltaTracker deltaTracker,
            Camera camera, ProfilerFiller profilerFiller,
            Matrix4f matrix4f, Matrix4f matrix4f2,
            ResourceHandle resourceHandle,
            ResourceHandle resourceHandle2,
            ResourceHandle resourceHandle3,
            ResourceHandle resourceHandle4,
            boolean bl, Frustum frustum,
            ResourceHandle resourceHandle5,
            CallbackInfo ci
    ) {
        CoreBuffers.ENTITY.drawBuffers();
        CoreBuffers.BLOCK.drawBuffers();
        CoreBuffers.POS.drawBuffers();
        CoreBuffers.POS_TEX.drawBuffers();
        CoreBuffers.POS_TEX_COLOR.drawBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.drawBuffers();

        CoreBuffers.ENTITY.clearBuffers();
        CoreBuffers.BLOCK.clearBuffers();
        CoreBuffers.POS.clearBuffers();
        CoreBuffers.POS_TEX.clearBuffers();
        CoreBuffers.POS_TEX_COLOR.clearBuffers();
        CoreBuffers.POS_COLOR_TEX_LIGHT.clearBuffers();
    }
}
