package com.example.danmarkskort;

import javafx.scene.paint.Color;

public enum ColorSheet {
    DEFAULT,
    ROAD_COASTLINE,
    ROAD_PRIMARY,
    ROAD_SECONDARY,
    ROAD_TERTIARY,
    ROAD_CYCLEWAY,
    ROAD_TRACK_PATH,
    ROAD_TREE_ROW,
    ROAD_ROUTE,
    ROAD_DEFAULT;

    public Color handlePalette(String palette) {
        return switch(palette) {
            case "midnight" -> paletteMidnight();
            default -> paletteDefault();
        };
    }

    //region Palettes
    public Color paletteDefault() {
        return switch(this) {
            case DEFAULT         -> Color.rgb(0, 74, 127, 0.2);
            case ROAD_COASTLINE  -> Color.BLACK;
            case ROAD_PRIMARY    -> Color.DARKORANGE;
            case ROAD_SECONDARY  -> Color.DARKSLATEBLUE;
            case ROAD_TERTIARY   -> Color.DARKGREEN;
            case ROAD_CYCLEWAY   -> Color.DARKMAGENTA;
            case ROAD_TRACK_PATH -> Color.SIENNA;
            case ROAD_TREE_ROW   -> Color.rgb(172, 210, 156);
            case ROAD_ROUTE      -> Color.rgb(255, 255, 255, 0.3);
            case ROAD_DEFAULT    -> Color.rgb(100, 100, 100);
        };
    }

    public Color paletteMidnight() {
        return switch(this) {
            case DEFAULT         -> Color.rgb(25, 5, 100, 0.2);
            case ROAD_COASTLINE  -> Color.MIDNIGHTBLUE;
            case ROAD_PRIMARY    -> Color.BLUE;
            case ROAD_SECONDARY  -> Color.BLUEVIOLET;
            case ROAD_TERTIARY   -> Color.DARKSLATEBLUE;
            case ROAD_CYCLEWAY   -> Color.DARKMAGENTA;
            case ROAD_TRACK_PATH -> Color.INDIANRED;
            case ROAD_TREE_ROW   -> Color.MEDIUMSEAGREEN;
            case ROAD_ROUTE      -> Color.rgb(255, 255, 255, 0.3);
            case ROAD_DEFAULT    -> Color.rgb(48, 0, 64);
        };
    }
    //endregion
}
