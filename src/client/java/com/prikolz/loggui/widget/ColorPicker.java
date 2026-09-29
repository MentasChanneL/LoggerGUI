package com.prikolz.loggui.widget;

import com.mojang.blaze3d.platform.NativeImage;
import com.prikolz.loggui.LogDialog;
import com.prikolz.loggui.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

import static com.prikolz.loggui.LogDialog.MOD_ID;

public class ColorPicker extends AbstractWidget {
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/block");
    public static final Identifier CURSOR_TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/cursor");
    public static final Identifier SLIDER_TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/slider");
    public static final Identifier SLIDER_OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(MOD_ID, "color_picker/slider_overlay");

    public ColorState color;
    public Consumer<Integer> onPick;
    public Palette palette;
    public boolean mouseIsDown = false;
    public boolean clickedInPalette = false;
    public boolean clickedInSlider = false;

    public ColorPicker(
            int x,
            int y,
            int width,
            int height,
            Component message,
            int initColor,
            Consumer<Integer> onPick
    ) {
        super(Math.max(x, 4), Math.max(y, 4), width, height, message);
        this.onPick = onPick;
        float hue = ColorUtil.getHue(initColor);
        this.palette = new HSVPalette((int) (width * 0.75), (int) (height * 0.84375), hue);
        this.color = new ColorState(initColor);
    }

    public int[] getPaletteBox() {
        int x1 = (int) (getX() + width * (5d / 64d));
        int y1 = (int) (getY() + height * (5d / 64d));
        return new int[] {
                x1,
                y1,
                x1 + palette.width,
                y1 + palette.height
        };
    }

    public int[] getSliderBox() {
        return new int[] {
                (int) (getX() + width * (55d / 64d)),
                (int) (getY() + height * (4d / 64d)),
                (int) (getX() + width * (61d / 64d)),
                (int) (getY() + height * (60d / 64d))
        };
    }

    public boolean isInside(int[] box, double x, double y) {
        return x >= box[0] && y >= box[1] && x <= box[2] && y <= box[3];
    }

    public int[] posInBox(int[] box, int x, int y) {
        var cursorX = x;
        var cursorY = y;
        if (cursorX < box[0]) cursorX = box[0];
        if (cursorY < box[1]) cursorY = box[1];
        if (cursorX >= box[2]) cursorX = box[2] - 1;
        if (cursorY >= box[3]) cursorY = box[3] - 1;
        cursorX -= box[0];
        cursorY -= box[1];
        return new int[] { cursorX, cursorY };
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), width, height);
        var paletteBox = getPaletteBox();
        var sliderBox = getSliderBox();
        int x = paletteBox[0];
        int y = paletteBox[1];
        graphics.blit(palette.id, x, y, paletteBox[2], paletteBox[3], 0f, 1f, 0f, 1f);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                CURSOR_TEXTURE,
                x + color.cursorX - 1,
                y + color.cursorY - 1,
                4,
                4
        );
        int sliderX = (int) (getX() + width * (56d / 64d));
        int lineH = sliderBox[3] - sliderBox[1];
        int sliderY = sliderBox[1] + (int)(lineH * color.hue);
        int sliderW = (int)(width * (5d / 64d));
        int sliderH = (int)(height * (4d / 64d));
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SLIDER_TEXTURE,
                sliderX,
                sliderY - sliderH / 2,
                sliderW,
                sliderH
        );
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SLIDER_OVERLAY_TEXTURE,
                sliderX,
                sliderY - sliderH / 2,
                sliderW,
                sliderH,
                ColorUtil.hsvToRgb(color.hue, 1f, 1f)
        );
    }

    public void onClick(double x, double y) {
        if (clickedInPalette) {
            var box = getPaletteBox();
            var pos = posInBox(box, (int) x, (int) y);
            color.color = palette.texture.getPixels().getPixel(pos[0], pos[1]);
            color.cursorX = pos[0];
            color.cursorY = pos[1];
            color.update(false);
            onPick.accept(color.color);
            return;
        }
        if (clickedInSlider) {
            var box = getSliderBox();
            var pos = posInBox(box, (int) x, (int) y);
            var height = box[3] - box[1];
            color.hue = (float) pos[1] / height;
            palette.update(color.hue);
            color.color = palette.texture.getPixels().getPixel(color.cursorX, color.cursorY);
            color.update(false);
            onPick.accept(color.color);
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (event.button() != 1) return;
        onClick(event.x(), event.y());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 1) return true;
        double x = event.x();
        double y = event.y();
        clickedInPalette = isInside(getPaletteBox(), x, y);
        if (!clickedInPalette) clickedInSlider = isInside(getSliderBox(), x, y);
        mouseIsDown = true;
        onClick(x, y);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        mouseIsDown = false;
        clickedInPalette = false;
        return super.mouseReleased(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    public class ColorState {
        public int color;
        public float hue;
        public int cursorX;
        public int cursorY;

        public ColorState(int color) {
            this.color = color;
            hue = ColorUtil.getHue(color);
            update(true);
        }
        public void update(boolean updatePos) {
            var rPos = palette.pickPixel(color, hue);
            if (updatePos) {
                cursorX = (int) ((palette.width - 1) * rPos[0]);
                cursorY = (int) ((palette.height - 1) * rPos[1]);
            }
            setTooltip(Tooltip.create(
                    Component.literal("\n\na").setStyle(
                            Style.EMPTY
                                    .withColor(color)
                                    .withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(MOD_ID, "gui")))
                    )
            ));
        }
    }

    public static abstract class Palette {
        public final Identifier id;
        public DynamicTexture texture;
        public final int width;
        public final int height;

        public Palette(String id, int width, int height, float hue) {
            this.width = width;
            this.height = height;
            this.id = Identifier.fromNamespaceAndPath(
                    MOD_ID, "dynamic/palette_" + id
            );
            update(hue);
        }

        public void update(float hue) {
            var image = new NativeImage(width, height, false);
            generate(image, hue);
            this.texture = new DynamicTexture(
                    id::toString,
                    image
            );
            Minecraft.getInstance().getTextureManager().register(this.id, this.texture);
        }

        public void generate(NativeImage image, float hue) {
            for (int y = 0; y < height; y++) {
                var rY = y / (float) height;
                for (int x = 0; x < width; x++) {
                    var rX = x / (float) width;
                    image.setPixel(x, y, putPixel(rX, rY, hue));
                }
            }
        }

        public abstract int putPixel(float x, float y, float hue);

        public abstract float[] pickPixel(int argb, float hue);
    }

    public static class HSVPalette extends Palette {
        public HSVPalette(int width, int height, float hue) {
            super("default", width, height, hue);
        }

        @Override
        public int putPixel(float x, float y, float hue) {
            return ColorUtil.hsvToRgb(hue, x, 1f - y);
        }

        @Override
        public float[] pickPixel(int argb, float hue) {
            float[] hsv = ColorUtil.argbToHsv(argb);
            float x = hsv[1];
            float y = 1f - hsv[2];
            return new float[] { x, y };
        }
    }
}
