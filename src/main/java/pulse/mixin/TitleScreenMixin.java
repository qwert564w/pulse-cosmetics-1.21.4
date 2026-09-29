package pulse.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pulse.gui.IntelMainMenuScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void cosmetics$redirectMainMenu(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && !(client.currentScreen instanceof IntelMainMenuScreen)) {
            client.setScreen(new IntelMainMenuScreen());
            ci.cancel();
        }
    }
}
