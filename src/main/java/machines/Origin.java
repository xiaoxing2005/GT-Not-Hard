package machines;


import com.dreammaster.block.BlockList;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicGenerator;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.recipe.RecipeMap;

import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.maps.FuelBackend;
import gregtech.api.render.TextureFactory;

import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.generators.MTEGasTurbine;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GTPPMultiBlockBase;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import util.OriginManager;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlockAnyMeta;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlockUnlocalizedName;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static gregtech.api.GregTechAPI.METATILEENTITIES;
import static gregtech.api.enums.GTValues.V;

import static gregtech.api.enums.HatchElement.Dynamo;
import static gregtech.api.enums.HatchElement.InputHatch;

import static gregtech.api.enums.HatchElement.Muffler;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_OIL_CRACKER_GLOW;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import static gregtech.api.util.GTUtility.validMTEList;
import static net.minecraft.init.Blocks.iron_bars;
import static net.minecraft.util.StatCollector.translateToLocalFormatted;

public class Origin extends GTPPMultiBlockBase<Origin> implements ISurvivalConstructable {

    public Origin(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public Origin(String aName) {
        super(aName);
    }

    private long tFuel = 0;
    private long tEu = 0;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setLong("tFule",tFuel);
        aNBT.setLong("tEu",tEu);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        this.tFuel = aNBT.getLong("tFule");
        this.tEu = aNBT.getLong("tEu");
    }

    private ItemStack InputItemStack;
    private FluidStack InputFluidStack;

    // 机器运行逻辑
    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic().setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Nonnull
    @Override

    public CheckRecipeResult checkProcessing() {
        ItemStack slot = getControllerSlot();
        mMaxProgresstime = 0;
        int Voltage = 0;
        long lFuel;
        if (slot != null){
            MetaTileEntity tile = (MetaTileEntity) METATILEENTITIES[slot.getItemDamage()];
            if (tile instanceof  MTEBasicGenerator generator){
                GTRecipe recipe = getFluidRecipe(generator,getStoredFluids());
                if (recipe != null){
                    lFuel = (long) recipe.mSpecialValue * generator.getEfficiency();
                    Voltage = (int) V[generator.mTier];
                    FluidStack stack = recipe.getRepresentativeFluidInput(0);
                    if (stack == null){
                        ItemStack itemStack = recipe.getRepresentativeInput(0);
                        if (!GTUtility.isStackValid(itemStack)){
                           InputFluidStack = GTUtility.getFluidForFilledItem(itemStack, true);
                            if (InputFluidStack != null){
                                tFuel = lFuel;
                                mMaxProgresstime = 1;
                            }
                        }
                    }
                }else{
                    recipe = getItemRecipe(generator,getAllStoredInputs());
                    if (recipe != null){
                        lFuel = (long) recipe.mSpecialValue * generator.getEfficiency();
                        Voltage = (int) V[generator.mTier];
                        InputItemStack = recipe.getRepresentativeInput(0);
                        tFuel = lFuel;
                        mMaxProgresstime = 1;
                    }
                }
            }
        }
        if (mMaxProgresstime > 0) {
            lEUt = (long) Voltage * getMaxParallelRecipes();
            return CheckRecipeResultRegistry.GENERATING;
        }
        return CheckRecipeResultRegistry.NO_RECIPE;
    }

    public GTRecipe getFluidRecipe(MTEBasicGenerator generator,ArrayList<FluidStack> FluidStackList) {
            RecipeMap<?> tRecipes = generator.getRecipeMap();
            if (!(FluidStackList.isEmpty() || !(tRecipes.getBackend() instanceof FuelBackend tFuels))) {
                for (var fluidStack : FluidStackList){
                return tFuels.findFuel(fluidStack);
                }
            }

        return null;
    }

