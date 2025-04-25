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
import java.util.List;

public abstract class PDFOutput {
    /**
     * Generates and opens a PDF describing a given route
     * @param roads the list Roads in the route
     */
    public static void generateRoute(List<Road> roads) throws FileNotFoundException, DocumentException {
        Document document = new Document();

        String path = createFilePath(roads);
        FileOutputStream outFile = new FileOutputStream("./output/"+ path +".pdf");         //Skaber PDF'en
        PdfWriter.getInstance(document, outFile);                                                  //Laver en Writer der kan finde ud af at skrive i PDF-dokumentet (I think???)

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);    //Laver en standard-skrifttype
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK); //Laver en tyk skrifttype (til overskriften)

        document.open(); //Åbner dokumentet så vi kan begynde at skrive i det

        //Tiløjer overskriften i PDF'en
        String overskrift = roads.getFirst().getRoadName() +" to "+ roads.getLast().getRoadName();
        Paragraph paragraph = new Paragraph(overskrift, boldFont);
        document.add(paragraph);
        document.add(Chunk.NEWLINE);

        //Tilføjer første vej med et bestemt prefix
        paragraph = new Paragraph("1. Start along "+roads.getFirst().getRoadName(), normalFont);
        document.add(paragraph);

        int step = 1; //Tilføjer alle andre veje med højre/venstre-angivelser
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

            String direction;
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
                System.out.println("prevFirst: "+prevFirst+" -- ("+prevFirst.getX()+", "+prevFirst.getY()+")");
                System.out.println("prevLast:  "+prevLast +" -- ("+prevLast.getX() +", "+prevLast.getY() +")");
                System.out.println("crntFirst: "+crntFirst+" -- ("+crntFirst.getX()+", "+crntFirst.getY()+")");
                System.out.println("crntLast:  "+crntLast +" -- ("+crntLast.getX() +", "+crntLast.getY() +")");
                throw new RuntimeException("Cursed vectors -- they do not relate as expected! :(");
            }

            paragraph = new Paragraph(step++ +". Turn "+direction+" at "+roadName);
            document.add(paragraph);
        }

        //Tilføjer sidste vej med et bestemt prefix
        paragraph = new Paragraph(step +". Conclude at "+roads.getLast().getRoadName(), normalFont);
        document.add(paragraph);

        document.close(); //Lukker dokumentet; vi er færdige med at skrive

        try { Desktop.getDesktop().open(new File("./output/"+ path +".pdf")); } //Forsøger at åbne dokumentet
        catch (IOException e) { System.out.println("Couldn't open file! Error: "+ e.getMessage()); }
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
        if (dot > 0) return "RIGHT";
        else if (dot < 0) return "LEFT";
        else return "STRAIGHT";
    }

    /**
     * Creates the file-name for a route.
     * @param roads the list of Roads in the route
     * @return the file-name for the route, w/o dots, and with dashes instead of spaces
     */
    private static String createFilePath(List<Road> roads) {
        String originRoad = roads.getFirst().getRoadName().isEmpty()? "Undefined-place" : roads.getFirst().getRoadName();
        String endingRoad = roads.getLast().getRoadName().isEmpty()?  "undefined-place" : roads.getLast().getRoadName();

        String path = originRoad +"-to-"+ endingRoad;        //Skaber pdf-filens filsti
        path = path.replaceAll("\\.", ""); //Fjerner alle punktummer fra filstien
        path = path.replaceAll(" ", "-");  //Udskifter mellemrum i filstien med bindestreger

        return path;
    }
}
