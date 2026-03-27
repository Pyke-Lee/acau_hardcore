package kr.pyke.acau_hardcore.mixin.server.fluid;

import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(ServerLevel level, BlockPos blockPos, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (level.dimension().equals(Level.END) || level.dimension().equals(ModDimensions.EXPERT_END)) {
            ci.cancel();
        }
    }
}
