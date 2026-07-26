package furgl.infinitory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public abstract class SlotMixin {
@Shadow @Mutable public int x;
@Shadow @Mutable public int y;
}

sed -i 's/"PlayerScreenHandlerMixin"/"PlayerScreenHandlerMixin",\n    "SlotMixin"/' src/main/resources/infinitory.mixins.json
cat > src/main/java/furgl/infinitory/mixin/SlotMixin.java << 'EOF'
package furgl.infinitory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public abstract class SlotMixin {
@Shadow @Mutable public int x;
@Shadow @Mutable public int y;
}
