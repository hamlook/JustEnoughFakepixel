package com.jef.justenoughfakepixel;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.data.ApiHandler;
import com.jef.justenoughfakepixel.features.misc.CurrentPetTracker;
import com.jef.justenoughfakepixel.features.misc.PetCache;
import com.jef.justenoughfakepixel.features.scoreboard.MaxwellPowerSync;
import com.jef.justenoughfakepixel.features.waypoints.WaypointStorage;
import com.jef.justenoughfakepixel.features.diana.DianaStats;
import com.jef.justenoughfakepixel.init.JefEventRegistrar;
import com.jef.justenoughfakepixel.repo.JefRepo;
import com.jef.justenoughfakepixel.repo.RepoHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@Mod(
        modid = JefMod.MODID,
        name = JefMod.NAME,
        version = JefMod.VERSION,
        clientSideOnly = true,
        guiFactory = "com.jef.justenoughfakepixel.JefGuiFactory"
)
public class JefMod {

    public static final String MODID   = "justenoughfakepixel";
    public static final String NAME    = "JustEnoughFakepixel";
    public static final String VERSION = "1.2.3";

    public static JefConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        JefConfig.init();
        JefRepo.init();
        WaypointStorage.getInstance().initFile(JefConfig.configDirectory);
        DianaStats.getInstance().initFile(JefConfig.configDirectory);
        MaxwellPowerSync.getInstance().initFile(JefConfig.configDirectory);
        PetCache.getInstance().initFile(JefConfig.configDirectory);
        CurrentPetTracker.getInstance().initFile(JefConfig.configDirectory);
    }

    @Mod.EventHandler
    public void clientInit(FMLInitializationEvent event) {
        JefConfig.register();
        WaypointStorage.getInstance().load();
        DianaStats.getInstance().load();
        MaxwellPowerSync.getInstance().load();
        PetCache.getInstance().load();
        CurrentPetTracker.getInstance().load();

        if (JefConfig.feature.misc.showCurrentPet)
            PetCache.getInstance().warmupTextures();

        MinecraftForge.EVENT_BUS.register(this);
        JefEventRegistrar.registerAll();
    }

    @SubscribeEvent
    public void onServerJoin(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        RepoHandler.refresh(JefRepo.KEY_PLAYERSIZES);
        RepoHandler.refresh(JefRepo.KEY_UPDATE);
        ApiHandler.onServerJoin();
    }
}