package com.dezzmeister.demonmod.core;

public class NotPossessingException extends Exception {

    public NotPossessingException() {
        super("Player is not possessing anything");
    }
}
