package com.example.danmarkskort;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
}
