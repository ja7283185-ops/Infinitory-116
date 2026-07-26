package furgl.infinitory;

import net.fabricmc.api.ClientModInitializer;

public class InfinitoryClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// Nothing to register here - the sort button packet is sent on demand from
		// InventoryScreenMixin, and the extra-slot count is synced automatically via the
		// vanilla ScreenHandler property added in PlayerScreenHandlerMixin.
	}
}
