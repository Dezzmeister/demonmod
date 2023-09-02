package com.dezzmeister.demonmod.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.dezzmeister.demonmod.DemonMod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team.CollisionRule;

public class PossessionController {
    private static PossessionController theInstance;

    private final HashMap<ServerPlayer, EntityPossessionState> entityPossessions;
    private final HashMap<ServerPlayer, PlayerPossessionState> playerPossessions;
    private final HashMap<Entity, ServerPlayer> changedDimensionEntities;

    public static PossessionController createOrGet() {
        if (theInstance == null) {
            theInstance = new PossessionController();
        }

        return theInstance;
    }

    private PossessionController() {
        this(new HashMap<>(), new HashMap<>());
    }

    private PossessionController(HashMap<ServerPlayer, EntityPossessionState> _entityPossessions, HashMap<ServerPlayer, PlayerPossessionState> _playerPossessions) {
        entityPossessions = _entityPossessions;
        playerPossessions = _playerPossessions;
        changedDimensionEntities = new HashMap<>();
    }

    public void possessEntity(ServerPlayer demon, Entity victim) throws AlreadyPossessingException {
        final EntityPossessionState currentVictim = entityPossessions.get(demon);

        if (currentVictim != null) {
            throw new AlreadyPossessingException(currentVictim.victim());
        }

        final MinecraftServer server = demon.getServer();

        if (server == null) {
            DemonMod.LOGGER.error("Could not get demon's MinecraftServer");
            return;
        }

        final boolean wasNoAi;

        if (victim instanceof Mob) {
            final Mob victimMob = (Mob) victim;
            wasNoAi = victimMob.isNoAi();

            victimMob.setNoAi(true);
        } else {
            wasNoAi = false;
        }
        
        // Move the player to the possessed entity. You can possess an entity in another dimension
        final CommandSourceStack serverStack = server.createCommandSourceStack();
        final Vec3 lookVec = victim.getLookAngle();
        final Vec3 absoluteLookVec = lookVec.add(victim.position());
        Teleporter.performTeleport(serverStack, demon, (ServerLevel) victim.level(), victim.getX(), victim.getY(), victim.getZ(), EnumSet.noneOf(RelativeMovement.class), victim.getYRot(), victim.getXRot(), new Teleporter.LookAt(absoluteLookVec));

        // Add the player and possessed entity to a dummy team so that they don't collide
        // This is necessary because pushing is handled on the client side
        final ServerLevel level = demon.serverLevel();
        final ServerScoreboard scoreboard = level.getScoreboard();
        final UUID teamName = UUID.randomUUID();
        final PlayerTeam dummyTeam = scoreboard.addPlayerTeam(teamName.toString());
        final PlayerTeam oldPlayerTeam = scoreboard.getPlayersTeam(demon.getStringUUID());
        final PlayerTeam oldVictimTeam = scoreboard.getPlayersTeam(victim.getStringUUID());
        scoreboard.addPlayerToTeam(demon.getStringUUID(), dummyTeam);
        scoreboard.addPlayerToTeam(victim.getStringUUID(), dummyTeam);
        dummyTeam.setCollisionRule(CollisionRule.PUSH_OTHER_TEAMS);

        // Save the player's existing invis effect, if they have one
        final MobEffectInstance oldInvis = demon.getEffect(MobEffects.INVISIBILITY);

        // Make the player invisible infinitely - until they release the victim
        final MobEffectInstance effectInstance = new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 1, true, false, false, null, Optional.empty());
        demon.addEffect(effectInstance);

        // Remove the player's armor so they aren't immediately spotted
        final ItemStack headItem = demon.getItemBySlot(EquipmentSlot.HEAD);
        final ItemStack chestItem = demon.getItemBySlot(EquipmentSlot.CHEST);
        final ItemStack legsItem = demon.getItemBySlot(EquipmentSlot.LEGS);
        final ItemStack feetItem = demon.getItemBySlot(EquipmentSlot.FEET);

        demon.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        demon.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        demon.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        demon.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

