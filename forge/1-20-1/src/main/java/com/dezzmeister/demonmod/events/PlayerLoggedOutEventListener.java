package com.dezzmeister.demonmod.events;

import com.dezzmeister.demonmod.core.NotPossessingException;
import com.dezzmeister.demonmod.core.PossessionController;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerLoggedOutEventListener {
    
    @SubscribeEvent
    public void handlePlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        final Player player = event.getEntity();

        if (!(player instanceof ServerPlayer)) {
            return;
        }

        final ServerPlayer demon = (ServerPlayer) player;
        final PossessionController controller = PossessionController.createOrGet();

        try {
            controller.release(demon);
        } catch (NotPossessingException e) {
            // Swallow this exception - if the player is not possession anything, that's fine
        }
    }
}
