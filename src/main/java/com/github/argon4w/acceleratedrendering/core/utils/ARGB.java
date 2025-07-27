package com.github.argon4w.acceleratedrendering.core.utils;

import net.minecraft.util.Mth;

public class ARGB {
    public static int alpha(int packedColor) {
        return packedColor >>> 24;
    }

    public static int red(int packedColor) {
        return packedColor >> 16 & 255;
    }

    public static int green(int packedColor) {
        return packedColor >> 8 & 255;
    }

    public static int blue(int packedColor) {
        return packedColor & 255;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    public static int multiply(int packedColourOne, int packedColorTwo) {
        return color(alpha(packedColourOne) * alpha(packedColorTwo) / 255, red(packedColourOne) * red(packedColorTwo) / 255, green(packedColourOne) * green(packedColorTwo) / 255, blue(packedColourOne) * blue(packedColorTwo) / 255);
    }

    public static int lerp(float delta, int min, int max) {
        int i = Mth.lerpInt(delta, alpha(min), alpha(max));
        int j = Mth.lerpInt(delta, red(min), red(max));
        int k = Mth.lerpInt(delta, green(min), green(max));
        int l = Mth.lerpInt(delta, blue(min), blue(max));
        return color(i, j, k, l);
    }

    public static int opaque(int color) {
        return color | -16777216;
    }

    public static int color(int alpha, int color) {
        return alpha << 24 | color & 16777215;
    }

    public static int average(int color1, int color2) {
        return color((alpha(color1) + alpha(color2)) / 2, (red(color1) + red(color2)) / 2, (green(color1) + green(color2)) / 2, (blue(color1) + blue(color2)) / 2);
    }
}
