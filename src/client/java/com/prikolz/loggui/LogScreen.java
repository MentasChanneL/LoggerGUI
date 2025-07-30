package com.prikolz.loggui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LogScreen extends Screen {

    private static final Component TITLE = Component.translatable("loggui.gui.title");
    private static final Component REFRESH = Component.translatable("loggui.gui.refresh");
    private static final Component EDIT_BOX = Component.translatable("loggui.gui.edit_box");
    private static final Component SPLIT_TIMES = Component.translatable("loggui.gui.split_times");
    private static final Component USE_COLORS = Component.translatable("loggui.gui.use_colors");

    private static final Tooltip CLOSE_MENU_TOOLTIP = Tooltip.create( Component.translatable("loggui.gui.tooltip.close") );
    private static final Tooltip REFRESH_TOOLTIP = Tooltip.create( Component.translatable("loggui.gui.tooltip.refresh") );
    private static final Tooltip SPLIT_TIMES_TOOLTIP = Tooltip.create( Component.translatable("loggui.gui.tooltip.split_times") );
    private static final Tooltip USE_COLORS_TOOLTIP = Tooltip.create( Component.translatable("loggui.gui.tooltip.use_colors") );

    private final Screen lastScreen;
    private boolean splitTimes;
    private boolean useColors;
    private double scroll;

    protected LogScreen(boolean splitTimes, boolean useColors, double scroll, Screen last) {
        super(TITLE);
        this.lastScreen = last;
        this.splitTimes = splitTimes;
        this.useColors = useColors;
        this.scroll = scroll;
    }

    public LogScreen() {
        this(Config.LOGGER_SPLIT_ON_TIMES, Config.LOGGER_USE_COLORS, -1, Minecraft.getInstance().screen);
    }

    public LogScreen(Screen last) {
        this(Config.LOGGER_SPLIT_ON_TIMES, Config.LOGGER_USE_COLORS, -1, last);
    }

    public Button closeButton;
    public Button refreshButton;
    public MultiLineEditBox editBox;
    public Checkbox splitTimesBox;
    public Checkbox useColorsBox;

    @Override
    protected void init() {
        clearWidgets();
        editBox = MultiLineEditBox.builder()
                .setX( this.width / 2 - (int) (this.width * 0.75) / 2 )
                .setY(25)
                .setTextColor(Config.LOGGER_TEXT_COLOR)
                .setTextShadow(Config.LOGGER_TEXT_SHADOW)
                .build(minecraft.fontFilterFishy, (int) (this.width * 0.75), (int) (this.height * 0.75), EDIT_BOX);
        editBox.setLineLimit(Integer.MAX_VALUE);
        editBox.setValue( LogGUIClient.readLogs(this.splitTimes, this.useColors) );
        editBox.setScrollAmount(this.scroll == -1 ? editBox.maxScrollAmount() : this.scroll);

        refreshButton = Button.builder(REFRESH, button -> {
                    this.scroll = -1;
                    init();
                })
                .bounds(width / 2 - 50, editBox.getY() + editBox.getHeight() + 10, 100, 20)
                .tooltip(REFRESH_TOOLTIP)
                .build();
        closeButton = Button.builder(Component.literal("X"), button -> onClose())
                .bounds(refreshButton.getX() - 25, refreshButton.getY(), 20, 20)
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

        addRenderableWidget( new StringWidget(width / 2 - 50, 5, 100, 20, TITLE, minecraft.fontFilterFishy) );
        addRenderableWidget(closeButton);
        addRenderableWidget(editBox);
        addRenderableWidget(splitTimesBox);
        addRenderableWidget(useColorsBox);
        addRenderableWidget(refreshButton);
    }

    private void update() {
        this.scroll = editBox.scrollAmount();
        init();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public boolean isPauseScreen() {
        if (lastScreen == null) return false;
        return lastScreen.isPauseScreen();
    }
}
