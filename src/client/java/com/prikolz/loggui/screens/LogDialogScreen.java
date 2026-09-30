package com.prikolz.loggui.screens;

import com.prikolz.loggui.LogDialog;
import com.prikolz.loggui.util.ColorUtil;
import com.prikolz.loggui.widget.ColorPicker;
import com.prikolz.loggui.widget.ColorPickerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class LogDialogScreen extends Screen {
    protected LogDialogScreen(Component title) {
        super(title);
    }

    public ColorSelector colorSelector(
            int initColor,
            int x,
            int y,
            Consumer<Integer> onChange
    ) {
        return new ColorSelector(
                initColor,
                x,
                y,
                60,
                20,
                20,
                20,
                80,
                80,
                onChange
        );
    }

    public static class EditBoxHolder extends AbstractWidget {
        public EditBox hold;
        public OnChange change;
        public boolean isNumber = false;

        public EditBoxHolder(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty());
            hold = new EditBox(Minecraft.getInstance().fontFilterFishy, x, y, w, h, Component.empty());
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            hold.updateWidgetNarration(narrationElementOutput);
        }

        @Override
        public void setFocused(boolean bl) {
            hold.setFocused(bl);
            if (!bl) this.change.onChange(this, true);
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            if ( isNumber && (event.codepoint() < 48 || event.codepoint() > 57)
                    && !(event.codepointAsString().equals("-") && hold.getValue().isEmpty())
            ) return false;
            boolean result = hold.charTyped(event);
            this.change.onChange(this, false);
            return result;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            boolean result = hold.keyPressed(event);
            change.onChange(this, event.isConfirmation());
            return result;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
            return hold.mouseClicked(event, bl);
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            hold.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        }

        public interface OnChange {
            void onChange(EditBoxHolder holder, boolean isEnter);
        }
    }

    public class ColorSelector {
        public LogDialogScreen.EditBoxHolder field;
        public ColorPickerButton button;
        public ColorPicker palette;
        public Consumer<Integer> onChangeColor;

        public ColorSelector(
                int initColor,
                int x,
                int y,
                int fieldWidth,
                int fieldHeight,
                int buttonWidth,
                int buttonHeight,
                int pickerWidth,
                int pickerHeight,
                Consumer<Integer> onChange
        ) {
            this.onChangeColor = onChange;
            String hex = ColorUtil.toHex(initColor, false);
            field = new LogDialogScreen.EditBoxHolder(x, y, fieldWidth, fieldHeight);
            field.hold.setValue(hex);
            field.hold.setMaxLength(7);
            field.change = (e, isEnter) -> {
                if (!isEnter) return;
                try {
                    var input = new StringBuilder(e.hold.getValue().substring(1));
                    while (input.length() < 6) input.append("0");
                    int argb = (int) Long.parseLong("FF" + input, 16);
                    button.color = argb;
                    e.hold.setValue(ColorUtil.toHex(argb, false));
                    onChangeColor.accept(argb);
                } catch (Exception er) {
                    LogDialog.LOGGER.warn(er.getMessage());
                    e.hold.setValue(ColorUtil.toHex(button.color, false));
                }
                removeWidget(palette);
                button.toggle = false;
            };
            palette = new ColorPicker(
                    x + fieldWidth + buttonWidth,
                    y,
                    pickerWidth,
                    pickerHeight,
                    Component.empty(),
                    initColor,
                    (color) -> {
                        button.color = color;
                        var hexColor = ColorUtil.toHex(color, false);
                        field.hold.setValue(hexColor);
                        onChangeColor.accept(color);
                    }
            );
            button = new ColorPickerButton(
                    x + fieldWidth,
                    y,
                    buttonWidth,
                    buttonHeight,
                    initColor,
                    (button) -> {
                        if (button.toggle) {
                            button.colorPicker.setColor(button.color);
                            button.colorPicker.setPosition(
                                    button.getX() + button.getWidth(),
                                    button.getY()
                            );
                            addRenderableWidget(button.colorPicker);
                        } else {
                            removeWidget(button.colorPicker);
                            button.setFocused(false);
                        }
                    }
            );
            button.colorPicker = palette;
        }

        public void addOnScreen() {
            addRenderableWidget(field);
            addRenderableWidget(button);
        }
    }
}
