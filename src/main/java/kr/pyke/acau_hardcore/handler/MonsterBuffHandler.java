package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;

public class MonsterBuffHandler {
    private MonsterBuffHandler() { }

    private static final String BUFFED_TAG = "expert_buffed";

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "expert_health");
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "expert_damage");
    private static final Identifier SPEED_MODIFIER_ID = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "expert_speed");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "expert_armor");
    private static final Identifier KNOCKBACK_MODIFIER_ID = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "expert_knockback");

    public static void onMonsterSpawn(Monster monster, ServerLevelAccessor level) {
        if (!ModDimensions.isExpertDimension(level.getLevel().dimension())) { return; }
        if (monster.getTags().contains(BUFFED_TAG)) { return; }

        applyBuffs(monster);
        monster.addTag(BUFFED_TAG);
    }

    private static void applyBuffs(Monster monster) {
        applyModifier(monster.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER_ID, ModConfig.INSTANCE.expertHealthMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(monster.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER_ID, ModConfig.INSTANCE.expertDamageMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(monster.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_MODIFIER_ID, ModConfig.INSTANCE.expertSpeedMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(monster.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID, ModConfig.INSTANCE.expertArmorBonus, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(monster.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_MODIFIER_ID, ModConfig.INSTANCE.expertKnockbackResistance, AttributeModifier.Operation.ADD_VALUE);

        monster.setHealth(monster.getMaxHealth());
    }

    private static void applyModifier(AttributeInstance attribute, Identifier id, double value, AttributeModifier.Operation operation) {
        if (attribute == null) { return; }
        if (attribute.getModifier(id) != null) { return; }

        attribute.addPermanentModifier(new AttributeModifier(id, value, operation));
    }
}
