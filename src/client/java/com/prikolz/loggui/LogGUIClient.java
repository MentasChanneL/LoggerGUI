package com.prikolz.loggui;

import com.mojang.blaze3d.platform.InputConstants;
import com.prikolz.loggui.mixin.client.KeyMappingMixin;
import com.prikolz.loggui.screens.LogScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LogGUIClient implements ClientModInitializer {

	public static final KeyMapping keyConsole = new KeyMapping("key.logger", Config.KEY_BIND, KeyMapping.Category.MISC);
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
        try {
		    if (screen instanceof KeyBindsScreen) {
		    	saveRequest = true;
                return;
		    } else if(saveRequest) {
		    	saveRequest = false;
		    	Config.KEY_BIND = keyMixin.getKey().getValue();
		    	Config.save();
		    }

            int key = keyMixin.getKey().getValue();
            if (key <= 0) return;
			if ( InputConstants.isKeyDown(minecraft.getWindow(), key) ) {
				if (
						screen instanceof KeyBindsScreen ||
						screen instanceof LogScreen ||
						screen instanceof ChatScreen ||
						screen instanceof LevelLoadingScreen ||
						screen instanceof SignEditScreen ||
						screen instanceof AnvilScreen
				) return;
				Minecraft.getInstance().getSoundManager().play(
						SimpleSoundInstance.forUI(SoundEvents.VILLAGER_WORK_LIBRARIAN, 1.0F)
				);
				minecraft.setScreen( new LogScreen() );
			}
		} catch (Throwable ignore) {}
	}

    public static String readLogs(boolean splitTimes, boolean useColors, int linesLimit) {
        File logs = new File(rootFolder, "logs/latest.log");
        if (!logs.isFile()) return "Logs file latest.log not found :(\n" + logs.getPath();

        var builder = new StringBuilder();
        int linesRead = 0;

        try (RandomAccessFile file = new RandomAccessFile(logs, "r")) {
            long fileLength = file.length();
            if (fileLength == 0) return "";

            long position = fileLength - 1;
            List<String> linesList = new ArrayList<>();
            ByteBuffer currentLine = new ByteBuffer();

            while (position >= 0 && linesRead < linesLimit) {
                file.seek(position);
                position--;
                byte b = file.readByte();
                char c = (char) (b & 0xFF);
                if (c < 32 && c != '\n') continue;

                if (c == '\n') {
                    linesList.addFirst( currentLine.getString() );
                    currentLine.clear();
                    linesRead++;
                } else currentLine.add(b);
            }

            if (currentLine.isNotEmpty()) linesList.addFirst( currentLine.getString() );
            for (String line : linesList) processLine(line, builder, splitTimes, useColors);
        } catch (Throwable th) {
            return "§cLogs file read error:§r " + th.getMessage();
        }

        return builder.toString();
    }

    private static void processLine(String line, StringBuilder builder, boolean splitTimes, boolean useColors) {
        byte useColorState = 1;
        int lineIndex = builder.length();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (i == 0 && c == '[' && splitTimes) {
                builder.append('\n');
                lineIndex++;
            }

            if (useColors) {
                if (useColorState == 3 && c == ' ') {
                    builder.append("§r");
                    useColorState = 0;
                }
                if (useColorState == 2) {
                    if (c == 'w' || c == 'W') builder.insert(lineIndex, Config.WARN_PREFIX);
                    if (c == 'e' || c == 'E') builder.insert(lineIndex, Config.ERR_PREFIX);
                    if (c == 'i' || c == 'I') builder.insert(lineIndex, Config.INFO_PREFIX);
                    useColorState = 3;
                }
                if (useColorState == 1 && c == '/') useColorState = 2;
            }

            builder.append(c);
        }
        builder.append('\n');
    }

    public static class ByteBuffer {
        public byte[] array = new byte[16384];
        public int index = array.length;

        public void add(byte b) {
            index--;
            array[index] = b;
        }

        public String getString() {
            byte[] line = new byte[array.length - index];
            System.arraycopy(array, index, line, 0, line.length);
            return new String( line, StandardCharsets.UTF_8 );
        }

        public void clear() {
            index = array.length;
        }

        public boolean isNotEmpty() { return index < array.length; }
    }

}