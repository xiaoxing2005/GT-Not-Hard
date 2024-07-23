package block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockRainBow extends Block {
    public BlockRainBow(){
        super(Material.ground);
        this.setBlockName("gtnothard.rainBow");
        this.setHardness(50);
        this.setStepSound(soundTypeGlass);
        this.setResistance(100.0F);
    }
}