    public GTRecipe getItemRecipe(MTEBasicGenerator generator,ArrayList<ItemStack> ItemStacks) {
        RecipeMap<?> tRecipes = generator.getRecipeMap();
        if (!ItemStacks.isEmpty() || getRecipeMap() == null){
            for (var itemStack : ItemStacks){
                if (!(GTUtility.isStackInvalid(itemStack)) && generator.solidFuelOverride(itemStack)){
                    return getRecipeMap().findRecipeQuery()
                        .items(itemStack)
                        .find();
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Collection<RecipeMap<?>> getAvailableRecipeMaps() {
        return super.getAvailableRecipeMaps();
    }

    @Override
    public String getMachineType() {
        return "";
    }

    @Override
    public int getMaxParallelRecipes() {
        var itemStack = getControllerSlot();
        if (!GTUtility.isStackInvalid(itemStack)){
        return getControllerSlot().stackSize;
        }
        return  0;
    }

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static String[][] Shape =  new String[][] { { "   B   ", "  BBB  ", "  BBB  ", "BHBBBHB", "BBB~BBB" },
        { "  FFF  ", " FBBBF ", " FBCBF ", "HFBBBFH", "BDDDDDB" },
        { "  AFA  ", " AEEEA ", " AECEA ", "HFEEEFH", "BDDDDDB" },
        { "  AFA  ", " A G A ", " AG GA ", "HF G FH", "BDDDDDB" },
        { "  AFA  ", " A G A ", " AG GA ", "HF G FH", "BDDDDDB" },
        { "  AFA  ", " AEEEA ", " AECEA ", "HFEEEFH", "BDDDDDB" },
        { "  FFF  ", " FBBBF ", " FBCBF ", "HFBBBFH", "BDDDDDB" },
        { "  BBB  ", " BBCBB ", " BCCCB ", "BBCCCBB", "BBBBBBB" }};

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 3, 4, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 3, 4, 0, elementBudget, env, false, true);
    }

    private static final ITexture SOLID_STEEL_MACHINE_CASING = Textures.BlockIcons
        .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings2, 0));
    private static IStructureDefinition<Origin> STRUCTURE_DEFINITION = null;
    private int mBlockTier = 0;

    @Override
    public IStructureDefinition<Origin> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<Origin>builder()
                .addShape(
                    STRUCTURE_PIECE_MAIN, Shape)
                .addElement('A', ofBlockUnlocalizedName("IC2", "blockAlloyGlass", 0, true))
                //
                .addElement(
                    'B',
                    buildHatchAdder(Origin.class).atLeast(Dynamo, InputHatch, Muffler)
                        .casingIndex(Textures.BlockIcons.getTextureIndex(SOLID_STEEL_MACHINE_CASING))
                        .dot(1)
                        .buildAndChain(GregTechAPI.sBlockCasings2, 0))
                .addElement('C', ofBlock(GregTechAPI.sBlockCasings2, 13))
                .addElement('D', ofBlock(GregTechAPI.sBlockCasings4, 1))
                .addElement('E', ofBlock(GregTechAPI.sBlockCasings5, 0))
                .addElement('F', ofFrame(Materials.StainlessSteel))
                .addElement(
                    'G',
                    ofBlocksTiered(
                        Origin::getTierOfBlock,
                        ImmutableList.of(
                            Pair.of(BlockList.BronzePlatedReinforcedStone.getBlock(), 0), // 三硝基甲苯HV
                            Pair.of(BlockList.SteelPlatedReinforcedStone.getBlock(), 0), // PETN EV
                            Pair.of(BlockList.TitaniumPlatedReinforcedStone.getBlock(), 0), // 硝化甘油 IV
                            Pair.of(BlockList.TungstensteelPlatedReinforcedStone.getBlock(), 0), // 奥克托今 LUV
                            Pair.of(BlockList.NaquadahPlatedReinforcedStone.getBlock(), 0)// CL-20 ZPM
                        ),
                        0,
                        (m, t) -> m.mBlockTier = t,
                        m -> m.mBlockTier))
                .addElement('H', ofBlockAnyMeta(iron_bars))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    public static int getTierOfBlock(Block block, int meta) {
        if (block == BlockList.BronzePlatedReinforcedStone.getBlock()) {
            return 1;
        } else if (block == BlockList.SteelPlatedReinforcedStone.getBlock()) {
            return 2;
        } else if (block == BlockList.TitaniumPlatedReinforcedStone.getBlock()) {
            return 3;
        } else if (block == BlockList.TungstensteelPlatedReinforcedStone.getBlock()) {
            return 4;
        } else if (block == BlockList.NaquadahPlatedReinforcedStone.getBlock()) {
            return 5;
        }
        return 0;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType(translateToLocalFormatted("mte.ImplosionGenerator"))
            .addInfo(translateToLocalFormatted("mte.ImplosionGenerator.tooltips1"))
            .addInfo(translateToLocalFormatted("mte.ImplosionGenerator.tooltips2"))
            .addInfo(translateToLocalFormatted("mte.ImplosionGenerator.tooltips3"))
            .beginStructureBlock(7, 5, 8, true)
            .addController("Front bottom")
            .addInputHatch("Hint block with dot 1")
            .addDynamoHatch("Hint block with dot 1")
            .addMufflerHatch("Hint block with dot 1")
            .toolTipFinisher();

        return tt;
    }

