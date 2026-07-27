package furgl.infinitory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import furgl.infinitory.impl.IInfinitorySlot;
import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public abstract class SlotMixin implements IInfinitorySlot {
@Shadow @Mutable public int x;
@Shadow @Mutable public int y;

@Override
public void infinitory$setPos(int x, int y) {
this.x = x;
this.y = y;
}
}