        entityPossessions.put(demon, new EntityPossessionState(victim, oldPlayerTeam, oldVictimTeam, dummyTeam, wasNoAi, oldInvis, headItem, chestItem, legsItem, feetItem));
    }

    public void possessPlayer(ServerPlayer demon, ServerPlayer victim) throws AlreadyPossessingException {
        final PlayerPossessionState currentVictimState = playerPossessions.get(demon);

        if (currentVictimState != null) {
            throw new AlreadyPossessingException(currentVictimState.victim());
        }

        playerPossessions.put(demon, new PlayerPossessionState(victim));
        // TODO: Set up player possession
    }

    public Entity release(ServerPlayer demon) throws NotPossessingException {
        final EntityPossessionState entityVictimState = entityPossessions.get(demon);
        final PlayerPossessionState playerVictimState = playerPossessions.get(demon);

        if (entityVictimState != null) {
            releaseEntity(demon, entityVictimState);
            return entityVictimState.victim();
        } else if (playerVictimState != null) {
            releasePlayer(demon, playerVictimState);
            return playerVictimState.victim();
        } else {
            throw new NotPossessingException();
        }
    }

    public boolean isPossessed(Entity entity) {
        if (entity instanceof ServerPlayer) {
            for (PlayerPossessionState state : playerPossessions.values()) {
                if (state.isPlayer((ServerPlayer) entity)) {
                    return true;
                }
            }
        } else {
            for (EntityPossessionState victimState : entityPossessions.values()) {
                if (victimState.isEntity(entity)) {
                    return true;
                }
            }
        }

        return false;
    }

    public @Nullable Entity getPossessed(ServerPlayer demon) {
        final Entity possessedEntity = getPossessedEntity(demon);

        if (possessedEntity != null) {
            return possessedEntity;
        }

        return getPossessedPlayer(demon);
    }

    public @Nullable Entity getPossessedEntity(ServerPlayer demon) {
        final EntityPossessionState victimState = entityPossessions.get(demon);

        if (victimState != null) {
            return victimState.victim();
        }

        return null;
    }

    public @Nullable ServerPlayer getPossessedPlayer(ServerPlayer demon) {
        final PlayerPossessionState victimState = playerPossessions.get(demon);

        if (victimState != null) {
            return victimState.victim();
        }

        return null;
    }

    public @Nullable ServerPlayer getPossessor(Entity victim) {
        if (victim instanceof ServerPlayer) {
            for (Entry<ServerPlayer, PlayerPossessionState> entry : playerPossessions.entrySet()) {
                if (entry.getValue().isPlayer((ServerPlayer) victim)) {
                    return entry.getKey();
                }
            }
        } else {
            for (Entry<ServerPlayer, EntityPossessionState> entry : entityPossessions.entrySet()) {
                if (entry.getValue().isEntity(victim)) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    public void setChangedDimensionEntity(ServerPlayer demon, Entity victim) {
        changedDimensionEntities.put(victim, demon);
    }

    /**
     * When an entity changes dimension, the old entity is marked as "removed" and a new one is
     * created in the destination dimension. We have to replace the old entity with the reborn one
     * to avoid memory leaks and to make the mod work properly.
     */
    public void handleEntityReborn(Entity old, Entity reborn) {
        final ServerPlayer demon = changedDimensionEntities.get(old);

        if (demon == null) {
            return;
        }

        final EntityPossessionState oldState = entityPossessions.get(demon);

        if (oldState == null) {
            return;
        }

        entityPossessions.put(demon, oldState.makeReborn(reborn));
        changedDimensionEntities.remove(old);
    }

    public void handleJoinPlayer(ServerPlayer player) {
        // TODO: Implement this
    }

    public boolean shouldHandleRebornEntities() {
        return !changedDimensionEntities.isEmpty();
    }

    private void releaseEntity(ServerPlayer demon, EntityPossessionState victimState) {
        final Entity victim = victimState.victim();
        entityPossessions.remove(demon);

        if (victim instanceof Mob) {
            final Mob victimMob = (Mob) victim;
            victimMob.setNoAi(victimState.wasNoAi());
        }

        // Remove player and entity from the team we added them to and delete the team
        final ServerLevel level = demon.serverLevel();
        final ServerScoreboard scoreboard = level.getScoreboard();

        if (victimState.oldPlayerTeam() != null) {
            scoreboard.addPlayerToTeam(demon.getStringUUID(), victimState.oldPlayerTeam());
        }

        if (victimState.oldVictimTeam() != null) {
            scoreboard.addPlayerToTeam(victim.getStringUUID(), victimState.oldVictimTeam());
        }

        scoreboard.removePlayerTeam(victimState.dummyTeam());

        // Remove invisibility effect from the player and restore old invis effect
        demon.removeEffect(MobEffects.INVISIBILITY);

        if (victimState.oldPlayerInvis() != null) {
            demon.addEffect(victimState.oldPlayerInvis());
        }

        // Restore player's armor
        demon.setItemSlot(EquipmentSlot.HEAD, victimState.headItem());
        demon.setItemSlot(EquipmentSlot.CHEST, victimState.chestItem());
        demon.setItemSlot(EquipmentSlot.LEGS, victimState.legsItem());
        demon.setItemSlot(EquipmentSlot.FEET, victimState.feetItem());
    }

    private void releasePlayer(ServerPlayer demon, PlayerPossessionState victimState) {
        playerPossessions.remove(demon);
    }
}
