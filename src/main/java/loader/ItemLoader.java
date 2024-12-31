package loader;

import net.minecraft.item.Item;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemLoader {

    public ItemLoader(FMLPreInitializationEvent event) {
        // register(ChaosMain, "Chaos");
    }

    public static void register(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }
}
