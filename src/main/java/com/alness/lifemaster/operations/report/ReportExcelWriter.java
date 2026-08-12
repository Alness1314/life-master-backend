package com.alness.lifemaster.operations.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ReportExcelWriter {
    public byte[] write(ReportDocument document) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte");
            Styles styles = styles(workbook);
            int columns = Math.max(2, document.columns().size());
            int rowIndex = 0;

            Row title = sheet.createRow(rowIndex++);
            Cell titleCell = title.createCell(0);
            titleCell.setCellValue(document.title());
            titleCell.setCellStyle(styles.title());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns - 1));

            rowIndex = metadata(sheet, rowIndex, "Periodo",
                    ReportValueFormatter.text(document.range().from()) + " - "
                            + ReportValueFormatter.text(document.range().to()), styles);
            if (document.currency() != null && !document.currency().isBlank()) {
                rowIndex = metadata(sheet, rowIndex, "Moneda", document.currency(), styles);
            }
            for (Map.Entry<String, Object> entry : document.summary().entrySet()) {
                Row row = sheet.createRow(rowIndex++);
                Cell label = row.createCell(0);
                label.setCellValue(entry.getKey());
                label.setCellStyle(styles.summaryLabel());
                setValue(row.createCell(1), entry.getValue(), styles);
            }
            rowIndex++;

            int headerRow = rowIndex;
            Row header = sheet.createRow(rowIndex++);
            for (int index = 0; index < document.columns().size(); index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(document.columns().get(index));
                cell.setCellStyle(styles.header());
            }
            for (List<Object> values : document.rows()) {
                Row row = sheet.createRow(rowIndex++);
                for (int index = 0; index < values.size(); index++) {
                    setValue(row.createCell(index), values.get(index), styles);
                }
            }

            sheet.createFreezePane(0, headerRow + 1);
            if (!document.rows().isEmpty()) {
                sheet.setAutoFilter(new CellRangeAddress(headerRow, rowIndex - 1, 0, document.columns().size() - 1));
            }
            for (int index = 0; index < columns; index++) {
                sheet.autoSizeColumn(index);
                sheet.setColumnWidth(index, Math.min(sheet.getColumnWidth(index) + 800, 55 * 256));
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el archivo Excel.", exception);
        }
    }

    private int metadata(Sheet sheet, int rowIndex, String name, String value, Styles styles) {
        Row row = sheet.createRow(rowIndex++);
        Cell label = row.createCell(0);
        label.setCellValue(name);
        label.setCellStyle(styles.summaryLabel());
        row.createCell(1).setCellValue(value);
        return rowIndex;
    }

    private void setValue(Cell cell, Object value, Styles styles) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof BigDecimal decimal) {
            cell.setCellValue(decimal.doubleValue());
            cell.setCellStyle(styles.number());
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            cell.setCellStyle(styles.number());
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date);
            cell.setCellStyle(styles.date());
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(dateTime);
            cell.setCellStyle(styles.dateTime());
        } else if (value instanceof LocalTime time) {
            cell.setCellValue(time.toString());
        } else {
            cell.setCellValue(String.valueOf(value));
            cell.setCellStyle(styles.text());
        }
    }

    private Styles styles(Workbook workbook) {
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle title = workbook.createCellStyle();
        title.setFont(titleFont);
        title.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setAlignment(HorizontalAlignment.LEFT);

        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle summaryLabel = workbook.createCellStyle();
        summaryLabel.setFont(bold);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle header = workbook.createCellStyle();
        header.setFont(headerFont);
        header.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle number = workbook.createCellStyle();
        number.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00########"));
        CellStyle date = workbook.createCellStyle();
        date.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        CellStyle dateTime = workbook.createCellStyle();
        dateTime.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        CellStyle text = workbook.createCellStyle();
        text.setWrapText(true);
        return new Styles(title, summaryLabel, header, number, date, dateTime, text);
    }

    private record Styles(CellStyle title, CellStyle summaryLabel, CellStyle header, CellStyle number,
                          CellStyle date, CellStyle dateTime, CellStyle text) {
    }
}
