package com.prikolz.loggui;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    public static boolean LOGGER_SPLIT_ON_TIMES = false;
    public static boolean LOGGER_USE_COLORS = true;
    public static int LOGGER_TEXT_COLOR = -1;
    public static boolean LOGGER_TEXT_SHADOW = false;
    public static int KEY_BIND = InputConstants.KEY_GRAVE;
    public static String INFO_PREFIX = "";
    public static String WARN_PREFIX = "§e";
    public static String ERR_PREFIX = "§c";

    public static void read() {
        File config = new File(LogGUIClient.rootFolder, "config/loggui/config.json");
        if (!config.isFile()) {
            LogGUI.LOGGER.info("Created default file settings");
            save();
            return;
        }
        try (FileReader reader = new FileReader(config)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            KEY_BIND = json.get("settings_key_bind").getAsInt();
            LOGGER_TEXT_SHADOW = json.get("logger_text_shadow").getAsBoolean();
            LOGGER_TEXT_COLOR = json.get("logger_text_color").getAsInt();
            LOGGER_USE_COLORS = json.get("settings_use_colors").getAsBoolean();
            LOGGER_SPLIT_ON_TIMES = json.get("settings_split_messages").getAsBoolean();
            INFO_PREFIX = json.get("logger_info_prefix").getAsString();
            WARN_PREFIX = json.get("logger_warn_prefix").getAsString();
            ERR_PREFIX = json.get("logger_err_prefix").getAsString();
        }catch (Throwable t) {
            LogGUI.LOGGER.error("Fail to read settings file: " + t.getMessage());
            save();
        }
    }

    public static void save() {
        try {
            new File(LogGUIClient.rootFolder, "config/loggui/").mkdirs();
        } catch (Throwable ignore) {}
        File config = new File(LogGUIClient.rootFolder, "config/loggui/config.json");
        JsonObject json = new JsonObject();
        json.add("logger_text_color", new JsonPrimitive(LOGGER_TEXT_COLOR));
        json.add("logger_text_shadow", new JsonPrimitive(LOGGER_TEXT_SHADOW));
        json.add("logger_info_prefix", new JsonPrimitive(INFO_PREFIX));
        json.add("logger_warn_prefix", new JsonPrimitive(WARN_PREFIX));
        json.add("logger_err_prefix", new JsonPrimitive(ERR_PREFIX));
        json.add("settings_key_bind", new JsonPrimitive(KEY_BIND));
        json.add("settings_use_colors", new JsonPrimitive(LOGGER_USE_COLORS));
        json.add("settings_split_messages", new JsonPrimitive(LOGGER_SPLIT_ON_TIMES));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(config)) {
            gson.toJson(json, writer);
        } catch (Throwable t) {
            LogGUI.LOGGER.error("Fail to save settings: " + t.getMessage());
        }
    }
}
