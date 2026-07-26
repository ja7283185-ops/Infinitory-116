package furgl.infinitory;

import furgl.infinitory.impl.IPlayerInventory;
import net.minecraft.entity.player.PlayerEntity;

public class Utils {

	/** Number of currently unlocked extra slots for this player's inventory. */
	public static int getAdditionalSlots(PlayerEntity player) {
		return ((IPlayerInventory) player.inventory).infinitory$getAdditionalSlots();
	}
}
