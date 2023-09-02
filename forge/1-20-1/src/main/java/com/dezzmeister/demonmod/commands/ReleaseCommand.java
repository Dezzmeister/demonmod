package com.dezzmeister.demonmod.commands;

import com.dezzmeister.demonmod.core.NotPossessingException;
import com.dezzmeister.demonmod.core.PossessionController;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ReleaseCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("release")
            .requires((src) -> {
                return src.hasPermission(2);
            })
            .executes(ReleaseCommand::doRelease)
        );
    }

    private static int doRelease(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("/release can only be run by a player"));
            return -1;
        }

        final ServerPlayer demon = source.getPlayerOrException();
        final PossessionController controller = PossessionController.createOrGet();

        try {
            final Entity victim = controller.release(demon);
            source.sendSystemMessage(Component.literal("Released ").append(victim.getDisplayName()));
        } catch (NotPossessingException e) {
            source.sendFailure(Component.literal("Must /possess something first"));
            return -1;
        }

        return 0;
    }
}
