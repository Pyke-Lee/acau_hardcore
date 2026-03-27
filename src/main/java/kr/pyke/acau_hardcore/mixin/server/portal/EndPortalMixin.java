package kr.pyke.acau_hardcore.mixin.server.portal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public class EndPortalMixin {
    @WrapOperation(
        method = "getPortalDestination",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
        )
    )
    private ServerLevel acauHardcore$redirectEndPortal(MinecraftServer server, ResourceKey<Level> dimension, Operation<ServerLevel> original) {
        return server.getLevel(ServerLevel.OVERWORLD);
    }

    @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
    private void redirectEndPortal(ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTransition> cir) {
        if (entity instanceof ServerPlayer serverPlayer) {
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);

            if (overworld != null) {
                cir.setReturnValue(serverPlayer.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));
            }
        }
    }
}
