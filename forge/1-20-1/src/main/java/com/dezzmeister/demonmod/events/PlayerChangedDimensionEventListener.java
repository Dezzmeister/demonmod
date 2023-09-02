package com.dezzmeister.demonmod.events;

import java.util.EnumSet;

import com.dezzmeister.demonmod.core.PossessionController;
import com.dezzmeister.demonmod.core.Teleporter;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerChangedDimensionEventListener {

    @SubscribeEvent
    public void handlePlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        final PossessionController controller = PossessionController.createOrGet();
        final Entity player = event.getEntity();

        if (!(player instanceof ServerPlayer)) {
            return;
        }
        
        final ServerPlayer demon = (ServerPlayer) player;
        final Entity victim = controller.getPossessed(demon);

        if (victim == null) {
            return;
        }

        final MinecraftServer server = event.getEntity().getServer();

        if (server == null) {
            return;
        }

        final ServerLevel toDimension = server.getLevel(event.getTo());

        if (toDimension == null) {
            return;
        }

        // We have to tell the possession controller that the victim will change dimension soon. This is
        // because when an entity is teleported, the existing entity is marked as "removed," and a copy
        // is created in the target dimension. In the TeleportingEventRebornEvent listener, we will pass
        // the reborn entity and the old entity to the controller so that the possession map can be updated.
        controller.setChangedDimensionEntity(demon, victim);

        final CommandSourceStack serverStack = server.createCommandSourceStack();
        final Vec3 lookVec = demon.getLookAngle();
        final Vec3 absoluteLookVec = lookVec.add(demon.position());
        Teleporter.performTeleport(serverStack, victim, toDimension, demon.getX(), demon.getY(), demon.getZ(), EnumSet.noneOf(RelativeMovement.class), demon.getYRot(), demon.getXRot(), new Teleporter.LookAt(absoluteLookVec));
    }
}
