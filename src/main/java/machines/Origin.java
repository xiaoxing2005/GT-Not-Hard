package machines;

import bartworks.API.recipe.BartWorksRecipeMaps;
import bartworks.util.Pair;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import goodgenerator.api.recipe.GoodGeneratorRecipeMaps;
import goodgenerator.items.GGMaterial;
import goodgenerator.util.CrackRecipeAdder;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.MaterialsUEVplus;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IWirelessEnergyHatchInformation;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.recipe.metadata.CompressionTierKey;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.ExoticEnergyInputHelper;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.OverclockCalculator;
import gregtech.common.blocks.ItemMachines;
import gtPlusPlus.api.recipe.GTPPRecipeMaps;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import util.OriginManager;
import util.TT_MultiMachineBase_EM;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static goodgenerator.main.GGConfigLoader.CoolantEfficiency;
import static goodgenerator.main.GGConfigLoader.ExcitedLiquidCoe;
import static gregtech.api.GregTechAPI.METATILEENTITIES;
import static gregtech.api.enums.GTValues.VN;
import static gregtech.api.enums.HatchElement.Dynamo;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_PROCESSING_ARRAY_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.casingTexturePages;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.metatileentity.implementations.MTEBasicMachine.isValidForLowGravity;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTUtility.validMTEList;
import static tectech.thing.metaTileEntity.multi.base.TTMultiblockBase.HatchElement.DynamoMulti;

public class Origin extends TT_MultiMachineBase_EM implements ISurvivalConstructable, IWirelessEnergyHatchInformation {

