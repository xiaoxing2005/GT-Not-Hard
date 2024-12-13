package machines;

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
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.SoundResource;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.*;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.recipe.metadata.CompressionTierKey;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.ExoticEnergyInputHelper;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.blocks.ItemMachines;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import util.ChaosManager;

import javax.annotation.Nonnull;

import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.GTValues.VN;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.metatileentity.implementations.MTEBasicMachine.isValidForLowGravity;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTUtility.validMTEList;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class Chaos extends MTEExtendedPowerMultiBlockBase<Chaos> implements ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<Chaos> STRUCTURE_DEFINITION = StructureDefinition
        .<Chaos>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(new String[][] {{ "hhh", "hhh", "hhh" }, { "h~h", "h-h", "hhh" }, { "hhh", "hhh", "hhh" }}))
        .addElement(
            'h',
            buildHatchAdder(Chaos.class)
                .atLeast(InputHatch, OutputHatch, InputBus, OutputBus, Maintenance, Energy)
                .casingIndex(48)
                .dot(1)
                .buildAndChain(
                    onElementPass(Chaos::onCasingAdded,ofBlock(GregTechAPI.sBlockCasings4,0))
                )
        )
        .build();

    private int mCasingAmount;

    private RecipeMap<?> mLastRecipeMap;
    private ItemStack lastControllerStack;
    private int tTier = 0;
    private int mMult = 0;
    private boolean downtierUEV = true;

    private void onCasingAdded() {
        mCasingAmount++;
    }

    public Chaos(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public Chaos(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new Chaos(this.mName);
    }

    @Override
    public MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Chaos")
            .addInfo("Runs supplied machines as if placed in the world")
            .addSeparator()
            .beginStructureBlock(3, 3, 3, true)
            .addController("Front center")
            .addCasingInfoRange("Robust Tungstensteel Machine Casing", 14, 24, false)
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
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing, int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            if (aActive) {
                return new ITexture[] {casingTexturePages[0][48],
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW)
                        .extFacing()
                        .glow()
                        .build()
                };
            }
            return new ITexture[] {casingTexturePages[0][48],
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_GLOW)
                    .extFacing()
                    .glow()
                    .build()
            };
        }
    return new ITexture[] {casingTexturePages[0][48]};
    }

    private RecipeMap<?> fetchRecipeMap() {
        if (isCorrectMachinePart(getControllerSlot())){
            return ChaosManager.giveRecipeMap(ChaosManager.getMachineName(getControllerSlot()));
        }
        return null;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return mLastRecipeMap;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return aStack != null && aStack.getUnlocalizedName().startsWith("gt.blockmachines.");
    }

    @Override
    protected void sendStartMultiBlockSoundLoop() {
        SoundResource sound = ChaosManager.getSoundResource(ChaosManager.getMachineName(getControllerSlot()));
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
    @NotNull
    public CheckRecipeResult checkProcessing() {
        if (!GTUtility.areStacksEqual(lastControllerStack, getControllerSlot())) {
            // controller slot has changed
            lastControllerStack = getControllerSlot();
            mLastRecipeMap = fetchRecipeMap();
            setTierAndMult();
        }
        if (mLastRecipeMap == null) {
            return SimpleCheckRecipeResult.ofFailure("no_machine");
        }
        if (mLockedToSingleRecipe && mSingleRecipeCheck != null) {
            if (mSingleRecipeCheck.getRecipeMap() != mLastRecipeMap){
                return SimpleCheckRecipeResult.ofFailure("machine_mismatch");
            }
        }
        return super.checkProcessing();
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {
            @Nonnull
            @Override
            protected CheckRecipeResult validateRecipe(@Nonnull GTRecipe recipe) {
                if (recipe.getMetadataOrDefault(CompressionTierKey.INSTANCE, 0) > 0) {
                    return CheckRecipeResultRegistry.NO_RECIPE;
                }
                if (GTMod.gregtechproxy.mLowGravProcessing && recipe.mSpecialValue == -100 && !isValidForLowGravity(recipe, getBaseMetaTileEntity().getWorld().provider.dimensionId)) {
                    return SimpleCheckRecipeResult.ofFailure("high_gravity");
                }
                if (recipe.mEUt > availableVoltage) {
                    return CheckRecipeResultRegistry.insufficientPower(recipe.mEUt);
                }
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }
        }.setMaxParallelSupplier(this::getMaxParallel);
    }

    @Override
    protected boolean canUseControllerSlotForRecipe() {
        return false;
    }

    @Override
    protected void setProcessingLogicPower(ProcessingLogic logic) {
        logic.setAvailableVoltage(GTValues.V[tTier] * (mLastRecipeMap != null ? mLastRecipeMap.getAmperage() : 1));
        logic.setAvailableAmperage(getMaxParallel());
        logic.setAmperageOC(false);
    }

    private void setTierAndMult() {
        IMetaTileEntity aMachine = ItemMachines.getMetaTileEntity(getControllerSlot());
        if (aMachine instanceof MTETieredMachineBlock) {
            tTier = ((MTETieredMachineBlock) aMachine).mTier;
        }
        else {
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

    private int getMaxParallel() {
        if (getControllerSlot() == null) {
            return 0;
        }
        return getControllerSlot().stackSize << mMult;
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
    public IStructureDefinition<Chaos> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
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
        super.saveNBTData(aNBT);
        aNBT.setBoolean("downtierUEV", downtierUEV);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        if (aNBT.hasKey("mSeparate")) {
            // backward compatibility
            inputSeparation = aNBT.getBoolean("mSeparate");
        }
        if (aNBT.hasKey("mUseMultiparallelMode")) {
            // backward compatibility
            batchMode = aNBT.getBoolean("mUseMultiparallelMode");
        }
        downtierUEV = aNBT.getBoolean("downtierUEV");
    }

    @Override
    public final void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (aPlayer.isSneaking()) {
            // Lock to single recipe
            super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ);
        }
        else {
            inputSeparation = !inputSeparation;
            GTUtility.sendChatToPlayer(aPlayer, StatCollector.translateToLocal("GT5U.machines.separatebus") + " " + inputSeparation);
        }
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (aPlayer.isSneaking()) {
            batchMode = !batchMode;
            if (batchMode) {
                GTUtility.sendChatToPlayer(aPlayer, "Batch recipes");
            }
            else {
                GTUtility.sendChatToPlayer(aPlayer, "Don't batch recipes");
            }
        }
        else {
            downtierUEV = !downtierUEV;
            GTUtility.sendChatToPlayer(aPlayer, "Treat UEV+ machines as multiple UHV " + downtierUEV);
        }
        return true;
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

    private List<IHatchElement<? super Chaos>> getAllowedHatches() {
        return ImmutableList.of(InputHatch, OutputHatch, InputBus, OutputBus, Maintenance, Energy, ExoticEnergy);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mExoticEnergyHatches.clear();
        mCasingAmount = 0;
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 1, 0) && mCasingAmount >= 14 && checkHatches();
    }

    @Override
    public String[] getInfoData() {
        long storedEnergy = 0;
        long maxEnergy = 0;
        for (MTEHatch tHatch : validMTEList(mExoticEnergyHatches)) {
            storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
            maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
        }
        return new String[]{
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
            StatCollector.translateToLocal("GT5U.Chaos.machinetier") + ": "
                + EnumChatFormatting.GREEN
                + tTier
                + EnumChatFormatting.RESET
                + " "
                + StatCollector.translateToLocal("GT5U.Chaos.discount")
                + ": "
                + EnumChatFormatting.GREEN
                + 1
                + EnumChatFormatting.RESET
                + " x",
            StatCollector.translateToLocal("GT5U.Chaos.parallel") + ": "
                + EnumChatFormatting.GREEN
                + GTUtility.formatNumbers(getMaxParallel())
                + EnumChatFormatting.RESET};
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

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y, int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        if (mLastRecipeMap != null && getControllerSlot() != null) {
            tag.setString("type", getControllerSlot().getDisplayName());
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
        final NBTTagCompound tag = accessor.getNBTData();
        if (tag.hasKey("type")) {
            currentTip.add("Machine: " + EnumChatFormatting.YELLOW + tag.getString("type"));
        } else {
            currentTip.add("Machine: " + EnumChatFormatting.YELLOW + "None");
        }
    }
}
