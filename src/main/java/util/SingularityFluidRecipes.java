package util;

import bartworks.system.material.WerkstoffLoader;
import gregtech.api.enums.Materials;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public class SingularityFluidRecipes {
    public static final Map<String, Map<Integer, FluidStack>> VoidFliudRecipes = new HashMap<>();

    public static void addVoidFliudRecipes() {
        //T0 - Overworld - Ow
        Map<Integer,FluidStack> OverworldRecipes = new HashMap<>();
        OverworldRecipes.put(1, Materials.Oil.getFluid(1000));
        OverworldRecipes.put(2, Materials.OilLight.getFluid(1000));
        OverworldRecipes.put(3, Materials.OilHeavy.getFluid(1000));
        OverworldRecipes.put(4, Materials.OilMedium.getFluid(1000));
        OverworldRecipes.put(5, Materials.NatruralGas.getGas(1000));

        //T1 - Moon - Mo
        Map<Integer,FluidStack> MoonRecipes = new HashMap<>();
        MoonRecipes.put(1, Materials.SaltWater.getFluid(1000));
        MoonRecipes.put(2, Materials.Helium_3.getGas(1000));

        //T2 - Mars - Ma
        Map<Integer,FluidStack> MarsRecipes = new HashMap<>();
        MarsRecipes.put(1, Materials.SaltWater.getFluid(1000));
        MarsRecipes.put(2, Materials.Chlorobenzene.getFluid(1000));

        //T3 - Callisto - Ca
        Map<Integer,FluidStack> CallistoRecipes = new HashMap<>();
        CallistoRecipes.put(1, Materials.Oxygen.getGas(1000));
        CallistoRecipes.put(2, Materials.LiquidAir.getFluid(1000));

        //T3 - Europa - Eu
        Map<Integer,FluidStack> EuropaRecipes = new HashMap<>();
        EuropaRecipes.put(1, Materials.SaltWater.getFluid(1000));
        EuropaRecipes.put(2, Materials.OilExtraHeavy.getFluid(1000));

        //T3 - Ross128b - Rb
        Map<Integer,FluidStack> Ross128bRecipes = new HashMap<>();
        Ross128bRecipes.put(1, Materials.Lava.getFluid(1000));
        Ross128bRecipes.put(2, Materials.NatruralGas.getGas(1000));
        Ross128bRecipes.put(3, Materials.OilExtraHeavy.getFluid(1000));

        //T4 - Io - Io
        Map<Integer,FluidStack> IoRecipes = new HashMap<>();
        IoRecipes.put(1, Materials.SulfuricAcid.getFluid(1000));
        IoRecipes.put(2, Materials.CarbonDioxide.getGas(1000));
        IoRecipes.put(3, Materials.Lead.getMolten(1000));

        //T4 - Mercury - Me
        Map<Integer,FluidStack> MercuryRecipes = new HashMap<>();
        MercuryRecipes.put(1, Materials.Iron.getMolten(1000));
        MercuryRecipes.put(2, Materials.Helium_3.getGas(1000));

        //T4 - Venus - Ve
        Map<Integer,FluidStack> VenusRecipes = new HashMap<>();
        VenusRecipes.put(1, Materials.SulfuricAcid.getFluid(1000));
        VenusRecipes.put(2, Materials.CarbonDioxide.getGas(1000));
        VenusRecipes.put(3, Materials.Lead.getMolten(1000));

        //T5 - Miranda - Mi
        Map<Integer,FluidStack> MirandaRecipes = new HashMap<>();
        MirandaRecipes.put(1, Materials.HydricSulfide.getGas(1000));

        //T5 - Oberon - Ob
        Map<Integer,FluidStack> OberonRecipes = new HashMap<>();
        OberonRecipes.put(1, Materials.CarbonMonoxide.getGas(1000));

        //T5 - Titan - Ti
        Map<Integer,FluidStack> TitanRecipes = new HashMap<>();
        TitanRecipes.put(1, Materials.Ethane.getGas(1000));
        TitanRecipes.put(2, Materials.Methane.getGas(1000));
        TitanRecipes.put(3, Materials.Deuterium.getGas(1000));
        TitanRecipes.put(4, Materials.Argon.getGas(1000));

        //T5 - Ross128ba - Ra
        Map<Integer,FluidStack> Ross128baRecipes = new HashMap<>();
        Ross128baRecipes.put(1, Materials.SaltWater.getFluid(1000));
        Ross128baRecipes.put(2, Materials.Helium_3.getGas(1000));
        Ross128baRecipes.put(3, WerkstoffLoader.Neon.getFluidOrGas(1000));
        Ross128baRecipes.put(4, WerkstoffLoader.Krypton.getFluidOrGas(1000));
        Ross128baRecipes.put(5, WerkstoffLoader.Xenon.getFluidOrGas(1000));

        //T6 - Proteus - Pr
        Map<Integer,FluidStack> ProteusRecipes = new HashMap<>();
        ProteusRecipes.put(1, Materials.Deuterium.getGas(1000));
        ProteusRecipes.put(2, Materials.Tritium.getGas(1000));
        ProteusRecipes.put(3, Materials.Helium_3.getGas(1000));
        ProteusRecipes.put(4, Materials.Ammonia.getGas(1000));

        //T6 - Triton - Tr
        Map<Integer,FluidStack> TritonRecipes = new HashMap<>();
        TritonRecipes.put(1, Materials.Ethylene.getGas(1000));
        TritonRecipes.put(2, Materials.Nitrogen.getGas(1000));

        //T7 - Makemake - MM
        Map<Integer,FluidStack> MakemakeRecipes = new HashMap<>();
        MakemakeRecipes.put(1, Materials.HydrofluoricAcid.getFluid(1000));

        //T7 - Pluto - Pl
        Map<Integer,FluidStack> PlutoRecipes = new HashMap<>();
        PlutoRecipes.put(1, Materials.Oxygen.getGas(1000));
        PlutoRecipes.put(2, Materials.Fluorine.getGas(1000));
        PlutoRecipes.put(3, Materials.Nitrogen.getGas(1000));
        PlutoRecipes.put(4, Materials.LiquidAir.getFluid(1000));

        //T8 - BarnardC - BC
        Map<Integer,FluidStack> BarnardCRecipes = new HashMap<>();
        BarnardCRecipes.put(1, Materials.OilExtraHeavy.getFluid(1000));

        //T8 - BarnardE - BE
        Map<Integer,FluidStack> BarnardERecipes = new HashMap<>();
        BarnardERecipes.put(1, Materials.LiquidAir.getFluid(1000));

        //T8 - BarnardF - BF
        Map<Integer,FluidStack> BarnardFRecipes = new HashMap<>();
        BarnardFRecipes.put(1, Materials.Tin.getMolten(1000));

        //T8 - TCetiE - TE
        Map<Integer,FluidStack> TCetiERecipes = new HashMap<>();
        TCetiERecipes.put(1, Materials.Hydrogen.getGas(1000));
        TCetiERecipes.put(2, Materials.OilExtraHeavy.getFluid(1000));

        //T10 - DeepDark - DD
        Map<Integer,FluidStack> DeepDarkRecipes = new HashMap<>();
        DeepDarkRecipes.put(1, Materials.Hydrogen.getGas(1000));
        DeepDarkRecipes.put(2, Materials.OilExtraHeavy.getFluid(1000));

        VoidFliudRecipes.put("Ow", OverworldRecipes);
        VoidFliudRecipes.put("Mo", MoonRecipes);
        VoidFliudRecipes.put("Ma", MarsRecipes);
        VoidFliudRecipes.put("Ca", CallistoRecipes);
        VoidFliudRecipes.put("Eu", EuropaRecipes);
        VoidFliudRecipes.put("Rb", Ross128bRecipes);
        VoidFliudRecipes.put("Io", IoRecipes);
        VoidFliudRecipes.put("Me", MercuryRecipes);
        VoidFliudRecipes.put("Ve", VenusRecipes);
        VoidFliudRecipes.put("Mi", MirandaRecipes);
        VoidFliudRecipes.put("Ob", OberonRecipes);
        VoidFliudRecipes.put("Ti", TitanRecipes);
        VoidFliudRecipes.put("Ra", Ross128baRecipes);
        VoidFliudRecipes.put("Pr", ProteusRecipes);
        VoidFliudRecipes.put("Tr", TritonRecipes);
        VoidFliudRecipes.put("MM", MakemakeRecipes);
        VoidFliudRecipes.put("Pl", PlutoRecipes);
        VoidFliudRecipes.put("BC", BarnardCRecipes);
        VoidFliudRecipes.put("BE", BarnardERecipes);
        VoidFliudRecipes.put("BF", BarnardFRecipes);
        VoidFliudRecipes.put("TE", TCetiERecipes);
        VoidFliudRecipes.put("DD", DeepDarkRecipes);
    }
}