    private int getFluidStackAmount(FluidStack fluidStack){
        ArrayList<FluidStack> fluidStacks = getStoredFluids();
        int amount = 0;
        for (var fluid : fluidStacks){
           if (GTUtility.areFluidsEqual(fluidStack,fluid)){
               amount += fluid.amount;
           }
        }
        return amount;
    }

    private int getItemStackAmount(ItemStack itemStack){
        ArrayList<ItemStack> itemStacks = getAllStoredInputs();
        int amount = 0;
        for (var item : itemStacks){
            if (GTUtility.areStacksEqual(itemStack,item)){
                amount += item.stackSize;
            }
        }
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (this.lEUt > 0) {

            int amount = (int) (lEUt / tFuel);
            if (!GTUtility.isStackValid(InputItemStack)){
                InputItemStack.stackSize = Math.min(amount,getItemStackAmount(InputItemStack));
                return depleteInput(InputItemStack) && addEnergyOutput(this.lEUt);
               // depleteInput()
            }else if (InputFluidStack != null){
                InputFluidStack.amount = Math.min(amount,getFluidStackAmount(InputFluidStack));
                return depleteInput(InputFluidStack) && addEnergyOutput(this.lEUt);
            }
            if (tEu > 0){
                if (mProgresstime > 0){
                    --mProgresstime;
                }
            }
            return true;
        }
        if (this.lEUt < 0) {
            if (!drainEnergyInput(getActualEnergyUsage())) {
                stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addEnergyOutput(long aEU) {
        if (aEU <= 0) {
            return true;
        }
        if (!this.mAllDynamoHatches.isEmpty()) {
            tEu -= aEU;
            long eu = addEnergyOutputMultipleDynamos(aEU, true,0);
            if (eu > 0) tEu += eu;
        }
        return null;
    }

    public long addEnergyOutputMultipleDynamos(long aEU, boolean aAllowMixedVoltageDynamos,int i) {
        int injected = 0;
        long aFirstVoltageFound = -1;
        for (MTEHatch aDynamo : validMTEList(mAllDynamoHatches)) {
            long aVoltage = aDynamo.maxEUOutput();
            // Check against voltage to check when hatch mixing
            if (aFirstVoltageFound == -1) {
                aFirstVoltageFound = aVoltage;
            }
        }

        long leftToInject;
        long aVoltage;
        int aAmpsToInject;
        int aRemainder;
        int ampsOnCurrentHatch;
        for (MTEHatch aDynamo : validMTEList(mAllDynamoHatches)) {
            leftToInject = aEU - injected;
            aVoltage = aDynamo.maxEUOutput();
            aAmpsToInject = (int) (leftToInject / aVoltage);
            aRemainder = (int) (leftToInject - (aAmpsToInject * aVoltage));
            ampsOnCurrentHatch = (int) Math.min(aDynamo.maxAmperesOut(), aAmpsToInject);

            // add full amps
            aDynamo.getBaseMetaTileEntity()
                .increaseStoredEnergyUnits(aVoltage * ampsOnCurrentHatch, false);
            injected += (int) (aVoltage * ampsOnCurrentHatch);

            // add reminder
            if (aRemainder > 0 && ampsOnCurrentHatch < aDynamo.maxAmperesOut()) {
                aDynamo.getBaseMetaTileEntity()
                    .increaseStoredEnergyUnits(aRemainder, false);
                injected += aRemainder;
            }
        }
        if (injected > 0){
            return aEU - injected;
        }else if (injected == aEU){
            return 0;
        }
        return aEU;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        if (checkPiece(STRUCTURE_PIECE_MAIN, 3, 4, 0) && mMufflerHatches.size() == 1) {
            fixAllIssues();
            return true;
        }
        return false;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 0;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity te, ForgeDirection side, ForgeDirection facing, int colorIndex,
                                 boolean active, boolean redstone) {

        if (side == facing) {
            if (active) return new ITexture[] { SOLID_STEEL_MACHINE_CASING, TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_OIL_CRACKER_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_OIL_CRACKER_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
            return new ITexture[] { SOLID_STEEL_MACHINE_CASING, TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_OIL_CRACKER)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_OIL_CRACKER_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] {
            Textures.BlockIcons.getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings2, 0)) };
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new Origin(this.mName);
    }
}
