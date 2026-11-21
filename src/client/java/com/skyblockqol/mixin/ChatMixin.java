package com.skyblockqol.mixin;

import com.skyblockqol.SkyblockQolClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feeds action-bar and chat lines into the Skyblock trackers. */
@Mixin(ClientPacketListener.class)
public class ChatMixin {
    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void skyblockqol$onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        String text = packet.content().getString();
        if (packet.overlay()) {
            SkyblockQolClient.SKILLS.onActionBar(text);
        } else {
            SkyblockQolClient.SKILLS.onChat(text);
            SkyblockQolClient.CHAT.onChat(text);
        }
    }
}