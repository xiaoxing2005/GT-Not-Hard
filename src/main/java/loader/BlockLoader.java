package loader;

import block.BlockRainBow;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class BlockLoader {
    public static Block rainBow = new BlockRainBow();

    public BlockLoader(FMLPreInitializationEvent event){
        register(rainBow,"rainBow");
    }

    private static void register(Block block,String name){
        GameRegistry.registerBlock(block,name);
    }
}
