package kr.pyke.acau_hardcore.mixin.server.block;

import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BasePressurePlateBlock.class)
public class BasePressurePlateBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    public void onEntityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean intersects, CallbackInfo ci) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (player.isCreative()) { return; }

            IHousingData housingData = ModComponents.HOUSING_DATA.get(level);
            for (HousingZone zone : housingData.getHousingZones()) {
                if (zone.isInsideZone(pos)) {
                    if (zone.getOwnerID() != null && !zone.getOwnerID().equals(player.getUUID())) {
                        ci.cancel();
                        return;
                    }
                }
            }

            if (level.dimension() == Level.OVERWORLD) { ci.cancel(); }
        }
    }
}
