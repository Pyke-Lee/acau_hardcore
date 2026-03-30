package kr.pyke.acau_hardcore.client.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record CircleGaugeRenderState(RenderPipeline pipeline, Matrix3x2f matrix, float cx, float cy, float radius, float thickness, float progress, int color, @Nullable ScreenRectangle scissorArea) implements GuiElementRenderState {
    @Override
    public void buildVertices(@NonNull VertexConsumer vertexConsumer) {
        int segments = 80;
        int drawSegments = (int) (segments * progress);

        Vector2f pos = new Vector2f();

        for (int i = 0; i < drawSegments; i++) {
            float a1 = (float) Math.toRadians((i * (360.0 / segments)) - 90.0);
            float a2 = (float) Math.toRadians(((i + 1) * (360.0 / segments)) - 90.0);

            vertex(vertexConsumer, matrix, pos, cx + Mth.cos(a1) * radius, cy + Mth.sin(a1) * radius);
            vertex(vertexConsumer, matrix, pos, cx + Mth.cos(a1) * (radius - thickness), cy + Mth.sin(a1) * (radius - thickness));
            vertex(vertexConsumer, matrix, pos, cx + Mth.cos(a2) * radius, cy + Mth.sin(a2) * radius);

            vertex(vertexConsumer, matrix, pos, cx + Mth.cos(a1) * (radius - thickness), cy + Mth.sin(a1) * (radius - thickness));
            vertex(vertexConsumer, matrix, pos, cx + Mth.cos(a2) * (radius - thickness), cy + Mth.sin(a2) * (radius - thickness));
            vertex(vertexConsumer, matrix, pos, cx + Mth.cos(a2) * radius, cy + Mth.sin(a2) * radius);
        }
    }

    private void vertex(VertexConsumer consumer, Matrix3x2f mat, Vector2f vec, float x, float y) {
        vec.set(x, y);
        mat.transformPosition(vec);

        consumer.addVertex(vec.x, vec.y, 0.0f).setColor(color);
    }

    @Override
    public @NonNull TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @NonNull ScreenRectangle bounds() {
        return new ScreenRectangle((int)(cx - radius), (int)(cy - radius), (int)(radius * 2), (int)(radius * 2));
    }
}