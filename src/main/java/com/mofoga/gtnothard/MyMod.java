package com.mofoga.gtnothard;

import static loader.MachinesLoader.loaderMachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import loader.AssemblyLineWithoutResearchRecipePool;
import loader.CraftingLoader;

@Mod(modid = MyMod.MODID, version = Tags.VERSION, name = "GT Not Hard", acceptedMinecraftVersions = "[1.7.10]")
public class MyMod {

    public static final String MODID = "gtnothard";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.mofoga.gtnothard.ClientProxy", serverSide = "com.mofoga.gtnothard.CommonProxy")
    public static CommonProxy proxy;

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
