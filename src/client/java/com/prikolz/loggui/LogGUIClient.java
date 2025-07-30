package com.prikolz.loggui;

import com.mojang.blaze3d.platform.InputConstants;
import com.prikolz.loggui.mixin.client.KeyMappingMixin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.io.File;
import java.io.FileReader;

public class LogGUIClient implements ClientModInitializer {

	public static final KeyMapping keyConsole = new KeyMapping("key.logger", Config.KEY_BIND, "key.categories.misc");
	public static final KeyMappingMixin keyMixin = (KeyMappingMixin) keyConsole;
	public static final File rootFolder = new File(FabricLoader.getInstance().getGameDir().toUri());

	@Override
	public void onInitializeClient() {
		Config.read();
		keyConsole.setKey( InputConstants.Type.KEYSYM.getOrCreate(Config.KEY_BIND) );
		LogGUI.LOGGER.info("initialized");
	}

	private static boolean saveRequest = false;

	public static void tick() {
		var minecraft = Minecraft.getInstance();
		var screen = minecraft.screen;
		if (screen instanceof KeyBindsScreen) {
			saveRequest = true;
		} else if(saveRequest) {
			saveRequest = false;
			Config.KEY_BIND = keyMixin.getKey().getValue();
			Config.save();
		}
		try {
			if (InputConstants.isKeyDown(
					minecraft.getWindow().getWindow(),
					keyMixin.getKey().getValue())
			) {
				if (
						screen instanceof KeyBindsScreen ||
						screen instanceof LogScreen ||
						screen instanceof ChatScreen ||
						screen instanceof LevelLoadingScreen
				) return;
				Minecraft.getInstance().getSoundManager().play(
						SimpleSoundInstance.forUI(SoundEvents.VILLAGER_WORK_LIBRARIAN, 1.0F)
				);
				minecraft.setScreen( new LogScreen() );
			}
		} catch (Throwable ignore) {}
	}

	public static String readLogs(boolean splitTimes, boolean useColors) {
		File logs = new File(rootFolder, "logs/latest.log");
		if (!logs.isFile()) return "Logs file latest.log not found :(\n" + logs.getPath();
		var builder = new StringBuilder();
		boolean timeRequest = false;
		byte useColorState = 1;
		int lineIndex = 0;
		try (FileReader reader = new FileReader(logs)) {
			char[] buffer = new char[1];
			while (reader.read(buffer) > 0) {
				char c = buffer[0];
				if (c < 32 && c != '\n') continue;
				if (useColors) {
					if (useColorState == 3 && c == ' ') {
						builder.append("§r");
						useColorState = 0;
					}
					if (useColorState == 2) {
						if (c == 'w' || c == 'W') builder.insert(lineIndex + 1, "§e");
						if (c == 'e' || c == 'E') builder.insert(lineIndex + 1, "§c");
						useColorState = 3;
					}
					if (useColorState == 1 && c == '/') useColorState = 2;
				}
				if (timeRequest) {
					if (c == '[') builder.append('\n');
					timeRequest = false;
				}
				builder.append( c );
				if (c == '\n') {
					if (splitTimes) timeRequest = true;
					useColorState = 1;
					lineIndex = builder.length();
				}
			}
		} catch (Throwable th) {
			return "§cLogs file read error:§r " + th.getMessage();
		}
		return builder.toString();
	}

}