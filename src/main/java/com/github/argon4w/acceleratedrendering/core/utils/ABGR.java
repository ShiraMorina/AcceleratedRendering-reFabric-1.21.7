package com.github.argon4w.acceleratedrendering.core.utils;

public class ABGR {
    public static int alpha(int packedColor) {
        return packedColor >>> 24;
    }

    public static int red(int packedColor) {
        return packedColor & 255;
    }

    public static int green(int packedColor) {
        return packedColor >> 8 & 255;
    }

    public static int blue(int packedColor) {
        return packedColor >> 16 & 255;
    }

    public static int transparent(int packedColor) {
        return packedColor & 16777215;
    }

    public static int opaque(int packedColor) {
        return packedColor | -16777216;
    }

    public static int color(int alpha, int blue, int green, int red) {
        return alpha << 24 | blue << 16 | green << 8 | red;
    }

    public static int color(int alpha, int packedColor) {
        return alpha << 24 | packedColor & 16777215;
    }

    public static int fromArgb32(int color) {
        return color & -16711936 | (color & 16711680) >> 16 | (color & 255) << 16;
    }
}
