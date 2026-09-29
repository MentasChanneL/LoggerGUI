package com.prikolz.loggui.screens;

import com.prikolz.loggui.Config;
import com.prikolz.loggui.LogDialog;
import com.prikolz.loggui.widget.CustomButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class LogScreen extends Screen {
    private static final Component TITLE = Component.translatable("loggui.gui.title");
    private static final Component REFRESH = Component.translatable("loggui.gui.refresh");
    private static final Component PAUSE = Component.translatable("loggui.gui.pause");
    private static final Component EDIT_BOX = Component.translatable("loggui.gui.edit_box");
    private static final Component SPLIT_TIMES = Component.translatable("loggui.gui.split_times");
    private static final Component USE_COLORS = Component.translatable("loggui.gui.use_colors");
    private static final Component CLOSE_MENU = CommonComponents.GUI_BACK;

    private static final Tooltip CLOSE_MENU_TOOLTIP = Tooltip.create(Component.translatable("loggui.gui.tooltip.close"));
    private static final Tooltip REFRESH_TOOLTIP = Tooltip.create(Component.translatable("loggui.gui.tooltip.refresh"));
    private static final Tooltip SPLIT_TIMES_TOOLTIP = Tooltip.create(
            Component.translatable("loggui.gui.tooltip.split_times")
    );
    private static final Tooltip USE_COLORS_TOOLTIP = Tooltip.create(Component.translatable("loggui.gui.tooltip.use_colors"));
    private static final Tooltip PARAMETER_TOOLTIP = Tooltip.create(Component.translatable("loggui.gui.tooltip.parameters"));

    private final Screen lastScreen;
    private boolean splitTimes;
    private boolean useColors;
    private boolean onPause;

    protected LogScreen(boolean splitTimes, boolean useColors, Screen last) {
        super(TITLE);
        this.lastScreen = last;
        this.splitTimes = splitTimes;
        this.useColors = useColors;
    }

    public LogScreen() {
        this(Config.LOGGER_SPLIT_ON_TIMES, Config.LOGGER_USE_COLORS, Minecraft.getInstance().gui.screen());
    }

    public LogScreen(Screen last) {
        this(Config.LOGGER_SPLIT_ON_TIMES, Config.LOGGER_USE_COLORS, last);
    }

    public Button closeButton;
    public Button refreshButton;
    public MultiLineEditBox editBox = null;
    public ModSettingsScreen.EditBoxHolder chatBox;
    public Checkbox splitTimesBox;
    public Checkbox useColorsBox;
    public CustomButton settingsButton;

    private int updateCD = 0;

    @Override
    protected void init() {
        clearWidgets();

        editBox = MultiLineEditBox.builder()
                .setX(this.width / 2 - (int) (this.width * 0.75) / 2)
                .setY(25)
                .setTextColor(Config.LOGGER_TEXT_COLOR)
                .setTextShadow(Config.LOGGER_TEXT_SHADOW)
                .build(minecraft.fontFilterFishy, (int) (this.width * 0.75), (int) (this.height * 0.65), EDIT_BOX);
        editBox.setLineLimit(Integer.MAX_VALUE);

        chatBox = new ModSettingsScreen.EditBoxHolder(editBox.getX() + 2, editBox.getY() + editBox.getHeight() + 2, editBox.getWidth() - 2, 10);
        chatBox.hold.setBordered(false);
        chatBox.hold.setValue("/");
        chatBox.hold.setMaxLength(256);
        chatBox.change = (e, isEnter) -> {
            if (!isEnter) return;
            String value = e.hold.getValue();
            e.hold.setValue(value.startsWith("/") ? "/" : "");
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return;
            if (value.startsWith("/")) connection.sendCommand(value.substring(1)); else connection.sendChat(value);
        };
        chatBox.hold.setFocused(true);

        refreshButton = Button.builder(onPause ? REFRESH : PAUSE, button -> {
                    onPause = !onPause;
                    if (onPause) button.setMessage(REFRESH);
                    else {
                        editBox.setScrollAmount(editBox.maxScrollAmount());
                        button.setMessage(PAUSE);
                    }
                })
                .bounds(width / 2 - 50, chatBox.getY() + chatBox.getHeight() + 6, 100, 20)
                .build();
        closeButton = Button.builder(CLOSE_MENU, button -> onClose())
                .bounds(refreshButton.getX(), refreshButton.getY() + 22, 100, 20)
                .tooltip(CLOSE_MENU_TOOLTIP)
                .build();

        splitTimesBox = Checkbox.builder(SPLIT_TIMES, minecraft.fontFilterFishy)
                .tooltip(SPLIT_TIMES_TOOLTIP)
                .selected(this.splitTimes)
                .pos(refreshButton.getX() + 120, refreshButton.getY() + 20)
                .onValueChange((checkbox, bl) -> {
                    this.splitTimes = bl;
                    Config.LOGGER_SPLIT_ON_TIMES = this.splitTimes;
                    Config.save();
                    update();
                })
                .build();

        useColorsBox = Checkbox.builder(USE_COLORS, minecraft.fontFilterFishy)
                .tooltip(USE_COLORS_TOOLTIP)
                .selected(this.useColors)
                .pos(refreshButton.getX() + 120, refreshButton.getY())
                .onValueChange((checkbox, bl) -> {
                    this.useColors = bl;
                    Config.LOGGER_USE_COLORS = this.useColors;
                    Config.save();
                    update();
                })
                .build();

        settingsButton = CustomButton.builder()
                .size(20, 20).pos(refreshButton.getX() - 25, refreshButton.getY())
                .sprites("log_dialog:params_0", "log_dialog:params_0", "log_dialog:params_1").onClick(() -> {
                    this.minecraft.setScreenAndShow(new ModSettingsScreen(this));
                }).build();
        settingsButton.setTooltip(PARAMETER_TOOLTIP);

        var textWidth = minecraft.fontFilterFishy.width(TITLE);
        addRenderableWidget(new StringWidget(width / 2 - textWidth / 2, 5, 100, 20, TITLE, minecraft.fontFilterFishy));
        addRenderableWidget(closeButton);
        addRenderableWidget(editBox);
        addRenderableWidget(chatBox);
        addRenderableWidget(splitTimesBox);
        addRenderableWidget(useColorsBox);
        addRenderableWidget(refreshButton);
        addRenderableWidget(settingsButton);
    }

    private void update() {
        init();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.lastScreen);
    }

    @Override
    public boolean isPauseScreen() {
        if (lastScreen == null) return false;
        return lastScreen.isPauseScreen();
    }
/*
    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(chatBox.hold.getX() - 2, chatBox.hold.getY() - 1, chatBox.hold.getX() + chatBox.hold.getWidth() + 5, chatBox.hold.getY() + chatBox.hold.getHeight() - 1, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        super.render(guiGraphics, i, j, f);
    }

 */

    @Override
    public void tick() {
        if (this.editBox != null && updateCD-- <= 0 && !onPause) {
            updateCD = 20;
            double scroll = editBox.scrollAmount();
            if (editBox.getValue().isEmpty()) scroll = -1;
            editBox.setValue(LogDialog.readLogs(this.splitTimes, this.useColors, Config.LOGGER_LINES_LIMIT));
            editBox.setScrollAmount(scroll < 0 ? editBox.maxScrollAmount() : scroll);
        }
    }
}
