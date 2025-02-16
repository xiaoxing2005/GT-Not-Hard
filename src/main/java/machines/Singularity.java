package machines;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.widgets.LockedWhileActiveButton;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
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
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GTPPMultiBlockBase.GTPPHatchElement.TTDynamo;

public class Singularity extends MTEExtendedPowerMultiBlockBase<Singularity> implements ISurvivalConstructable {

    public Singularity(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public Singularity(String aName) {
        super(aName);
    }
    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    private static final int mcasingIndex = Textures.BlockIcons.getTextureIndex(
        Textures.BlockIcons.getCasingTextureForId(
            GTUtility.getCasingTextureIndex(
                GregTechAPI.sBlockCasings4, 2
            )
        )
    );

    // 定义机器结构
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<Singularity> STRUCTURE_DEFINITION = StructureDefinition.<Singularity>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(new String[][] {
                { "hhh", "hhh", "hhh" },
                { "h~h", "h-h", "hhh" },
                { "hhh", "hhh", "hhh" } }))
        .addElement(
            'h',
            buildHatchAdder(Singularity.class)
                .atLeast(InputHatch, OutputHatch, InputBus, OutputBus, Maintenance, Energy.or(ExoticEnergy))
                .casingIndex(mcasingIndex)
                .dot(1)
                .buildAndChain(onElementPass(Singularity::onCasingAdded, ofBlock(GregTechAPI.sBlockCasings4, 2))))
        .build();

    private int mCasingAmount;
    private void onCasingAdded() {
        mCasingAmount++;
    }

