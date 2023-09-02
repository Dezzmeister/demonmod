package com.dezzmeister.demonmod.core;

import javax.annotation.Nullable;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;

public record EntityPossessionState(
    Entity victim,
    @Nullable PlayerTeam oldPlayerTeam,
    @Nullable PlayerTeam oldVictimTeam,
    PlayerTeam dummyTeam,
    boolean wasNoAi,
    @Nullable MobEffectInstance oldPlayerInvis,
    ItemStack headItem,
    ItemStack chestItem,
    ItemStack legsItem,
    ItemStack feetItem
) {
    public boolean isEntity(Entity other) {
        return victim.equals(other);
    }

    public EntityPossessionState makeReborn(Entity rebornVictim) {
        return new EntityPossessionState(
            rebornVictim,
            oldPlayerTeam,
            oldVictimTeam,
            dummyTeam,
            wasNoAi,
            oldPlayerInvis,
            headItem,
            chestItem,
            legsItem,
            feetItem
        );
    }
}
