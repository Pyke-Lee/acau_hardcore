package kr.pyke.acau_hardcore.mixin.server.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.handler.RuneEventHandler;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(
        method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At("RETURN"), cancellable = true
    )
    private void modifyLootDrops(LootParams params, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> drops = cir.getReturnValue();
        if (drops == null || drops.isEmpty()) { return; }

        Player player = getKillerPlayer(params);
        if (player == null) { return; }

        List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(player);
        if (runes.isEmpty()) { return; }

        ObjectArrayList<ItemStack> modifiedDrops = new ObjectArrayList<>();
        Entity killedEntity = null;
        if (params.contextMap().has(LootContextParams.THIS_ENTITY)) {
            killedEntity = params.contextMap().getOptional(LootContextParams.THIS_ENTITY);
        }

        BlockState blockState = null;
        if (params.contextMap().has(LootContextParams.BLOCK_STATE)) {
            blockState = params.contextMap().getOptional(LootContextParams.BLOCK_STATE);
        }

        for (ItemStack drop : drops) {
            if (drop.isDamageableItem()) {
                modifiedDrops.add(drop);
                continue;
            }

            ItemStack currentDrop = drop.copy();

            for (RuneInstance rune : runes) {
                if (rune.effect() == RUNE_EFFECT.FISHER_FORTUNE) {
                    if (currentDrop.is(ItemTags.FISHES)) {
                        int maxExtra = (int) rune.value1();
                        if (maxExtra > 0) {
                            int extra = player.getRandom().nextInt(maxExtra + 1);
                            if (extra > 0) {
                                currentDrop.grow(extra);
                            }
                        }
                    }
                }

                if (blockState != null) {
                    if (rune.effect() == RUNE_EFFECT.SMELT_ORE) {
                        RecipeManager recipeManager = params.getLevel().recipeAccess();
                        TagKey<Item> oreTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "ores"));

                        if (currentDrop.is(oreTag)) {
                            SingleRecipeInput input = new SingleRecipeInput(currentDrop);
                            Optional<RecipeHolder<BlastingRecipe>> recipe = recipeManager.getRecipeFor(RecipeType.BLASTING, input, params.getLevel());
                            if (recipe.isPresent()) {
                                ItemStack smelted = recipe.get().value().assemble(input, params.getLevel().registryAccess()).copy();
                                smelted.setCount(currentDrop.getCount());
                                currentDrop = smelted;
                            }
                        }
                    }
                    else if (rune.effect() == RUNE_EFFECT.SAND_TO_GLASS && (blockState.is(Blocks.SAND) || blockState.is(Blocks.RED_SAND))) {
                        currentDrop = new ItemStack(Blocks.GLASS, currentDrop.getCount());
                    }
                    else if (rune.effect() == RUNE_EFFECT.HARVEST_BONEMEAL && blockState.is(BlockTags.CROPS)) {
                        if (player.getRandom().nextFloat() * 100f <= rune.value1()) {
                            modifiedDrops.add(new ItemStack(Items.BONE_MEAL, (int) rune.value2()));
                        }
                    }
                    else if (rune.effect() == RUNE_EFFECT.CROP_BONUS && blockState.is(BlockTags.CROPS)) {
                        if (player.getRandom().nextFloat() * 100f <= rune.value1()) {
                            if (currentDrop.is(Items.WHEAT) || currentDrop.is(Items.CARROT) || currentDrop.is(Items.POTATO) || currentDrop.is(Items.BEETROOT) || currentDrop.is(Items.MELON_SLICE)) {
                                currentDrop.grow((int) rune.value2());
                            }
                        }
                    }
                    else if (rune.effect() == RUNE_EFFECT.SEED_BONUS && blockState.is(BlockTags.CROPS)) {
                        if (player.getRandom().nextFloat() * 100f <= rune.value1()) {
                            if (currentDrop.is(Items.WHEAT_SEEDS) || currentDrop.is(Items.BEETROOT_SEEDS) || currentDrop.is(Items.MELON_SEEDS) || currentDrop.is(Items.PUMPKIN_SEEDS) || currentDrop.is(Items.TORCHFLOWER_SEEDS) || currentDrop.is(Items.PITCHER_POD)) {
                                currentDrop.grow((int) rune.value2());
                            }
                        }
                    }
                }
                else if (killedEntity != null && killedEntity != player) {
                    boolean isTargetMatch = isTargetMatch(rune, killedEntity);
                    if (isTargetMatch) {
                        float chance = player.getRandom().nextFloat() * 100f;
                        if (chance <= rune.value1()) {
                            int extra = (int) rune.value2();
                            currentDrop.grow(extra);
                        }
                    }
                }
            }
            modifiedDrops.add(currentDrop);
        }
        cir.setReturnValue(modifiedDrops);
    }

    @ModifyVariable(
        method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Consumer<ItemStack> modifyEntityLootConsumer(Consumer<ItemStack> originalConsumer, LootParams params, long seed) {
        Player player = getKillerPlayer(params);
        if (player == null) { return originalConsumer; }

        List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(player);
        if (runes.isEmpty()) { return originalConsumer; }

        Entity killedEntity = null;
        if (params.contextMap().has(LootContextParams.THIS_ENTITY)) {
            killedEntity = params.contextMap().getOptional(LootContextParams.THIS_ENTITY);
        }

        if (killedEntity == null || killedEntity == player) { return originalConsumer; }

        final Entity finalKilledEntity = killedEntity;

        return stack -> {
            if (stack.isDamageableItem()) {
                originalConsumer.accept(stack);
                return;
            }

            ItemStack currentDrop = stack.copy();

            for (RuneInstance rune : runes) {
                boolean isTargetMatch = isTargetMatch(rune, finalKilledEntity);

                if (isTargetMatch) {
                    float chance = player.getRandom().nextFloat() * 100f;
                    if (chance <= rune.value1()) {
                        int extra = (int) rune.value2();
                        currentDrop.grow(extra);
                    }
                }
            }
            originalConsumer.accept(currentDrop);
        };
    }

    @Unique
    private Player getKillerPlayer(LootParams params) {
        if (params.contextMap().has(LootContextParams.LAST_DAMAGE_PLAYER)) {
            Entity entity = params.contextMap().getOptional(LootContextParams.LAST_DAMAGE_PLAYER);
            if (entity instanceof Player player) { return player; }
        }
        if (params.contextMap().has(LootContextParams.ATTACKING_ENTITY)) {
            Entity entity = params.contextMap().getOptional(LootContextParams.ATTACKING_ENTITY);
            if (entity instanceof Player player) { return player; }
        }
        if (params.contextMap().has(LootContextParams.DAMAGE_SOURCE)) {
            DamageSource damageSource = params.contextMap().getOptional(LootContextParams.DAMAGE_SOURCE);
            if (damageSource != null && damageSource.getEntity() instanceof Player player) { return player; }
        }
        if (params.contextMap().has(LootContextParams.THIS_ENTITY)) {
            Entity entity = params.contextMap().getOptional(LootContextParams.THIS_ENTITY);
            if (entity instanceof Player player) { return player; }
            else if (entity instanceof FishingHook hook && hook.getOwner() instanceof Player player) { return player; }
        }
        return null;
    }

    @Unique
    private static boolean isTargetMatch(RuneInstance rune, Entity killedEntity) {
        return switch (rune.effect()) {
            case CHICKEN_LOOT -> killedEntity.getType() == EntityType.CHICKEN;
            case RABBIT_LOOT -> killedEntity.getType() == EntityType.RABBIT;
            case PIG_LOOT -> killedEntity.getType() == EntityType.PIG;
            case SHEEP_LOOT -> killedEntity.getType() == EntityType.SHEEP;
            case COW_LOOT -> killedEntity.getType() == EntityType.COW;
            case MONSTER_LOOT -> killedEntity instanceof Monster;
            default -> false;
        };
    }
}