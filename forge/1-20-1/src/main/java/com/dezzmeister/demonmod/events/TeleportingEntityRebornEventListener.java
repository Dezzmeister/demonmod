package com.dezzmeister.demonmod.events;

import com.dezzmeister.demonmod.core.PossessionController;
import com.dezzmeister.demonmod.events.custom.TeleportingEntityRebornEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TeleportingEntityRebornEventListener {

    @SubscribeEvent
    public void handleTeleportingEntityRebornEvent(final TeleportingEntityRebornEvent event) {
        final PossessionController controller = PossessionController.createOrGet();

        if (controller.shouldHandleRebornEntities()) {
            controller.handleEntityReborn(event.getOldEntity(), event.getEntity());
        }
    }
}
