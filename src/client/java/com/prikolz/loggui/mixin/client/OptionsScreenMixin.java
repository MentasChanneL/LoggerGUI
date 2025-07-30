package com.prikolz.loggui.mixin.client;

import com.prikolz.loggui.LogScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    private static final Tooltip LOGGER_TOOLTIP = Tooltip.create( Component.translatable("loggui.gui.tooltip.options") );

    @Inject(at = @At("RETURN"), method = "init()V")
    private void init(CallbackInfo info) {
        var mixinScreen = (ScreenMixin) this;
        Button b = Button.builder(Component.literal(">_"), button -> {
            Minecraft.getInstance().setScreen( new LogScreen(null) );
        })
                .bounds(mixinScreen.getWidth() / 2 + 110, mixinScreen.getHeight() - 26, 20, 20)
                .tooltip(LOGGER_TOOLTIP)
                .build();
        mixinScreen.invokeAddRenderableWidget(b);
    }
}
