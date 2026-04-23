package com.finance.tracker.controller;

import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Экспорт финансового отчёта")
public class ExportController {

    private final ExportService exportService;
    private final SecurityUtils securityUtils;

    @GetMapping("/pdf")
    @Operation(summary = "Экспорт транзакций в PDF")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) throws Exception {
        byte[] pdf = exportService.exportPdf(securityUtils.currentUserId(), from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("finance-report-" + from + "-" + to + ".pdf")
                        .build()
        );
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
