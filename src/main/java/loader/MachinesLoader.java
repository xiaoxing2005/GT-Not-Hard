package loader;

import machines.Origin;
import net.minecraft.item.ItemStack;

import machines.Chaos;

public class MachinesLoader {

    public static ItemStack ChaosMain;
    public static ItemStack OriginMain;

    public static void loaderMachines() {
        ChaosMain = new Chaos(25565, "Chaos", "Chaos").getStackForm(1);
        OriginMain = new Origin(25531, "Origin", "Origin").getStackForm(1);
    }

}
