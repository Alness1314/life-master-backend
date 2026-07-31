package com.alness.lifemaster.operations.bankimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class BankStatementFileParserTest {

    private final BankStatementFileParser parser = new BankStatementFileParser();

    @Test
    void parsesCsvWithSpanishHeaders() {
        byte[] content = """
                fecha,descripcion,importe,tipo,moneda
                2026-07-01,Nómina,1500.50,INGRESO,MXN
                2026-07-02,Supermercado,325.10,GASTO,MXN
                """.getBytes(StandardCharsets.UTF_8);

        ParsedBankStatement result = parser.parse(content, "estado.csv", "MXN");

        assertThat(result.format()).isEqualTo("CSV");
        assertThat(result.movements()).hasSize(2);
        assertThat(result.failures()).isEmpty();
        assertThat(result.movements().get(1).type()).isEqualTo(MovementType.EXPENSE);
    }

    @Test
    void keepsValidCsvRowsAndReportsInvalidRows() {
        byte[] content = """
                fecha,descripcion,importe,tipo,moneda
                2026-07-01,Nómina,1500.50,INGRESO,MXN
                fecha-invalida,Compra,abc,GASTO,MXN
                """.getBytes(StandardCharsets.UTF_8);

        ParsedBankStatement result = parser.parse(content, "estado.csv", "MXN");

        assertThat(result.movements()).singleElement()
                .satisfies(movement -> assertThat(movement.rowNumber()).isEqualTo(2));
        assertThat(result.failures()).singleElement()
                .satisfies(failure -> {
                    assertThat(failure.rowNumber()).isEqualTo(3);
                    assertThat(failure.description()).isEqualTo("Compra");
                    assertThat(failure.reason()).isEqualTo("Fecha inválida.");
                });
    }

    @Test
    void parsesXlsxFirstSheet() throws Exception {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Movimientos");
            Row header = sheet.createRow(0);
            String[] headers = { "fecha", "concepto", "monto", "tipo", "moneda" };
            for (int index = 0; index < headers.length; index++) {
                header.createCell(index).setCellValue(headers[index]);
            }
            Row movement = sheet.createRow(1);
            movement.createCell(0).setCellValue("29/07/2026");
            movement.createCell(1).setCellValue("Pago de servicio");
            movement.createCell(2).setCellValue(450.75);
            movement.createCell(3).setCellValue("cargo");
            movement.createCell(4).setCellValue("MXN");
            workbook.write(output);
            content = output.toByteArray();
        }

        ParsedBankStatement result = parser.parse(content, "estado.xlsx", "MXN");

        assertThat(result.format()).isEqualTo("EXCEL");
        assertThat(result.movements()).singleElement()
                .satisfies(movement -> {
                    assertThat(movement.description()).isEqualTo("Pago de servicio");
                    assertThat(movement.type()).isEqualTo(MovementType.EXPENSE);
                });
    }

    @Test
    void rejectsPdfFiles() {
        byte[] content = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> parser.parse(content, "estado.pdf", "MXN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Formato no compatible. Usa CSV, XLS o XLSX.");
    }
}
