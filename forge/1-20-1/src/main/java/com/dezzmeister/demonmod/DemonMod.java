package com.dezzmeister.demonmod;

import com.dezzmeister.demonmod.events.LivingAttackEventListener;
import com.dezzmeister.demonmod.events.LivingDeathEventListener;
import com.dezzmeister.demonmod.events.LivingTickEventListener;
import com.dezzmeister.demonmod.events.PlayerChangedDimensionEventListener;
import com.dezzmeister.demonmod.events.PlayerLoggedOutEventListener;
import com.dezzmeister.demonmod.events.RegisterCommandsEventListener;
import com.dezzmeister.demonmod.events.ServerChatEventListener;
import com.dezzmeister.demonmod.events.TeleportingEntityRebornEventListener;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DemonMod.MODID)
public class DemonMod {
    public static final String MODID = "demonmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DemonMod() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RegisterCommandsEventListener());
        MinecraftForge.EVENT_BUS.register(new LivingTickEventListener());
        MinecraftForge.EVENT_BUS.register(new ServerChatEventListener());
        MinecraftForge.EVENT_BUS.register(new LivingDeathEventListener());
        MinecraftForge.EVENT_BUS.register(new LivingAttackEventListener());
        MinecraftForge.EVENT_BUS.register(new PlayerChangedDimensionEventListener());
        MinecraftForge.EVENT_BUS.register(new TeleportingEntityRebornEventListener());
        MinecraftForge.EVENT_BUS.register(new PlayerLoggedOutEventListener());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartedEvent event) {
        LOGGER.info("Demonmod starting");
    }
}
