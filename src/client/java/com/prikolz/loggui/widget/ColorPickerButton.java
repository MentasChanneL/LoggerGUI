package com.prikolz.loggui.widget;

import com.prikolz.loggui.Localization;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

import static com.prikolz.loggui.LogDialog.MOD_ID;

public class ColorPickerButton extends AbstractButton {
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/button");
    public static final Identifier HIGHLIGHT_TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/button_highlight");
    public static final Identifier OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/button_overlay");
    public int color;
    public boolean toggle = false;
    public Consumer<ColorPickerButton> onClick;
    public ColorPicker colorPicker = null;

    public ColorPickerButton(
            int x,
            int y,
            int width,
            int height,
            int color,
            Consumer<ColorPickerButton> onClick
    ) {
        super(x, y, width, height, Component.empty());
        this.color = color;
        this.onClick = onClick;
        setTooltip(Tooltip.create(
                Component.translatable(Localization.SETTINGS_COLOR_PICKER_TOOLTIP_OPEN)
        ));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.toggle = !this.toggle;
        this.onClick.accept(this);
        if (this.toggle)
            setTooltip(Tooltip.create(
                    Component.translatable(Localization.SETTINGS_COLOR_PICKER_TOOLTIP_CLOSE)
            ));
        else
            setTooltip(Tooltip.create(
                    Component.translatable(Localization.SETTINGS_COLOR_PICKER_TOOLTIP_OPEN)
            ));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                (toggle || isHovered()) ? HIGHLIGHT_TEXTURE : TEXTURE,
                getX(),
                getY(),
                width,
                height
        );
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                OVERLAY_TEXTURE,
                getX(),
                getY(),
                width,
                height,
                color
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
