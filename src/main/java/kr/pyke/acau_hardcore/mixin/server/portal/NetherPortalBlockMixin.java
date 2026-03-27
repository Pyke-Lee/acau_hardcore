package kr.pyke.acau_hardcore.mixin.server.portal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @WrapOperation(
        method = "getPortalDestination",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
        )
    )
    private ServerLevel acauHardcore$redirectNetherPortal(MinecraftServer server, ResourceKey<Level> vanillaDestination, Operation<ServerLevel> original, @Local(argsOnly = true, ordinal = 0) ServerLevel sourceLevel) {
        ResourceKey<Level> customDest = ModDimensions.NETHER_PORTAL_MAP.get(sourceLevel.dimension());
        if (customDest != null) { return original.call(server, customDest); }

        return null;
    }
}
