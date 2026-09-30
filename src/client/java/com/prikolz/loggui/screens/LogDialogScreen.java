package com.prikolz.loggui.screens;

import com.prikolz.loggui.widget.ColorPicker;
import com.prikolz.loggui.widget.ColorPickerButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class LogDialogScreen extends Screen {
    protected LogDialogScreen(Component title) {
        super(title);
    }

    public ColorPickerButton colorPicker(
            int initColor,
            int x,
            int y,
            Consumer<Integer> onPick
    ) { return colorPicker(initColor, x, y, 20, 20, onPick); }

    public ColorPickerButton colorPicker(
            int initColor,
            int x,
            int y,
            int width,
            int height,
            Consumer<Integer> onPick
    ) { return colorPicker(initColor, x, y, width, height, 80, 80, onPick); }

    public ColorPickerButton colorPicker(
            int initColor,
            int x,
            int y,
            int width,
            int height,
            int pickerWidth,
            int pickerHeight,
            Consumer<Integer> onPick
    ) {
        return new ColorPickerButton(
                x,
                y,
                width,
                height,
                initColor,
                (button) -> {
                    if (button.toggle) {
                        if (button.colorPicker == null)
                            button.colorPicker = new ColorPicker(
                                    button.getX() + button.getWidth(),
                                    button.getY(),
                                    pickerWidth,
                                    pickerHeight,
                                    Component.empty(),
                                    button.color,
                                    (c) -> {
                                        button.color = c;
                                        onPick.accept(c);
                                    }
                            );
                        else {
                            button.colorPicker.setColor(button.color);
                            button.colorPicker.setPosition(
                                    button.getX() + button.getWidth(),
                                    button.getY()
                            );
                        }
                        addRenderableWidget(button.colorPicker);
                    } else {
                        if (button.colorPicker != null) removeWidget(button.colorPicker);
                        button.setFocused(false);
                    }
                }
        );
    }
}
