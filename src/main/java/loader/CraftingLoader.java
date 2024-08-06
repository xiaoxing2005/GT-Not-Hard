package loader;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import item.ItemGoldenEgg;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static loader.ItemLoader.goldenEgg;

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
                new ItemStack(goldenEgg),
                "###","#*#","###",'#',new ItemStack(Items.diamond),'*', new ItemStack(Items.stick)
            )
        );
    }

    private static void registerSmelting(){
        GameRegistry.addSmelting(Blocks.dirt,new ItemStack(Items.stick),0.5F);
    }

    private static void registerFuel(){
        GameRegistry.registerFuelHandler(
            new IFuelHandler() {
                @Override
                public int getBurnTime(ItemStack fuel) {
                    return Items.diamond != fuel.getItem() ? 0:12800;
                }
            }
        );
    }
}
