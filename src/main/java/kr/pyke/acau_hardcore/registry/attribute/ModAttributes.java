package kr.pyke.acau_hardcore.registry.attribute;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModAttributes {
    private ModAttributes() { }

    public static final Holder<Attribute> LIFE_STEAL = register("life_steal", new RangedAttribute("attribute.acau_hardcore.life_steal", 0d, 0d, 100d));
    public static final Holder<Attribute> CRIT_CHANCE = register("crit_chance", new RangedAttribute("attribute.acau_hardcore.crit_chance", 0d, 0d, 100d));
    public static final Holder<Attribute> CRIT_DAMAGE = register("crit_damage", new RangedAttribute("attribute.acau_hardcore.crit_damage", 0d, 0d, 1000d));
    public static final Holder<Attribute> DODGE_CHANCE = register("dodge_chance", new RangedAttribute("attribute.acau_hardcore.dodge_chance", 0d, 0d, 100d));

    private static Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, name), attribute);
    }

    public static void register() {
        AcauHardCore.LOGGER.info("Registering attributes");
    }
}
