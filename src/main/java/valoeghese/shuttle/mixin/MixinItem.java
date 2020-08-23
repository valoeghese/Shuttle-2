package valoeghese.shuttle.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Item;
import valoeghese.shuttle.api.ShuttleItem;

@Mixin(Item.class)
public abstract class MixinItem implements ShuttleItem {
}
