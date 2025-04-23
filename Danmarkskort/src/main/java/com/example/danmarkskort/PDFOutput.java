package com.example.danmarkskort;

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
import java.util.Random;

public class PDFOutput {
    Document document;
    PdfWriter pdfWriter;
    Font boldFont;
    Font normalFont;

    public PDFOutput() throws DocumentException, FileNotFoundException {
        document = new Document();
        pdfWriter = PdfWriter.getInstance(document, new FileOutputStream("./output/Rutevejledning.pdf"));
        documentSettings();

        document.open();
        writeInstructions();
        document.close();
    }

    private PDFOutput(List<String> roads) throws DocumentException, FileNotFoundException {
        roads.removeIf(road -> road == null || road.isEmpty());

        document = new Document(); //Skaber et nyt dokument der kan skrives i
        String route = roads.getFirst() +"-to-"+ roads.getLast(); //Skaber pdf-filens titel
        route = route.replaceAll("\\.", "").replaceAll(" ", "-");

        String path = "./output/"+ route +".pdf"; //Angiver hvor filen skal skabes
        FileOutputStream outFile = new FileOutputStream(path);

        pdfWriter = PdfWriter.getInstance(document, outFile); //Gør at vi kan skrive i filen (tror jeg??)
        documentSettings(); //Konfigurerer 2 skrifttyper vi kan bruge til at skrive

        document.open(); //Åbner dokumentet så vi kan begynde at skrive
        //-----
        //Tilføjer en linje i dokumentet med hvor vi skal fra og hvortil (som en slags titel)
        route = roads.getFirst() +" to "+ roads.getLast();
        Paragraph paragraph = new Paragraph(route, boldFont);
        document.add(paragraph);
        document.add(Chunk.NEWLINE);

        int stepCount = 1;
        String lastRoad = roads.getFirst();

        //Tilføjer den første vej med et bestemt prefix
        paragraph = new Paragraph(stepCount++ +". Start along "+roads.getFirst(), normalFont);
        document.add(paragraph);
        roads.removeFirst();

        //Tilføjer alt mellem første og sidste vej med tilfældige prefixes
        String[] genericText = {"Continue along ", "Follow ", "Proceed on ", "Go down "};
        Random random = new Random();
        for (String road : roads) { //For hver vej tilføjes vejen og en generic "motivation"
            if (road.equals(roads.getLast())) break; //Stopper hvis der kun er én vej tilbage, så den kan få et bestemt prefix
            if (road.equals(lastRoad)) continue;

            String prefix = genericText[random.nextInt(genericText.length)];
            paragraph = new Paragraph(stepCount++ +". "+prefix+road, normalFont);
            document.add(paragraph);
            lastRoad = road;
        }

        //Tilføjer den sidste vej med et bestemt prefix
        paragraph = new Paragraph(stepCount +". Concluding at "+roads.getLast(), normalFont);
        document.add(paragraph);

        //-----
        document.close(); //Dokumentet lukkes igen

        //Forsøger at åbne dokumentet
        try { Desktop.getDesktop().open(new File(path)); }
        catch (IOException e) { System.out.println("Couldn't open file! Error: "+ e.getMessage()); }
    }

    private void writeInstructions() throws DocumentException {
        Chunk chunk = new Chunk("Hej med dig!", boldFont);
        document.add(chunk);
        chunk = new Chunk("Hej med dig!", normalFont);
        document.add(chunk);
    }

    private void documentSettings() {
        normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    }

    public static void generateRoute(List<String> roads) {
        try { new PDFOutput(roads); }
        catch (Exception e) { System.out.println(e.getMessage()); }
    }
}
