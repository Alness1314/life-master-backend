package com.alness.lifemaster.operations.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

class ReportExportWritersTests {
    private final ReportDocument report = sampleReport();

    @Test
    void csvIncludesUtf8BomMetadataHeadersAndEscapedFormulaValues() {
        byte[] bytes = new ReportCsvWriter().write(report);
        assertThat(bytes).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        String csv = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        assertThat(csv)
                .contains("Reporte,Reporte de gastos")
                .contains("Periodo,01/08/2026 - 31/08/2026")
                .contains("Fecha,Descripción,Importe")
                .contains("'=SUM(A1:A2)");
    }

    @Test
    void xlsxContainsTypedCellsAndCanBeOpened() throws Exception {
        byte[] bytes = new ReportExcelWriter().write(report);
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Reporte");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Reporte de gastos");
            assertThat(sheet.getRow(sheet.getLastRowNum()).getCell(2).getCellType())
                    .isEqualTo(CellType.NUMERIC);
            assertThat(sheet.getRow(sheet.getLastRowNum()).getCell(2).getNumericCellValue()).isEqualTo(100.00);
        }
        writeVerificationFile("reporte-gastos.xlsx", bytes);
    }

    @Test
    void pdfContainsReportDataAndCanBeOpened() throws Exception {
        byte[] bytes = new ReportPdfWriter().write(report);
        assertThat(new String(bytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        try (PDDocument pdf = Loader.loadPDF(bytes)) {
            assertThat(pdf.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            String text = new PDFTextStripper().getText(pdf);
            assertThat(text).contains("Reporte de gastos", "Periodo:", "Descripción", "1250.75");
        }
        writeVerificationFile("reporte-gastos.pdf", bytes);
    }

    private ReportDocument sampleReport() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Movimientos", 2);
        summary.put("Gasto total", new BigDecimal("1350.75"));
        return new ReportDocument(
                "Reporte de gastos",
                new ReportRange(ReportPeriod.MONTHLY, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)),
                "MXN",
                summary,
                List.of("Fecha", "Descripción", "Importe"),
                List.of(
                        List.of(LocalDate.of(2026, 8, 10), "Comida", new BigDecimal("1250.75")),
                        List.of(LocalDate.of(2026, 8, 11), "=SUM(A1:A2)", new BigDecimal("100.00"))));
    }

    private void writeVerificationFile(String name, byte[] content) throws Exception {
        Path directory = Path.of("target", "report-export-verification");
        Files.createDirectories(directory);
        Files.write(directory.resolve(name), content);
    }
}
