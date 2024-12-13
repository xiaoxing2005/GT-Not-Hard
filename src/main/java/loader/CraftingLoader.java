package loader;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static loader.MachinesLoader.ChaosMain;

public class CraftingLoader {
    public CraftingLoader(){
        registerRecipe();
        registerSmelting();
        registerFuel();
    }

    private static final long bits = GTModHandler.RecipeBits.NOT_REMOVABLE
        | GTModHandler.RecipeBits.REVERSIBLE
        | GTModHandler.RecipeBits.BUFFERED;

    private static final long bitsd = GTModHandler.RecipeBits.DISMANTLEABLE
        | bits;

    private static void registerRecipe(){
        GTModHandler.addCraftingRecipe(
            ChaosMain,
            bitsd,
            new Object[] {
                "CBC",
                "FMF",
                "CBC",
                'M', ItemList.Hull_HV,
                'B', OrePrefixes.pipeLarge.get(Materials.StainlessSteel),
                'C', OrePrefixes.circuit.get(Materials.EV),
                'F', ItemList.Electric_Pump_HV
            }
        );
    }

    private static void registerSmelting() {
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
