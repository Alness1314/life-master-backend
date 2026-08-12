package com.alness.lifemaster.operations.report;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportExportService {
    private final ReportDocumentFactory documentFactory;
    private final ReportCsvWriter csvWriter;
    private final ReportExcelWriter excelWriter;
    private final ReportPdfWriter pdfWriter;

    public ReportExportFile export(UUID userId, ReportKind kind, ReportExportFormat format,
            ReportPeriod period, LocalDate referenceDate, String currency) {
        ReportDocument document = documentFactory.create(userId, kind, period, referenceDate, currency);
        byte[] content = switch (format) {
            case CSV -> csvWriter.write(document);
            case XLSX -> excelWriter.write(document);
            case PDF -> pdfWriter.write(document);
        };
        String name = "life-master-" + kind.name().toLowerCase(Locale.ROOT) + "-"
                + document.range().from() + "-" + document.range().to() + "." + format.extension();
        return new ReportExportFile(content, format.contentType(), name);
    }
}
