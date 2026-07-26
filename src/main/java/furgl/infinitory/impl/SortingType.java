package furgl.infinitory.impl;

import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

/**
 * The different ways the player's inventory can be sorted.
 * Cycled through by clicking the sort button in the inventory screen.
 */
public enum SortingType {
	NONE,
	NAME,
	QUANTITY,
	ID;

	public SortingType next() {
		SortingType[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}

	/**
	 * Sorts the given list of item stacks in place according to this sorting type.
	 */
	public void sort(List<ItemStack> list, boolean ascending) {
		Comparator<ItemStack> comparator;
		switch (this) {
			case NAME:
				comparator = Comparator.comparing(stack -> stack.getName().getString().toLowerCase());
				break;
			case QUANTITY:
				comparator = Comparator.comparingInt(ItemStack::getCount);
				break;
			case ID:
				comparator = Comparator.comparingInt(stack -> Registry.ITEM.getRawId(stack.getItem()));
				break;
			case NONE:
			default:
				return;
		}
		if (!ascending) {
			comparator = comparator.reversed();
		}
		list.sort(comparator);
	}
}
