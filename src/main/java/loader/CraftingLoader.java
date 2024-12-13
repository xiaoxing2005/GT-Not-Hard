package loader;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static loader.MachinesLoader.ChaosMain;

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
                ChaosMain,
                "CBC", "FMF", "CBC",
                'M', ItemList.Hull_HV,
                'B', OrePrefixes.pipeLarge.get(Materials.StainlessSteel),
                'C', OrePrefixes.circuit.get(Materials.EV),
                'F', ItemList.Electric_Pump_HV
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
