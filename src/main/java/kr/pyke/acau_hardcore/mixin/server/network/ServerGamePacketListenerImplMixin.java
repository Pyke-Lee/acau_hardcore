package kr.pyke.acau_hardcore.mixin.server.network;

import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void injectDisplayNameToChat(PlayerChatMessage message, CallbackInfo ci) {
        DisplayNameData data = DisplayNameData.getServerState(Objects.requireNonNull(this.player.level().getServer()));
        String displayName = data.getDisplayName(this.player.getUUID());

        if (displayName != null && !displayName.isEmpty()) {
            this.player.level().getServer().getPlayerList().broadcastChatMessage(message, this.player, ChatType.bind(ChatType.CHAT, this.player.level().registryAccess(), Component.literal(displayName)));
            ci.cancel();
        }
    }
}