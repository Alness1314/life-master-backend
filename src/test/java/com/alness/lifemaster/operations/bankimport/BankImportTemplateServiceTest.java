package com.alness.lifemaster.operations.bankimport;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class BankImportTemplateServiceTest {
    private static final String[] HEADERS = { "fecha", "descripcion", "importe", "tipo", "moneda" };

    private final BankImportTemplateService service = new BankImportTemplateService();

    @Test
    void createsCsvTemplateWithRequiredHeaders() {
        String content = new String(service.csv(), StandardCharsets.UTF_8);

        assertThat(content).isEqualTo("fecha,descripcion,importe,tipo,moneda\r\n");
    }

    @Test
    void createsExcelTemplateWithRequiredHeadersAndInstructions() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(service.excel()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("Movimientos");
            assertThat(IntStream.range(0, HEADERS.length)
                    .mapToObj(index -> workbook.getSheetAt(0).getRow(0).getCell(index).getStringCellValue()))
                    .containsExactly(HEADERS);
            assertThat(workbook.getSheet("Instrucciones").getRow(0).getCell(0).getStringCellValue())
                    .contains("sin cambiar los encabezados");
            assertThat(workbook.getSheetAt(0).getDataValidations()).hasSize(2);
        }
    }
}