    protected IStructureDefinition<Origin> STRUCTURE_DEFINITION = null;
    private static final String STRUCTURE_PIECE_MAIN = "main";
    public IStructureDefinition<Origin> getStructure_EM() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<Origin>builder()
                .addShape(
                    STRUCTURE_PIECE_MAIN,
                    transpose(new String[][] {
                        { "hhh", "hhh", "hhh" },
                        { "h~h", "h-h", "hhh" },
                        { "hhh", "hhh", "hhh" } }))
                .addElement(
                    'h',
                    buildHatchAdder(Origin.class)
                        .atLeast(InputHatch, OutputHatch, InputBus, Maintenance, Dynamo.or(DynamoMulti))
                        .casingIndex(
                            Textures.BlockIcons.getTextureIndex(
                                Textures.BlockIcons.getCasingTextureForId(
                                    GTUtility.getCasingTextureIndex(
                                        GregTechAPI.sBlockCasings4,1
                                    )
                                )
                            )
                        )
                        .dot(1)
                        .buildAndChain(onElementPass(Origin::onCasingAdded, ofBlock(GregTechAPI.sBlockCasings4, 1))))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }


    private int mCasingAmount;
    private int mCasingIndex = Textures.BlockIcons.getTextureIndex(Textures.BlockIcons.getCasingTextureForId(
            GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4,1)));

    private RecipeMap<?> mLastRecipeMap;
    private ItemStack lastControllerStack;
    private int tTier = 0;
    private int mMult = 0;
    private int mode = 0;
    private boolean updateMode = false;
    private boolean isMultiBlock = false;

    protected long leftEnergy = 0;
    protected long trueOutput = 0;
    protected int trueEff = 0;
    protected FluidStack lockedFluid = null;
    protected int times = 1;
    protected int basicOutput;

    private static final List<Pair<FluidStack, Integer>> excitedLiquid;

    private static final List<Pair<FluidStack, Integer>> coolant;

    static {
        excitedLiquid = Arrays.asList(
            new Pair<>(MaterialsUEVplus.Space.getMolten(20L), ExcitedLiquidCoe[0]),
            new Pair<>(GGMaterial.atomicSeparationCatalyst.getMolten(20), ExcitedLiquidCoe[1]),
            new Pair<>(Materials.Naquadah.getMolten(20L), ExcitedLiquidCoe[2]),
            new Pair<>(Materials.Uranium235.getMolten(180L), ExcitedLiquidCoe[3]),
            new Pair<>(Materials.Caesium.getMolten(180L), ExcitedLiquidCoe[4]));
        coolant = Arrays.asList(
            new Pair<>(MaterialsUEVplus.Time.getMolten(20L), CoolantEfficiency[0]),
            new Pair<>(FluidRegistry.getFluidStack("cryotheum", 1000), CoolantEfficiency[1]),
            new Pair<>(Materials.SuperCoolant.getFluid(1000L), CoolantEfficiency[2]),
            new Pair<>(FluidRegistry.getFluidStack("ic2coolant", 1000), CoolantEfficiency[3]));
    }


    private void onCasingAdded() {
        mCasingAmount++;
    }

    public Origin(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public Origin(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new Origin(this.mName);
    }

    @Override
    public MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Origin")
            .addInfo("Runs supplied machines as if placed in the world")
            .addInfo("Parallel quantity = 2^x")
            .addInfo("x = Number of machines in the controller")
            .addInfo("If the machine within the controller contains multiple modes,")
            .addInfo("sneak left click controller to switch machine mode")
            .addSeparator()
            .beginStructureBlock(3, 3, 3, true)
            .addController("Front center")
            .addCasingInfoRange("Clean Stainless Steel Machine Casing", 14, 24, false)
            .addEnergyHatch("Any casing", 1)
            .addMaintenanceHatch("Any casing", 1)
            .addInputBus("Any casing", 1)
            .addInputHatch("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addOutputHatch("Any casing", 1)
            .toolTipFinisher();
        return tt;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
                                 int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            if (aActive) {
                return new ITexture[] { casingTexturePages[0][mCasingIndex], TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE)
                    .extFacing()
                    .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW)
                        .extFacing()
                        .glow()
                        .build() };
            }
            return new ITexture[] { casingTexturePages[0][mCasingIndex], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { casingTexturePages[0][mCasingIndex] };
    }

    // 读取通常大机器配方
    private RecipeMap<?> fetchRecipeMap() {
        if (isCorrectMachinePart(getControllerSlot())) {
            getControllerSlot().getItemDamage();
            RecipeMap<?> RecipeMap = getMultifunctionalRecipeMap(getControllerSlot().getItemDamage());
            if (RecipeMap != null) {
                isMultiBlock = true;
                return RecipeMap;
            }
            MetaTileEntity e = (MetaTileEntity) METATILEENTITIES[getControllerSlot().getItemDamage()];
            if (e instanceof MTEMultiBlockBase mteMultiBlockBase) {
                isMultiBlock = true;
                RecipeMap = mteMultiBlockBase.getRecipeMap();
                return RecipeMap;// = RecipeMaps.assemblylineVisualRecipes;// ? AssemblyLineWithoutResearchRecipe : RecipeMap;
            }
            isMultiBlock = false;
            return OriginManager.giveRecipeMap(OriginManager.getMachineName(getControllerSlot()));
        }
        return null;
    }

    // 多类型机器配方列表
    //磁通量效应监视器-358
    private static final String[] Magnetic_Flux_Exhibitor_mod = {"Polarizer", "Electromagnetic Separator"};
    private static final RecipeMap<?>[] Magnetic_Flux_Exhibitor = {RecipeMaps.polarizerRecipes, RecipeMaps.electroMagneticSeparatorRecipes};
    //涡轮装罐机Pro-360
    private static final String[] TurboCan_Pro_mod = {"Fluid Canner", "Canner"};
    private static final RecipeMap<?>[] TurboCan_Pro = {RecipeMaps.fluidCannerRecipes, RecipeMaps.cannerRecipes};
    //工业辊压机-792
    private static final String[] Industrial_Material_Press_mod = {"Forming Press", "Bending Machine"};
    private static final RecipeMap<?>[] Industrial_Material_Press = {RecipeMaps.formingPressRecipes, RecipeMaps.benderRecipes};
    //工业洗矿厂-850
    private static final String[] Ore_Washing_Plant_mod = {"Ore Washer", "Simple Washer", "Chemical Bath"};
    private static final RecipeMap<?>[] Ore_Washing_Plant = {RecipeMaps.oreWasherRecipes, GTPPRecipeMaps.simpleWasherRecipes, RecipeMaps.chemicalBathRecipes};
    //工业电弧炉-862
    private static final String[] High_Current_Industrial_Arc_Furnace_mod = {"Electric Arc Furnace", "Plasma Arc Furnace"};
    private static final RecipeMap<?>[] High_Current_Industrial_Arc_Furnace = {RecipeMaps.arcFurnaceRecipes, RecipeMaps.plasmaArcFurnaceRecipes};
    //亚马逊仓库-942
    private static final String[] Amazon_Warehousing_Depot_mod = {"Packager", "Unpackeager"};
    private static final RecipeMap<?>[] Amazon_Warehousing_Depot = {RecipeMaps.packagerRecipes, RecipeMaps.unpackagerRecipes};
    //工业切割机-992
    private static final String[] Industrial_Cutting_Factory_mod = {"Cutting", "Slicing"};
    private static final RecipeMap<?>[] Industrial_Cutting_Factory = {RecipeMaps.cutterRecipes, RecipeMaps.slicerRecipes};
    //真空干燥炉-995
    private static final String[] Utupu_Tanuri_mod = {"Dehydrator", "Vacuum Furnace"};
    private static final RecipeMap<?>[] Utupu_Tanuri = {GTPPRecipeMaps.chemicalDehydratorNonCellRecipes, GTPPRecipeMaps.vacuumFurnaceRecipes};
    //黑洞压缩机-3008
    private static final String[] Pseudostable_Black_Hole_Containment_Field_mod = {"Compressor", "Advanced Neutronium Compressor"};
    private static final RecipeMap<?>[] Pseudostable_Black_Hole_Containment_Field = {RecipeMaps.compressorRecipes, RecipeMaps.neutroniumCompressorRecipes};
    //电路装配线-12735
    private static final String[] Circuit_Assembly_Line_mod = {"Circuit Assembly Line", "Circuit Assembly"};
    private static final RecipeMap<?>[] Circuit_Assembly_Line = {BartWorksRecipeMaps.circuitAssemblyLineRecipes, RecipeMaps.circuitAssemblerRecipes};
    //丹格特蒸馏厂-31021
    private static final String[] Dangote_Distillus_mod = {"Distillery", "distillation tower"};
    private static final RecipeMap<?>[] Dangote_Distillus = {RecipeMaps.distilleryRecipes, RecipeMaps.distillationTowerRecipes};
    //精密自动组装机MT-3662-32018
    private static final String[] Precise_Auto_Assembler_MT_3662_mod = {"Precise Assembler", "Assembler"};
    private static final RecipeMap<?>[] Precise_Auto_Assembler_MT_3662 = {
        GoodGeneratorRecipeMaps.preciseAssemblerRecipes, RecipeMaps.assemblerRecipes };


    // 潜行左键切换多类型机器的类型
    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aPlayer.isSneaking() && getBaseMetaTileEntity().isServerSide()) {
            this.mode = (this.mode + 1) % 3;
            updateMode = true;
            switch (getControllerSlot().getItemDamage()) {
                case 358 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Magnetic_Flux_Exhibitor_mod[Math.min(mode,1)]);
                }
                case 360 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + TurboCan_Pro_mod[Math.min(mode,1)]);
                }
                case 792 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Industrial_Material_Press_mod[Math.min(mode,1)]);
                }
                case 850 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Ore_Washing_Plant_mod[Math.min(mode,2)]);
                }
                case 862 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + High_Current_Industrial_Arc_Furnace_mod[Math.min(mode,1)]);
                }
                case 942 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Amazon_Warehousing_Depot_mod[Math.min(mode,1)]);
                }
                case 992 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Industrial_Cutting_Factory_mod[Math.min(mode,1)]);
                }
                case 995 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Utupu_Tanuri_mod[Math.min(mode,1)]);
                }
                case 3008 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Pseudostable_Black_Hole_Containment_Field_mod[Math.min(mode,1)]);
                }
                case 12735 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Circuit_Assembly_Line_mod[Math.min(mode,1)]);
                }
                case 31021 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Dangote_Distillus_mod[Math.min(mode,1)]);
                }
                case 32018 -> {
                    GTUtility.sendChatToPlayer(aPlayer, "mode:" + Precise_Auto_Assembler_MT_3662_mod[Math.min(mode,1)]);
                }
                default -> {
                    break;
                }
            }
        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

    // 读取多类型机器的配方
    private RecipeMap<?> getMultifunctionalRecipeMap(int meta) {
        switch (meta) {
            case 358 -> {
                return Magnetic_Flux_Exhibitor[Math.min(mode,1)];
            }
            case 360 -> {
                return TurboCan_Pro[Math.min(mode,1)];
            }
            case 792 -> {
                return Industrial_Material_Press[Math.min(mode,1)];
            }
            case 850 -> {
                return Ore_Washing_Plant[Math.min(mode,2)];
            }
            case 862 -> {
                return High_Current_Industrial_Arc_Furnace[Math.min(mode,1)];
            }
            case 942 -> {
                return Amazon_Warehousing_Depot[Math.min(mode,1)];
            }
            case 992 -> {
                return Industrial_Cutting_Factory[Math.min(mode, 1)];
            }
            case 995 -> {
                return Utupu_Tanuri[Math.min(mode,1)];
            }
            case 3008 -> {
                return Pseudostable_Black_Hole_Containment_Field[Math.min(mode,1)];
            }
            case 12735 -> {
                return Circuit_Assembly_Line[Math.min(mode,1)];
            }
            case 31021 -> {
                return Dangote_Distillus[Math.min(mode, 1)];
            }
            case 32018 -> {
                return Precise_Auto_Assembler_MT_3662[Math.min(mode, 1)];
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return mLastRecipeMap;
    }

    @Override
    protected boolean filtersFluid() {
        return false;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return aStack != null && aStack.getUnlocalizedName()
            .startsWith("gt.blockmachines.");
    }

    @Override
    protected void sendStartMultiBlockSoundLoop() {
        SoundResource sound = OriginManager.getSoundResource(OriginManager.getMachineName(getControllerSlot()));
        if (sound != null) {
            sendLoopStart((byte) sound.id);
        }
    }

    @Override
    public void startSoundLoop(byte aIndex, double aX, double aY, double aZ) {
        super.startSoundLoop(aIndex, aX, aY, aZ);
        SoundResource sound = SoundResource.get(aIndex < 0 ? aIndex + 256 : 0);
        if (sound != null) {
            GTUtility.doSoundAtClient(sound, getTimeBetweenProcessSounds(), 1.0F, aX, aY, aZ);
        }
    }

    @Override
    public @NotNull CheckRecipeResult checkProcessing_EM() {

        ArrayList<FluidStack> tFluids = getStoredFluids();
        for (int i = 0; i < tFluids.size() - 1; i++) {
            for (int j = i + 1; j < tFluids.size(); j++) {
                if (GTUtility.areFluidsEqual(tFluids.get(i), tFluids.get(j))) {
                    if ((tFluids.get(i)).amount >= (tFluids.get(j)).amount) {
                        tFluids.remove(j--);
                    } else {
                        tFluids.remove(i--);
                        break;
                    }
                }
            }
        }

        GTRecipe tRecipe = GoodGeneratorRecipeMaps.naquadahReactorFuels.findRecipeQuery()
            .fluids(tFluids.toArray(new FluidStack[0]))
            .find();
        if (tRecipe != null) {
            Pair<FluidStack, Integer> excitedInfo = getExcited(tFluids.toArray(new FluidStack[0]), false);
            int pall = excitedInfo == null ? 1 : excitedInfo.getValue();
            if (consumeFuel(
                CrackRecipeAdder.copyFluidWithAmount(tRecipe.mFluidInputs[0], pall),
                tFluids.toArray(new FluidStack[0]))) {
                mOutputFluids = new FluidStack[] {
                    CrackRecipeAdder.copyFluidWithAmount(tRecipe.mFluidOutputs[0], pall) };
                basicOutput = tRecipe.mSpecialValue;
                times = pall;
                lockedFluid = excitedInfo == null ? null : excitedInfo.getKey();
                mMaxProgresstime = tRecipe.mDuration;
                return CheckRecipeResultRegistry.GENERATING;
            }
        }

        return CheckRecipeResultRegistry.NO_FUEL_FOUND;
    }

    public boolean consumeFuel(FluidStack target, FluidStack[] input) {
        if (target == null) return false;
        for (FluidStack inFluid : input) {
            if (inFluid != null && inFluid.isFluidEqual(target) && inFluid.amount >= target.amount) {
                inFluid.amount -= target.amount;
                return true;
            }
        }
        return false;
    }

    public Pair<FluidStack, Integer> getExcited(FluidStack[] input, boolean isConsume) {
        for (Pair<FluidStack, Integer> fluidPair : excitedLiquid) {
            FluidStack tFluid = fluidPair.getKey();
            for (FluidStack inFluid : input) {
                if (inFluid != null && inFluid.isFluidEqual(tFluid) && inFluid.amount >= tFluid.amount) {
                    if (isConsume) inFluid.amount -= tFluid.amount;
                    return fluidPair;
                }
            }
        }
        return null;
    }


    // 机器运行逻辑
    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Nonnull
            @Override
            protected CheckRecipeResult validateRecipe(@Nonnull GTRecipe recipe) {
                if (recipe.getMetadataOrDefault(CompressionTierKey.INSTANCE, 0) > 0) {
                    return CheckRecipeResultRegistry.NO_RECIPE;
                }
                if (GTMod.gregtechproxy.mLowGravProcessing && recipe.mSpecialValue == -100
                    && !isValidForLowGravity(recipe, getBaseMetaTileEntity().getWorld().provider.dimensionId)) {
                    return SimpleCheckRecipeResult.ofFailure("high_gravity");
                }
                if (recipe.mEUt > availableVoltage) {
                    return CheckRecipeResultRegistry.insufficientPower(recipe.mEUt);
                }
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }

            // 高炉线圈炉温设定逻辑
            @NotNull
            @Override
            protected OverclockCalculator createOverclockCalculator(@Nonnull GTRecipe recipe) {
                return super.createOverclockCalculator(recipe).setRecipeHeat(recipe.mSpecialValue)
                    .setMachineHeat(999999)
                    .setHeatOC(false)
                    .setHeatDiscount(false);
            }

        }.setMaxParallelSupplier(this::getMaxParallel)
            .setEuModifier(0.00001F);
    }

    @Override
    protected boolean canUseControllerSlotForRecipe() {
        return false;
    }

    // 判断小机器等级或大机器电压等级
    @Override
    protected void setProcessingLogicPower(ProcessingLogic logic) {
        logic.setAvailableVoltage(
            (isMultiBlock ? getAverageInputVoltage() : GTValues.V[tTier])
                * (mLastRecipeMap != null ? mLastRecipeMap.getAmperage() : 1));
        logic.setAvailableAmperage(getMaxParallel());
        logic.setAmperageOC(false);
    }

    /*
    // 运行时电压等级与无损降频
    private void setTierAndMult() {
        IMetaTileEntity aMachine = ItemMachines.getMetaTileEntity(getControllerSlot());
        if (aMachine instanceof MTETieredMachineBlock) {
            tTier = ((MTETieredMachineBlock) aMachine).mTier;
        } else {
            tTier = 0;
        }
        mMult = 0;
        if (downtierUEV && tTier > 9) {
            // Lowers down the tier by 1 to allow for bigger parallel
            tTier--;
            // Multiplies Parallels by 4x, keeping the energy cost
            mMult = 2;
        }
    }

     */

    // 并行数目
    private int getMaxParallel() {
        if (getControllerSlot() == null) {
            return 1;
        }
        // return getControllerSlot().stackSize << mMult;
        if (getControllerSlot().stackSize < 31) {
            return (int) Math.pow(2, getControllerSlot().stackSize);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (mMachine && aTick % 20 == 0) {
            for (MTEHatchInputBus tInputBus : mInputBusses) {
                tInputBus.mRecipeMap = mLastRecipeMap;
            }
            for (MTEHatchInput tInputHatch : mInputHatches) {
                tInputHatch.mRecipeMap = mLastRecipeMap;
            }
        }
    }

    @Override
    public void construct(ItemStack aStack, boolean aHintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, aStack, aHintsOnly, 1, 1, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) {
            return -1;
        }
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 1, 1, 0, elementBudget, env, false, true);
    }

    private boolean checkHatches() {
        return mMaintenanceHatches.size() == 1;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        this.times = aNBT.getInteger("mTimes");
        this.leftEnergy = aNBT.getLong("mLeftEnergy");
        this.basicOutput = aNBT.getInteger("mbasicOutput");
        if (FluidRegistry.getFluid(aNBT.getString("mLockedFluidName")) != null) this.lockedFluid = new FluidStack(
            FluidRegistry.getFluid(aNBT.getString("mLockedFluidName")),
            aNBT.getInteger("mLockedFluidAmount"));
        else this.lockedFluid = null;
        super.loadNBTData(aNBT);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        aNBT.setInteger("mTimes", this.times);
        aNBT.setLong("mLeftEnergy", this.leftEnergy);
        aNBT.setInteger("mbasicOutput", this.basicOutput);
        if (lockedFluid != null) {
            aNBT.setString(
                "mLockedFluidName",
                this.lockedFluid.getFluid()
                    .getName());
            aNBT.setInteger("mLockedFluidAmount", this.lockedFluid.amount);
        }
        super.saveNBTData(aNBT);
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    private List<IHatchElement<? super Origin>> getAllowedHatches() {
        return ImmutableList.of(InputHatch, OutputHatch, InputBus, Maintenance, Dynamo, DynamoMulti);
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasingAmount = 0;
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 1, 0) && mCasingAmount >= 14 && checkHatches();
    }

    @Override
    public String[] getInfoData() {
        long storedEnergy = 0;
        long maxEnergy = 0;
        for (MTEHatch tHatch : validMTEList(mExoticEnergyHatches)) {
            storedEnergy += tHatch.getBaseMetaTileEntity()
                .getStoredEU();
            maxEnergy += tHatch.getBaseMetaTileEntity()
                .getEUCapacity();
        }
        return new String[] {
            StatCollector.translateToLocal("GT5U.multiblock.Progress") + ": "
                + EnumChatFormatting.GREEN
                + GTUtility.formatNumbers(mProgresstime / 20)
                + EnumChatFormatting.RESET
                + " s / "
                + EnumChatFormatting.YELLOW
                + GTUtility.formatNumbers(mMaxProgresstime / 20)
                + EnumChatFormatting.RESET
                + " s",
            StatCollector.translateToLocal("GT5U.multiblock.energy") + ": "
                + EnumChatFormatting.GREEN
                + GTUtility.formatNumbers(storedEnergy)
                + EnumChatFormatting.RESET
                + " EU / "
                + EnumChatFormatting.YELLOW
                + GTUtility.formatNumbers(maxEnergy)
                + EnumChatFormatting.RESET
                + " EU",
            StatCollector.translateToLocal("GT5U.multiblock.usage") + ": "
                + EnumChatFormatting.RED
                + GTUtility.formatNumbers(-lEUt)
                + EnumChatFormatting.RESET
                + " EU/t",
            StatCollector.translateToLocal("GT5U.multiblock.mei") + ": "
                + EnumChatFormatting.YELLOW
                + GTUtility
                .formatNumbers(ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList()))
                + EnumChatFormatting.RESET
                + " EU/t(*"
                + GTUtility
                .formatNumbers(ExoticEnergyInputHelper.getMaxInputAmpsMulti(getExoticAndNormalEnergyHatchList()))
                + "A) "
                + StatCollector.translateToLocal("GT5U.machines.tier")
                + ": "
                + EnumChatFormatting.YELLOW
                + VN[GTUtility
                .getTier(ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList()))]
                + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.multiblock.problems") + ": "
                + EnumChatFormatting.RED
                + (getIdealStatus() - getRepairStatus())
                + EnumChatFormatting.RESET
                + " "
                + StatCollector.translateToLocal("GT5U.multiblock.efficiency")
                + ": "
                + EnumChatFormatting.YELLOW
                + mEfficiency / 100.0F
                + EnumChatFormatting.RESET
                + " %",
            StatCollector.translateToLocal("GT5U.Origin.machinetier") + ": "
                + EnumChatFormatting.GREEN
                + tTier
                + EnumChatFormatting.RESET
                + " "
                + StatCollector.translateToLocal("GT5U.Origin.discount")
                + ": "
                + EnumChatFormatting.GREEN
                + 1
                + EnumChatFormatting.RESET
                + " x",
            StatCollector.translateToLocal("GT5U.Origin.parallel") + ": "
                + EnumChatFormatting.GREEN
                + GTUtility.formatNumbers(getMaxParallel())
                + EnumChatFormatting.RESET };
    }

    @Override
    public boolean supportsInputSeparation() {
        return true;
    }

    @Override
    public boolean supportsBatchMode() {
        return true;
    }

    @Override
    public boolean supportsSingleRecipeLocking() {
        return true;
    }

    @Override
    public boolean supportsVoidProtection() {
        return true;
    }

    @Override
    protected boolean supportsSlotAutomation(int aSlot) {
        return aSlot == getControllerSlotIndex();
    }

    /*
    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
                    downtierUEV = !downtierUEV;
                    setTierAndMult();
                })
                .setPlayClickSound(true)
                .setBackground(() -> {
                    if (downtierUEV) {
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                            GTUITextures.OVERLAY_BUTTON_DOWN_TIERING_ON };
                    } else {
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                            GTUITextures.OVERLAY_BUTTON_DOWN_TIERING_OFF };
                    }
                })
                .setPos(80, 91)
                .setSize(16, 16)
                .addTooltip(StatCollector.translateToLocal("GT5U.gui.button.down_tier"))
                .setTooltipShowUpDelay(TOOLTIP_DELAY))
            .widget(new FakeSyncWidget.BooleanSyncer(() -> downtierUEV, val -> downtierUEV = val));
    }

     */

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
                                int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        if (mLastRecipeMap != null && getControllerSlot() != null) {
            tag.setString("type", getControllerSlot().getDisplayName());
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
                             IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
        final NBTTagCompound tag = accessor.getNBTData();
        if (tag.hasKey("type")) {
            currentTip.add("Machine: " + EnumChatFormatting.YELLOW + tag.getString("type"));
        } else {
            currentTip.add("Machine: " + EnumChatFormatting.YELLOW + "None");
        }
    }

}
