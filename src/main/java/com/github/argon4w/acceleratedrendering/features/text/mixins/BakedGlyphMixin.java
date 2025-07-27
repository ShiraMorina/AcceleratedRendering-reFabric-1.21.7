package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedBakedGlyphRenderer;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ExtensionMethod(VertexConsumerExtension.class)
@Mixin(value = BakedGlyph.class, priority = Integer.MIN_VALUE)
public class BakedGlyphMixin {

    @Unique
    private final AcceleratedBakedGlyphRenderer normalRenderer = new AcceleratedBakedGlyphRenderer((BakedGlyph) (Object) this, false);
    @Unique
    private final AcceleratedBakedGlyphRenderer italicRenderer = new AcceleratedBakedGlyphRenderer((BakedGlyph) (Object) this, true);

    @Inject(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V", at = @At("HEAD"), cancellable = true)
    public void renderFast(
            boolean italic, float x, float y, float z, Matrix4f pose, VertexConsumer buffer, int color, boolean bold, int packedLight, CallbackInfo ci
    ) {
        var extension = buffer.getAccelerated();

        if (CoreFeature.isRenderingLevel()
                && AcceleratedTextRenderingFeature.isEnabled()
                && AcceleratedTextRenderingFeature.shouldUseAcceleratedPipeline()
                && extension.isAccelerated()
        ) {
            ci.cancel();

            var renderer = italic
                    ? italicRenderer
                    : normalRenderer;
            extension.doRender(
                    renderer,
                    new Vector2f(x, y),
                    pose,
                    null,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    color
            );
        }
    }
}
