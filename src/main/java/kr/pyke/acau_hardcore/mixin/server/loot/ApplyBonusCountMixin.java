package kr.pyke.acau_hardcore.mixin.server.loot;

import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.handler.RuneEventHandler;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ApplyBonusCount.class)
public class ApplyBonusCountMixin {
    @Final @Shadow private Holder<Enchantment> enchantment;

    @ModifyVariable(
        method = "run",
        at = @At(value = "STORE", ordinal = 0),
        ordinal = 0
    )
    private int modifyEnchantmentLevel(int originalLevel, ItemStack stack, LootContext context) {
        if (!this.enchantment.is(Enchantments.FORTUNE)) { return originalLevel; }

        Entity entity = null;
        if (context.hasParameter(LootContextParams.THIS_ENTITY)) {
            entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        }

        if (!(entity instanceof Player player)) { return originalLevel; }

        List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(player);
        int runeFortuneBonus = 0;

        if (!runes.isEmpty()) {
            BlockState blockState = null;
            if (context.hasParameter(LootContextParams.BLOCK_STATE)) {
                blockState = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
            }

            for (RuneInstance rune : runes) {
                if (blockState != null) {
                    if (rune.effect() == RUNE_EFFECT.MINER_FORTUNE) {
                        TagKey<Block> oreTag = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "ores"));
                        if (blockState.is(oreTag) || blockState.is(BlockTags.COAL_ORES) || blockState.is(BlockTags.IRON_ORES) || blockState.is(BlockTags.GOLD_ORES) || blockState.is(BlockTags.DIAMOND_ORES) || blockState.is(BlockTags.LAPIS_ORES) || blockState.is(BlockTags.REDSTONE_ORES) || blockState.is(BlockTags.EMERALD_ORES) || blockState.is(BlockTags.COPPER_ORES)) {
                            runeFortuneBonus += (int) rune.value1();
                        }
                    }
                    else if (rune.effect() == RUNE_EFFECT.FARMER_FORTUNE) {
                        if (blockState.is(BlockTags.CROPS)) {
                            runeFortuneBonus += (int) rune.value1();
                        }
                    }
                }
            }
        }

        int newLevel = originalLevel + runeFortuneBonus;

        return Math.max(0, newLevel);
    }
}