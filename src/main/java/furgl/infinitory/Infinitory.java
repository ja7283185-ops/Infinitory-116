package furgl.infinitory;

import furgl.infinitory.config.Config;
import furgl.infinitory.impl.IPlayerInventory;
import furgl.infinitory.network.InfinitoryNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class Infinitory implements ModInitializer {

	@Override
	public void onInitialize() {
		Config.load();

		// Player clicks the sort button -> cycle to the next sorting mode and re-sort.
		ServerPlayNetworking.registerGlobalReceiver(InfinitoryNetworking.CYCLE_SORT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				IPlayerInventory inventory = (IPlayerInventory) player.inventory;
				inventory.infinitory$setSortingType(inventory.infinitory$getSortingType().next());
				inventory.infinitory$sort();
				player.inventory.markDirty();
			});
		});
	}
}
