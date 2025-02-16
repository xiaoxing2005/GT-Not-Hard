package com.mofoga.gtnothard;

import static com.gtnewhorizon.structurelib.StructureLibAPI.setDebugEnabled;
import static loader.MachinesLoader.loaderMachines;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import bartworks.common.configs.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import galacticgreg.registry.GalacticGregRegistry;
import gtneioreplugin.util.DimensionHelper;
import loader.AssemblyLineWithoutResearchRecipePool;
import loader.CraftingLoader;

@Mod(modid = MyMod.MODID, version = Tags.VERSION, name = "GT Not Hard", acceptedMinecraftVersions = "[1.7.10]")
public class MyMod {

    public static final String MODID = "gtnothard";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.mofoga.gtnothard.ClientProxy", serverSide = "com.mofoga.gtnothard.CommonProxy")
    public static CommonProxy proxy;

    static {
        setDebugEnabled(true);
    }

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {

        proxy.preInit(event);
        // new MachinesLoader();
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        loaderMachines();
        new CraftingLoader();
        // new MachinesLoader();
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        // ChaosRecipeLoader loadRecipes = new ChaosRecipeLoader();
        // loadRecipes.loadRecipes();

    }

    public static BiMap<Integer, String> dimMapping = HashBiMap.create();
    public static List<String> dimName = Arrays.asList(DimensionHelper.DimName);
    public static HashMap<Integer, String> cahce = new HashMap<>();

    @SubscribeEvent
    public void a(WorldEvent.Load e) {
        for (int i : DimensionManager.getStaticDimensionIDs()) {
            if (dimMapping.containsKey(i)) continue;
            String name = getNameForID(i);

            int index;
            if ((index = dimName.indexOf(name)) >= 0) {
                dimMapping.forcePut(i, DimensionHelper.DimNameDisplayed[index]);
                // dimDefMapping.forcePut(def, DimensionHelper.DimNameDisplayed[index]);
            } ;
        }

        try {
            cahce.put(
                e.world.provider.dimensionId,
                ((ChunkProviderServer) e.world.getChunkProvider()).currentChunkProvider.getClass()
                    .getName());
        } catch (Exception ee) {}

    }

    public static String getNameForID(int id) {
        if (id == Configuration.CrossModInteractions.ross128btier) {
            return "Ross128b";
        }
        if (id == Configuration.crossModInteractions.ross128BAID) {
            return "Ross128ba";
        }
        if (id == 0) {
            return "Overworld";
        }
        if (id == -1) {
            return "Nether";
        }
        if (id == 7) {
            return "Twilight";
        }
        if (id == 1) {
            return "TheEnd";
        }

        return GalacticGregRegistry.getModContainers()
            .stream()
            .flatMap(
                modContainer -> modContainer.getDimensionList()
                    .stream())

            .filter(s -> {
                if (DimensionManager.getWorld(id) == null) return false;
                return s.getChunkProviderName()
                    .equals(
                        DimensionManager.getProvider(id)
                            .createChunkGenerator()
                            .getClass()
                            .getName());
            })
            .map(s -> s.getDimIdentifier())
            .findFirst()
            .orElse(null);
    }

    @Mod.EventHandler
    public void completeInit(FMLLoadCompleteEvent event) {
        AssemblyLineWithoutResearchRecipePool assemblyLineWithoutResearchRecipePool = new AssemblyLineWithoutResearchRecipePool();
        assemblyLineWithoutResearchRecipePool.loadRecipes();
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
