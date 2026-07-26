package furgl.infinitory.impl;

import net.minecraft.nbt.CompoundTag;

/**
 * Implemented by the PlayerInventoryMixin. Lets other classes (screen handler mixin,
 * player entity mixin, network handlers, etc.) talk to the "extra slots" state without
 * needing to know how it's stored internally.
 */
public interface IPlayerInventory {

	/** Number of currently unlocked extra slots (always a multiple of 9, 0 = none unlocked yet). */
	int infinitory$getAdditionalSlots();

	/** Directly set the number of unlocked extra slots (will be clamped + rounded to a multiple of 9). */
	void infinitory$setAdditionalSlots(int additionalSlots);

	/** Recalculates whether a new row of extra slots should be unlocked/removed, based on current contents. */
	void infinitory$updateAdditionalSlots();

	SortingType infinitory$getSortingType();

	void infinitory$setSortingType(SortingType type);

	boolean infinitory$isSortAscending();

	void infinitory$setSortAscending(boolean ascending);

	/** Sorts the (non-hotbar) main inventory contents according to the current sorting type. */
	void infinitory$sort();

	/** Called right before vanilla writes this inventory to NBT, to avoid corrupting data past slot 255. */
	void infinitory$prepareForVanillaWrite();

	/** Called right after vanilla writes this inventory to NBT; appends the extra slots' contents. */
	void infinitory$finishVanillaWrite(CompoundTag playerTag);

	/** Called right after vanilla reads this inventory from NBT; restores the extra slots' contents. */
	void infinitory$readExtraFromTag(CompoundTag playerTag);
}
