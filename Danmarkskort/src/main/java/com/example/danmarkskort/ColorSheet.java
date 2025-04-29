package com.example.danmarkskort;

import javafx.scene.paint.Color;

public enum ColorSheet {
    //region values
    ROAD_COASTLINE,
    ROAD_PRIMARY,
    ROAD_SECONDARY,
    ROAD_TERTIARY,
    ROAD_CYCLEWAY,
    ROAD_TRACK_PATH,
    ROAD_TREE_ROW,
    ROAD_ROUTE,
    ROAD_DEFAULT,

    POLY_COMMERCIAL,
    POLY_CONSTRUCTION,
    POLY_EDUCATION,
    POLY_FAIRGROUND,
    POLY_INDUSTRIAL,
    POLY_RESIDENTIAL,
    POLY_RETAIL,
    POLY_INSTITUTIONAL,
    POLY_AQUACULTURE,
    POLY_ALLOTMENTS,
    POLY_FARMLAND,
    POLY_FARMYARD,
    POLY_PADDY,
    POLY_ANIMAL_KEEPING,
    POLY_FLOWER_BED,
    POLY_FOREST,
    POLY_GREENHOUSE_HORTICULTURE,
    POLY_LOGGING,
    POLY_MEADOW,
    POLY_ORCHARD,
    POLY_PLANT_NURSERY,
    POLY_VINEYARD,
    POLY_BASIN,
    POLY_SALT_POND,
    POLY_BROWNFIELD,
    POLY_CEMETERY,
    POLY_DEPOT,
    POLY_GARAGES,
    POLY_GRASS,
    POLY_GREENFIELD,
    POLY_LANDFILL,
    POLY_MILITARY,
    POLY_PORT,
    POLY_QUARRY,
    POLY_RAILWAY,
    POLY_RECREATION_GROUND,
    POLY_RELIGIOUS,
    POLY_VILLAGE_GREEN,
    POLY_GREENERY,
    POLY_WINTER_SPORTS,
    POLY_GRASSLAND,
    POLY_HEATH,
    POLY_SCRUB,
    POLY_TREE,
    POLY_TREE_ROW,
    POLY_TREE_STUMP,
    POLY_TUNDRA,
    POLY_WOOD,
    POLY_BAY,
    POLY_BEACH,
    POLY_BLOWHOLE,
    POLY_CAPE,
    POLY_CREVASSE,
    POLY_GEYSER,
    POLY_GLACIER,
    POLY_HOT_SPRING,
    POLY_ISTHMUS,
    POLY_MUD,
    POLY_PENINSULA,
    POLY_REEF,
    POLY_SHINGLE,
    POLY_SHOAL,
    POLY_SPRING,
    POLY_STRAIT,
    POLY_WATER,
    POLY_WETLAND,
    POLY_ARETE,
    POLY_BARE_ROCK,
    POLY_CAVE_ENTRANCE,
    POLY_CLIFF,
    POLY_EARTH_BANK,
    POLY_FUMAROLE,
    POLY_GULLY,
    POLY_PEAK,
    POLY_RIDGE,
    POLY_SADDLE,
    POLY_SAND,
    POLY_SCREE,
    POLY_VOLCANO,
    POLY_ADULT_GAMING_CENTRE,
    POLY_AMUSEMENT_ARCADE,
    POLY_BANDSTAND,
    POLY_BATHING_PLACE,
    POLY_BEACH_RESORT,
    POLY_BIRD_HIDE,
    POLY_BLEACHERS,
    POLY_BOWLING_ALLEY,
    POLY_COMMON,
    POLY_DANCE,
    POLY_DISC_GOLF_COURSE,
    POLY_DOG_PARK,
    POLY_ESCAPE_GAME,
    POLY_FIREPIT,
    POLY_FISHING,
    POLY_FITNESS_CENTRE,
    POLY_FITNESS_STATION,
    POLY_GARDEN,
    POLY_GOLF_COURSE,
    POLY_HACKERSPACE,
    POLY_HORSE_RIDING,
    POLY_ICE_RINK,
    POLY_MARINA,
    POLY_MINIATURE_GOLF,
    POLY_NATURE_RESERVE,
    POLY_OUTDOOR_SEATING,
    POLY_PARK,
    POLY_SPORT,
    POLY_PICNIC_TABLE,
    POLY_PITCH,
    POLY_PLAYGROUND,
    POLY_RESORT,
    POLY_SAUNA,
    POLY_SLIPWAY,
    POLY_SPORTS_CENTRE,
    POLY_SPORTS_HALL,
    POLY_STADIUM,
    POLY_SUMMER_CAMP,
    POLY_SWIMMING_AREA,
    POLY_SWIMMING_POOL,
    POLY_TANNING_SALON,
    POLY_TRACK,
    POLY_TRAMPOLINE_PARK,
    POLY_WATER_PARK,
    POLY_WILDLIFE_HIDE,
    POLY_BREAKWATER,
    POLY_BRIDGE,
    POLY_GROYNE,
    POLY_PIER,
    POLY_WASTEWATER_PLANT,
    POLY_WATERWORKS,
    POLY_AERODROME,
    POLY_APRON,
    POLY_RUNWAY,
    POLY_TERMINAL,
    POLY_ISLAND,
    POLY_ISOLATED_DWELLING,
    POLY_AMENITY,
    POLY_AREAHIGHWAY,
    POLY_ATTRACTION,
    POLY_BARRIER,
    POLY_BOUNDARY,
    POLY_BRIDGESUPPORT,
    POLY_BUILDING,
    POLY_CAIRN,
    POLY_COASTLINE,
    POLY_EMBANKMENT,
    POLY_FERRY,
    POLY_HIGHWAY,
    POLY_HISTORIC,
    POLY_INDOOR,
    POLY_MAST,
    POLY_POWER,
    POLY_ROCK,
    POLY_SILO,
    POLY_STAGE,
    POLY_STONE,
    POLY_STORAGE_TANK,
    POLY_SURFACE,
    POLY_SQUARE,
    POLY_TOURISM,
    POLY_WATERWAY,
    POLY_CITYRINGEN,
    POLY_SHRUBBERY,
    POLY_FENCE_TYPE,
    POLY_FLOWERBED,
    POLY_ROUTE,
    POLY_YES,
    POLY_PAVED,
    POLY_forestOesterled,
    POLY_scrubStrandvejenStrandvejen,
    POLY_DEFAULT;
    //endregion

