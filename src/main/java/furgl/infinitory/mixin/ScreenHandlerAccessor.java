package furgl.infinitory.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {

@Accessor("slots")
List<Slot> infinitory$getSlots();

@Invoker("addSlot")
Slot infinitory$addSlot(Slot slot);

@Invoker("addProperties")
void infinitory$addProperties(PropertyDelegate delegate);

@Invoker("insertItem")
boolean infinitory$insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast);
}
