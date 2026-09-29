package com.prikolz.loggui.mixin.client;

import com.prikolz.loggui.screens.LogScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.prikolz.loggui.LogDialog.LOGGER;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {
    @Unique
    private static final Tooltip LOGGER_TOOLTIP = Tooltip.create( Component.translatable("loggui.gui.tooltip.options") );

    @Inject(at = @At("TAIL"), method = "init()V")
    private void init(CallbackInfo info) {
        var self = (Screen)(Object) this;
        Button b = Button.builder(Component.literal(">_"), button -> {
            Minecraft.getInstance().setScreenAndShow( new LogScreen(null) );
        })
                .bounds(self.width / 2 + 110, self.height - 26, 20, 20)
                .tooltip(LOGGER_TOOLTIP)
                .build();
        try {
            var method = Screen.class.getDeclaredMethod(
                    "addRenderableWidget",
                    GuiEventListener.class
            );
            method.setAccessible(true);
            method.invoke(self, b);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
