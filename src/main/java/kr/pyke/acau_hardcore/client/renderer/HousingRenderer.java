package kr.pyke.acau_hardcore.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.housing.HousingStructureManager;
import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import kr.pyke.acau_hardcore.registry.item.housing.HousingManageItem;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class HousingRenderer {
    private static final Identifier GRADIENT_TEXTURE = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "textures/gradient.png");
    private static final RenderType HOUSING_ZONE_RENDER_TYPE = RenderType.create(
        "housing_zone_render",
        RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
            .withTexture("Sampler0", GRADIENT_TEXTURE)
            .useLightmap()
            .useOverlay()
            .sortOnUpload()
            .createRenderSetup()
    );
    private static boolean boundaryVisible = true;

    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
            renderHousingZones(context);
            renderManagePreview(context);
        });
    }

    private static int getAdjustedLight(Level level, BlockPos pos) {
        int light = LevelRenderer.getLightColor(level, pos);
        int blockLight = LightTexture.block(light);
        int skyLight = LightTexture.sky(light);

        blockLight = Math.max(blockLight, 3);

        return LightTexture.pack(blockLight, skyLight);
    }

    private static void renderHousingZones(WorldRenderContext context) {
        if (!boundaryVisible) { return; }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { return; }

        IHousingData housingData = ModComponents.HOUSING_DATA.get(mc.level);
        if (housingData.getHousingZones().isEmpty()) { return; }

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        VertexConsumer consumer = context.consumers().getBuffer(HOUSING_ZONE_RENDER_TYPE);
        UUID playerUUID = mc.player.getUUID();

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(mc.player.blockPosition())) {
                int light = getAdjustedLight(mc.level, zone.getMinPos());

                if (zone.getOwnerID() == null) {
                    drawGradientBox(consumer, cameraPos, zone.getMinPos(), zone.getMaxPos(), 200, 200, 200, light);
                }
                else if (zone.getOwnerID().equals(playerUUID)) {
                    drawGradientBox(consumer, cameraPos, zone.getMinPos(), zone.getMaxPos(), 119, 221, 119, light);
                }
                else {
                    drawGradientBox(consumer, cameraPos, zone.getMinPos(), zone.getMaxPos(), 255, 105, 97, light);
                }
            }
        }
    }

    private static void renderManagePreview(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { return; }

        if (mc.player.getMainHandItem().getItem() instanceof HousingManageItem) {
            UUID playerUUID = mc.player.getUUID();
            BlockPos[] positions = HousingStructureManager.getPositions(playerUUID);
            if (positions != null) {
                BlockPos pos1 = positions[0];
                BlockPos pos2 = positions[1];
                if (pos1 != null || pos2 != null) {
                    Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
                    VertexConsumer consumer = context.consumers().getBuffer(HOUSING_ZONE_RENDER_TYPE);

                    if (pos1 != null && pos2 != null) {
                        BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
                        BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
                        int light = getAdjustedLight(mc.level, min);
                        drawGradientBox(consumer, cameraPos, min, max, 174, 198, 207, light);
                    }
                    else if (pos1 != null) {
                        int light = getAdjustedLight(mc.level, pos1);
                        drawGradientBox(consumer, cameraPos, pos1, pos1, 174, 198, 207, light);
                    }
                    else {
                        int light = getAdjustedLight(mc.level, pos2);
                        drawGradientBox(consumer, cameraPos, pos2, pos2, 174, 198, 207, light);
                    }
                }
            }
        }
    }

    private static void drawGradientBox(VertexConsumer consumer, Vec3 cameraPos, BlockPos min, BlockPos max, int r, int g, int b, int light) {
        float offset = 0.01f;
        float minX = (float) (min.getX() - cameraPos.x) + offset;
        float minY = (float) (min.getY() - cameraPos.y) + offset;
        float minZ = (float) (min.getZ() - cameraPos.z) + offset;
        float maxX = (float) (max.getX() + 1.f - cameraPos.x) - offset;
        float maxY = (float) (max.getY() + 1.f - cameraPos.y) - offset;
        float maxZ = (float) (max.getZ() + 1.f - cameraPos.z) - offset;

        float gradY = Math.min(maxY, minY + 8.f);

        int alphaBottom = 160;
        int alphaTop = 160;

        drawQuad(consumer, minX, gradY, minZ, alphaTop, maxX, gradY, minZ, alphaTop, maxX, minY, minZ, alphaBottom, minX, minY, minZ, alphaBottom, r, g, b, light);
        drawQuad(consumer, maxX, gradY, maxZ, alphaTop, minX, gradY, maxZ, alphaTop, minX, minY, maxZ, alphaBottom, maxX, minY, maxZ, alphaBottom, r, g, b, light);
        drawQuad(consumer, minX, gradY, maxZ, alphaTop, minX, gradY, minZ, alphaTop, minX, minY, minZ, alphaBottom, minX, minY, maxZ, alphaBottom, r, g, b, light);
        drawQuad(consumer, maxX, gradY, minZ, alphaTop, maxX, gradY, maxZ, alphaTop, maxX, minY, maxZ, alphaBottom, maxX, minY, minZ, alphaBottom, r, g, b, light);
    }

    private static void drawQuad(VertexConsumer consumer, float x1, float y1, float z1, int a1, float x2, float y2, float z2, int a2, float x3, float y3, float z3, int a3, float x4, float y4, float z4, int a4, int r, int g, int b, int light) {
        addVertex(consumer, x1, y1, z1, r, g, b, a1, 0.f, 0.f, light);
        addVertex(consumer, x2, y2, z2, r, g, b, a2, 1.f, 0.f, light);
        addVertex(consumer, x3, y3, z3, r, g, b, a3, 1.f, 1.f, light);
        addVertex(consumer, x4, y4, z4, r, g, b, a4, 0.f, 1.f, light);

        addVertex(consumer, x4, y4, z4, r, g, b, a4, 0.f, 1.f, light);
        addVertex(consumer, x3, y3, z3, r, g, b, a3, 1.f, 1.f, light);
        addVertex(consumer, x2, y2, z2, r, g, b, a2, 1.f, 0.f, light);
        addVertex(consumer, x1, y1, z1, r, g, b, a1, 0.f, 0.f, light);
    }

    private static void addVertex(VertexConsumer consumer, float x, float y, float z, int r, int g, int b, int a, float u, float v, int light) {
        consumer.addVertex(x, y, z)
            .setColor(r, g, b, a)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0.f, 1.f, 0.f);
    }

    public static void toggleBoundary() { boundaryVisible = !boundaryVisible; }

    public static boolean isBoundaryVisible() { return boundaryVisible; }
}