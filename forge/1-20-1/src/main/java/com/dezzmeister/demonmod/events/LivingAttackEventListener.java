package com.dezzmeister.demonmod.events;

import java.lang.reflect.Field;

import com.dezzmeister.demonmod.core.PossessionController;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class LivingAttackEventListener {
    private static Field damageSourceCausingEntity = null;
    
    @SubscribeEvent
    public void handleLivingAttackEvent(LivingAttackEvent event) {
        final DamageSource src = event.getSource();
        final Entity attacker = src.getEntity();

        if (!(attacker instanceof ServerPlayer)) {
            return;
        }

        final ServerPlayer demon = (ServerPlayer) attacker;
        final PossessionController controller = PossessionController.createOrGet();
        final Entity possessed = controller.getPossessed(demon);

        if (possessed == null) {
            return;
        }

        if (possessed.equals(event.getEntity())) {
            // This prevents players from accidentally hitting their victims when
            // trying to attack something else
            event.setCanceled(true);
        } else {
            // Change the attacking entity so that the thing being attacked thinks it was
            // the possessed who attacked them
            final Field field = LivingAttackEventListener.createOrGetField();

            try {
                field.set(src, possessed);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static Field createOrGetField() {
        if (damageSourceCausingEntity == null) {
            damageSourceCausingEntity = ObfuscationReflectionHelper.findField(DamageSource.class, "causingEntity");
        }

        return damageSourceCausingEntity;
    }
}
