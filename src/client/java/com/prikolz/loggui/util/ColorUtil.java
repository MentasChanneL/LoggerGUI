package com.prikolz.loggui.util;

public class ColorUtil {
    public static int fromRGBA(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
    public static int[] toRGBA(int rgba) {
        int e1 = (rgba >> 24) & 0xFF;
        int e2 = (rgba >> 16) & 0xFF;
        int e3 = (rgba >> 8) & 0xFF;
        int e4 = rgba & 0xFF;
        return new int[]{e2, e3, e4, e1};
    }
}
