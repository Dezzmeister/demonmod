package com.dezzmeister.demonmod.events;

import com.dezzmeister.demonmod.core.NotPossessingException;
import com.dezzmeister.demonmod.core.PossessionController;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LivingDeathEventListener {
    
    @SubscribeEvent
    public void handleLivingDeathEvent(LivingDeathEvent event) {
        final Entity deadEntity = event.getEntity();
        final PossessionController controller = PossessionController.createOrGet();

        if (deadEntity instanceof ServerPlayer) {
            final ServerPlayer demon = (ServerPlayer) deadEntity;
            final Entity releasedEntityVictim = controller.getPossessedEntity(demon);

            if (releasedEntityVictim != null) {
                try {
                    controller.release(demon);
                } catch (NotPossessingException e) {
                    e.printStackTrace();
                }
            } // TODO: Check for released player victim
        } else {
            final ServerPlayer demon = controller.getPossessor(deadEntity);

            if (demon != null) {
                try {
                    controller.release(demon);
                } catch (NotPossessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
