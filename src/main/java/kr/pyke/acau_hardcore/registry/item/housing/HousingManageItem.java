package kr.pyke.acau_hardcore.registry.item.housing;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.housing.HousingStructureManager;
import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import kr.pyke.util.constants.COLOR;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public class HousingManageItem extends Item {
    public HousingManageItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) { return InteractionResult.PASS; }

        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        UUID playerID = player.getUUID();

        if (player.isShiftKeyDown()) {
            HousingStructureManager.setPos2(playerID, blockPos);
            if (!level.isClientSide()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    PykeLib.sendSystemMessage(serverPlayer, COLOR.GOLD.getColor(), String.format("Housing Pos2 at %s", blockPos));
                }
            }
        }
        else {
            HousingStructureManager.setPos1(playerID, blockPos);
            if (!level.isClientSide()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    PykeLib.sendSystemMessage(serverPlayer, COLOR.GOLD.getColor(), String.format("Housing Pos1 at %s", blockPos));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                UUID playerID = player.getUUID();
                BlockPos[] positions = HousingStructureManager.getPositions(playerID);

                if (positions != null && positions[0] != null && positions[1] != null) {
                    BlockPos pos1 = positions[0];
                    BlockPos pos2 = positions[1];
                    int minX = Math.min(pos1.getX(), pos2.getX());
                    int minY = Math.min(pos1.getY(), pos2.getY());
                    int minZ = Math.min(pos1.getZ(), pos2.getZ());
                    int maxX = Math.max(pos1.getX(), pos2.getX());
                    int maxY = Math.max(pos1.getY(), pos2.getY());
                    int maxZ = Math.max(pos1.getZ(), pos2.getZ());

                    BlockPos minPos = new BlockPos(minX, minY, minZ);
                    BlockPos maxPos = new BlockPos(maxX, maxY, maxZ);
                    UUID zoneID = UUID.randomUUID();

                    HousingZone newZone = new HousingZone(zoneID, null, minPos, maxPos, 0);
                    IHousingData housingData = ModComponents.HOUSING_DATA.get(level);
                    housingData.addZone(newZone);
                    HousingStructureManager.clearPositions(playerID);

                    if (player instanceof ServerPlayer serverPlayer) {
                        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "새로운 거주 구역이 등록되었습니다.");
                    }
                    return InteractionResult.SUCCESS;
                }
                else {
                    if (player instanceof ServerPlayer serverPlayer) {
                        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "Pos1과 Pos2를 모두 설정해야 구역을 등록할 수 있습니다.");
                    }
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext ctx, @NonNull TooltipDisplay display, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag) {
        consumer.accept(Component.literal("블록 우클릭 시 Pos1을 설정합니다.").withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.literal("블록 쉬프트 + 우클릭 시 Pos2을 설정합니다.").withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.literal("허공에 쉬프트 + 우클릭 시 구역을 설정합니다.").withStyle(ChatFormatting.GRAY));
    }
}