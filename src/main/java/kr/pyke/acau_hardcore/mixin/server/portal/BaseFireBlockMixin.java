package kr.pyke.acau_hardcore.mixin.server.portal;

import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    /**
     * @author AcauHardCore
     * @reason 커스텀 디멘션에서 네더 포탈 점화 허용
     */
    @Overwrite
    private static boolean inPortalDimension(Level level) {
        return level.dimension().equals(ModDimensions.BEGINNER_OVERWORLD) || level.dimension() == Level.NETHER
            || level.dimension().equals(ModDimensions.EXPERT_OVERWORLD) || level.dimension().equals(ModDimensions.EXPERT_NETHER);
    }
}
