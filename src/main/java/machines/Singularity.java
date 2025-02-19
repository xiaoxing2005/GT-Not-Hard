package machines;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.GTValues.debugDriller;
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
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.common.UndergroundOil.undergroundOil;
import static gregtech.common.UndergroundOil.undergroundOilReadInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import gregtech.api.util.ValidationType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.mofoga.gtnothard.MyMod;

import bwcrossmod.galacticgreg.VoidMinerUtility;
import galacticgreg.registry.GalacticGregRegistry;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.objects.XSTR;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.OverclockCalculator;
import gregtech.api.util.ParallelHelper;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtneioreplugin.plugin.block.ModBlocks;
import gtneioreplugin.plugin.item.ItemDimensionDisplay;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import org.jetbrains.annotations.NotNull;
import util.SingularityFluidRecipes;

public class Singularity extends MTEExtendedPowerMultiBlockBase<Singularity> implements ISurvivalConstructable {

    public Singularity(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    private String mLastDimensionOverride = "None";

    public Singularity(String aName) {
        super(aName);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setBoolean("mBlacklist", this.mBlacklist);
        aNBT.setString("mLastDimensionOverride", this.mLastDimensionOverride);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        this.mBlacklist = aNBT.getBoolean("mBlacklist");
        this.mLastDimensionOverride = aNBT.getString("mLastDimensionOverride");

    }

    private static final int mcasingIndex = Textures.BlockIcons.getTextureIndex(
        Textures.BlockIcons.getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings4, 2)));

    // 定义机器结构
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<Singularity> STRUCTURE_DEFINITION = StructureDefinition
        .<Singularity>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(new String[][] { { "hhh", "hhh", "hhh" }, { "h~h", "h h", "hhh" }, { "hhh", "hhh", "hhh" } }))
        .addElement(
            'h',
            buildHatchAdder(Singularity.class)
                .atLeast(OutputHatch, OutputBus, Maintenance)
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

    // 主机ToolTips
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

    // 创造自动搭建
    @Override
    public void construct(ItemStack aStack, boolean aHintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, aStack, aHintsOnly, 1, 1, 0);
    }

    // 生存自动搭建
    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) {
            return -1;
        }
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 1, 1, 0, elementBudget, env, false, true);
    }

    // 检查机器结构
    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasingAmount = 0;
        return checkPiece(STRUCTURE_PIECE_MAIN, 1, 1, 0) && mCasingAmount >= 14 && checkHatches();
    }

    // 检查仓室
    private boolean checkHatches() {
        return mMaintenanceHatches.size() == 1;
    }

    private List<IHatchElement<? super Singularity>> getAllowedHatches() {
        return ImmutableList.of(OutputHatch, OutputBus, Maintenance);
    }

    // 获取最大并行数
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
        return aStack != null;
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

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    private boolean mBlacklist = false;

    // 黑白名单配置
    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        this.mBlacklist = !this.mBlacklist;
        GTUtility.sendChatToPlayer(aPlayer, "Mode: " + (this.mBlacklist ? "Blacklist" : "Whitelist"));
    }

    private boolean VoidFluidMode = false;

    // 潜行左键切换多类型机器的类型
    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aPlayer.isSneaking() && getBaseMetaTileEntity().isServerSide()) {
            VoidFluidMode = !VoidFluidMode;
            if (VoidFluidMode) {
                GTUtility.sendChatToPlayer(aPlayer, "mode: VoidFluid");
            }
        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

    Map<Integer, FluidStack> FluidRecipes = SingularityFluidRecipes.VoidFliudRecipes.get(mLastDimensionOverride);

    //机器运行逻辑
    @Nonnull
    @Override
    public CheckRecipeResult checkProcessing() {
        mMaxProgresstime = 20;
        String dim = Optional.ofNullable(this.mInventory[1])
            .filter(s -> s.getItem() instanceof ItemDimensionDisplay)
            .map(ItemDimensionDisplay::getDimension)
            .orElse("None");
        if (VoidFluidMode){
            if (FluidRecipes != null){
                this.FluidOutPuts();
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }
        } else {
            if (!Objects.equals(dim, mLastDimensionOverride)) {
                mLastDimensionOverride = dim;
                totalWeight = 0;
            }
            if (this.dropMap == null || this.totalWeight == 0) {
                this.calculateDropMap();
            }
            if (this.totalWeight != 0.f) {
                for (int mLoop = 0; mLoop < 256; mLoop++){
                    this.handleOutputs();
                }
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }
        }
        this.stopMachine(ShutDownReasonRegistry.NONE);
        return CheckRecipeResultRegistry.NO_RECIPE;
    }

    //获取主方块显示内容
    private String getDimensionNameText() {
        String ext = null;
        try {
            Block block = ModBlocks.getBlock(mLastDimensionOverride);
            ext = new ItemStack(block).getDisplayName();
        } catch (Exception ignored) {}
        return "Dimension Override:" + (ext == null ? mLastDimensionOverride : ext);
    }

    //主方块中显示信息
    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);
        screenElements.widget(
            TextWidget.dynamicString(this::getDimensionNameText)
                .setSynced(true)
                .setTextAlignment(Alignment.CenterLeft)
                .setEnabled(true));
    }

    //获取主方块中维度方块的的维度名称
    private String getPluginDimensionName() {
        return Optional.ofNullable(this.mInventory[1])
            .filter(s -> s.getItem() instanceof ItemDimensionDisplay)
            .map(ItemDimensionDisplay::getDimension)
            .orElse("None");
    }

    private VoidMinerUtility.DropMap dropMap = null;
    private VoidMinerUtility.DropMap extraDropMap = null;
    private float totalWeight;

    // 下一次产出矿石
    private ItemStack nextOre() {
        float currentWeight = 0.f;
        while (true) {
            float randomNumber = XSTR.XSTR_INSTANCE.nextFloat() * this.totalWeight;
            for (Map.Entry<GTUtility.ItemId, Float> entry : this.dropMap.getInternalMap()
                .entrySet()) {
                currentWeight += entry.getValue();
                if (randomNumber < currentWeight) return entry.getKey()
                    .getItemStack();
            }
            for (Map.Entry<GTUtility.ItemId, Float> entry : this.extraDropMap.getInternalMap()
                .entrySet()) {
                currentWeight += entry.getValue();
                if (randomNumber < currentWeight) return entry.getKey()
                    .getItemStack();
            }
        }
    }

    //处理额外添加的矿石
    private void handleExtraDrops(int id) {
        id = MyMod.dimMapping.inverse()
            .getOrDefault(getPluginDimensionName(), id);
        if (VoidMinerUtility.extraDropsDimMap.containsKey(id)) {
            extraDropMap = VoidMinerUtility.extraDropsDimMap.get(id);
        }
    }

    private int dim;

    //获取指定维度的矿石列表
    private void handleModDimDef(int id) {
        id = dim = MyMod.dimMapping.inverse()
            .getOrDefault(getPluginDimensionName(), id);
        if (VoidMinerUtility.dropMapsByDimId.containsKey(id)) {
            this.dropMap = VoidMinerUtility.dropMapsByDimId.get(id);
        } else {
            String chunkProviderName = ((ChunkProviderServer) this.getBaseMetaTileEntity()
                .getWorld()
                .getChunkProvider()).currentChunkProvider.getClass()
                    .getName();
            chunkProviderName = MyMod.cahce.getOrDefault(dim, chunkProviderName);
            if (VoidMinerUtility.dropMapsByChunkProviderName.containsKey(chunkProviderName)) {
                this.dropMap = VoidMinerUtility.dropMapsByChunkProviderName.get(chunkProviderName);
            }
        }
    }

    //计算矿石归一化的权重
    private void calculateDropMap() {
        this.dropMap = new VoidMinerUtility.DropMap();
        this.extraDropMap = new VoidMinerUtility.DropMap();
        int id = this.getBaseMetaTileEntity()
            .getWorld().provider.dimensionId;
        this.handleModDimDef(id);
        this.handleExtraDrops(id);
        this.totalWeight = dropMap.getTotalWeight() + extraDropMap.getTotalWeight();
    }

    //虚空矿石输出
    private void handleOutputs() {
        final List<ItemStack> inputOres = this.getStoredInputs()
            .stream()
            .filter(GTUtility::isOre)
            .collect(Collectors.toList());
        final ItemStack output = this.nextOre();
        output.stackSize = getMaxParallel();
        if (inputOres.isEmpty() || this.mBlacklist && inputOres.stream()
            .noneMatch(is -> GTUtility.areStacksEqual(is, output))
            || !this.mBlacklist && inputOres.stream()
            .anyMatch(is -> GTUtility.areStacksEqual(is, output)))
            this.addOutput(output);
        this.updateSlots();
    }

    private FluidStack fluid = new FluidStack(FluidRegistry.WATER, 0) {
        public String getLocalizedName() {
            return "None";
        }
    };

    //虚空流体输出
    private void FluidOutPuts() {
        for (int i = 0; i < FluidRecipes.size(); i++){
            FluidStack recipeFluid = FluidRecipes.get(i);
            fluid = recipeFluid.copy();
            mOutputFluids = new FluidStack[] {fluid};
            final FluidStack output = fluid;
            output.amount = getMaxParallel();
        }
    }
}
