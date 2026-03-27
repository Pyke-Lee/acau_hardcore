package kr.pyke.acau_hardcore.mixin.server.entity;

import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) { return; }

        if (self instanceof Player player) {
            if (AcauHardCoreCache.shouldGlow(player.getUUID())) {
                cir.setReturnValue(true);
            }
        }
    }
}
