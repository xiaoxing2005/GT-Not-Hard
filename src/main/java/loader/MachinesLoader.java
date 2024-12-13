package loader;

import machines.Chaos;
import net.minecraft.item.ItemStack;

public class MachinesLoader {
    public static ItemStack ChaosMain;
    public static void loaderMachines(){
        //LargeChemicalReactorTest largeChemicalReactorTest = new LargeChemicalReactorTest(20001,"LargeChemicalReactorTest","aNameLargeChemicalReactorTest");
        ChaosMain = new Chaos(20001, "Chaos", "Chaos").getStackForm(1);
    }

}
