package com.dezzmeister.demonmod.commands;

import com.dezzmeister.demonmod.core.AlreadyPossessingException;
import com.dezzmeister.demonmod.core.PossessionController;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PossessCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("possess")
            .requires((src) -> {
                return src.hasPermission(2);
            })
            .then(
                Commands.argument("victim", EntityArgument.entity())
                .executes(PossessCommand::doPossession)
            )
        );
    }

    private static int doPossession(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();

        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("/possess can only be run by a player"));
            return -1;
        }

        final ServerPlayer demon = source.getPlayerOrException();
        final Entity victim = EntityArgument.getEntity(context, "victim");
        final PossessionController controller = PossessionController.createOrGet();

        if (demon.equals(victim)) {
            source.sendFailure(Component.literal("Cannot possess yourself"));
            return -1;
        }
        
        try {
            if (victim instanceof ServerPlayer) {
                // TODO: Implement possession of other players
                source.sendFailure(Component.literal("Cannot possess players (yet!)"));
                return -1;
            } else {
                controller.possessEntity(demon, victim);
                source.sendSystemMessage(Component.literal("Possessed entity ").append(victim.getDisplayName()));
            }
        } catch (AlreadyPossessingException e) {
            source.sendFailure(Component.literal("Already possessing ").append(e.victim.getDisplayName()));
            return -1;
        }

        return 1;
    }
}
