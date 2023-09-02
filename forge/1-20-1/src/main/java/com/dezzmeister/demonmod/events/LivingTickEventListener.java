package com.dezzmeister.demonmod.events;

import com.dezzmeister.demonmod.core.PossessionController;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LivingTickEventListener {
    
    @SubscribeEvent
    public void handleLivingTickEvent(LivingTickEvent event) {
        final Entity actor = event.getEntity();

        if (!(actor instanceof ServerPlayer)) {
            return;
        }

        final ServerPlayer demon = (ServerPlayer) actor;
        final PossessionController controller = PossessionController.createOrGet();
        final Entity entityVictim = controller.getPossessedEntity(demon);
        
        if (entityVictim != null && !entityVictim.isRemoved()) {
            controlPossessedEntity(demon, entityVictim);
            return;
        }

        final ServerPlayer playerVictim = controller.getPossessedPlayer(demon);

        if (playerVictim != null) {
            controlPossessedPlayer(demon, playerVictim);
        }
    }

    private void controlPossessedEntity(ServerPlayer demon, Entity victim) {
        victim.setPos(demon.getPosition(1.0f));
        victim.setYHeadRot(demon.getYHeadRot());
        victim.setXRot(demon.getXRot());
        victim.setYRot(demon.getYRot());
    }

    private void controlPossessedPlayer(ServerPlayer demon, ServerPlayer victim) {
        // TODO: Implement this
    }
}
