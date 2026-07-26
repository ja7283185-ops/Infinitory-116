package furgl.infinitory.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import furgl.infinitory.Utils;
import furgl.infinitory.config.Config;
import furgl.infinitory.impl.IPlayerInventory;
import furgl.infinitory.impl.InfinitorySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.property.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

/**
 * Adds a pool of pre-created (but mostly inactive) extra slots after the offhand slot, and
 * keeps their "active count" synced to the client using a vanilla ScreenHandler Property -
 * the same built-in mechanism vanilla uses to sync a furnace's burn time, so no custom
 * networking is needed for this part.
 *
 * Shift-click routing (transferSlot) is fully replaced so items can flow into/out of the
 * extra slots too. Simplified compared to the original 1.17.1 mod: armor pieces are no
 * longer auto-equipped on shift-click (they just go to general storage like anything else),
 * and expanded 3x3 crafting was left out.
 */
@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

	@Shadow @Final private PlayerEntity owner;
	@Shadow @Final public List<Slot> slots;

	@Shadow
	protected abstract boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void infinitory$addExtraSlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
		PlayerScreenHandler self = (PlayerScreenHandler) (Object) this;

		java.util.List<InfinitorySlot> extraSlots = new java.util.ArrayList<>();
		for (int i = 0; i < Config.maxExtraSlots; i++) {
			int rawIndex = 36 + i;
			// start every extra slot far off-screen; infinitory$repositionExtraSlots below moves
			// the currently-active ones into the visible grid.
			InfinitorySlot extraSlot = new InfinitorySlot(inventory, rawIndex, i, -10000, -10000);
			extraSlots.add(extraSlot);
			self.addSlot(extraSlot);
		}

		PropertyDelegate delegate = new PropertyDelegate() {
			@Override
			public int get(int index) {
				return ((IPlayerInventory) inventory).infinitory$getAdditionalSlots();
			}

			@Override
			public void set(int index, int value) {
				((IPlayerInventory) inventory).infinitory$setAdditionalSlots(value);
				infinitory$repositionExtraSlots(extraSlots, value);
			}

			@Override
			public int size() {
				return 1;
			}
		};
		self.addProperties(delegate);

		infinitory$repositionExtraSlots(extraSlots, ((IPlayerInventory) inventory).infinitory$getAdditionalSlots());
	}

	/** Moves the first `activeCount` extra slots into the visible grid below the hotbar, and hides the rest off-screen. */
	@Unique
	private static void infinitory$repositionExtraSlots(java.util.List<InfinitorySlot> extraSlots, int activeCount) {
		int columns = 9;
		int slotSize = 18;
		int startX = 8;
		int startY = 172; // just below the vanilla inventory background

		for (int i = 0; i < extraSlots.size(); i++) {
			InfinitorySlot slot = extraSlots.get(i);
			if (i < activeCount) {
				int col = i % columns;
				int row = i / columns;
				slot.x = startX + col * slotSize;
				slot.y = startY + row * slotSize;
			} else {
				slot.x = -10000;
				slot.y = -10000;
			}
		}
	}

	@Overwrite
	public ItemStack transferSlot(PlayerEntity player, int index) {
		Slot slot = this.slots.get(index);
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
			// crafting result, crafting grid, or armor -> anywhere in general storage
			success = this.insertItem(stackInSlot, mainStart, extraEnd, true);
			if (success && index == 0) {
				slot.onQuickTransfer(stackInSlot, original);
			}
		} else if (index >= extraStart && index < extraEnd) {
			// extra slot -> main storage + hotbar
			success = this.insertItem(stackInSlot, mainStart, hotbarEnd, false);
		} else if (index >= hotbarStart && index < hotbarEnd) {
			// hotbar -> main storage, then extra
			success = this.insertItem(stackInSlot, mainStart, hotbarStart, false)
					|| this.insertItem(stackInSlot, extraStart, extraEnd, false);
		} else if (index >= mainStart && index < hotbarStart) {
			// main storage -> hotbar, then extra
			success = this.insertItem(stackInSlot, hotbarStart, hotbarEnd, false)
					|| this.insertItem(stackInSlot, extraStart, extraEnd, false);
		} else if (index == offhand) {
			// offhand -> anywhere
			success = this.insertItem(stackInSlot, mainStart, extraEnd, false);
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
