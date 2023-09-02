package com.dezzmeister.demonmod.core;

import net.minecraft.server.level.ServerPlayer;

public record PlayerPossessionState(ServerPlayer victim) {

    public boolean isPlayer(ServerPlayer player) {
        return victim.equals(player);
    }
}
