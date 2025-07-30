package com.prikolz.loggui.mixin.client;

import com.prikolz.loggui.LogGUIClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public class OptionsMixin {

	@Mutable
	@Shadow
	private KeyMapping[] keyMappings;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V")
	private void init(Minecraft minecraft, File file, CallbackInfo info) {
		keyMappings = ArrayUtils.addAll(keyMappings, LogGUIClient.keyConsole);
	}
}