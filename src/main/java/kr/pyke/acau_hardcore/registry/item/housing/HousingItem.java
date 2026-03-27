package kr.pyke.acau_hardcore.registry.item.housing;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.housing.HousingStructureManager;
import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class HousingItem extends Item {
    private final int targetTier;

    public HousingItem(Properties properties, int targetTier) {
        super(properties);
        this.targetTier = targetTier;
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (level.isClientSide()) { return InteractionResult.PASS; }
        if (ModComponents.HARDCORE_INFO.get(player).getHousingID() != null) {
            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.RED.getColor(), "이미 구역을 소유하고 있습니다.");
            return InteractionResult.FAIL;
        }

        BlockPos blockPos = player.blockPosition();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(player.level());

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(blockPos)) {
                if (this.targetTier == 1) {
                    if (zone.getOwnerID() == null) {
                        zone.setOwnerID(player.getUUID());
                        zone.setTier(1);
                        housingData.addZone(zone);
                        ItemStack itemStack = player.getItemInHand(hand);
                        itemStack.shrink(1);
                        String structureID = "structure_" + this.targetTier;
                        HousingStructureManager.changeTier((ServerLevel) level, zone, targetTier, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, structureID), false);
                        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "1단계 집을 획득하셨습니다.");
                        player.getCooldowns().addCooldown(itemStack, 20);
                        return InteractionResult.CONSUME;
                    }
                    else if (!zone.getOwnerID().equals(player.getUUID())) {
                        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "다른 플레이어가 소유중인 구역입니다.");
                        return InteractionResult.FAIL;
                    }
                    else {
                        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "이미 소유중인 구역입니다.");
                        return InteractionResult.FAIL;
                    }
                }
                else {
                    if (zone.getOwnerID() != null && zone.getOwnerID().equals(player.getUUID())) {
                        if (zone.getTier() == this.targetTier - 1) {
                            zone.setTier(this.targetTier);
                            housingData.addZone(zone);
                            ItemStack itemStack = player.getItemInHand(hand);
                            itemStack.shrink(1);
                            String structureID = "structure_" + this.targetTier;
                            HousingStructureManager.changeTier((ServerLevel) level, zone, targetTier, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, structureID), false);
                            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), this.targetTier + "단계로 집을 업그레이드 하셨습니다.");
                            player.getCooldowns().addCooldown(itemStack, 20);
                            return InteractionResult.CONSUME;
                        }
                        else {
                            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "업그레이드 조건이 맞지 않습니다. 현재 단계: " + zone.getTier());
                            return InteractionResult.FAIL;
                        }
                    }
                    else {
                        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "본인 소유의 구역에서만 업그레이드가 가능합니다.");
                        return InteractionResult.FAIL;
                    }
                }
            }
        }

        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "거주 구역 안에서 사용해주세요.");
        return InteractionResult.FAIL;
    }
}
