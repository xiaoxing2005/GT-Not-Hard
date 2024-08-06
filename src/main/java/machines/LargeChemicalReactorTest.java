package machines;

import com.github.bartimaeusnek.bartworks.common.tileentities.tiered.GT_MetaTileEntity_HumongousInputHatch;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.*;
import com.gtnewhorizon.structurelib.util.ItemStackPredicate;
import gregtech.api.GregTech_API;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.interfaces.IHeatingCoil;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_ExtendedPowerMultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.swing.plaf.PanelUI;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;

public class LargeChemicalReactorTest extends GT_MetaTileEntity_EnhancedMultiBlockBase<LargeChemicalReactorTest> implements ISurvivalConstructable {
    private static final int CASING_INDEX = 176;
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<LargeChemicalReactorTest> STRUCTURE_DEFINITION = StructureDefinition
        .<LargeChemicalReactorTest>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(new String[][]{{"ccc","cxc","ccc"},{ "c~c", "xPx", "cxc" }, { "ccc", "cxc", "ccc" },})
        )
        .addElement('P',ofBlock(GregTech_API.sBlockCasings8,1))
        .addElement(
            'c',
            buildHatchAdder(LargeChemicalReactorTest.class)
                .atLeast(InputHatch,OutputHatch, InputBus, OutputBus, Maintenance, Energy)
                .casingIndex(CASING_INDEX)
                .dot(1)
                .buildAndChain(
                    onElementPass(
                        LargeChemicalReactorTest::onCasingAdded,
                        ofBlock(GregTech_API.sBlockCasings8,0)
                    )
                )
        )
        .addElement(
            'x',
            buildHatchAdder(LargeChemicalReactorTest.class)
                .atLeast(InputHatch,OutputHatch,InputBus,OutputBus,Maintenance,Energy)
                .casingIndex(CASING_INDEX)
                .dot(1)
                .buildAndChain(
                    CoilStructureElement.INSTANCE,
                    onElementPass(
                        LargeChemicalReactorTest::onCasingAdded,
                        ofBlock(GregTech_API.sBlockCasings8,0)
                    )
                )
        )
        .build();

    private int mCasingAmount;
    private int mCoilAmount;

    public LargeChemicalReactorTest(int aID, String aName, String aNameRegional){
        super(aID,aName,aNameRegional);
    }

    public LargeChemicalReactorTest(String aName){
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity){
        return new LargeChemicalReactorTest(this.mName);
    }

    @Override
    public GT_Multiblock_Tooltip_Builder createTooltip(){
        final GT_Multiblock_Tooltip_Builder tt =new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Chemical Reactor")
            .addInfo("LargeChemicalReactorTest")
            .addSeparator()
            .beginStructureBlock(3,3,3,false)
            .addController("Front center")
            .addCasingInfoRange("Chemically Inert Machine Casing",8,22,false)
            .addOtherStructurePart("PTFE Pipe Machine Casing", "Center")
            .addOtherStructurePart("Heating Coil", "Adjacent to the PTFE Pipe Machine Casing", 1)
            .addEnergyHatch("Any casing", 1, 2)
            .addMaintenanceHatch("Any casing", 1, 2)
            .addInputBus("Any casing", 1, 2)
            .addInputHatch("Any casing", 1, 2)
            .addOutputBus("Any casing", 1, 2)
            .addOutputHatch("Any casing", 1, 2)
            .addStructureInfo("You can have multiple hatches/busses")
            .toolTipFinisher("gtnothard");
        return tt;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing, int colorIndex, boolean aActive, boolean redstoneLevel){
        if (side == aFacing){
            if (aActive){
                return new ITexture[]{
                    casingTexturePages[1][48],
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_LARGE_CHEMICAL_REACTOR_ACTIVE)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_LARGE_CHEMICAL_REACTOR_ACTIVE_GLOW)
                        .extFacing()
                        .glow()
                        .build()
                };
            }
            return new ITexture[]{
                casingTexturePages[1][48],
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_LARGE_CHEMICAL_REACTOR)
                    .extFacing()
                    .build(),
                TextureFactory.builder()
                    .addIcon(OVERLAY_FRONT_LARGE_CHEMICAL_REACTOR_GLOW)
                    .extFacing()
                    .glow()
                    .build()
            };
        }
        return new ITexture[]{casingTexturePages[1][48]};
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack){
        return true;
    }

    @Override
    public boolean supportsSingleRecipeLocking(){
        return true;
    }

    @Override
    public RecipeMap<?> getRecipeMap(){
        return RecipeMaps.multiblockChemicalReactorRecipes;
    }

    @Override
    protected ProcessingLogic createProcessingLogic(){
        return new ProcessingLogic().enablePerfectOverclock();
    }

    @Override
    public IStructureDefinition<LargeChemicalReactorTest> getStructureDefinition(){
        return STRUCTURE_DEFINITION;
    }

    private void onCasingAdded(){
        mCasingAmount++;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack){
        mCasingAmount = 0;
        mCoilAmount = 0;
        return  checkPiece(STRUCTURE_PIECE_MAIN,1,1,0) && mCasingAmount >= 8 && mCoilAmount == 1 && !mEnergyHatches.isEmpty() && mMaintenanceHatches.size() == 1;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 0;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack){
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack){
        return false;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly){
        mCoilAmount = 0;
        buildPiece(STRUCTURE_PIECE_MAIN,stackSize,hintsOnly,1,1,0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env){
        if (mMachine){
            return -1;
        }
        mCoilAmount = 0;
        return survivialBuildPiece(STRUCTURE_PIECE_MAIN,stackSize,1,1,0,elementBudget,env,false,true);
    }

    @Override
    public boolean supportsVoidProtection(){
        return true;
    }

    private enum CoilStructureElement implements IStructureElement<LargeChemicalReactorTest>{
        INSTANCE;

        @Override
        public boolean check(LargeChemicalReactorTest t, World world,int x,int y,int z){
            Block block = world.getBlock(x,y,z);
            if (block instanceof IHeatingCoil && ((IHeatingCoil)block).getCoilHeat(world.getBlockMetadata(x,y,z)) != HeatingCoilLevel.None){
                return t.mCoilAmount++ == 0;
            } else {
                return false;
            }
        }

        @Override
        public boolean spawnHint(LargeChemicalReactorTest t,World world,int x,int y,int z,ItemStack trigger){
            StructureLibAPI.hintParticle(world,x,y,z,GregTech_API.sBlockCasings5,0);
            return true;
        }

        @Override
        public boolean placeBlock(LargeChemicalReactorTest t,World world,int x,int y,int z,ItemStack trigger){
            if (t.mCoilAmount > 0){
                return false;
            }
            boolean b = world.setBlock(x,y,z,GregTech_API.sBlockCasings5,0,3);
            if (b){
                t.mCoilAmount++;
            }
            return b;
        }

        @Override
        public PlaceResult survivalPlaceBlock(LargeChemicalReactorTest t, World world, int x, int y, int z, ItemStack trigger, IItemSource s, EntityPlayerMP actor, Consumer<IChatComponent> chatter){
            return survivalPlaceBlock(t,world,x,y,z,trigger,AutoPlaceEnvironment.fromLegacy(s,actor,chatter));
        }

        @Override
        public BlocksToPlace getBlocksToPlace(LargeChemicalReactorTest largeChemicalReactorTest,World world,int x,int y,int z,ItemStack trigger,AutoPlaceEnvironment env){
            return BlocksToPlace.create(
                IntStream.range(0,8)
                    .mapToObj(i -> new ItemStack(GregTech_API.sBlockCasings5,1,i))
                    .collect(Collectors.toList())
            );
        }

        //@Override
        public PlaceResult survivalPlaceBloc(LargeChemicalReactorTest t, World world, int x, int y, int z, ItemStack trigger,AutoPlaceEnvironment env){
            if (t.mCoilAmount > 0){
                return PlaceResult.SKIP;
            }
            if (check(t,world,x,y,z)){
                return PlaceResult.SKIP;
            }
            if(!StructureLibAPI.isBlockTriviallyReplaceable(world,x,y,z,env.getActor())){
                return PlaceResult.REJECT;
            }
            ItemStack result = env.getSource()
                .takeOne(ItemStackPredicate.from(GregTech_API.sBlockCasings5),true);
            if (result == null){
                return  PlaceResult.REJECT;
            }
            PlaceResult ret = StructureUtility.survivalPlaceBlock(
                result,
                ItemStackPredicate.NBTMode.EXACT,
                null,
                true,
                world,
                x,
                y,
                z,
                env.getSource(),
                env.getActor(),
                env.getChatter()
            );
            if (ret == PlaceResult.ACCEPT){
                t.mCoilAmount++;
            }
            return ret;
        }
    }
}
