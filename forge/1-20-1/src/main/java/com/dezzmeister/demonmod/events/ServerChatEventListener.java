package com.dezzmeister.demonmod.events;

import com.dezzmeister.demonmod.core.PossessionController;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerChatEventListener {
    
    @SubscribeEvent
    public void handleServerChatEvent(ServerChatEvent event) {
        final PossessionController controller = PossessionController.createOrGet();
        final ServerPlayer demon = event.getPlayer();
        final Entity victim = controller.getPossessedEntity(demon);

        if (victim != null) {
            event.setCanceled(true);

            final MinecraftServer server = demon.getServer();

            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("[").append(victim.getDisplayName()).append(Component.literal("] " + event.getRawText() )), false);
            }
        }
    }
}
