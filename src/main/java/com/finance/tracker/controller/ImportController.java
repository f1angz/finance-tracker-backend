package com.finance.tracker.controller;

import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.ImportService;
import com.finance.tracker.service.ImportService.ImportResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@Tag(name = "Import", description = "Импорт банковских выписок")
public class ImportController {

    private final ImportService importService;
    private final SecurityUtils securityUtils;

    @PostMapping(value = "/bank-statement", consumes = "multipart/form-data")
    @Operation(summary = "Импорт выписки Т-Банка (PDF)")
    public ImportResultDto importBankStatement(@RequestParam("file") MultipartFile file) throws Exception {
        return importService.importStatement(securityUtils.currentUserId(), file);
    }
}
