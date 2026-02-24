package com.prikolz.loggui.mixin.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractWidget.class)
public interface AbstractWidgetMixin {
    @Invoker("onDrag")
    void onDrag(MouseButtonEvent mouseButtonEvent, double d, double e);
}
