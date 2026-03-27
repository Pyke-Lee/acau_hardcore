package kr.pyke.acau_hardcore.registry.item;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.registry.item.drink.DrinkItem;
import kr.pyke.acau_hardcore.registry.item.drink.WaterDrinkItem;
import kr.pyke.acau_hardcore.registry.item.food.CombatRationItem;
import kr.pyke.acau_hardcore.registry.item.food.SurvivalFoodItem;
import kr.pyke.acau_hardcore.registry.item.housing.HousingItem;
import kr.pyke.acau_hardcore.registry.item.housing.HousingManageItem;
import kr.pyke.acau_hardcore.registry.item.idcard.IDCardItem;
import kr.pyke.acau_hardcore.registry.item.key.JailKeyItem;
import kr.pyke.acau_hardcore.registry.item.medicalkit.MedicalKitItem;
import kr.pyke.acau_hardcore.registry.item.randombox.RandomBoxItem;
import kr.pyke.acau_hardcore.registry.item.rune.RuneItem;
import kr.pyke.acau_hardcore.registry.item.scroll.TownReturnScrollItem;
import kr.pyke.acau_hardcore.registry.item.ticket.TicketItem;
import kr.pyke.acau_hardcore.type.RUNE_TYPE;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;

import java.util.function.Function;

public class ModItems {
    private ModItems() { }

    public static final Item HARDCORE_TICKET = registerFactory("hardcore_ticket", TicketItem::new);

    public static final Item ID_CARD = registerFactory("id_card", IDCardItem::new);

    public static final WaterDrinkItem DIRTY_WATER = register("water/dirty", new WaterDrinkItem(new Item.Properties().setId(key("water/dirty")).stacksTo(99).component(DataComponents.CONSUMABLE, DrinkItem.drinkConsumable()), 300));
    public static final WaterDrinkItem PURIFIED_WATER = register("water/purified", new WaterDrinkItem(new Item.Properties().setId(key("water/purified")).stacksTo(99).component(DataComponents.CONSUMABLE, DrinkItem.drinkConsumable()), 600));
    public static final WaterDrinkItem CLEAN_WATER = register("water/clean", new WaterDrinkItem(new Item.Properties().setId(key("water/clean")).stacksTo(99).component(DataComponents.CONSUMABLE, DrinkItem.drinkConsumable()), 1800));

    public static final Item RANDOM_BOX = registerFactory("random_box", RandomBoxItem::new);

    public static final Item COMBAT_RUNE = register("rune/combat", new RuneItem(new Item.Properties().setId(key("rune/combat")).stacksTo(99), RUNE_TYPE.COMBAT));
    public static final Item LIFE_RUNE = register("rune/life", new RuneItem(new Item.Properties().setId(key("rune/life")).stacksTo(99), RUNE_TYPE.LIFE));

    public static final Item TOWN_RETURN_SCROLL = registerFactory("scroll/town_return", TownReturnScrollItem::new);

    private static final FoodProperties SURVIVAL_FOOD_PROPERTIES = new FoodProperties.Builder().nutrition(20).saturationModifier(0).alwaysEdible().build();
    private static final Consumable SURVIVAL_FOOD_CONSUMABLE = Consumable.builder().consumeSeconds(0.5f).animation(ItemUseAnimation.EAT).build();
    public static final Item SURVIVAL_FOOD = register("food/survival_food", new SurvivalFoodItem(new Item.Properties().setId(key("food/survival_food")).food(SURVIVAL_FOOD_PROPERTIES, SURVIVAL_FOOD_CONSUMABLE).stacksTo(99)));
    private static final FoodProperties COMBAT_RATION_PROPERTIES = new FoodProperties.Builder().nutrition(20).saturationModifier(0.15f).alwaysEdible().build();
    public static final Item COMBAT_RATION = register("food/combat_ration", new CombatRationItem(new Item.Properties().setId(key("food/combat_ration")).food(COMBAT_RATION_PROPERTIES, SURVIVAL_FOOD_CONSUMABLE).stacksTo(99)));

    public static final Item MEDICAL_KIT = registerFactory("medical_kit", MedicalKitItem::new);

    public static final Item SWAMP_COIN = registerFactory("swamp_coin", Item::new);
    public static final Item INVITATION = registerFactory("invitation", Item::new);
    public static final Item JAIL_KEY = registerFactory("jail_key", JailKeyItem::new);

    public static final Item COAL_GEM = registerFactory("gem/coal", Item::new);
    public static final Item IRON_GEM = registerFactory("gem/iron", Item::new);
    public static final Item GOLD_GEM = registerFactory("gem/gold", Item::new);
    public static final Item COPPER_GEM = registerFactory("gem/copper", Item::new);
    public static final Item DIAMOND_GEM = registerFactory("gem/diamond", Item::new);
    public static final Item EMERALD_GEM = registerFactory("gem/emerald", Item::new);
    public static final Item LAPIS_GEM = registerFactory("gem/lapis", Item::new);
    public static final Item REDSTONE_GEM = registerFactory("gem/redstone", Item::new);
    public static final Item OBSIDIAN_GEM = registerFactory("gem/obsidian", Item::new);

    public static final Item HOUSING_TOOL = registerFactory("housing/tool", HousingManageItem::new);
    public static final Item HOUSING_TIER_1 = register("housing/tier_1", new HousingItem(new Item.Properties().setId(key("housing/tier_1")).stacksTo(99), 1));
    public static final Item HOUSING_TIER_2 = register("housing/tier_2", new HousingItem(new Item.Properties().setId(key("housing/tier_2")).stacksTo(99), 2));
    public static final Item HOUSING_TIER_3 = register("housing/tier_3", new HousingItem(new Item.Properties().setId(key("housing/tier_3")).stacksTo(99), 3));

    private static Item registerFactory(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> resourceKey = key(name);
        Item.Properties properties = new Item.Properties().setId(resourceKey).stacksTo(99);

        return Registry.register(BuiltInRegistries.ITEM, resourceKey, factory.apply(properties));
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, key(name), item);
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, name));
    }

    public static void register() {
        AcauHardCore.LOGGER.info("Registering Mod Items for " + AcauHardCore.MOD_ID);
    }
}
