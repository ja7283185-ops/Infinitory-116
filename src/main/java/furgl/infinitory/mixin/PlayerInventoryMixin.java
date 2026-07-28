package furgl.infinitory.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import furgl.infinitory.config.Config;
import furgl.infinitory.impl.IPlayerInventory;
import furgl.infinitory.impl.SortingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

/**
 * Ported from Furgl's original 1.17.1 PlayerInventoryMixin. The core "when should the
 * inventory grow" algorithm below is taken directly from the original mod; the NBT and
 * screen-handler integration have been re-worked to be simpler / more robust to port blind
 * (i.e. without being able to compile-test against the real 1.16.5 game jar).
 */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory, IPlayerInventory {

	@Shadow @Final public PlayerEntity player;
	@Shadow @Final @Mutable public DefaultedList<ItemStack> main;

	@Unique private int infinitory$additionalSlots = 0;
	@Unique private SortingType infinitory$sortingType = SortingType.NONE;
	@Unique private boolean infinitory$sortAscending = true;
	@Unique private DefaultedList<ItemStack> infinitory$savedMain;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void infinitory$constructor(PlayerEntity player, CallbackInfo ci) {
		// The backing list is always pre-sized to hold every possible extra slot (padded with
		// EMPTY). This is what lets the screen handler safely pre-create every InfinitorySlot
		// up front (see PlayerScreenHandlerMixin) instead of inserting/removing slots at
		// runtime. `infinitory$additionalSlots` tracks how many of those slots are "unlocked"
		// and therefore usable - the rest just sit there empty and disabled.
		DefaultedList<ItemStack> grown = DefaultedList.of();
		for (ItemStack stack : this.main) {
			grown.add(stack);
		}
		while (grown.size() < 36 + Config.maxExtraSlots) {
			grown.add(ItemStack.EMPTY);
		}
		this.main = grown;
	}

	// ========================= SORTING =========================

	@Override
	public SortingType infinitory$getSortingType() {
		return this.infinitory$sortingType;
	}

	@Override
	public void infinitory$setSortingType(SortingType type) {
		this.infinitory$sortingType = type == null ? SortingType.NONE : type;
	}

	@Override
	public boolean infinitory$isSortAscending() {
		return this.infinitory$sortAscending;
	}

	@Override
	public void infinitory$setSortAscending(boolean ascending) {
		this.infinitory$sortAscending = ascending;
	}

	@Override
	public void infinitory$sort() {
		if (this.player.world.isClient || this.infinitory$sortingType == SortingType.NONE) {
			return;
		}

		// Only the storage rows (raw index 9+) get sorted - the hotbar (0-8) is left alone.
		int effectiveSize = 36 + this.infinitory$additionalSlots;
		List<ItemStack> list = new ArrayList<>();
		for (int i = 9; i < effectiveSize; ++i) {
			ItemStack adding = this.main.get(i).copy();
			if (adding.isEmpty()) {
				continue;
			}

			for (ItemStack existing : list) {
				if (!existing.isEmpty() && (ItemStack.areItemsEqual(existing, adding) && ItemStack.areTagsEqual(existing, adding))) {
					int room = this.getMaxCountPerStack() - existing.getCount();
					int amount = Math.min(room, adding.getCount());
					if (amount > 0) {
						existing.increment(amount);
						adding.decrement(amount);
						if (adding.isEmpty()) {
							break;
						}
					}
				}
			}

			if (!adding.isEmpty()) {
				list.add(adding);
			}
		}

		this.infinitory$sortingType.sort(list, this.infinitory$sortAscending);

		for (int i = 9; i < effectiveSize; ++i) {
			ItemStack stack = (i - 9 < list.size()) ? list.get(i - 9) : ItemStack.EMPTY;
			this.main.set(i, stack);
		}
	}

	// ================= AUTO-EXPANDING INVENTORY =================

	@Override
	public int infinitory$getAdditionalSlots() {
		return this.infinitory$additionalSlots;
	}

	@Override
	public void infinitory$setAdditionalSlots(int additionalSlots) {
		additionalSlots = MathHelper.clamp(additionalSlots, 0, Config.maxExtraSlots);
		if (additionalSlots % 9 != 0) {
			additionalSlots += (9 - additionalSlots % 9);
		}
		// No list resizing needed - the backing list is always pre-sized to 36+maxExtraSlots
		// (see the constructor injection above). This just moves the "unlocked" boundary.
		this.infinitory$additionalSlots = additionalSlots;
	}

	/**
	 * Recalculates how many extra rows should be unlocked, based on how full the inventory
	 * currently is. Ported directly from the original mod's algorithm: grows by one row once
	 * the last row of the (extended) inventory is either completely full, or full except for
	 * a trailing gap right after the last item (so the player always has at least a little
	 * breathing room without the inventory jumping by more than one row at a time).
	 */
	@Override
	public void infinitory$updateAdditionalSlots() {
		if (this.player.world.isClient) {
			return;
		}

		int effectiveSize = 36 + this.infinitory$additionalSlots;
		boolean isFull = true;
		boolean isFullBeforeLastItem = true;
		boolean lastRowEmpty = true;
		int lastItem = -1;

		for (int i = effectiveSize - 1; i >= 9; --i) {
			boolean empty = this.main.get(i).isEmpty();
			if (!empty && lastItem == -1) {
				lastItem = i;
			}
			if (empty) {
				if (lastItem != -1) {
					isFullBeforeLastItem = false;
				}
				isFull = false;
			}
			if (i >= effectiveSize - 9 && !empty) {
				lastRowEmpty = false;
			}
		}

		boolean growByRow = lastItem != -1 && (lastItem + 1) % 9 == 0 && (isFull || (isFullBeforeLastItem && lastRowEmpty));
		this.infinitory$setAdditionalSlots(lastItem - 35 + (growByRow ? 9 : 0));
	}

	// ========================= STACK SIZE =========================

	@Override
	public int getMaxCountPerStack() {
		return Config.maxStackSize;
	}

	@Inject(method = "canStackAddMore", at = @At("RETURN"), cancellable = true)
	private void infinitory$canStackAddMore(ItemStack existing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(!existing.isEmpty() && (ItemStack.areItemsEqual(existing, stack) && ItemStack.areTagsEqual(existing, stack)));
	}


	@Redirect(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
	private int infinitory$addStackMaxCount(ItemStack stack) {
		return Config.maxStackSize;
	}

	// ================= KEEPING EVERYTHING IN SYNC =================

	@Inject(method = "markDirty", at = @At("RETURN"))
	private void infinitory$markDirty(CallbackInfo ci) {
		this.infinitory$updateAdditionalSlots();
		if (this.infinitory$sortingType == SortingType.QUANTITY) {
			this.infinitory$sort();
		}
	}

	@Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
	private void infinitory$removeStack(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
		this.infinitory$updateAdditionalSlots();
	}

	// ========================= NBT =========================
	// Vanilla writes each non-empty stack's slot index as a single byte, which would silently
	// wrap around/corrupt data once the inventory grows past 255 slots. To avoid that, we
	// temporarily shrink `main` back down to 36 right before vanilla's own write runs, then
	// restore it and append the extra slots ourselves under a separate NBT key afterwards.

	@Override
	public void infinitory$prepareForVanillaWrite() {
		if (this.main.size() > 36) {
			this.infinitory$savedMain = this.main;
			DefaultedList<ItemStack> truncated = DefaultedList.of();
			for (int i = 0; i < 36; ++i) {
				truncated.add(this.infinitory$savedMain.get(i));
			}
			this.main = truncated;
		} else {
			this.infinitory$savedMain = null;
		}
	}

	@Override
	public void infinitory$finishVanillaWrite(NbtCompound playerTag) {
		DefaultedList<ItemStack> full = this.infinitory$savedMain != null ? this.infinitory$savedMain : this.main;
		this.main = full;
		this.infinitory$savedMain = null;

		NbtList extraList = new NbtList();
		for (int i = 36; i < full.size(); ++i) {
			ItemStack stack = full.get(i);
			if (!stack.isEmpty()) {
				NbtCompound itemTag = new NbtCompound();
				itemTag.putInt("Slot", i);
				stack.writeNbt(itemTag);
				extraList.add(itemTag);
			}
		}

		NbtCompound infinitoryTag = new NbtCompound();
		infinitoryTag.put("Items", extraList);
		infinitoryTag.putInt("AdditionalSlots", this.infinitory$additionalSlots);
		infinitoryTag.putString("SortingType", this.infinitory$sortingType.name());
		infinitoryTag.putBoolean("SortAscending", this.infinitory$sortAscending);
		playerTag.put("InfinitoryData", infinitoryTag);
	}

	@Override
	public void infinitory$readExtraFromTag(NbtCompound playerTag) {
		if (!playerTag.contains("InfinitoryData")) {
			return;
		}
		NbtCompound infinitoryTag = playerTag.getCompound("InfinitoryData");

		this.infinitory$setAdditionalSlots(infinitoryTag.getInt("AdditionalSlots"));

		NbtList items = infinitoryTag.getList("Items", 10); // 10 = NBT compound tag type id
		for (int i = 0; i < items.size(); ++i) {
			NbtCompound itemTag = items.getCompound(i);
			int slot = itemTag.getInt("Slot");
			if (slot >= 0 && slot < this.main.size()) {
				this.main.set(slot, ItemStack.fromNbt(itemTag));
			}
		}

		try {
			this.infinitory$sortingType = SortingType.valueOf(infinitoryTag.getString("SortingType"));
		} catch (IllegalArgumentException e) {
			this.infinitory$sortingType = SortingType.NONE;
		}
		this.infinitory$sortAscending = infinitoryTag.getBoolean("SortAscending");
	}
}
