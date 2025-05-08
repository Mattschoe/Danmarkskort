package com.example.danmarkskort;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class PDFOutput {
    /**
     * Generates and opens a PDF describing a given route
     * @param roadsUnmodifiable the unmodifiable list of Roads in the route
     * @return the last turning direction given in the PDF, either {@code Turn left}, {@code Turn right} or {@code Continue straight}
     */
    public static String generateRoute(List<Road> roadsUnmodifiable, boolean openPDF) throws FileNotFoundException, DocumentException {
        /*
         * Kommer den første Road -i listen af Roads i ruten- bagerst,
         * fordi den sidste Road i ruten somehow er placeret først i listen...
         */
        List<Road> roads = new LinkedList<>(roadsUnmodifiable);

        Road finalRoad = roads.getFirst();
        roads.removeFirst();
        roads.add(finalRoad);

        Document document = new Document();

        String path = createFilePath(roads);
        FileOutputStream outFile = new FileOutputStream("./output/"+ path +".pdf");         //Skaber PDF'en
        PdfWriter.getInstance(document, outFile);                                                  //Laver en Writer der kan finde ud af at skrive i PDF-dokumentet (I think???)

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);    //Laver en standard-skrifttype
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK); //Laver en tyk skrifttype (til overskriften)

        document.open(); //Åbner dokumentet så vi kan begynde at skrive i det

        //Tiløjer overskriften i PDF'en
        String originRoad = roads.getFirst().getRoadName().isEmpty()? "NAMELESS PLACE" : roads.getFirst().getRoadName();
        String endingRoad = roads.getLast().getRoadName().isEmpty()?  "NAMELESS PLACE" : roads.getLast().getRoadName();
        String overskrift = originRoad +" to "+ endingRoad;
        Paragraph paragraph = new Paragraph(overskrift, boldFont);
        document.add(paragraph);
        document.add(Chunk.NEWLINE);

        //Tilføjer første vej med et bestemt prefix
        paragraph = new Paragraph("1. Start along "+originRoad, normalFont);
        document.add(paragraph);

        String direction = "";
        int step = 2; //Tilføjer alle andre veje med højre/venstre-angivelser
        for (int i=1; i < roads.size(); ++i) {
            Road road = roads.get(i);
            Road previousRoad = roads.get(i-1);

            String roadName = road.getRoadName().isEmpty()? "NAMELESS ROAD" : road.getRoadName(); //Gemmer vejnavnene
            String previousRoadName = previousRoad.getRoadName().isEmpty()? "NAMELESS ROAD" : previousRoad.getRoadName();
            if (roadName.equals(previousRoadName)) continue; //Skipper vejen hvis den har samme navn -- sker ofte fordi én vej kan være opdelt i mange mindre bidder

            //Node prevFirst = previousRoad.getNodes().get(previousRoad.getNodes().size() - 2);
            Node prevFirst = previousRoad.getNodes().getFirst();
            Node prevLast = previousRoad.getNodes().getLast();
            Node crntFirst = road.getNodes().getFirst();
            Node crntLast = road.getNodes().getLast();
            /*
             * NB! Det *kan* være at højre/venstre bliver angivet forkert. I det tilfælde vil mit
             * (OFS) gæt være, at det har at gøre med at funktionen her ikke tager højde for Roads
             * der er opdelt i mere end to dele. Det burde være et yderst sjældent edge-case og
             * jeg er helt rundtosset af at arbejde med højre/venstre-sving at the moment, så det
             * kommer jeg simpelthen ikke til at implementere med mindre det viser sig at være et
             * større og vigtigere problem end jeg har forudset. Thank you for coming to my TED talk
             */

            if (prevFirst.equals(crntFirst)) {
                direction = determineDirection(prevFirst, prevLast, crntLast);
            }
            else if (prevFirst.equals(crntLast)) {
                direction = determineDirection(prevFirst, prevLast, crntFirst);
            }
            else if (prevLast.equals(crntLast)) {
                direction = determineDirection(prevLast, prevFirst, crntFirst);
            }
            else if (prevLast.equals(crntFirst)) {
                direction = determineDirection(prevLast, prevFirst, crntLast);
            }
            else {
                document.close();
                throw new RuntimeException("Fishy vectors aren't relating as expected!");
            }

            paragraph = new Paragraph(step++ +". "+direction+" at "+roadName);
            document.add(paragraph);
        }

        //Tilføjer sidste vej med et bestemt prefix
        paragraph = new Paragraph(step +". Conclude at "+endingRoad, normalFont);
        document.add(paragraph);

        document.close(); //Lukker dokumentet; vi er færdige med at skrive

        if (openPDF) {
            try { Desktop.getDesktop().open(new File("./output/"+ path +".pdf")); } //Forsøger at åbne dokumentet
            catch (IOException e) { System.out.println("Couldn't open file! Error: "+ e.getMessage()); }
        }
        return direction;
    }

    /**
     * Determines the direction of a turn given three points.
     * The points are used to interpret the two vectors between which the angle is present.
     * @param p0 the origin-point of both vectors
     * @param p1 the end-point of the first vector
     * @param p2 the end-point of the second vector
     * @return {@code "RIGHT"}, {@code "LEFT"} or {@code "STRAIGHT"} depending on how the second vector relates to the first
     */
    private static String determineDirection(Node p0, Node p1, Node p2) {
        //Vektor a går fra p0 til p1, vektor b går fra p0 til p2
        float[] vectorA = {p1.getX()-p0.getX(), p1.getY()-p0.getY()};
        float[] vectorB = {p2.getX()-p0.getX(), p2.getY()-p0.getY()};

        float dot = vectorA[0] * - vectorB[1] + vectorA[1] * vectorB[0];
        if (dot > 0) return "Turn right";
        else if (dot < 0) return "Turn left";
        else return "Continue straight";
    }

    /**
     * Creates the file-name for a route.
     * @param roads the list of Roads in the route
     * @return the file-name for the route, w/o dots, and with dashes instead of spaces
     */
    private static String createFilePath(List<Road> roads) {
        String originRoad = roads.getFirst().getRoadName().trim().isEmpty()? "Nameless-place" : roads.getFirst().getRoadName();
        String endingRoad = roads.getLast().getRoadName().trim().isEmpty()?  "nameless-place" : roads.getLast().getRoadName();

        String path = originRoad +"-to-"+ endingRoad;        //Skaber pdf-filens filsti
        path = path.replaceAll("\\.", ""); //Fjerner alle punktummer fra filstien
        path = path.replaceAll(" ", "-");  //Udskifter mellemrum i filstien med bindestreger

        return path;
    }
}
