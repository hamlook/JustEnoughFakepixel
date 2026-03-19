package com.jef.justenoughfakepixel;

import com.jef.justenoughfakepixel.data.ApiHandler;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.command.SimpleCommandFilter;
import com.jef.justenoughfakepixel.features.diana.*;
import com.jef.justenoughfakepixel.features.dungeons.BloodMobDisplay;
import com.jef.justenoughfakepixel.features.dungeons.DungeonStats;
import com.jef.justenoughfakepixel.features.general.CursorResetHandler;
import com.jef.justenoughfakepixel.features.general.DamageSplashes;
import com.jef.justenoughfakepixel.features.farming.LockMouse;
import com.jef.justenoughfakepixel.features.farming.LockMouseCommand;
import com.jef.justenoughfakepixel.features.general.EnchantProcessor;
import com.jef.justenoughfakepixel.features.general.GyroWandHelper;
import com.jef.justenoughfakepixel.features.general.GyroWandOverlay;
import com.jef.justenoughfakepixel.features.general.MissingEnchants;
import com.jef.justenoughfakepixel.features.general.SkyblockIdTooltip;
import com.jef.justenoughfakepixel.features.mining.FetchurOverlay;
import com.jef.justenoughfakepixel.features.misc.*;
import com.jef.justenoughfakepixel.features.scoreboard.CustomScoreboard;
import com.jef.justenoughfakepixel.features.scoreboard.MaxwellPowerSync;
import com.jef.justenoughfakepixel.features.waypoints.WaypointCommand;
import com.jef.justenoughfakepixel.features.waypoints.WaypointRenderer;
import com.jef.justenoughfakepixel.features.waypoints.WaypointStorage;
import com.jef.justenoughfakepixel.repo.JefRepo;
import com.jef.justenoughfakepixel.repo.RepoHandler;
import com.jef.justenoughfakepixel.repo.VersionChecker;
import com.jef.justenoughfakepixel.utils.ItemStackUtils;
import com.jef.justenoughfakepixel.utils.PartyCommands;
import com.jef.justenoughfakepixel.utils.TablistParser;
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
        clientSideOnly = true,
        guiFactory = "com.jef.justenoughfakepixel.JefGuiFactory"
)
public class JefMod {

    public static final String MODID   = "justenoughfakepixel";
    public static final String NAME    = "JustEnoughFakepixel";
    public static final String VERSION = "1.2.3";

    @SubscribeEvent
    public void onServerJoin(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        RepoHandler.refresh(JefRepo.KEY_PLAYERSIZES);
        RepoHandler.refresh(JefRepo.KEY_UPDATE);
        ApiHandler.onServerJoin();
    }

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
        MinecraftForge.EVENT_BUS.register(new SearchBar());
        MinecraftForge.EVENT_BUS.register(MaxwellPowerSync.getInstance());
        MinecraftForge.EVENT_BUS.register(new DamageSplashes());
        MinecraftForge.EVENT_BUS.register(new BloodMobDisplay());
        MinecraftForge.EVENT_BUS.register(new DungeonStats());
        MinecraftForge.EVENT_BUS.register(new CustomScoreboard());
        MinecraftForge.EVENT_BUS.register(new PartyCommands());
        MinecraftForge.EVENT_BUS.register(new TablistParser());
        MinecraftForge.EVENT_BUS.register(new FetchurOverlay());
        MinecraftForge.EVENT_BUS.register(new PerformanceHUD());
        MinecraftForge.EVENT_BUS.register(new SkillXpDisplay());
        MinecraftForge.EVENT_BUS.register(new SkyblockIdTooltip());
        MinecraftForge.EVENT_BUS.register(new SimpleCommandFilter());
        MinecraftForge.EVENT_BUS.register(new WaypointRenderer());
        MinecraftForge.EVENT_BUS.register(new VersionChecker());
        MinecraftForge.EVENT_BUS.register(new BrewingStandHelper());
        MinecraftForge.EVENT_BUS.register(new ItemStackUtils());
        MinecraftForge.EVENT_BUS.register(new CursorResetHandler());
        MinecraftForge.EVENT_BUS.register(new GyroWandOverlay());
        MinecraftForge.EVENT_BUS.register(new GyroWandHelper());
        MinecraftForge.EVENT_BUS.register(new EnchantProcessor());
        MinecraftForge.EVENT_BUS.register(new MissingEnchants());
        MinecraftForge.EVENT_BUS.register(new DianaTracker());
        MinecraftForge.EVENT_BUS.register(new DianaMobDetect());
        MinecraftForge.EVENT_BUS.register(new DianaEventOverlay());
        MinecraftForge.EVENT_BUS.register(new DianaLootOverlay());
        MinecraftForge.EVENT_BUS.register(new InqHealthOverlay());
        MinecraftForge.EVENT_BUS.register(new DianaMobHealthOverlay());
        MinecraftForge.EVENT_BUS.register(CurrentPetTracker.getInstance());
        MinecraftForge.EVENT_BUS.register(new CurrentPetOverlay());
        MinecraftForge.EVENT_BUS.register(new LockMouse());
        ClientCommandHandler.instance.registerCommand(new LockMouseCommand());
        ClientCommandHandler.instance.registerCommand(new DianaCommand());
        ClientCommandHandler.instance.registerCommand(new WaypointCommand());
    }
}