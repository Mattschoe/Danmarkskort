package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.ColorSheet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static com.example.danmarkskort.ColorSheet.*;

public class Polygon implements Serializable, MapObject{
    @Serial private static final long serialVersionUID = 1444149606229887777L;

    //region Fields
    private float[] xPoints;
    private float[] yPoints;
    private int nodesSize;
    private String type; //The type of polygon, fx: "Building", "Coastline", etc.
    private String palette;
    private ColorSheet cs;
    private transient Color color;
    private float[] boundingBox;
    //endregion

    //region Constructor(s)
    /** A {@link Polygon} is a collection of {@link Node}'s with the same start- and end {@link Node}
     *  @param nodes the collection of nodes belonging to the Polygon
     */
    public Polygon(List<Node> nodes, String type) {
        assert nodes.size() != 1;
        this.type = type.intern();
        this.palette = "default";
        determineColor();

        createArrays(nodes);
        calculateBoundingBox();
    }

    ///A polygon loaded from a binary file
    public Polygon (float[] xPoints, float[] yPoints, String type) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.type = type;
        this.palette = "default";
        determineColor();
        calculateBoundingBox();
    }
    //endregion

    //region Methods
    ///Skaber to Arrays til stroke- og fillPolygon-metoderne der kaldes ved tegning
    private void createArrays(List<Node> nodes) {
        nodesSize = nodes.size();

        xPoints = new float[nodesSize];
        yPoints = new float[nodesSize];

        for (int i = 0; i < nodesSize; i++) {
            xPoints[i] = nodes.get(i).getX();
            yPoints[i] = nodes.get(i).getY();
        }
    }

    /// Standard draw method, calls the other draw-method with {@code drawLines} as true
    @Override public void draw(GraphicsContext gc) { draw(gc, false); }

    public void draw(GraphicsContext gc, boolean drawLines) {
        //Converts our array into temporary double arrays to preserve space
        double[] tempXPoints = new double[nodesSize];
        double[] tempYPoints = new double[nodesSize];

        for (int i = 0; i < nodesSize; i++) {
            tempXPoints[i] = xPoints[i];
            tempYPoints[i] = yPoints[i];
        }

        if (drawLines) {
            gc.setStroke(color.darker().darker());
            gc.strokePolygon(tempXPoints, tempYPoints, nodesSize);
        }

        gc.setFill(color);
        gc.fillPolygon(tempXPoints, tempYPoints, nodesSize);

        //TODO %% FARVER KANTEN RUNDT OM COAST-POLYGONER PÅ SAMME MÅDE SOM COAST-ROAD; might be labour intensive??
        if (type.equals("coastline")) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.5/Math.sqrt(gc.getTransform().determinant()));
            gc.strokePolygon(tempXPoints, tempYPoints, nodesSize);
            gc.setLineWidth(1/Math.sqrt(gc.getTransform().determinant()));
        }
    }
    
    public void assertColorSheetProp() {
        cs = switch(type) {
            //region landuse: developed-land
            case "commercial"    -> POLY_COMMERCIAL;
            case "construction"  -> POLY_CONSTRUCTION;
            case "education"     -> POLY_EDUCATION;
            case "fairground"    -> POLY_FAIRGROUND;
            case "industrial"    -> POLY_INDUSTRIAL;
            case "residential"   -> POLY_RESIDENTIAL;
            case "retail"        -> POLY_RETAIL;
            case "institutional" -> POLY_INSTITUTIONAL;
            //endregion
            //region landuse: rural-and-agricultural
            case "aquaculture"    -> POLY_AQUACULTURE;
            case "allotments"     -> POLY_ALLOTMENTS;
            case "farmland"       -> POLY_FARMLAND;
            case "farmyard"       -> POLY_FARMYARD;
            case "paddy"          -> POLY_PADDY;
            case "animal_keeping" -> POLY_ANIMAL_KEEPING;
            case "flower_bed"     -> POLY_FLOWER_BED;
            case "forest"         -> POLY_FOREST;
            case "logging"        -> POLY_LOGGING;
            case "greenhouse_horticulture" -> POLY_GREENHOUSE_HORTICULTURE;
            case "meadow"         -> POLY_MEADOW;
            case "orchard"        -> POLY_ORCHARD;
            case "plant_nursery"  -> POLY_PLANT_NURSERY;
            case "vineyard"       -> POLY_VINEYARD;
            //endregion
            //region landuse: water
            case "basin"     -> POLY_BASIN;
            case "salt_pond" -> POLY_SALT_POND;
            //endregion
            //region landuse: other
            case "brownfield"        -> POLY_BROWNFIELD;
            case "cemetery"          -> POLY_CEMETERY;
            case "depot"             -> POLY_DEPOT;
            case "garages"           -> POLY_GARAGES;
            case "grass"             -> POLY_GRASS;
            case "greenfield"        -> POLY_GREENFIELD;
            case "landfill"          -> POLY_LANDFILL;
            case "military"          -> POLY_MILITARY;
            case "port"              -> POLY_PORT;
            case "quarry"            -> POLY_QUARRY;
            case "railway"           -> POLY_RAILWAY;
            case "recreation_ground" -> POLY_RECREATION_GROUND;
            case "religious"         -> POLY_RELIGIOUS;
            case "village_green"     -> POLY_VILLAGE_GREEN;
            case "greenery"          -> POLY_GREENERY;
            case "winter_sports"     -> POLY_WINTER_SPORTS;
            //endregion

            //region natural: vegetation
            case "grassland"  -> POLY_GRASSLAND;
            case "heath"      -> POLY_HEATH;
            case "scrub"      -> POLY_SCRUB;
            case "tree"       -> POLY_TREE;
            case "tree_row"   -> POLY_TREE_ROW;
            case "tree_stump" -> POLY_TREE_STUMP;
            case "tundra"     -> POLY_TUNDRA;
            case "wood"       -> POLY_WOOD;
            //endregion
            //region natural: water
            case "bay"        -> POLY_BAY;
            case "beach"      -> POLY_BEACH;
            case "blowhole"   -> POLY_BLOWHOLE;
            case "cape"       -> POLY_CAPE;
            case "crevasse"   -> POLY_CREVASSE;
            case "geyser"     -> POLY_GEYSER;
            case "glacier"    -> POLY_GLACIER;
            case "hot_spring" -> POLY_HOT_SPRING;
            case "isthmus"    -> POLY_ISTHMUS;
            case "mud"        -> POLY_MUD;
            case "peninsula"  -> POLY_PENINSULA;
            case "reef"       -> POLY_REEF;
            case "shingle"    -> POLY_SHINGLE;
            case "shoal"      -> POLY_SHOAL;
            case "spring"     -> POLY_SPRING;
            case "strait"     -> POLY_STRAIT;
            case "water"      -> POLY_WATER;
            case "wetland"    -> POLY_WETLAND;
            //endregion
            //region natural: geology
            case "arete"         -> POLY_ARETE;
            case "bare_rock"     -> POLY_BARE_ROCK;
            case "cave_entrance" -> POLY_CAVE_ENTRANCE;
            case "cliff"         -> POLY_CLIFF;
            case "earth_bank"    -> POLY_EARTH_BANK;
            case "fumarole"      -> POLY_FUMAROLE;
            case "gully"         -> POLY_GULLY;
            case "peak"          -> POLY_PEAK;
            case "ridge"         -> POLY_RIDGE;
            case "saddle"        -> POLY_SADDLE;
            case "sand"          -> POLY_SAND;
            case "scree"         -> POLY_SCREE;
            case "volcano"       -> POLY_VOLCANO;
            //endregion

            //region leisure
            case "adult_gaming_centre" -> POLY_ADULT_GAMING_CENTRE;
            case "amusement_arcade"    -> POLY_AMUSEMENT_ARCADE;
            case "bandstand"           -> POLY_BANDSTAND;
            case "bathing_place"       -> POLY_BATHING_PLACE;
            case "beach_resort"        -> POLY_BEACH_RESORT;
            case "bird_hide"           -> POLY_BIRD_HIDE;
            case "bleachers"           -> POLY_BLEACHERS;
            case "bowling_alley"       -> POLY_BOWLING_ALLEY;
            case "common"              -> POLY_COMMON;
            case "dance"               -> POLY_DANCE;
            case "disc_golf_course"    -> POLY_DISC_GOLF_COURSE;
            case "dog_park"            -> POLY_DOG_PARK;
            case "escape_game"         -> POLY_ESCAPE_GAME;
            case "firepit"             -> POLY_FIREPIT;
            case "fishing"             -> POLY_FISHING;
            case "fitness_centre"      -> POLY_FITNESS_CENTRE;
            case "fitness_station"     -> POLY_FITNESS_STATION;
            case "garden"              -> POLY_GARDEN;
            case "golf_course"         -> POLY_GOLF_COURSE;
            case "hackerspace"         -> POLY_HACKERSPACE;
            case "horse_riding"        -> POLY_HORSE_RIDING;
            case "ice_rink"            -> POLY_ICE_RINK;
            case "marina"              -> POLY_MARINA;
            case "miniature_golf"      -> POLY_MINIATURE_GOLF;
            case "nature_reserve"      -> POLY_NATURE_RESERVE;
            case "outdoor_seating"     -> POLY_OUTDOOR_SEATING;
            case "park"                -> POLY_PARK;
            case "picnic_table"        -> POLY_PICNIC_TABLE;
            case "pitch"               -> POLY_PITCH;
            case "playground"          -> POLY_PLAYGROUND;
            case "resort"              -> POLY_RESORT;
            case "sauna"               -> POLY_SAUNA;
            case "slipway"             -> POLY_SLIPWAY;
            case "sports_centre"       -> POLY_SPORTS_CENTRE;
            case "sports_hall"         -> POLY_SPORTS_HALL;
            case "stadium"             -> POLY_STADIUM;
            case "summer_camp"         -> POLY_SUMMER_CAMP;
            case "swimming_area"       -> POLY_SWIMMING_AREA;
            case "swimming_pool"       -> POLY_SWIMMING_POOL;
            case "tanning_salon"       -> POLY_TANNING_SALON;
            case "track"               -> POLY_TRACK;
            case "trampoline_park"     -> POLY_TRAMPOLINE_PARK;
            case "water_park"          -> POLY_WATER_PARK;
            case "wildlife_hide"       -> POLY_WILDLIFE_HIDE;
            //endregion

            //region man_made
            case "breakwater"       -> POLY_BREAKWATER;
            case "bridge"           -> POLY_BRIDGE;
            case "groyne"           -> POLY_GROYNE;
            case "pier"             -> POLY_PIER;
            case "wastewater_plant" -> POLY_WASTEWATER_PLANT;
            case "waterworks"       -> POLY_WATERWORKS;
            case "quay"             -> POLY_ARETE;
            //endregion

            //region aeroway
            case "aerodrome" -> POLY_AERODROME;
            case "apron"     -> POLY_APRON;
            case "runway"    -> POLY_RUNWAY;
            case "terminal"  -> POLY_TERMINAL;
            //endregion

            //region place
            case "island" -> POLY_ISLAND;
            case "isolated_dwelling" -> POLY_ISOLATED_DWELLING;
            //endregion

            //region other
            case "amenity"        -> POLY_AMENITY;
            case "area:highway"   -> POLY_AREAHIGHWAY;
            case "attraction"     -> POLY_ATTRACTION;
            case "barrier"        -> POLY_BARRIER;
            case "boundary"       -> POLY_BOUNDARY;
            case "bridge:support" -> POLY_BRIDGESUPPORT;
            case "building"       -> POLY_BUILDING;
            case "cairn"          -> POLY_CAIRN;
            case "coastline"      -> POLY_COASTLINE;
            case "embankment"     -> POLY_EMBANKMENT;
            case "ferry"          -> POLY_FERRY;
            case "highway"        -> POLY_HIGHWAY;
            case "historic"       -> POLY_HISTORIC;
            case "indoor"         -> POLY_INDOOR;
            case "mast"           -> POLY_MAST;
            case "power"          -> POLY_POWER;
            case "rock"           -> POLY_ROCK;
            case "silo"           -> POLY_SILO;
            case "stage"          -> POLY_STAGE;
            case "stone"          -> POLY_STONE;
            case "storage_tank"   -> POLY_STORAGE_TANK;
            case "surface"        -> POLY_SURFACE;
            case "square"         -> POLY_SQUARE;
            case "tourism"        -> POLY_TOURISM;
            case "waterway"       -> POLY_WATERWAY;
            //endregion

            //region patches
            case "Cityringen" -> POLY_CITYRINGEN;
            case "shrubbery"  -> POLY_SHRUBBERY;
            case "fence_type" -> POLY_FENCE_TYPE;
            case "flowerbed"  -> POLY_FLOWERBED;
            case "route"      -> POLY_ROUTE;
            case "sport"      -> POLY_SPORT;
            case "yes"        -> POLY_YES;
            case "paved"      -> POLY_PAVED;
            case "forestØsterled" -> POLY_forestOesterled;
            case "scrubStrandvejenStrandvejen" -> POLY_scrubStrandvejenStrandvejen;
            //endregion
            default -> POLY_DEFAULT;
        };
    }

    public void determineColor() {
        assertColorSheetProp();
        color = cs.handlePalette(palette);
    }

    private void calculateBoundingBox() {
        boundingBox = new float[4];
        boundingBox[0] = Float.POSITIVE_INFINITY; //minX
        boundingBox[1] = Float.POSITIVE_INFINITY; //minY
        boundingBox[2] = Float.NEGATIVE_INFINITY; //maxX
        boundingBox[3] = Float.NEGATIVE_INFINITY; //maxY

        //Finds the lowest and highest X
        for (float x : xPoints) {
            if (x < boundingBox[0]) boundingBox[0] = x;
            if (x > boundingBox[2]) boundingBox[2] = x;
        }

        //Finds the lowest and highest Y
        for (float y : yPoints) {
            if (y < boundingBox[1]) boundingBox[1] = y;
            if (y > boundingBox[3]) boundingBox[3] = y;
        }
    }
    //endregion

    //region Getters and setters
    public String getType() { return type; }
    @Override public float[] getBoundingBox() { return boundingBox; }
    public float[] getXPoints() { return xPoints; }
    public float[] getYPoints() { return yPoints; }
    public void setType(String type) {
        this.type = type;
        determineColor();
    }
    public void setPalette(String palette) {
        this.palette = palette;
        determineColor();
    }
    //endregion
}