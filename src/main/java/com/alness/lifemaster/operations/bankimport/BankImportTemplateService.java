package com.alness.lifemaster.operations.bankimport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
class BankImportTemplateService {
    private static final String[] HEADERS = { "fecha", "descripcion", "importe", "tipo", "moneda" };

    byte[] csv() {
        return String.join(",", HEADERS).concat("\r\n").getBytes(StandardCharsets.UTF_8);
    }

    byte[] excel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet movements = workbook.createSheet("Movimientos");
            Row header = movements.createRow(0);
            CellStyle headerStyle = headerStyle(workbook);
            for (int index = 0; index < HEADERS.length; index++) {
                header.createCell(index).setCellValue(HEADERS[index]);
                header.getCell(index).setCellStyle(headerStyle);
            }
            movements.createFreezePane(0, 1);
            movements.setColumnWidth(0, 14 * 256);
            movements.setColumnWidth(1, 42 * 256);
            movements.setColumnWidth(2, 16 * 256);
            movements.setColumnWidth(3, 16 * 256);
            movements.setColumnWidth(4, 14 * 256);
            addListValidation(movements, 3, "INGRESO", "GASTO");
            addListValidation(movements, 4, "MXN", "USD", "EUR");

            Sheet instructions = workbook.createSheet("Instrucciones");
            String[] notes = {
                    "Completa la hoja Movimientos sin cambiar los encabezados.",
                    "fecha: usa AAAA-MM-DD, DD/MM/AAAA o DD-MM-AAAA.",
                    "descripcion: concepto del movimiento, máximo 256 caracteres.",
                    "importe: número mayor que cero; no incluyas fórmulas.",
                    "tipo: INGRESO o GASTO.",
                    "moneda: MXN, USD o EUR."
            };
            for (int index = 0; index < notes.length; index++) {
                instructions.createRow(index).createCell(0).setCellValue(notes[index]);
            }
            instructions.setColumnWidth(0, 72 * 256);

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar la plantilla Excel.", exception);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private void addListValidation(Sheet sheet, int column, String... values) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        CellRangeAddressList rows = new CellRangeAddressList(1, 1000, column, column);
        DataValidation validation = helper.createValidation(constraint, rows);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Valor no válido", "Selecciona un valor de la lista.");
        sheet.addValidationData(validation);
    }
}
