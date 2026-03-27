package kr.pyke.acau_hardcore.registry;

import kr.pyke.acau_hardcore.registry.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import org.jspecify.annotations.NonNull;

public class AcauHardCoreModelProvider extends FabricModelProvider {
    public AcauHardCoreModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NonNull BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.HARDCORE_TICKET, ModelTemplates.FLAT_ITEM); // 하드코어 입장권

        itemModelGenerators.generateFlatItem(ModItems.DIRTY_WATER, ModelTemplates.FLAT_ITEM); // 더러운 물
        itemModelGenerators.generateFlatItem(ModItems.PURIFIED_WATER, ModelTemplates.FLAT_ITEM); // 정수된 물
        itemModelGenerators.generateFlatItem(ModItems.CLEAN_WATER, ModelTemplates.FLAT_ITEM); // 깨끗한 물

        itemModelGenerators.generateFlatItem(ModItems.ID_CARD, ModelTemplates.FLAT_ITEM); // 닉네임 변경권

        itemModelGenerators.generateFlatItem(ModItems.RANDOM_BOX, ModelTemplates.FLAT_ITEM); // 랜덤 상자

        itemModelGenerators.generateFlatItem(ModItems.COMBAT_RUNE, ModelTemplates.FLAT_ITEM); // 전투 룬
        itemModelGenerators.generateFlatItem(ModItems.LIFE_RUNE, ModelTemplates.FLAT_ITEM); // 생활 룬

        itemModelGenerators.generateFlatItem(ModItems.TOWN_RETURN_SCROLL, ModelTemplates.FLAT_ITEM); // 마을 귀환 주문서

        itemModelGenerators.generateFlatItem(ModItems.SURVIVAL_FOOD, ModelTemplates.FLAT_ITEM); // 비상 식량
        itemModelGenerators.generateFlatItem(ModItems.COMBAT_RATION, ModelTemplates.FLAT_ITEM); // 전투 식량
        itemModelGenerators.generateFlatItem(ModItems.MEDICAL_KIT, ModelTemplates.FLAT_ITEM); // 응급 치료 키트

        itemModelGenerators.generateFlatItem(ModItems.SWAMP_COIN, ModelTemplates.FLAT_ITEM); // 늪지대 코인
        itemModelGenerators.generateFlatItem(ModItems.JAIL_KEY, ModelTemplates.FLAT_ITEM); // 감옥 탈출용 열쇠
        itemModelGenerators.generateFlatItem(ModItems.INVITATION, ModelTemplates.FLAT_ITEM); // 초대권

        itemModelGenerators.generateFlatItem(ModItems.COAL_GEM, ModelTemplates.FLAT_ITEM); // 석탄
        itemModelGenerators.generateFlatItem(ModItems.IRON_GEM, ModelTemplates.FLAT_ITEM); // 철
        itemModelGenerators.generateFlatItem(ModItems.GOLD_GEM, ModelTemplates.FLAT_ITEM); // 금
        itemModelGenerators.generateFlatItem(ModItems.COPPER_GEM, ModelTemplates.FLAT_ITEM); // 구리
        itemModelGenerators.generateFlatItem(ModItems.LAPIS_GEM, ModelTemplates.FLAT_ITEM); // 청금석
        itemModelGenerators.generateFlatItem(ModItems.REDSTONE_GEM, ModelTemplates.FLAT_ITEM); // 레드스톤
        itemModelGenerators.generateFlatItem(ModItems.DIAMOND_GEM, ModelTemplates.FLAT_ITEM); // 다이아몬드
        itemModelGenerators.generateFlatItem(ModItems.EMERALD_GEM, ModelTemplates.FLAT_ITEM); // 에메랄드
        itemModelGenerators.generateFlatItem(ModItems.OBSIDIAN_GEM, ModelTemplates.FLAT_ITEM); // 옵시디언

        itemModelGenerators.generateFlatItem(ModItems.HOUSING_TOOL, ModelTemplates.FLAT_ITEM); // 구역 설정 도구
        itemModelGenerators.generateFlatItem(ModItems.HOUSING_TIER_1, ModelTemplates.FLAT_ITEM); // 구역 점유 문서(1단계)
        itemModelGenerators.generateFlatItem(ModItems.HOUSING_TIER_2, ModelTemplates.FLAT_ITEM); // 구역 강화 문서(2단계)
        itemModelGenerators.generateFlatItem(ModItems.HOUSING_TIER_3, ModelTemplates.FLAT_ITEM); // 구역 강화 문서(3단계)
    }
}
