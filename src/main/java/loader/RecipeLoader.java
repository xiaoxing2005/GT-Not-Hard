package loader;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTModHandler;

public class RecipeLoader implements Runnable{

    private static final long bits = GTModHandler.RecipeBits.NOT_REMOVABLE | GTModHandler.RecipeBits.REVERSIBLE
        | GTModHandler.RecipeBits.BUFFERED;
    private static final long bitsd = GTModHandler.RecipeBits.DISMANTLEABLE | bits;

    private static void registerShapedCraftingRecipes() {
        ChaosRecipeLoader.registerDefaultGregtechMaps();
        GTModHandler.addCraftingRecipe(
            ItemList.Distillation_Tower.get(1L),
            bitsd,
            new Object[] { "CBC", "FMF", "CBC", 'M', ItemList.Hull_HV, 'B',
                OrePrefixes.pipeLarge.get(Materials.StainlessSteel), 'C', OrePrefixes.circuit.get(Materials.EV), 'F',
                ItemList.Electric_Pump_HV });
    }

    @Override
    public void run() {
        registerShapedCraftingRecipes();
        GTLog.out.println("GTMod: Recipes for MetaTileEntities.");
    }
}
