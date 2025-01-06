package loader;

import net.minecraft.item.ItemStack;

import machines.Chaos;

public class MachinesLoader {

    public static ItemStack ChaosMain;

    public static void loaderMachines() {
        ChaosMain = new Chaos(25565, "Chaos", "Chaos").getStackForm(1);
    }

}
