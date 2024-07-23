package loader;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import item.ItemGoldenEgg;
import net.minecraft.item.Item;

public class ItemLoader {
    public static Item goldenEgg = new ItemGoldenEgg();
    public ItemLoader(FMLPreInitializationEvent event){
        register(goldenEgg, "golden_egg");
    }
    public static void register(Item item, String name){
        GameRegistry.registerItem(item,name);
    }
}
