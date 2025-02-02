package loader;

import static loader.MachinesLoader.ChaosMain;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;

public class CraftingLoader {

    public CraftingLoader() {
        registerRecipe();
    }

    private static final long bits = GTModHandler.RecipeBits.NOT_REMOVABLE | GTModHandler.RecipeBits.REVERSIBLE
        | GTModHandler.RecipeBits.BUFFERED;

    private static final long bitsd = GTModHandler.RecipeBits.DISMANTLEABLE | bits;

    private static void registerRecipe() {
        ChaosRecipeLoader.registerDefaultGregtechMaps();
        GTModHandler.addCraftingRecipe(
            ChaosMain,
            bitsd,
            new Object[] { "CBC", "FMF", "CBC", 'M', ItemList.Hull_HV, 'B', OrePrefixes.pipeLarge.get(Materials.Steel),
                'C', OrePrefixes.circuit.get(Materials.EV), 'F', ItemList.Electric_Pump_HV });
    }
}
