package com.example.hangmanproject.controller;

import com.example.hangmanproject.dto.ReportResponse;
import com.example.hangmanproject.service.PdfReportService;
import com.example.hangmanproject.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    public ReportController(ReportService reportService, PdfReportService pdfReportService) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping
    public ReportResponse getReport(
            @RequestParam(defaultValue = "1") int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate) {
        return reportService.generateReport(months, anchorDate);
    }

    @GetMapping("/download-pdf")
    public ResponseEntity<InputStreamResource> downloadPdf(
            @RequestParam(defaultValue = "1") int months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchorDate) {

        ReportResponse report = reportService.generateReport(months, anchorDate);
        ByteArrayInputStream pdfStream = pdfReportService.generatePdfReport(report);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=report-" + months + "-months.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}