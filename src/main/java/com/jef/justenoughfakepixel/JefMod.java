package com.jef.justenoughfakepixel;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.command.SimpleCommandFilter;
import com.jef.justenoughfakepixel.utils.ItemStackUtils;
import com.jef.justenoughfakepixel.utils.PartyCommands;
import com.jef.justenoughfakepixel.features.mining.FetchurHelper;
import com.jef.justenoughfakepixel.features.general.DamageSplashes;
import com.jef.justenoughfakepixel.features.general.SkyblockIdTooltip;
import com.jef.justenoughfakepixel.features.general.CursorResetHandler;
import com.jef.justenoughfakepixel.features.misc.BrewingStandHelper;
import com.jef.justenoughfakepixel.features.misc.PerformanceHUD;
import com.jef.justenoughfakepixel.features.misc.SkillXpDisplay;
import com.jef.justenoughfakepixel.features.misc.SearchBar;
import com.jef.justenoughfakepixel.features.dungeons.BloodMobDisplay;
import com.jef.justenoughfakepixel.features.dungeons.DungeonStats;
import com.jef.justenoughfakepixel.features.waypoints.WaypointCommand;
import com.jef.justenoughfakepixel.features.waypoints.WaypointRenderer;
import com.jef.justenoughfakepixel.features.waypoints.WaypointStorage;
import com.jef.justenoughfakepixel.features.diana.DianaCommand;
import com.jef.justenoughfakepixel.features.diana.DianaOverlay;
import com.jef.justenoughfakepixel.features.diana.DianaStats;
import com.jef.justenoughfakepixel.features.diana.DianaTracker;
import com.jef.justenoughfakepixel.repo.RepoHandler;
import com.jef.justenoughfakepixel.repo.JefRepo;
import com.jef.justenoughfakepixel.repo.VersionChecker;
import net.minecraftforge.client.ClientCommandHandler;
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
        clientSideOnly = true
)
public class JefMod {

    public static final String MODID = "justenoughfakepixel";
    public static final String NAME  = "JustEnoughFakepixel";
    public static final String VERSION = "1.2.0";

    @SubscribeEvent
    public void onServerJoin(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        RepoHandler.refresh(JefRepo.KEY_PLAYERSIZES);
        RepoHandler.refresh(JefRepo.KEY_UPDATE);
    }

    // Make config accessible to features
    public static JefConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        JefConfig.init();
        JefRepo.init();

        // Point waypoint storage at the same config directory as JEF
        WaypointStorage.getInstance().initFile(JefConfig.configDirectory);
        // Point diana tracker to JEF config dir
        DianaStats.getInstance().initFile(JefConfig.configDirectory);
    }

    @Mod.EventHandler
    public void clientInit(FMLInitializationEvent event) {
        JefConfig.register();
        WaypointStorage.getInstance().load();
        DianaStats.getInstance().load();
        MinecraftForge.EVENT_BUS.register(this);


        // Register features
        MinecraftForge.EVENT_BUS.register(new SearchBar());
        MinecraftForge.EVENT_BUS.register(new DamageSplashes());
        MinecraftForge.EVENT_BUS.register(new BloodMobDisplay());
        MinecraftForge.EVENT_BUS.register(new DungeonStats());
        MinecraftForge.EVENT_BUS.register(new PartyCommands());
        MinecraftForge.EVENT_BUS.register(new FetchurHelper());
        MinecraftForge.EVENT_BUS.register(new PerformanceHUD());
        MinecraftForge.EVENT_BUS.register(new SkillXpDisplay());
        MinecraftForge.EVENT_BUS.register(new SkyblockIdTooltip());
        MinecraftForge.EVENT_BUS.register(new SimpleCommandFilter());
        MinecraftForge.EVENT_BUS.register(new WaypointRenderer());
        MinecraftForge.EVENT_BUS.register(new VersionChecker());
        MinecraftForge.EVENT_BUS.register(new BrewingStandHelper());
        MinecraftForge.EVENT_BUS.register(new ItemStackUtils());
        MinecraftForge.EVENT_BUS.register(new CursorResetHandler());
        MinecraftForge.EVENT_BUS.register(new DianaTracker());
        MinecraftForge.EVENT_BUS.register(new DianaOverlay());
        ClientCommandHandler.instance.registerCommand(new DianaCommand());
        ClientCommandHandler.instance.registerCommand(new WaypointCommand());
    }
}