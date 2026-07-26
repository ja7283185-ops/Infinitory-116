package furgl.infinitory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import furgl.infinitory.impl.IPlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;

/**
 * Hooks into the entity-level save/load methods (rather than PlayerInventory's own NBT
 * methods, whose exact names differ between 1.16 and 1.17) to save/restore the extra
 * inventory slots. See IPlayerInventory for what actually happens on each hook.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Shadow public PlayerInventory inventory;

	@Inject(method = "writeCustomDataToTag", at = @At("HEAD"))
	private void infinitory$preWrite(NbtCompound tag, CallbackInfo ci) {
		((IPlayerInventory) this.inventory).infinitory$prepareForVanillaWrite();
	}

	@Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
	private void infinitory$postWrite(NbtCompound tag, CallbackInfo ci) {
		((IPlayerInventory) this.inventory).infinitory$finishVanillaWrite(tag);
	}

	@Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
	private void infinitory$postRead(NbtCompound tag, CallbackInfo ci) {
		((IPlayerInventory) this.inventory).infinitory$readExtraFromTag(tag);
	}
}
