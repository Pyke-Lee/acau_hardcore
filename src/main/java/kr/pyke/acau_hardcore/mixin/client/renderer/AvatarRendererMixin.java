package kr.pyke.acau_hardcore.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import kr.pyke.acau_hardcore.client.state.IPrefixRenderState;
import kr.pyke.acau_hardcore.prefix.PrefixData;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.prefix.IPrefixes;
import kr.pyke.acau_hardcore.util.ColorParser;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void onExtractRenderState(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (avatar instanceof Player player) {
            IPrefixes prefixes = ModComponents.PREFIXES.get(player);
            String selectedPrefix = prefixes.getSelectedPrefix();
            ((IPrefixRenderState) state).acau_hardcore$setPrefixID(selectedPrefix);
        }
    }

    @Inject(method = "submitNameTag*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void onSubmitNameTag(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        String prefixID = ((IPrefixRenderState) state).acau_hardcore$getPrefixID();

        if (prefixID != null && !prefixID.isEmpty() && !prefixID.equals("none")) {
            PrefixData prefixData = PrefixRegistry.get(prefixID);

            if (prefixData != null && state.nameTag != null) {
                Component prefixComponent = ColorParser.parse(prefixData.prefix());
                int yOffset = state.showExtraEars ? -10 : 0;

                poseStack.translate(0.f, 9.f * 1.15f * 0.025f, 0.f);

                collector.submitNameTag(poseStack, state.nameTagAttachment, yOffset, prefixComponent, !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, cameraState);
            }
        }
    }
}