package kr.pyke.acau_hardcore.registry.creativemodetabs;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    private ModCreativeModeTabs() { }

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hardcore_creative_tab"));
    public static final ResourceKey<CreativeModeTab> PREFIX_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "prefix_creative_tab"));

    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModItems.HARDCORE_TICKET))
        .title(Component.translatable("itemGroup.acau_hardcore.creative_tab"))
        .displayItems((itemDisplayParameters, output) -> {
            output.accept(ModItems.HARDCORE_TICKET);
            output.accept(ModItems.DIRTY_WATER);
            output.accept(ModItems.PURIFIED_WATER);
            output.accept(ModItems.CLEAN_WATER);
            output.accept(ModItems.ID_CARD);
            output.accept(ModItems.COMBAT_RUNE);
            output.accept(ModItems.LIFE_RUNE);
            output.accept(ModItems.TOWN_RETURN_SCROLL);
            output.accept(ModItems.SURVIVAL_FOOD);
            output.accept(ModItems.COMBAT_RATION);
            output.accept(ModItems.MEDICAL_KIT);
            output.accept(ModItems.SWAMP_COIN);
            output.accept(ModItems.JAIL_KEY);
            output.accept(ModItems.INVITATION);
            output.accept(ModItems.COAL_GEM);
            output.accept(ModItems.IRON_GEM);
            output.accept(ModItems.GOLD_GEM);
            output.accept(ModItems.COPPER_GEM);
            output.accept(ModItems.LAPIS_GEM);
            output.accept(ModItems.REDSTONE_GEM);
            output.accept(ModItems.DIAMOND_GEM);
            output.accept(ModItems.EMERALD_GEM);
            output.accept(ModItems.OBSIDIAN_GEM);
            output.accept(ModItems.HOUSING_TOOL);
            output.accept(ModItems.HOUSING_TIER_1);
            output.accept(ModItems.HOUSING_TIER_2);
            output.accept(ModItems.HOUSING_TIER_3);
        })
        .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
    }
}