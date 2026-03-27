package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import kr.pyke.acau_hardcore.registry.item.rune.RuneItemHelper;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

public class BlockBreakEventHandler {
    private BlockBreakEventHandler() { }

    public static final Map<Block, Item> GEM_DROPS = Map.ofEntries(
        Map.entry(Blocks.COAL_ORE, ModItems.COAL_GEM),
        Map.entry(Blocks.DEEPSLATE_COAL_ORE, ModItems.COAL_GEM),
        Map.entry(Blocks.IRON_ORE, ModItems.IRON_GEM),
        Map.entry(Blocks.DEEPSLATE_IRON_ORE, ModItems.IRON_GEM),
        Map.entry(Blocks.COPPER_ORE, ModItems.COPPER_GEM),
        Map.entry(Blocks.DEEPSLATE_COPPER_ORE, ModItems.COPPER_GEM),
        Map.entry(Blocks.GOLD_ORE, ModItems.GOLD_GEM),
        Map.entry(Blocks.DEEPSLATE_GOLD_ORE, ModItems.GOLD_GEM),
        Map.entry(Blocks.NETHER_GOLD_ORE, ModItems.GOLD_GEM),
        Map.entry(Blocks.REDSTONE_ORE, ModItems.REDSTONE_GEM),
        Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, ModItems.REDSTONE_GEM),
        Map.entry(Blocks.EMERALD_ORE, ModItems.EMERALD_GEM),
        Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, ModItems.EMERALD_GEM),
        Map.entry(Blocks.LAPIS_ORE, ModItems.LAPIS_GEM),
        Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, ModItems.LAPIS_GEM),
        Map.entry(Blocks.DIAMOND_ORE, ModItems.DIAMOND_GEM),
        Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, ModItems.DIAMOND_GEM),
        Map.entry(Blocks.OBSIDIAN, ModItems.OBSIDIAN_GEM)
    );

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, blockPos, blockState, blockEntity) -> {
            float chance = ModDimensions.isExpertDimension(level.dimension()) ? ModConfig.INSTANCE.expertRareOreDropChance : ModConfig.INSTANCE.rareOreDropChance;
            if (player.getRandom().nextFloat() < chance / 100.f) {
                Item item = GEM_DROPS.get(blockState.getBlock());
                if (item != null) {
                    ItemStack itemStack = new ItemStack(item);
                    Block.popResource(level, blockPos, itemStack);
                }
            }

            RuneInstance runeInstance = RuneItemHelper.getRune(player.getMainHandItem());
            if (runeInstance != null && runeInstance.effect() == RUNE_EFFECT.ORE_BONUS) {
                chance = ModDimensions.isExpertDimension(level.dimension()) ? (runeInstance.value1() * 2.f) : runeInstance.value1();
                if (player.getRandom().nextFloat() < chance / 100.f) {
                    Item item = GEM_DROPS.get(blockState.getBlock());
                    if (item != null) {
                        ItemStack itemStack = new ItemStack(item);
                        Block.popResource(level, blockPos, itemStack);
                    }
                }
            }

            return true;
        });
    }
}
