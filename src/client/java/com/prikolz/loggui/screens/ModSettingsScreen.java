package com.prikolz.loggui.screens;

import com.prikolz.loggui.Config;
import com.prikolz.loggui.LogDialog;
import com.prikolz.loggui.util.ColorUtil;
import com.prikolz.loggui.widget.ColorPickerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class ModSettingsScreen extends LogDialogScreen {
    private static final Component TITLE = Component.translatable("loggui.settings.title");
    private static final Component TEXT_SHADOW = Component.translatable("loggui.settings.text_shadow");
    private static final Component COLOR_TITLE = Component.translatable("loggui.settings.color_title");
    private static final Component INFO_TITLE = Component.translatable("loggui.settings.info_title");
    private static final Component WARN_TITLE = Component.translatable("loggui.settings.warn_title");
    private static final Component ERR_TITLE = Component.translatable("loggui.settings.err_title");
    private static final Component LINES_LIMIT_TITLE = Component.translatable("loggui.settings.lines_limit_title");

    private final Screen parent;

    public ModSettingsScreen(@NotNull Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    public Button doneButton;
    public Checkbox useShadow;
    public ColorSelector textColor;
    public MultiLineEditBoxHolder preview;
    public EditBoxHolder infoPrefix;
    public EditBoxHolder warnPrefix;
    public EditBoxHolder errPrefix;
    public EditBoxHolder linesLimit;

    private void updatePreview() {
        preview.hold = MultiLineEditBox.builder()
                .setX(120).setY(25)
                .setTextColor( Config.LOGGER_TEXT_COLOR )
                .setTextShadow( Config.LOGGER_TEXT_SHADOW )
                .build(
                    Minecraft.getInstance().fontFilterFishy,
                    300, 200, Component.empty()
                );
        preview.hold.setValue(
                Config.INFO_PREFIX + "[00:00:00] [Example/INFO]§r (Minecraft) Example output.\n" +
                Config.WARN_PREFIX + "[00:00:00] [Example/WARN]§r (Minecraft) Example warn.\n" +
                Config.ERR_PREFIX + "[00:00:00] [Example/ERROR]§r (Minecraft) Example error."
        );
    }

    @Override
    protected void init() {
        doneButton = Button.builder(CommonComponents.GUI_DONE, b -> {
                    Config.save();
                    Minecraft.getInstance().setScreenAndShow(parent);
        }).bounds(this.width / 2 - 100, this.height - 40, 200, 20).build();
        useShadow = Checkbox.builder(TEXT_SHADOW, Minecraft.getInstance().fontFilterFishy)
                .onValueChange((ch, bl) -> {
                    Config.LOGGER_TEXT_SHADOW = bl;
                    updatePreview();
                    Config.save();
                })
                .selected( Config.LOGGER_TEXT_SHADOW )
                .pos(5, 20)
                .build();

        this.addRenderableWidget( new StringWidget(5, 45, 100, 20, COLOR_TITLE, minecraft.fontFilterFishy) );

        textColor = colorSelector(
                Config.LOGGER_TEXT_COLOR,
                5,
                65,
                (color) -> {
                    Config.LOGGER_TEXT_COLOR = color;
                    updatePreview();
                }
        );
        final int prefixY = textColor.field.getY() + 22;

        this.addRenderableWidget( new StringWidget(5, prefixY, 100, 20, INFO_TITLE, minecraft.fontFilterFishy) );
        infoPrefix = new EditBoxHolder(5, prefixY + 20, 100, 20);
        infoPrefix.hold.setValue( prefixFormat(Config.INFO_PREFIX) );
        infoPrefix.hold.setMaxLength(128);
        infoPrefix.change = (e, isEnter) -> {
            Config.INFO_PREFIX = prefixConvert(e.hold.getValue());
            updatePreview();
        };

        this.addRenderableWidget( new StringWidget(5, prefixY + 40, 100, 20, WARN_TITLE, minecraft.fontFilterFishy) );
        warnPrefix = new EditBoxHolder(5, prefixY + 60, 100, 20);
        warnPrefix.hold.setValue( prefixFormat(Config.WARN_PREFIX) );
        warnPrefix.hold.setMaxLength(128);
        warnPrefix.change = (e, isEnter) -> {
            Config.WARN_PREFIX = prefixConvert(e.hold.getValue());
            updatePreview();
        };

        this.addRenderableWidget( new StringWidget(5, prefixY + 80, 100, 20, ERR_TITLE, minecraft.fontFilterFishy) );
        errPrefix = new EditBoxHolder(5, prefixY + 100, 100, 20);
        errPrefix.hold.setValue( prefixFormat(Config.ERR_PREFIX) );
        errPrefix.hold.setMaxLength(128);
        errPrefix.change = (e, isEnter) -> {
            Config.ERR_PREFIX = prefixConvert(e.hold.getValue());
            updatePreview();
        };

        this.addRenderableWidget( new StringWidget(5, prefixY + 120, 100, 20, LINES_LIMIT_TITLE, minecraft.fontFilterFishy) );
        linesLimit = new EditBoxHolder(5, prefixY + 140, 100, 20);
        linesLimit.isNumber = true;
        linesLimit.hold.setValue( Config.LOGGER_LINES_LIMIT + "" );
        linesLimit.hold.setMaxLength(6);
        linesLimit.change = (e, isEnter) -> {
            int lines = -1;
            try {
                lines = Integer.parseInt(e.hold.getValue());
            } catch (Throwable ignore) {}
            Config.LOGGER_LINES_LIMIT = lines;
            updatePreview();
        };

        preview = new MultiLineEditBoxHolder( new MultiLineEditBox.Builder().build(
                Minecraft.getInstance().fontFilterFishy, 1, 1, Component.empty()
        ) );
        updatePreview();

        addRenderableWidget( new StringWidget(width / 2 - 100, 5, 200, 20, TITLE, minecraft.fontFilterFishy) );
        this.addRenderableWidget(doneButton);
        textColor.addOnScreen();
        this.addRenderableWidget(useShadow);
        this.addRenderableWidget(warnPrefix);
        this.addRenderableWidget(errPrefix);
        this.addRenderableWidget(infoPrefix);
        this.addRenderableWidget(linesLimit);
        this.addRenderableWidget(preview);
    }

    private String prefixFormat(String prefix) {
        return prefix.replaceAll("§", "&").replaceAll("\n", "\\\\n");
    }

    private String prefixConvert(String prefix) {
        return prefix.replaceAll("&", "§").replaceAll("\\\\n", "\n");
    }

    @Override
    public boolean isPauseScreen() {
        return parent.isPauseScreen();
    }

    @Override
    public void onClose() {
        Config.save();
    }



    public static class MultiLineEditBoxHolder extends AbstractWidget {
        public MultiLineEditBox hold;

        public MultiLineEditBoxHolder(MultiLineEditBox hold) {
            super(hold.getX(), hold.getY(), hold.getWidth(), hold.getHeight(), Component.empty());
            this.hold = hold;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            hold.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            hold.updateWidgetNarration(narrationElementOutput);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean bl) {
            hold.onClick(event, bl);
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            hold.onRelease(event);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return hold.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double d, double e) {
            return hold.mouseDragged(event, d, e);
        }
    }
}
