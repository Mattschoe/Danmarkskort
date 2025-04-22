package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.ColorSheet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static com.example.danmarkskort.ColorSheet.DEFAULT;

public class Polygon implements Serializable, MapObject{
    @Serial private static final long serialVersionUID = 1444149606229887777L;

    //region Fields
    private final List<Node> nodes;
    private float[] xPoints;
    private float[] yPoints;
    private int nodesSize;
    private String type; //The type of polygon, fx: "Building", "Coastline", etc.
    private String palette;
    private final ColorSheet cs;
    private transient Color color;
    private float[] boundingBox;
    //endregion

    //region Constructor(s)
    /** A {@link Polygon} is a collection of {@link Node}'s with the same start- and end {@link Node}
     *  @param nodes the collection of nodes belonging to the Polygon
     */
    public Polygon(List<Node> nodes, String type) {
        assert nodes.size() != 1;
        this.nodes = nodes;
        this.type = type;
        this.palette = "default";
        this.cs = DEFAULT;

        createArrays();
        determineColor2();
        calculateBoundingBox();
    }
    //endregion

    //region Methods
    ///Skaber to Arrays til stroke- og fillPolygon-metoderne der kaldes ved tegning
    private void createArrays() {
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

    public void determineColor2() {
        color = cs.handlePalette(palette);
    }

    /// Enormous switch-statement determines the Polygon's color based off its 'type'-field
    public void determineColor() {
        color = switch(type) {
            //region landuse: developed-land
            case "commercial"    -> Color.rgb(242, 217, 216);
            case "construction"  -> Color.rgb(199, 199, 180);
            case "education"     -> Color.RED;
            case "fairground"    -> Color.RED;
            case "industrial"    -> Color.rgb(235, 219, 233);
            case "residential"   -> Color.rgb(225, 225, 225);
            case "retail"        -> Color.rgb(225, 213, 208);
            case "institutional" -> Color.RED;
            //endregion
            //region landuse: rural-and-agricultural
            case "aquaculture"    -> Color.RED;
            case "allotments"     -> Color.rgb(201, 225, 191);
            case "farmland"       -> Color.rgb(238, 240, 213);
            case "farmyard"       -> Color.rgb(239, 213, 179);
            case "paddy"          -> Color.RED;
            case "animal_keeping" -> Color.RED;
            case "flower_bed"     -> Color.rgb(205, 235, 176);
            case "forest"         -> Color.rgb(172, 210, 156);
            case "logging"        -> Color.RED;
            case "greenhouse_horticulture" -> Color.rgb(238, 240, 213);
            case "meadow"         -> Color.rgb(205, 235, 176);
            case "orchard"        -> Color.rgb(172, 224, 161);
            case "plant_nursery"  -> Color.rgb(172, 224, 161);
            case "vineyard"       -> Color.rgb(172, 224, 161);
            //endregion
            //region landuse: water
            case "basin"     -> Color.rgb(170, 211, 223);
            case "salt_pond" -> Color.rgb(170, 211, 223);
            //endregion
            //region landuse: other
            case "brownfield"        -> Color.rgb(199, 199, 180);
            case "cemetery"          -> Color.rgb(170, 203, 175);
            case "depot"             -> Color.RED;
            case "garages"           -> Color.rgb(222, 221, 204);
            case "grass"             -> Color.rgb(205, 235, 176);
            case "greenfield"        -> Color.rgb(241, 238, 232);
            case "landfill"          -> Color.rgb(182, 182, 144);
            case "military"          -> Color.rgb(154, 46, 47, 0.3);
            case "port"              -> Color.RED;
            case "quarry"            -> Color.rgb(196, 194, 194);
            case "railway"           -> Color.rgb(235, 219, 233);
            case "recreation_ground" -> Color.rgb(223, 252, 226);
            case "religious"         -> Color.rgb(205, 204, 201);
            case "village_green"     -> Color.rgb(205, 235, 176);
            case "greenery"          -> Color.RED;
            case "winter_sports"     -> Color.RED;
            //endregion

            //region natural: vegetation
            case "grassland"  -> Color.rgb(205, 235, 176);
            case "heath"      -> Color.rgb(214, 217, 159);
            case "scrub"      -> Color.rgb(200, 215, 171);
            case "tree"       -> Color.rgb(173, 212, 175);
            case "tree_row"   -> Color.rgb(169, 206, 161);
            case "tree_stump" -> Color.RED;
            case "tundra"     -> Color.RED;
            case "wood"       -> Color.rgb(172, 210, 156);
            //endregion
            //region natural: water
            case "bay"        -> Color.rgb(170, 211, 223);
            case "beach"      -> Color.rgb(255, 241, 186);
            case "blowhole"   -> Color.RED;
            case "cape"       -> Color.RED;
            case "crevasse"   -> Color.RED;
            case "geyser"     -> Color.RED;
            case "glacier"    -> Color.rgb(221, 236, 236);
            case "hot_spring" -> Color.RED;
            case "isthmus"    -> Color.RED;
            case "mud"        -> Color.rgb(230, 220, 210);
            case "peninsula"  -> Color.RED;
            case "reef"       -> Color.RED;
            case "shingle"    -> Color.rgb(237, 228, 220);
            case "shoal"      -> Color.rgb(255, 241, 186);
            case "spring"     -> Color.rgb(122, 188, 236);
            case "strait"     -> Color.rgb(170, 211, 223);
            case "water"      -> Color.rgb(170, 211, 223);
            case "wetland"    -> Color.rgb(223, 235, 248);
            //endregion
            //region natural: geology
            case "arete"         -> Color.rgb(242, 239, 233);
            case "bare_rock"     -> Color.rgb(237, 228, 220);
            case "cave_entrance" -> Color.rgb(0, 0, 0);
            case "cliff"         -> Color.rgb(242, 239, 233);
            case "earth_bank"    -> Color.RED;
            case "fumarole"      -> Color.RED;
            case "gully"         -> Color.RED;
            case "peak"          -> Color.rgb(208, 143, 85);
            case "ridge"         -> Color.rgb(242, 239, 233);
            case "saddle"        -> Color.rgb(208, 143, 85);
            case "sand"          -> Color.rgb(245, 233, 198);
            case "scree"         -> Color.rgb(237, 228, 220);
            case "volcano"       -> Color.rgb(212, 0, 0);
            //endregion

            //region leisure
            case "adult_gaming_centre" -> Color.RED;
            case "amusement_arcade"    -> Color.rgb(14, 133, 23);
            case "bandstand"           -> Color.RED;
            case "bathing_place"       -> Color.RED;
            case "beach_resort"        -> Color.rgb(14, 133, 23);
            case "bird_hide"           -> Color.rgb(14, 133, 23);
            case "bleachers"           -> Color.rgb(116, 190, 161);
            case "bowling_alley"       -> Color.rgb(14, 133, 23);
            case "common"              -> Color.RED;
            case "dance"               -> Color.rgb(14, 133, 23);
            case "disc_golf_course"    -> Color.DEEPPINK;
            case "dog_park"            -> Color.rgb(224, 252, 227);
            case "escape_game"         -> Color.RED;
            case "firepit"             -> Color.rgb(115, 74, 8);
            case "fishing"             -> Color.rgb(14, 133, 23);
            case "fitness_centre"      -> Color.rgb(14, 133, 23);
            case "fitness_station"     -> Color.rgb(14, 133, 23);
            case "garden"              -> Color.rgb(206, 236, 178);
            case "golf_course"         -> Color.rgb(181, 226, 181);
            case "hackerspace"         -> Color.RED;
            case "horse_riding"        -> Color.RED;
            case "ice_rink"            -> Color.rgb(222, 237, 237);
            case "marina"              -> Color.rgb(139, 173, 228, 0.3);
            case "miniature_golf"      -> Color.rgb(222, 246, 192);
            case "nature_reserve"      -> Color.rgb(157, 199, 159, 0.3);
            case "outdoor_seating"     -> Color.rgb(14, 133, 23);
            case "park"                -> Color.rgb(205, 247, 201);
            case "picnic_table"        -> Color.rgb(115, 74, 8);
            case "pitch"               -> Color.rgb(136, 224, 190);
            case "playground"          -> Color.rgb(14, 133, 23);
            case "resort"              -> Color.rgb(140, 220, 255, 0.3);
            case "sauna"               -> Color.rgb(14, 133, 23);
            case "slipway"             -> Color.rgb(0, 146, 128);
            case "sports_centre"       -> Color.rgb(223, 252, 226);
            case "sports_hall"         -> Color.RED;
            case "stadium"             -> Color.rgb(161, 219, 166, 0.3);
            case "summer_camp"         -> Color.RED;
            case "swimming_area"       -> Color.rgb(14, 133, 23);
            case "swimming_pool"       -> Color.rgb(106, 177, 197);
            case "tanning_salon"       -> Color.RED;
            case "track"               -> Color.rgb(136, 224, 190);
            case "trampoline_park"     -> Color.RED;
            case "water_park"          -> Color.rgb(14, 133, 23);
            case "wildlife_hide"       -> Color.RED;
            //endregion

            //region man_made
            case "breakwater"       -> Color.rgb(184, 184, 184);
            case "bridge"           -> Color.rgb(184, 184, 184);
            case "groyne"           -> Color.rgb(153, 153, 153);
            case "pier"             -> Color.rgb(243, 239, 233);
            case "wastewater_plant" -> Color.rgb(235, 219, 233);
            case "waterworks"       -> Color.rgb(235, 219, 233);
            //endregion

            //region aeroway
            case "aerodrome" -> Color.rgb(233, 231, 226);
            case "apron"     -> Color.rgb(218, 218, 224);
            case "runway"    -> Color.rgb(187, 187, 204);
            case "terminal"  -> Color.rgb(196, 182, 171);
            //endregion

            //region place
            case "island" -> Color.rgb(242, 239, 233);
            case "isolated_dwelling" -> Color.rgb(242, 239, 233);
            //endregion

            //region other
            case "amenity"        -> Color.rgb(196, 182, 171);
            case "area:highway"   -> Color.rgb(50, 50, 50, 0.3);
            case "attraction"     -> Color.rgb(239, 213, 179, 0.3);
            case "barrier"        -> Color.rgb(111, 111, 111, 0.3);
            case "boundary"       -> Color.TRANSPARENT; /*Color.rgb(207, 155, 203, 0.3);*/
            case "bridge:support" -> Color.rgb(111, 111, 111, 0.3);
            case "building"       -> Color.rgb(217, 208, 201);
            case "cairn"          -> Color.TAN;
            case "coastline"      -> Color.rgb(242, 239, 233);
            case "embankment"     -> Color.rgb(91, 127, 0, 0.3);
            case "ferry"          -> Color.rgb(125, 138, 245, 0.3);
            case "highway"        -> Color.rgb(50, 50, 50, 0.3);
            case "historic"       -> Color.rgb(115, 74, 8, 0.3);
            case "indoor"         -> Color.rgb(158, 148, 140);
            case "mast"           -> Color.WHITE;
            case "power"          -> Color.rgb(227, 204, 223);
            case "rock"           -> Color.GREY;
            case "silo"           -> Color.STEELBLUE;
            case "stage"          -> Color.STEELBLUE;
            case "stone"          -> Color.DARKGREY;
            case "storage_tank"   -> Color.STEELBLUE;
            case "surface"        -> Color.DARKGREY;
            case "square"         -> Color.STEELBLUE;
            case "tourism"        -> Color.rgb(222, 246, 192);
            case "waterway"       -> Color.TRANSPARENT;
            //endregion

            //region patches
            case "Cityringen" -> Color.TRANSPARENT;
            case "shrubbery"  -> Color.rgb(199, 199, 180);
            case "fence_type" -> Color.rgb(158, 148, 140, 0.3);
            case "flowerbed"  -> Color.RED;
            case "route"      -> Color.RED;
            case "sport"      -> Color.RED;
            case "yes"        -> Color.TRANSPARENT;
            case "paved"      -> Color.RED;
            case "forestØsterled" -> Color.RED;
            case "scrubStrandvejenStrandvejen" -> Color.RED;
            //endregion
            default -> Color.rgb(0, 74, 127, 0.1);
        };
    }

    private void calculateBoundingBox() {
        boundingBox = new float[4];
        boundingBox[0] = Float.POSITIVE_INFINITY; //minX
        boundingBox[1] = Float.POSITIVE_INFINITY; //minY
        boundingBox[2] = Float.NEGATIVE_INFINITY; //maxX
        boundingBox[3]= Float.NEGATIVE_INFINITY; //maxY

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
    public String getType() { return type;            }
    public boolean hasType() { return !type.isEmpty(); }
    public void setType(String type) {
        this.type = type;
        determineColor2();
    }

    public void setPalette(String palette) {
        this.palette = palette;
        determineColor2();
    }

    @Override
    public float[] getBoundingBox() { return boundingBox; }
    //endregion
}