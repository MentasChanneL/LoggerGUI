package com.prikolz.loggui.mixin.client;

import com.prikolz.loggui.LogGUIClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    private void tick(CallbackInfo info) { LogGUIClient.tick(); }
}
