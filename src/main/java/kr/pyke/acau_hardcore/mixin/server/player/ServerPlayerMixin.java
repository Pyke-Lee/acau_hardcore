package kr.pyke.acau_hardcore.mixin.server.player;

import com.mojang.authlib.GameProfile;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.ServerSavedData;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.prefix.PrefixData;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.util.ColorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Unique private static final Identifier ACAU_HUNGER_PENALTY_ID = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hunger_health_penalty");

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideTabListName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        var info = ModComponents.HARDCORE_INFO.get(self);

        String displayName = DisplayNameData.getServerState(self.level().getServer()).getDisplayName(self.getUUID());
        if (info.isStarted()) {
            displayName = displayName + String.format("(%s)", info.getHardcoreType().getDisplayName());
        }

        MutableComponent nameComponent = Component.empty();
        var prefixes = ModComponents.PREFIXES.get(self);

        if (!prefixes.getSelectedPrefix().equals("none")) {
            PrefixData prefixData = PrefixRegistry.get(prefixes.getSelectedPrefix());
            if (prefixData != null) {
                nameComponent.append(ColorParser.parse(prefixData.prefix())).append(Component.literal(" "));
            }
        }

        if (displayName != null && !displayName.isEmpty()) {
            nameComponent.append(Component.literal(displayName));
            cir.setReturnValue(nameComponent);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        int foodLevel = this.getFoodData().getFoodLevel();
        AttributeInstance maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealthAttr != null) {
            double desiredPenalty = 0d;

            if (foodLevel == 0) { desiredPenalty = -10d; }
            else if (foodLevel <= 10) { desiredPenalty = -5d; }

            AttributeModifier currentModifier = maxHealthAttr.getModifier(ACAU_HUNGER_PENALTY_ID);
            double currentPenalty = 0d;

            if (currentModifier != null) { currentPenalty = currentModifier.amount(); }

            if (desiredPenalty != currentPenalty) {
                maxHealthAttr.removeModifier(ACAU_HUNGER_PENALTY_ID);

                if (desiredPenalty != 0d) {
                    maxHealthAttr.addTransientModifier(new AttributeModifier(ACAU_HUNGER_PENALTY_ID, desiredPenalty, AttributeModifier.Operation.ADD_VALUE));
                }

                if (this.getHealth() > this.getMaxHealth()) {
                    this.setHealth(this.getMaxHealth());
                }
            }
        }
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"), cancellable = true)
    private void overrideRespawnPositionAndUseSpawnBlock(boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleportTransition, CallbackInfoReturnable<TeleportTransition> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        var component = ModComponents.HARDCORE_INFO.get(self);
        ServerSavedData data = ServerSavedData.getServerState(self.level().getServer());

        TeleportTransition transition;
        if (component.isJail()) {
            transition = data.createJailTransition(self.level().getServer(), postTeleportTransition);
        }
        else {
            transition = data.createLobbyTransition(self.level().getServer(), postTeleportTransition);
        }

        if (transition != null) { cir.setReturnValue(transition); }
    }
}