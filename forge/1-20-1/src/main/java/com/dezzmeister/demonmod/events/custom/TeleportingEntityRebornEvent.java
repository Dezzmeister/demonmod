package com.dezzmeister.demonmod.events.custom;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * Custom event fired when an entity is teleported to another dimension and the entity is
 * "removed" and replaced with a copy. See {@link com.dezzmeister.demonmod.mixins.EntityMixin}
 * to see where exactly this event is fired.
 */
public class TeleportingEntityRebornEvent extends EntityEvent {
    private final Entity oldEntity;

    public TeleportingEntityRebornEvent(Entity _newEntity, Entity _oldEntity) {
        super(_newEntity);
        this.oldEntity = _oldEntity;
    }
    
    public Entity getOldEntity() {
        return oldEntity;
    }
}
