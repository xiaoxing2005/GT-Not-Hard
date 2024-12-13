package loader;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;


public class ItemLoader {
    public ItemLoader(FMLPreInitializationEvent event){
        //register(ChaosMain, "Chaos");
    }
    public static void register(Item item, String name){
        GameRegistry.registerItem(item,name);
    }
}
