package furgl.infinitory.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import furgl.infinitory.Utils;
import furgl.infinitory.network.InfinitoryNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * Grows the survival inventory screen to show the currently-unlocked extra rows, and adds a
 * small button to cycle through the sorting modes.
 *
 * This is the part of the port most likely to need a small tweak after the first build -
 * a couple of the exact HandledScreen field/method names below are the ones I'm least sure
 * carried over unchanged from 1.17.1 to 1.16.5. If something doesn't compile here, your IDE's
 * autocomplete on `this.` will show the real 1.16.5 name to swap in.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

	// Real constructor is never called (mixins don't get instantiated directly), but Java
	// requires one that matches a superclass constructor for this to compile.
	public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void infinitory$init(CallbackInfo ci) {
		int activeSlots = Utils.getAdditionalSlots(this.client.player);
		int activeRows = (activeSlots + 8) / 9;

		this.backgroundHeight = 174 + (activeRows > 0 ? activeRows * 18 + 6 : 0);
		this.y = (this.height - this.backgroundHeight) / 2;

		this.addButton(new ButtonWidget(this.x + this.backgroundWidth - 20, this.y + 4, 16, 16,
				new LiteralText("S"), button -> {
					PacketByteBuf buf = PacketByteBufs.create();
					ClientPlayNetworking.send(InfinitoryNetworking.CYCLE_SORT_PACKET_ID, buf);
				}));
	}

	@Inject(method = "drawBackground", at = @At("TAIL"))
	private void infinitory$drawExtraBackground(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
		int activeSlots = Utils.getAdditionalSlots(this.client.player);
		int activeRows = (activeSlots + 8) / 9;
		if (activeRows > 0) {
			int top = this.y + 171;
			int bottom = top + activeRows * 18 + 2;
			this.fill(matrices, this.x + 6, top, this.x + this.backgroundWidth - 6, bottom, 0x66000000);
		}
	}
}
