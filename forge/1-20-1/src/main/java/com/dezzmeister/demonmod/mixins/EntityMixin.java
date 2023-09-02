package com.dezzmeister.demonmod.mixins;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.dezzmeister.demonmod.events.custom.TeleportingEntityRebornEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraftforge.common.MinecraftForge;

/**
 * Injects some code into {@link Entity#teleportTo(ServerLevel, double, double, double, Set, float, float)} to fire a custom
 * event with the old entity being removed, and the new entity being created in its place. Some notes about this mixin:
 * 
 * <ul>
 *  <li>    Because we are capturing local variables at the point of injection, the method signature of our `teleportTo` 
 *          must be different. In addition to the CallbackInfoReturnable that usually comes at the end (or just CallbackInfo),
 *          we need to put all of the local variables in order of scope - those with the largest scope first, etc.
 *  <li>    The code is injected AFTER the target method is invoked. The format of the selector for the target method can be
 *          found by reading the javadoc for {@link org.spongepowered.asm.mixin.injection.struct.MemberInfo}.
 * </ul>    
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "teleportTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addDuringTeleport(Lnet/minecraft/world/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void teleportTo(ServerLevel p_265257_, double p_265407_, double p_265727_, double p_265410_, Set<RelativeMovement> p_265083_, float p_265573_, float p_265094_, CallbackInfoReturnable<Boolean> cb, float f, Entity entity) {
        final TeleportingEntityRebornEvent event = new TeleportingEntityRebornEvent(entity, (Entity) (Object) this);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
