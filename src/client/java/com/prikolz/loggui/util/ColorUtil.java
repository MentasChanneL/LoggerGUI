package com.prikolz.loggui.util;

public class ColorUtil {
    public static int fromRGBA(float r, float g, float b, float a) {
        int ir = (int)(Math.clamp(r, 0f, 1f) * 255f);
        int ig = (int)(Math.clamp(g, 0f, 1f) * 255f);
        int ib = (int)(Math.clamp(b, 0f, 1f) * 255f);
        int ia = (int)(Math.clamp(a, 0f, 1f) * 255f);
        return fromRGBA(ir, ig, ib, ia);
    }

    public static int fromRGBA(int[] rgba) {
        return fromRGBA(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static int fromRGBA(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int[] toRGBA(int argb) {
        int e1 = (argb >> 24) & 0xFF;
        int e2 = (argb >> 16) & 0xFF;
        int e3 = (argb >> 8) & 0xFF;
        int e4 = argb & 0xFF;
        return new int[]{e2, e3, e4, e1};
    }

    public static int hsvToRgb(float h, float s, float v) {
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return 0xFF000000 | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    public static float getHue(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        if (delta == 0) return 0f;

        float hue;
        if (max == r) {
            hue = 60f * (((g - b) / delta) % 6);
        } else if (max == g) {
            hue = 60f * ((b - r) / delta + 2);
        } else { // max == b
            hue = 60f * ((r - g) / delta + 4);
        }

        if (hue < 0) hue += 360f;
        return hue / 360f;
    }

    public static float[] argbToHsv(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue = 0f;
        if (delta != 0f) {
            if (max == r) {
                hue = ((g - b) / delta) % 6f;
            } else if (max == g) {
                hue = ((b - r) / delta) + 2f;
            } else {
                hue = ((r - g) / delta) + 4f;
            }
            hue /= 6f;
            if (hue < 0f) hue += 1f;
        }

        float saturation = (max == 0f) ? 0f : delta / max;

        return new float[] { hue, saturation, max };
    }

    public static String toHex(int argb, boolean includeAlpha) {
        if (includeAlpha) return String.format("#%08X", argb);
        return String.format("#%06X", argb & 0xFFFFFF);
    }
}
