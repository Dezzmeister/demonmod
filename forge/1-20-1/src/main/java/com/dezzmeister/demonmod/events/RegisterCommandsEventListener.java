package com.dezzmeister.demonmod.events;

import com.dezzmeister.demonmod.commands.PossessCommand;
import com.dezzmeister.demonmod.commands.ReleaseCommand;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommandsEventListener {
    
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        PossessCommand.register(dispatcher);
        ReleaseCommand.register(dispatcher);
    }
}
