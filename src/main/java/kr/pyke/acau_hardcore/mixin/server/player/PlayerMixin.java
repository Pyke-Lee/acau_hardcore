package kr.pyke.acau_hardcore.mixin.server.player;

import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.handler.RuneEventHandler;
import kr.pyke.acau_hardcore.registry.attribute.ModAttributes;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(Player.class)
public class PlayerMixin {
    @Redirect(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getName()Lnet/minecraft/network/chat/Component;"))
    private Component redirectGetName(Player instance) {
        String originalName = instance.getGameProfile().name();
        String displayName = "";

        if (instance.level().isClientSide()) { displayName = AcauHardCoreCache.displayNames.get(instance.getUUID()); }
        else if (instance.level().getServer() != null) {
            DisplayNameData data = DisplayNameData.getServerState(Objects.requireNonNull(instance.level().getServer()));
            displayName = data.getDisplayName(instance.getUUID());
        }

        if (null == displayName || displayName.isEmpty()) { return Component.literal(originalName); }

        return Component.literal(displayName);
    }

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue()
            .add(ModAttributes.LIFE_STEAL)
            .add(ModAttributes.CRIT_CHANCE)
            .add(ModAttributes.CRIT_DAMAGE)
            .add(ModAttributes.DODGE_CHANCE);
    }

    @ModifyVariable(method = "giveExperiencePoints", at = @At("HEAD"), argsOnly = true)
    private int modifyExperience(int originalXp) {
        Player player = (Player) (Object) this;
        List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(player);
        int finalXp = originalXp;

        for (RuneInstance rune : runes) {
            if (rune.effect() == RUNE_EFFECT.XP_BOOST) {
                if (player.getRandom().nextFloat() * 100f <= rune.value1()) {
                    finalXp += (int) rune.value2();
                }
            }
        }

        return finalXp;
    }
}
