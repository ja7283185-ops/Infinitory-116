package furgl.infinitory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import furgl.infinitory.impl.IPlayerInventory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

@Mixin(LivingEntity.class)
public abstract class PlayerEntityMixin {

@Inject(method = "writeCustomDataToTag", at = @At("HEAD"))
private void infinitory$preWrite(NbtCompound tag, CallbackInfo ci) {
if (!((Object) this instanceof PlayerEntity)) return;
PlayerEntity player = (PlayerEntity) (Object) this;
((IPlayerInventory) player.inventory).infinitory$prepareForVanillaWrite();
}

@Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
private void infinitory$postWrite(NbtCompound tag, CallbackInfo ci) {
if (!((Object) this instanceof PlayerEntity)) return;
PlayerEntity player = (PlayerEntity) (Object) this;
((IPlayerInventory) player.inventory).infinitory$finishVanillaWrite(tag);
}

@Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
private void infinitory$postRead(NbtCompound tag, CallbackInfo ci) {
if (!((Object) this instanceof PlayerEntity)) return;
PlayerEntity player = (PlayerEntity) (Object) this;
((IPlayerInventory) player.inventory).infinitory$readExtraFromTag(tag);
}
}
