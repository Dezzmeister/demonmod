package com.dezzmeister.demonmod.core;

import net.minecraft.world.entity.Entity;

public class AlreadyPossessingException extends Exception {
    public final Entity victim;

    public AlreadyPossessingException(Entity _victim) {
        super("Player is already possessing a victim");
        victim = _victim;
    }
}
