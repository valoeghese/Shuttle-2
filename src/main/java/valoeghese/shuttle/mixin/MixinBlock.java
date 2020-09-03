package valoeghese.shuttle.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.Block;
import valoeghese.shuttle.api.ShuttleBlock;

@Mixin(Block.class)
public class MixinBlock implements ShuttleBlock {

}
