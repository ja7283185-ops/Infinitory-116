package furgl.infinitory.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import furgl.infinitory.Utils;
import furgl.infinitory.config.Config;
import furgl.infinitory.impl.IInfinitorySlot;
import furgl.infinitory.impl.IPlayerInventory;
import furgl.infinitory.impl.InfinitorySlot;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

@Shadow @Final private PlayerEntity owner;

@Inject(method = "<init>", at = @At("TAIL"))
private void infinitory$addExtraSlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
PlayerScreenHandler self = (PlayerScreenHandler) (Object) this;
ScreenHandlerAccessor accessor = (ScreenHandlerAccessor) self;

int columns = 9;
int slotSize = 18;
int startX = 8;
int startY = 172;

java.util.List<InfinitorySlot> extraSlots = new java.util.ArrayList<>();
for (int i = 0; i < Config.maxExtraSlots; i++) {
int rawIndex = 36 + i;
InfinitorySlot extraSlot = new InfinitorySlot(inventory, rawIndex, i, -10000, -10000);
extraSlots.add(extraSlot);
accessor.infinitory$addSlot(extraSlot);
}

PropertyDelegate delegate = new PropertyDelegate() {
@Override
public int get(int index) {
return ((IPlayerInventory) inventory).infinitory$getAdditionalSlots();
}

@Override
public void set(int index, int value) {
((IPlayerInventory) inventory).infinitory$setAdditionalSlots(value);
infinitory$repositionExtraSlots(extraSlots, value, columns, slotSize, startX, startY);
}

@Override
public int size() {
return 1;
}
};
accessor.infinitory$addProperties(delegate);

infinitory$repositionExtraSlots(extraSlots, ((IPlayerInventory) inventory).infinitory$getAdditionalSlots(), columns, slotSize, startX, startY);
}

private static void infinitory$repositionExtraSlots(java.util.List<InfinitorySlot> extraSlots, int activeCount, int columns, int slotSize, int startX, int startY) {
for (int i = 0; i < extraSlots.size(); i++) {
IInfinitorySlot slot = (IInfinitorySlot) extraSlots.get(i);
if (i < activeCount) {
int col = i % columns;
int row = i / columns;
slot.infinitory$setPos(startX + col * slotSize, startY + row * slotSize);
} else {
slot.infinitory$setPos(-10000, -10000);
}
}
}

@Overwrite
public ItemStack transferSlot(PlayerEntity player, int index) {
ScreenHandlerAccessor accessor = (ScreenHandlerAccessor) this;
Slot slot = accessor.infinitory$getSlots().get(index);
if (slot == null || !slot.hasStack()) {
return ItemStack.EMPTY;
}

ItemStack stackInSlot = slot.getStack();
ItemStack original = stackInSlot.copy();

int activeExtra = Utils.getAdditionalSlots(player);
final int mainStart = 9;
final int hotbarStart = 36;
final int hotbarEnd = 45;
final int offhand = 45;
final int extraStart = 46;
int extraEnd = 46 + activeExtra;

boolean success;
if (index == 0 || (index >= 1 && index < 9)) {
success = accessor.infinitory$insertItem(stackInSlot, mainStart, extraEnd, true);
if (success && index == 0) {
slot.onQuickTransfer(stackInSlot, original);
}
} else if (index >= extraStart && index < extraEnd) {
success = accessor.infinitory$insertItem(stackInSlot, mainStart, hotbarEnd, false);
} else if (index >= hotbarStart && index < hotbarEnd) {
success = accessor.infinitory$insertItem(stackInSlot, mainStart, hotbarStart, false)
|| accessor.infinitory$insertItem(stackInSlot, extraStart, extraEnd, false);
} else if (index >= mainStart && index < hotbarStart) {
success = accessor.infinitory$insertItem(stackInSlot, hotbarStart, hotbarEnd, false)
|| accessor.infinitory$insertItem(stackInSlot, extraStart, extraEnd, false);
} else if (index == offhand) {
success = accessor.infinitory$insertItem(stackInSlot, mainStart, extraEnd, false);
} else {
success = false;
}

if (!success) {
return ItemStack.EMPTY;
}

if (stackInSlot.isEmpty()) {
slot.setStack(ItemStack.EMPTY);
} else {
slot.markDirty();
}

if (stackInSlot.getCount() == original.getCount()) {
return ItemStack.EMPTY;
}

slot.onTakeItem(player, stackInSlot);
return original;
}
}
