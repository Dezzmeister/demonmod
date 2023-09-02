package com.dezzmeister.demonmod.core;

import java.util.Set;

import javax.annotation.Nullable;

import com.dezzmeister.demonmod.DemonMod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Teleporter {
    // Taken from Minecraft's TeleportCommand
    public static void performTeleport(CommandSourceStack source, Entity target, ServerLevel level, double x, double y, double z, Set<RelativeMovement> relCoords, float yRot, float xRot, @Nullable LookAt lookAt) {
        net.minecraftforge.event.entity.EntityTeleportEvent.TeleportCommand event = net.minecraftforge.event.ForgeEventFactory.onEntityTeleportCommand(target, x, y, z);
        if (event.isCanceled()) return;
        x = event.getTargetX(); y = event.getTargetY(); z = event.getTargetZ();
        BlockPos blockpos = BlockPos.containing(x, y, z);

        if (!Level.isInSpawnableBounds(blockpos)) {
           DemonMod.LOGGER.error("Failed to teleport entity");
           return;
        }

        float f = Mth.wrapDegrees(yRot);
        float f1 = Mth.wrapDegrees(xRot);

        if (target.teleportTo(level, x, y, z, relCoords, f, f1)) {
            if (lookAt != null) {
                lookAt.perform(source, target);
            }

            label23: {
                if (target instanceof LivingEntity) {
                   LivingEntity livingentity = (LivingEntity)target;
                   if (livingentity.isFallFlying()) {
                      break label23;
                   }
                }
                target.setDeltaMovement(target.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
                target.setOnGround(true);
            }

            if (target instanceof PathfinderMob) {
                PathfinderMob pathfindermob = (PathfinderMob)target;
                pathfindermob.getNavigation().stop();
            }
        }
    }

    public static class LookAt {
        private final Vec3 position;
        private final Entity entity;
        private final EntityAnchorArgument.Anchor anchor;

        public LookAt(Entity p_139056_, EntityAnchorArgument.Anchor p_139057_) {
            this.entity = p_139056_;
            this.anchor = p_139057_;
            this.position = p_139057_.apply(p_139056_);
        }

        public LookAt(Vec3 p_139059_) {
            this.entity = null;
            this.position = p_139059_;
            this.anchor = null;
        }

        public void perform(CommandSourceStack p_139061_, Entity p_139062_) {
            if (this.entity != null) {
                if (p_139062_ instanceof ServerPlayer) {
                    ((ServerPlayer)p_139062_).lookAt(p_139061_.getAnchor(), this.entity, this.anchor);
                } else {
                    p_139062_.lookAt(p_139061_.getAnchor(), this.position);
                }
            } else {
                p_139062_.lookAt(p_139061_.getAnchor(), this.position);
            }
        }
    }
}