    public Color handlePalette(String palette) {
        return switch(palette) {
            case "midnight" -> paletteMidnight();
            case "basic"    -> paletteBasic();
            default         -> paletteDefault();
        };
    }

    //region Palettes
    public Color paletteDefault() {
        return switch(this) {
            //region road colors
            case ROAD_COASTLINE  -> Color.BLACK;
            case ROAD_PRIMARY    -> Color.DARKORANGE;
            case ROAD_SECONDARY  -> Color.SLATEBLUE;
            case ROAD_TERTIARY   -> Color.DARKGREEN;
            case ROAD_CYCLEWAY   -> Color.DARKMAGENTA;
            case ROAD_TRACK_PATH -> Color.SIENNA;
            case ROAD_TREE_ROW   -> Color.rgb(172, 210, 156);
            case ROAD_ROUTE      -> Color.rgb(255, 255, 255, 0.3);
            case ROAD_DEFAULT    -> Color.rgb(100, 100, 100);
            //endregion
            //region landuse: developed-land
            case POLY_COMMERCIAL    -> Color.rgb(242, 217, 216);
            case POLY_CONSTRUCTION  -> Color.rgb(199, 199, 180);
            case POLY_EDUCATION     -> Color.RED;
            case POLY_FAIRGROUND    -> Color.RED;
            case POLY_INDUSTRIAL    -> Color.rgb(235, 219, 233);
            case POLY_RESIDENTIAL   -> Color.rgb(225, 225, 225);
            case POLY_RETAIL        -> Color.rgb(225, 213, 208);
            case POLY_INSTITUTIONAL -> Color.RED;
            //endregion
            //region landuse: rural-and-agricultural
            case POLY_AQUACULTURE    -> Color.RED;
            case POLY_ALLOTMENTS     -> Color.rgb(201, 225, 191);
            case POLY_FARMLAND       -> Color.rgb(238, 240, 213);
            case POLY_FARMYARD       -> Color.rgb(239, 213, 179);
            case POLY_PADDY          -> Color.RED;
            case POLY_ANIMAL_KEEPING -> Color.RED;
            case POLY_FLOWER_BED     -> Color.rgb(205, 235, 176);
            case POLY_FOREST         -> Color.rgb(172, 210, 156);
            case POLY_LOGGING        -> Color.RED;
            case POLY_GREENHOUSE_HORTICULTURE -> Color.rgb(238, 240, 213);
            case POLY_MEADOW         -> Color.rgb(205, 235, 176);
            case POLY_ORCHARD        -> Color.rgb(172, 224, 161);
            case POLY_PLANT_NURSERY  -> Color.rgb(172, 224, 161);
            case POLY_VINEYARD       -> Color.rgb(172, 224, 161);
            //endregion
            //region landuse: water
            case POLY_BASIN     -> Color.rgb(170, 211, 223);
            case POLY_SALT_POND -> Color.rgb(170, 211, 223);
            //endregion
            //region landuse: other
            case POLY_BROWNFIELD        -> Color.rgb(199, 199, 180);
            case POLY_CEMETERY          -> Color.rgb(170, 203, 175);
            case POLY_DEPOT             -> Color.RED;
            case POLY_GARAGES           -> Color.rgb(222, 221, 204);
            case POLY_GRASS             -> Color.rgb(205, 235, 176);
            case POLY_GREENFIELD        -> Color.rgb(241, 238, 232);
            case POLY_LANDFILL          -> Color.rgb(182, 182, 144);
            case POLY_MILITARY          -> Color.rgb(154, 46, 47, 0.3);
            case POLY_PORT              -> Color.RED;
            case POLY_QUARRY            -> Color.rgb(196, 194, 194);
            case POLY_RAILWAY           -> Color.rgb(235, 219, 233);
            case POLY_RECREATION_GROUND -> Color.rgb(223, 252, 226);
            case POLY_RELIGIOUS         -> Color.rgb(205, 204, 201);
            case POLY_VILLAGE_GREEN     -> Color.rgb(205, 235, 176);
            case POLY_GREENERY          -> Color.RED;
            case POLY_WINTER_SPORTS     -> Color.RED;
            //endregion

            //region natural: vegetation
            case POLY_GRASSLAND  -> Color.rgb(205, 235, 176);
            case POLY_HEATH      -> Color.rgb(214, 217, 159);
            case POLY_SCRUB      -> Color.rgb(200, 215, 171);
            case POLY_TREE       -> Color.rgb(173, 212, 175);
            case POLY_TREE_ROW   -> Color.rgb(169, 206, 161);
            case POLY_TREE_STUMP -> Color.RED;
            case POLY_TUNDRA     -> Color.RED;
            case POLY_WOOD       -> Color.rgb(172, 210, 156);
            //endregion
            //region natural: water
            case POLY_BAY        -> Color.rgb(170, 211, 223);
            case POLY_BEACH      -> Color.rgb(255, 241, 186);
            case POLY_BLOWHOLE   -> Color.RED;
            case POLY_CAPE       -> Color.RED;
            case POLY_CREVASSE   -> Color.RED;
            case POLY_GEYSER     -> Color.RED;
            case POLY_GLACIER    -> Color.rgb(221, 236, 236);
            case POLY_HOT_SPRING -> Color.RED;
            case POLY_ISTHMUS    -> Color.RED;
            case POLY_MUD        -> Color.rgb(230, 220, 210);
            case POLY_PENINSULA  -> Color.RED;
            case POLY_REEF       -> Color.RED;
            case POLY_SHINGLE    -> Color.rgb(237, 228, 220);
            case POLY_SHOAL      -> Color.rgb(255, 241, 186);
            case POLY_SPRING     -> Color.rgb(122, 188, 236);
            case POLY_STRAIT     -> Color.rgb(170, 211, 223);
            case POLY_WATER      -> Color.rgb(170, 211, 223);
            case POLY_WETLAND    -> Color.rgb(223, 235, 248);
            //endregion
            //region natural: geology
            case POLY_ARETE         -> Color.rgb(242, 239, 233);
            case POLY_BARE_ROCK     -> Color.rgb(237, 228, 220);
            case POLY_CAVE_ENTRANCE -> Color.rgb(0, 0, 0);
            case POLY_CLIFF         -> Color.rgb(242, 239, 233);
            case POLY_EARTH_BANK    -> Color.RED;
            case POLY_FUMAROLE      -> Color.RED;
            case POLY_GULLY         -> Color.RED;
            case POLY_PEAK          -> Color.rgb(208, 143, 85);
            case POLY_RIDGE         -> Color.rgb(242, 239, 233);
            case POLY_SADDLE        -> Color.rgb(208, 143, 85);
            case POLY_SAND          -> Color.rgb(245, 233, 198);
            case POLY_SCREE         -> Color.rgb(237, 228, 220);
            case POLY_VOLCANO       -> Color.rgb(212, 0, 0);
            //endregion

            //region leisure
            case POLY_ADULT_GAMING_CENTRE -> Color.RED;
            case POLY_AMUSEMENT_ARCADE    -> Color.rgb(14, 133, 23);
            case POLY_BANDSTAND           -> Color.RED;
            case POLY_BATHING_PLACE       -> Color.RED;
            case POLY_BEACH_RESORT        -> Color.rgb(14, 133, 23);
            case POLY_BIRD_HIDE           -> Color.rgb(14, 133, 23);
            case POLY_BLEACHERS           -> Color.rgb(116, 190, 161);
            case POLY_BOWLING_ALLEY       -> Color.rgb(14, 133, 23);
            case POLY_COMMON              -> Color.RED;
            case POLY_DANCE               -> Color.rgb(14, 133, 23);
            case POLY_DISC_GOLF_COURSE    -> Color.DEEPPINK;
            case POLY_DOG_PARK            -> Color.rgb(224, 252, 227);
            case POLY_ESCAPE_GAME         -> Color.RED;
            case POLY_FIREPIT             -> Color.rgb(115, 74, 8);
            case POLY_FISHING             -> Color.rgb(14, 133, 23);
            case POLY_FITNESS_CENTRE      -> Color.rgb(14, 133, 23);
            case POLY_FITNESS_STATION     -> Color.rgb(14, 133, 23);
            case POLY_GARDEN              -> Color.rgb(206, 236, 178);
            case POLY_GOLF_COURSE         -> Color.rgb(181, 226, 181);
            case POLY_HACKERSPACE         -> Color.RED;
            case POLY_HORSE_RIDING        -> Color.RED;
            case POLY_ICE_RINK            -> Color.rgb(222, 237, 237);
            case POLY_MARINA              -> Color.rgb(139, 173, 228, 0.3);
            case POLY_MINIATURE_GOLF      -> Color.rgb(222, 246, 192);
            case POLY_NATURE_RESERVE      -> Color.rgb(157, 199, 159, 0.3);
            case POLY_OUTDOOR_SEATING     -> Color.rgb(14, 133, 23);
            case POLY_PARK                -> Color.rgb(205, 247, 201);
            case POLY_PICNIC_TABLE        -> Color.rgb(115, 74, 8);
            case POLY_PITCH               -> Color.rgb(136, 224, 190);
            case POLY_PLAYGROUND          -> Color.rgb(14, 133, 23);
            case POLY_RESORT              -> Color.rgb(140, 220, 255, 0.3);
            case POLY_SAUNA               -> Color.rgb(14, 133, 23);
            case POLY_SLIPWAY             -> Color.rgb(0, 146, 128);
            case POLY_SPORTS_CENTRE       -> Color.rgb(223, 252, 226);
            case POLY_SPORTS_HALL         -> Color.RED;
            case POLY_STADIUM             -> Color.rgb(161, 219, 166, 0.3);
            case POLY_SUMMER_CAMP         -> Color.RED;
            case POLY_SWIMMING_AREA       -> Color.rgb(14, 133, 23);
            case POLY_SWIMMING_POOL       -> Color.rgb(106, 177, 197);
            case POLY_TANNING_SALON       -> Color.RED;
            case POLY_TRACK               -> Color.rgb(136, 224, 190);
            case POLY_TRAMPOLINE_PARK     -> Color.RED;
            case POLY_WATER_PARK          -> Color.rgb(14, 133, 23);
            case POLY_WILDLIFE_HIDE       -> Color.RED;
            //endregion

            //region man_made
            case POLY_BREAKWATER       -> Color.rgb(184, 184, 184);
            case POLY_BRIDGE           -> Color.rgb(184, 184, 184);
            case POLY_GROYNE           -> Color.rgb(153, 153, 153);
            case POLY_PIER             -> Color.rgb(243, 239, 233);
            case POLY_WASTEWATER_PLANT -> Color.rgb(235, 219, 233);
            case POLY_WATERWORKS       -> Color.rgb(235, 219, 233);
            //endregion

            //region aeroway
            case POLY_AERODROME -> Color.rgb(233, 231, 226);
            case POLY_APRON     -> Color.rgb(218, 218, 224);
            case POLY_RUNWAY    -> Color.rgb(187, 187, 204);
            case POLY_TERMINAL  -> Color.rgb(196, 182, 171);
            //endregion

            //region place
            case POLY_ISLAND -> Color.rgb(242, 239, 233);
            case POLY_ISOLATED_DWELLING -> Color.rgb(242, 239, 233);
            //endregion

            //region other
            case POLY_AMENITY        -> Color.rgb(196, 182, 171);
            case POLY_AREAHIGHWAY   -> Color.rgb(50, 50, 50, 0.3);
            case POLY_ATTRACTION     -> Color.rgb(239, 213, 179, 0.3);
            case POLY_BARRIER        -> Color.rgb(111, 111, 111, 0.3);
            case POLY_BOUNDARY       -> Color.TRANSPARENT; /*Color.rgb(207, 155, 203, 0.3);*/
            case POLY_BRIDGESUPPORT -> Color.rgb(111, 111, 111, 0.3);
            case POLY_BUILDING       -> Color.rgb(217, 208, 201);
            case POLY_CAIRN          -> Color.TAN;
            case POLY_COASTLINE      -> Color.rgb(242, 239, 233);
            case POLY_EMBANKMENT     -> Color.rgb(91, 127, 0, 0.3);
            case POLY_FERRY          -> Color.rgb(125, 138, 245, 0.3);
            case POLY_HIGHWAY        -> Color.rgb(50, 50, 50, 0.3);
            case POLY_HISTORIC       -> Color.rgb(115, 74, 8, 0.3);
            case POLY_INDOOR         -> Color.rgb(158, 148, 140);
            case POLY_MAST           -> Color.WHITE;
            case POLY_POWER          -> Color.rgb(227, 204, 223);
            case POLY_ROCK           -> Color.GREY;
            case POLY_SILO           -> Color.STEELBLUE;
            case POLY_STAGE          -> Color.STEELBLUE;
            case POLY_STONE          -> Color.DARKGREY;
            case POLY_STORAGE_TANK   -> Color.STEELBLUE;
            case POLY_SURFACE        -> Color.DARKGREY;
            case POLY_SQUARE         -> Color.STEELBLUE;
            case POLY_TOURISM        -> Color.rgb(222, 246, 192);
            case POLY_WATERWAY       -> Color.TRANSPARENT;
            //endregion

            //region patches
            case POLY_CITYRINGEN -> Color.TRANSPARENT;
            case POLY_SHRUBBERY  -> Color.rgb(199, 199, 180);
            case POLY_FENCE_TYPE -> Color.rgb(158, 148, 140, 0.3);
            case POLY_FLOWERBED  -> Color.RED;
            case POLY_ROUTE      -> Color.RED;
            case POLY_SPORT      -> Color.RED;
            case POLY_YES        -> Color.TRANSPARENT;
            case POLY_PAVED      -> Color.RED;
            case POLY_forestOesterled -> Color.RED;
            case POLY_scrubStrandvejenStrandvejen -> Color.RED;
            case POLY_DEFAULT         -> Color.rgb(0, 74, 127, 0.2);
            //endregion
        };
    }

