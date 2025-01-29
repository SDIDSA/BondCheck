package com.sdidsa.bondcheck.abs.style;

import android.graphics.Color;

import androidx.annotation.NonNull;

public class ColorPalette {
    public String background_primary;
    public String background_secondary;
    public String background_tertiary;
    public String text_primary;
    public String text_secondary;
    public String text_muted;
    public String text_error;
    public String text_positive;
    public String accent_1;

    @NonNull
    @Override
    public String toString() {
        return String.format("""
                        {
                          "background_primary": "%s",
                          "background_secondary": "%s",
                          "background_tertiary": "%s",
                          "text_primary": "%s",
                          "text_secondary": "%s",
                          "text_muted": "%s",
                          "text_error": "%s",
                          "text_positive": "%s",
                          "accent_1": "%s"
                        }""",
                background_primary, background_secondary, background_tertiary,
                text_primary, text_secondary, text_muted, text_error, text_positive,
                accent_1);
    }


    private static String colorToHex(int color) {
        return String.format("#%08X", color);
    }

    private static void colorToHSL(int color, float[] hsl) {
        float r = Color.red(color) / 255f;
        float g = Color.green(color) / 255f;
        float b = Color.blue(color) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h, s, l;
        l = (max + min) / 2;

        if (max == min) {
            h = s = 0;
        } else {
            float d = max - min;
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);

            if (max == r) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else {
                h = (r - g) / d + 4;
            }
            h *= 60;
        }

        hsl[0] = h;
        hsl[1] = s;
        hsl[2] = l;
    }

    private static int HSLToColor(float[] hsl) {
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];

        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = l - c / 2;

        float r, g, b;

        if (h < 60) {
            r = c;
            g = x;
            b = 0;
        } else if (h < 120) {
            r = x;
            g = c;
            b = 0;
        } else if (h < 180) {
            r = 0;
            g = c;
            b = x;
        } else if (h < 240) {
            r = 0;
            g = x;
            b = c;
        } else if (h < 300) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }

        return Color.argb(255,
                Math.round((r + m) * 255),
                Math.round((g + m) * 255),
                Math.round((b + m) * 255));
    }

    private static int createColorWithHue(int color, float saturation, float lightness) {
        float[] hsl = new float[3];
        colorToHSL(color, hsl);
        hsl[0] = hsl[0] % 360f; // Ensure hue is within [0, 360)
        hsl[1] = saturation;
        hsl[2] = lightness;
        return HSLToColor(hsl);
    }

    public static ColorPalette generateDarkPalette(String accentColorHex) {
        ColorPalette palette = new ColorPalette();

        // Parse accent color
        int accentColor = Color.parseColor(accentColorHex);
        accentColor = createColorWithHue(accentColor, .9f, .5f);

        // Generate background colors using accent's hue with fixed saturation and lightness
        int bgPrimary = createColorWithHue(accentColor, 0.07f, 0.14f);
        int bgSecondary = createColorWithHue(accentColor, 0.07f, 0.20f);
        int bgTertiary = createColorWithHue(accentColor, 0.13f, 0.10f);

        // Generate text colors (unchanged)
        int textPrimary = Color.WHITE;
        int textSecondary = Color.rgb(194, 182, 182);
        int textMuted = Color.rgb(153, 153, 153);

        // Error and positive colors (unchanged)
        int errorColor = Color.rgb(252, 116, 116);
        int positiveColor = Color.rgb(76, 175, 80);

        // Assign all colors with alpha
        palette.background_primary = colorToHex(bgPrimary);
        palette.background_secondary = colorToHex(bgSecondary);
        palette.background_tertiary = colorToHex(bgTertiary);

        palette.text_primary = colorToHex(textPrimary);
        palette.text_secondary = colorToHex(textSecondary);
        palette.text_muted = colorToHex(textMuted);

        palette.text_error = colorToHex(errorColor);
        palette.text_positive = colorToHex(positiveColor);

        palette.accent_1 = colorToHex(accentColor);

        return palette;
    }

    public static ColorPalette generateLightPalette(String accentColorHex) {
        ColorPalette palette = new ColorPalette();

        // Parse accent color
        int accentColor = Color.parseColor(accentColorHex);
        accentColor = createColorWithHue(accentColor, .7f, .45f);

        // Generate background colors using accent's hue with fixed saturation/lightness
        int bgPrimary = createColorWithHue(accentColor, 0.05f, 0.97f);   // Subtle tint
        int bgSecondary = createColorWithHue(accentColor, 0.07f, 0.93f); // Slightly stronger
        int bgTertiary = createColorWithHue(accentColor, 0.10f, 0.88f);  // Most noticeable

        // Text colors optimized for light theme
        int textPrimary = Color.rgb(32, 32, 32);       // Near-black
        int textSecondary = Color.rgb(90, 90, 90);     // Medium gray
        int textMuted = Color.rgb(130, 130, 130);      // Light gray

        int errorColor = Color.rgb(211, 47, 47);    // Darker red for light theme
        int positiveColor = Color.rgb(56, 142, 60);

        // Assign all colors
        palette.background_primary = colorToHex(bgPrimary);
        palette.background_secondary = colorToHex(bgSecondary);
        palette.background_tertiary = colorToHex(bgTertiary);

        palette.text_primary = colorToHex(textPrimary);
        palette.text_secondary = colorToHex(textSecondary);
        palette.text_muted = colorToHex(textMuted);

        palette.text_error = colorToHex(errorColor);
        palette.text_positive = colorToHex(positiveColor);

        palette.accent_1 = colorToHex(accentColor);

        return palette;
    }

    public static ColorPalette generatePalette(String accent, boolean dark) {
        if (dark) {
            return generateDarkPalette(accent);
        } else {
            return generateLightPalette(accent);
        }
    }
}