    @Override
    public IStructureDefinition<Singularity> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
                                 int colorIndex, boolean aActive, boolean redstoneLevel) {
        if (side == aFacing) {
            if (aActive) {
                return new ITexture[] { casingTexturePages[0][mcasingIndex], TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE)
                    .extFacing()
                    .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW)
                        .extFacing()
                        .glow()
                        .build() };
            }
            return new ITexture[] { casingTexturePages[0][mcasingIndex], TextureFactory.builder()
                .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_GLOW)
                    .extFacing()
                    .glow()
                    .build() };
        }
        return new ITexture[] { casingTexturePages[0][mcasingIndex] };
    }

    //主机ToolTips
    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Singularity")
            .addInfo("Runs supplied machines as if placed in the world")
            .addInfo("Parallel quantity = 2^x")
            .addInfo("x = Number of machines in the controller")
            .addInfo("If the machine within the controller contains multiple modes,")
            .addInfo("sneak left click controller to switch machine mode")
            .addSeparator()
            .beginStructureBlock(3, 3, 3, true)
            .addController("Front center")
            .addCasingInfoRange("Stable Titanium Machine Casing", 14, 24, false)
            .addEnergyHatch("Any casing", 1)
            .addMaintenanceHatch("Any casing", 1)
            .addInputBus("Any casing", 1)
            .addInputHatch("Any casing", 1)
            .addOutputHatch("Any casing", 1)
            .toolTipFinisher();
        return tt;
    }

    //创造自动搭建
    @Override
    public void construct(ItemStack aStack, boolean aHintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, aStack, aHintsOnly, 1, 1, 0);
    }

    //生存自动搭建
    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) {
            return -1;
        }
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 1, 1, 0, elementBudget, env, false, true);
    }

    //检查机器结构
    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasingAmount = 0;
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 1, 0) && mCasingAmount >= 14 && checkHatches();
    }

    //检查仓室
    private boolean checkHatches() {
        return mMaintenanceHatches.size() == 1;
    }

    private List<IHatchElement<? super Singularity>> getAllowedHatches() {
        return ImmutableList.of(InputHatch, OutputHatch, InputBus, OutputBus, Maintenance, Energy, ExoticEnergy);
    }

    //获取最大并行数
    private int getMaxParallel() {
        if (getControllerSlot() == null) {
            return 1;
        }
        if (getControllerSlot().stackSize < 31) {
            return (int) Math.pow(2, getControllerSlot().stackSize);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return aStack != null && aStack.getUnlocalizedName()
            .startsWith("gt.blockmachines.");
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

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new Singularity(this.mName);
    }

    private boolean mBlacklist = false;

    //黑白名单配置
    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        this.mBlacklist = !this.mBlacklist;
        GTUtility.sendChatToPlayer(aPlayer, "Mode: " + (this.mBlacklist ? "Blacklist" : "Whitelist"));
    }

    protected boolean mChunkLoadingEnabled = true;
    protected int workState;
    protected static final int STATE_DOWNWARD = 0, STATE_AT_BOTTOM = 1, STATE_UPWARD = 2, STATE_ABORT = 3;

    //控制器UI绘制
    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);
        final int BUTTON_Y_LEVEL = 91;

        builder.widget(
                new LockedWhileActiveButton(this.getBaseMetaTileEntity(), builder)
                    .setOnClick((clickData, widget) -> mChunkLoadingEnabled = !mChunkLoadingEnabled)
                    .setPlayClickSound(true)
                    .setBackground(() -> {
                        if (mChunkLoadingEnabled) {
                            return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                                GTUITextures.OVERLAY_BUTTON_CHUNK_LOADING };
                        }
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                            GTUITextures.OVERLAY_BUTTON_CHUNK_LOADING_OFF };
                    })
                    .attachSyncer(
                        new FakeSyncWidget.BooleanSyncer(
                            () -> mChunkLoadingEnabled,
                            newBoolean -> mChunkLoadingEnabled = newBoolean),
                        builder,
                        (widget, val) -> widget.notifyTooltipChange())
                    .dynamicTooltip(
                        () -> ImmutableList.of(
                            StatCollector.translateToLocal(
                                mChunkLoadingEnabled ? "GT5U.gui.button.chunk_loading_on"
                                    : "GT5U.gui.button.chunk_loading_off")))
                    .setTooltipShowUpDelay(TOOLTIP_DELAY)
                    .setPos(new Pos2d(80, BUTTON_Y_LEVEL))
                    .setSize(16, 16))
            .widget(
                new ButtonWidget().setOnClick((clickData, widget) -> abortDrilling())
                    .setPlayClickSound(true)
                    .setBackground(() -> {
                        if (workState == STATE_ABORT) {
                            return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                                GTUITextures.OVERLAY_BUTTON_RETRACT_PIPE, GTUITextures.OVERLAY_BUTTON_LOCKED };
                        }
                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                            GTUITextures.OVERLAY_BUTTON_RETRACT_PIPE };
                    })
                    .attachSyncer(
                        new FakeSyncWidget.IntegerSyncer(() -> workState, (newInt) -> workState = newInt),
                        builder,
                        (widget, integer) -> widget.notifyTooltipChange())
                    .dynamicTooltip(
                        () -> ImmutableList.of(
                            StatCollector.translateToLocalFormatted(
                                workState == STATE_ABORT ? "GT5U.gui.button.drill_retract_pipes_active"
                                    : "GT5U.gui.button.drill_retract_pipes")))
                    .setTooltipShowUpDelay(TOOLTIP_DELAY)
                    .setPos(new Pos2d(174, 112))
                    .setSize(16, 16));

        int left = 98;
        for (ButtonWidget button : getAdditionalButtons(builder, buildContext)) {
            button.setPos(new Pos2d(left, BUTTON_Y_LEVEL))
                .setSize(16, 16);
            builder.widget(button);
            left += 18;
        }
    }

    /** Called once when the abort button is clicked. Use to perform any needed cleanup (e.g. unloading chunks.) */
    protected void onAbort() {}

    /** Allows inheritors to supply custom shutdown failure messages. */
    private @NotNull String shutdownReason = "";
    
    protected void setShutdownReason(@NotNull String newReason) {
        shutdownReason = newReason;
    }

    protected void abortDrilling() {
        if (workState != STATE_ABORT) {
            workState = STATE_ABORT;
            onAbort();
            setShutdownReason("");

            if (!isAllowedToWork()) {
                enableWorking();
            }
        }
    }

    protected List<ButtonWidget> getAdditionalButtons(ModularWindow.Builder builder, UIBuildContext buildContext) {
        return ImmutableList.of();
    }
}