    public Color paletteMidnight() {
        return switch(this) {
            case ROAD_COASTLINE  -> Color.ANTIQUEWHITE;
            case ROAD_PRIMARY    -> Color.WHEAT;
            case ROAD_SECONDARY  -> Color.TAN;
            case ROAD_TERTIARY   -> Color.SANDYBROWN;
            case ROAD_CYCLEWAY   -> Color.THISTLE;
            case ROAD_TRACK_PATH -> Color.INDIANRED;
            case ROAD_TREE_ROW   -> Color.MEDIUMSEAGREEN;
            case ROAD_ROUTE      -> Color.rgb(255, 255, 255, 0.3);
            case ROAD_DEFAULT    -> Color.LIGHTGREY;
            case POLY_BOUNDARY       -> Color.TRANSPARENT;
            default              -> Color.rgb(255, 255, 255, 0.2);
        };
    }

    public Color paletteBasic() {
        return switch(this) {
            case ROAD_COASTLINE -> Color.BLACK;
            case ROAD_PRIMARY    -> Color.RED.darker();
            case ROAD_SECONDARY  -> Color.CHARTREUSE;
            case ROAD_TERTIARY   -> Color.BLUE;
            case ROAD_CYCLEWAY   -> Color.ORANGE;
            case ROAD_TRACK_PATH -> Color.MAGENTA;
            case ROAD_TREE_ROW   -> Color.LIGHTGREEN;
            case ROAD_ROUTE      -> Color.rgb(255, 255, 255, 0.3);
            case ROAD_DEFAULT    -> Color.DIMGREY;
            case POLY_BOUNDARY       -> Color.TRANSPARENT;
            default -> Color.rgb(206, 169, 128, 0.4);
        };
    }
    //endregion
}
