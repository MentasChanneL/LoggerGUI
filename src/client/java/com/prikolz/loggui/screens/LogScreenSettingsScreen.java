package com.prikolz.loggui.screens;

import com.prikolz.loggui.Config;
import com.prikolz.loggui.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class LogScreenSettingsScreen extends Screen {

    private static final Component TITLE = Component.translatable("loggui.settings.title");
    private static final Component TEXT_SHADOW = Component.translatable("loggui.settings.text_shadow");
    private static final Component COLOR_TITLE = Component.translatable("loggui.settings.color_title");
    private static final Component RED = Component.translatable("loggui.settings.red");
    private static final Component GREEN = Component.translatable("loggui.settings.green");
    private static final Component BLUE = Component.translatable("loggui.settings.blue");
    private static final Component INFO_TITLE = Component.translatable("loggui.settings.info_title");
    private static final Component WARN_TITLE = Component.translatable("loggui.settings.warn_title");
    private static final Component ERR_TITLE = Component.translatable("loggui.settings.err_title");

    private final LogScreen parent;

    protected LogScreenSettingsScreen(@NotNull LogScreen parent) {
        super(TITLE);
        this.parent = parent;
    }

    public Button doneButton;
    public Checkbox useShadow;
    public IntSlider redSlider;
    public IntSlider greenSlider;
    public IntSlider blueSlider;
    public MultiLineEditBoxHolder preview;
    public EditBoxHolder infoPrefix;
    public EditBoxHolder warnPrefix;
    public EditBoxHolder errPrefix;

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
        doneButton = Button.builder(CommonComponents.GUI_DONE, b -> Minecraft.getInstance().setScreen(parent))
                .bounds(this.width / 2 - 100, this.height - 40, 200, 20)
                .build();
        useShadow = Checkbox.builder(TEXT_SHADOW, Minecraft.getInstance().fontFilterFishy)
                .onValueChange((ch, bl) -> {
                    Config.LOGGER_TEXT_SHADOW = bl;
                    updatePreview();
                    Config.save();
                })
                .selected( Config.LOGGER_TEXT_SHADOW )
                .pos(5, 20)
                .build();

        this.addRenderableWidget( new StringWidget(5, 45, 100, 20, COLOR_TITLE, minecraft.fontFilterFishy).alignLeft() );

        final int sliderW = 100;
        final int sliderH = 20;
        final int sliderY = 60;
        final int sliderOffset = 5;

        int[] rgba = ColorUtil.toRGBA(Config.LOGGER_TEXT_COLOR);

        redSlider = new IntSlider(5, sliderY, sliderW, sliderH, 0, 255, rgba[0], (s, i) -> {
            s.setMessage( RED.copy().append(": " + i) );
            changeTextColor();
        });
        redSlider.setMessage( RED.copy().append(": " + redSlider.intValue()) );

        greenSlider = new IntSlider(5, sliderY + sliderH + sliderOffset, sliderW, sliderH, 0, 255, rgba[1], (s, i) -> {
            s.setMessage( GREEN.copy().append(": " + i) );
            changeTextColor();
        });
        greenSlider.setMessage( GREEN.copy().append(": " + greenSlider.intValue()) );

        blueSlider = new IntSlider(5, sliderY + (sliderH + sliderOffset) * 2, sliderW, sliderH, 0, 255, rgba[2], (s, i) -> {
            s.setMessage( BLUE.copy().append(": " + i) );
            changeTextColor();
        });
        blueSlider.setMessage( BLUE.copy().append(": " + blueSlider.intValue()) );

        final int prefixY = blueSlider.getY() + 22;

        this.addRenderableWidget( new StringWidget(5, prefixY, 100, 20, INFO_TITLE, minecraft.fontFilterFishy).alignLeft() );
        infoPrefix = new EditBoxHolder(5, prefixY + 20, 100, 20);
        infoPrefix.hold.setValue( prefixFormat(Config.INFO_PREFIX) );
        infoPrefix.hold.setMaxLength(128);
        infoPrefix.change = (e) -> {
            Config.INFO_PREFIX = prefixConvert(e.hold.getValue());
            updatePreview();
            Config.save();
        };

        this.addRenderableWidget( new StringWidget(5, prefixY + 40, 100, 20, WARN_TITLE, minecraft.fontFilterFishy).alignLeft() );
        warnPrefix = new EditBoxHolder(5, prefixY + 60, 100, 20);
        warnPrefix.hold.setValue( prefixFormat(Config.WARN_PREFIX) );
        warnPrefix.hold.setMaxLength(128);
        warnPrefix.change = (e) -> {
            Config.WARN_PREFIX = prefixConvert(e.hold.getValue());
            updatePreview();
            Config.save();
        };

        this.addRenderableWidget( new StringWidget(5, prefixY + 80, 100, 20, ERR_TITLE, minecraft.fontFilterFishy).alignLeft() );
        errPrefix = new EditBoxHolder(5, prefixY + 100, 100, 20);
        errPrefix.hold.setValue( prefixFormat(Config.ERR_PREFIX) );
        errPrefix.hold.setMaxLength(128);
        errPrefix.change = (e) -> {
            Config.ERR_PREFIX = prefixConvert(e.hold.getValue());
            updatePreview();
            Config.save();
        };

        preview = new MultiLineEditBoxHolder( new MultiLineEditBox.Builder().build(
                Minecraft.getInstance().fontFilterFishy, 1, 1, Component.empty()
        ) );
        updatePreview();

        addRenderableWidget( new StringWidget(width / 2 - 100, 5, 200, 20, TITLE, minecraft.fontFilterFishy) );
        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(useShadow);
        this.addRenderableWidget(redSlider);
        this.addRenderableWidget(greenSlider);
        this.addRenderableWidget(blueSlider);
        this.addRenderableWidget(warnPrefix);
        this.addRenderableWidget(errPrefix);
        this.addRenderableWidget(infoPrefix);
        this.addRenderableWidget(preview);
    }

    private String prefixFormat(String prefix) {
        return prefix.replaceAll("§", "&").replaceAll("\n", "\\\\n");
    }

    private String prefixConvert(String prefix) {
        return prefix.replaceAll("&", "§").replaceAll("\\\\n", "\n");
    }

    private void changeTextColor() {
        Config.LOGGER_TEXT_COLOR = ColorUtil.fromRGBA( redSlider.intValue(), greenSlider.intValue(), blueSlider.intValue(), 255 );
        updatePreview();
        Config.save();
    }

    @Override
    public boolean isPauseScreen() {
        return parent.isPauseScreen();
    }

    public static class EditBoxHolder extends AbstractWidget {
        public EditBox hold;
        public OnChange change;

        public EditBoxHolder(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty());
            hold = new EditBox(Minecraft.getInstance().fontFilterFishy, x, y, w, h, Component.empty());
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
            hold.renderWidget(guiGraphics, i, j, f);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            hold.updateWidgetNarration(narrationElementOutput);
        }

        @Override
        public void onClick(double d, double e) {
            hold.onClick(d, e);
        }

        @Override
        public void onRelease(double d, double e) {
            hold.onRelease(d, e);
        }

        @Override
        public boolean keyPressed(int i, int j, int k) {
            boolean result = hold.keyPressed(i, j, k);
            if (result && change != null) change.onChange(this);
            return result;
        }

        @Override
        public boolean charTyped(char c, int i) {
            boolean result = hold.charTyped(c, i);
            if (result && change != null) change.onChange(this);
            return result;
        }

        @Override
        public void setFocused(boolean bl) { hold.setFocused(bl); }

        public interface OnChange {
            void onChange(EditBoxHolder holder);
        }
    }

    public static class MultiLineEditBoxHolder extends AbstractWidget {

        public MultiLineEditBox hold;

        public MultiLineEditBoxHolder(MultiLineEditBox hold) {
            super(hold.getX(), hold.getY(), hold.getWidth(), hold.getHeight(), Component.empty());
            this.hold = hold;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
            hold.renderWidget(guiGraphics, i, j, f);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            hold.updateWidgetNarration(narrationElementOutput);
        }

        @Override
        public void onRelease(double d, double e) {
            hold.onRelease(d, e);
        }
    }

    public static class IntSlider extends AbstractSliderButton {

        private final OnChange onChange;
        private final int min;
        private final int max;

        public IntSlider(int x, int y, int w, int h, int min, int max, int instance, @NotNull OnChange onChange) {
            super(x, y, w, h, Component.empty(), (double) instance / Math.abs(max - min));
            this.onChange = onChange;
            this.min = min;
            this.max = max;
        }

        @Override
        protected void updateMessage() {}

        @Override
        protected void applyValue() {
            onChange.onChange(this, intValue());
        }

        public int intValue() { return (int) (this.value * Math.abs(max - min) - min); }

        public interface OnChange {
            void onChange(IntSlider slider, int value);
        }
    }
}
