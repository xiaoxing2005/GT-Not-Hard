package loader;

import cpw.mods.fml.common.registry.GameRegistry;
import item.ItemGoldenEgg;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class CraftingLoader {
    public CraftingLoader(){
        registerRecipe();
        registerSmelting();
        registerFuel();
    }

    private static void registerRecipe(){
/*
        GameRegistry.addShapedRecipe(new ItemStack(new ItemGoldenEgg()),new Object[]{
            "###","#*#","###",'#', Items.diamond,'*', Items.stick
        });
*/
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(new ItemGoldenEgg()),new Object[]{
                "###","#*#","###",'#',new ItemStack(Items.diamond),'*', Items.stick
                }
            )
        );
    }

    private static void registerSmelting(){

    }

    private static void registerFuel(){

    }
}
