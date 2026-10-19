package com.example.hangmanproject.service;

import com.example.hangmanproject.dto.ReportResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfReportService {

    public ByteArrayInputStream generatePdfReport(ReportResponse report) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Hangman Analytics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Subtitle
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC);
            Paragraph sub = new Paragraph("Period: " + report.startDate() + " to " + report.endDate() + " (" + report.periodMonths() + " Month/s)", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            // Summary Table
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addTableCell(summaryTable, "Total Events", true);
            addTableCell(summaryTable, "Won Games", true);
            addTableCell(summaryTable, "Lost Games", true);
            addTableCell(summaryTable, "Total Points", true);

            addTableCell(summaryTable, String.valueOf(report.totalEvents()), false);
            addTableCell(summaryTable, String.valueOf(report.wonEvents()), false);
            addTableCell(summaryTable, String.valueOf(report.lostEvents()), false);
            addTableCell(summaryTable, String.valueOf(report.totalPointsEarned()), false);

            document.add(summaryTable);

            // Category Breakdown Section
            Font secFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph categoryHeading = new Paragraph("Events Breakdown by Category", secFont);
            categoryHeading.setSpacingAfter(10);
            document.add(categoryHeading);

            PdfPTable catTable = new PdfPTable(2);
            catTable.setWidthPercentage(100);

            addTableCell(catTable, "Category", true);
            addTableCell(catTable, "Event Count", true);

            if (report.eventsByCategory() != null && !report.eventsByCategory().isEmpty()) {
                report.eventsByCategory().forEach((category, count) -> {
                    addTableCell(catTable, category, false);
                    addTableCell(catTable, String.valueOf(count), false);
                });
            } else {
                addTableCell(catTable, "No category data available", false);
                addTableCell(catTable, "0", false);
            }

            document.add(catTable);

            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableCell(PdfPTable table, String text, boolean isHeader) {
        Font font = isHeader ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10) : FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        if (isHeader) {
            cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        }
        table.addCell(cell);
    }
}