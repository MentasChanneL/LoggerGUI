package com.prikolz.loggui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

public class CustomButton extends AbstractWidget {

    public WidgetSprites sprites;
    public Runnable onClick;
    public SimpleSoundInstance sound;

    public CustomButton(int x, int y, int w, int h, Runnable onClick, SimpleSoundInstance sound, WidgetSprites sprites) {
        super(x, y, w, h, Component.empty());
        this.onClick = onClick;
        this.sound = sound;
        this.sprites = sprites;
    }

    @Override
    public void onClick(double d, double e) {
        if (sound != null) Minecraft.getInstance().getSoundManager().play(sound);
        if (onClick != null) onClick.run();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprites.get(this.active, this.isHoveredOrFocused()),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(this.alpha)
        );
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int w = 0;
        private int h = 0;
        private WidgetSprites sprites = new WidgetSprites(
                ResourceLocation.withDefaultNamespace("widget/button"),
                ResourceLocation.withDefaultNamespace("widget/button_disabled"),
                ResourceLocation.withDefaultNamespace("widget/button_highlighted")
        );
        private Runnable run = null;
        private Holder.Reference<SoundEvent> soundHolder = SoundEvents.UI_BUTTON_CLICK;
        private SoundEvent sound = null;

        public Builder size(int w, int h) {
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder sprites(String tex1, String tex2, String tex3) {
            sprites = new WidgetSprites(
                    ResourceLocation.parse(tex1),
                    ResourceLocation.parse(tex2),
                    ResourceLocation.parse(tex3)
            );
            return this;
        }

        public Builder sound(SoundEvent sound) {
            this.sound = sound;
            soundHolder = null;
            return this;
        }

        public Builder sound(Holder.Reference<SoundEvent> sound) {
            this.soundHolder = sound;
            this.sound = null;
            return this;
        }

        public Builder onClick(Runnable run) {
            this.run = run;
            return this;
        }

        public CustomButton build() {
            SimpleSoundInstance simpleSound = null;
            if (sound == null) {
                if (soundHolder != null) simpleSound = SimpleSoundInstance.forUI(soundHolder, 1f);
            } else simpleSound = SimpleSoundInstance.forUI(sound, 1f);
            return new CustomButton(this.x, this.y, this.w, this.h, this.run, simpleSound, sprites);
        }
    }
}
