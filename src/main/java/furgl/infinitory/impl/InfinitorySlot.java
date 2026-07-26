package furgl.infinitory.impl;

import furgl.infinitory.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * A slot representing one of the "extra" inventory slots. All of these are created up-front
 * (up to Config.maxExtraSlots) when the screen handler is built, but only the first N
 * (N = the player's currently unlocked additional slot count) are active/usable/visible.
 * This avoids ever having to insert/remove slots from a screen handler's slot list at runtime,
 * which is fragile to get exactly right without being able to test against the real game.
 */
public class InfinitorySlot extends Slot {

	/** 0-based position of this slot within the extra slot pool (not the raw inventory index). */
	private final int extraIndex;

	public InfinitorySlot(Inventory inventory, int rawInventoryIndex, int extraIndex, int x, int y) {
		super(inventory, rawInventoryIndex, x, y);
		this.extraIndex = extraIndex;
	}

	public int getExtraIndex() {
		return extraIndex;
	}

	public boolean isActive() {
		return this.extraIndex < ((IPlayerInventory) this.inventory).infinitory$getAdditionalSlots();
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return isActive();
	}

	@Override
	public boolean canTakeItems(PlayerEntity player) {
		return isActive();
	}

	@Override
	public int getMaxItemCount() {
		return Config.maxStackSize;
	}
}
