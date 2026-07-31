package com.alness.lifemaster.operations.bankimport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.common.currency.CurrencyCode;

@Component
class BankStatementFileParser {
    private static final Set<String> DATE_HEADERS = Set.of("date", "fecha");
    private static final Set<String> DESCRIPTION_HEADERS = Set.of(
            "description", "descripcion", "concepto", "movimiento", "detalle");
    private static final Set<String> AMOUNT_HEADERS = Set.of("amount", "monto", "importe");
    private static final Set<String> TYPE_HEADERS = Set.of("type", "tipo", "naturaleza");
    private static final Set<String> CURRENCY_HEADERS = Set.of("currency", "moneda", "divisa");
    private static final Map<String, MovementType> TYPES = Map.ofEntries(
            Map.entry("EXPENSE", MovementType.EXPENSE),
            Map.entry("GASTO", MovementType.EXPENSE),
            Map.entry("CARGO", MovementType.EXPENSE),
            Map.entry("DEBIT", MovementType.EXPENSE),
            Map.entry("DEBITO", MovementType.EXPENSE),
            Map.entry("INCOME", MovementType.INCOME),
            Map.entry("INGRESO", MovementType.INCOME),
            Map.entry("ABONO", MovementType.INCOME),
            Map.entry("CREDIT", MovementType.INCOME),
            Map.entry("CREDITO", MovementType.INCOME));

    ParsedBankStatement parse(byte[] content, String originalFilename, String defaultCurrency) {
        normalizeCurrency(defaultCurrency);
        return switch (detectFormat(content, originalFilename)) {
            case "CSV" -> parseCsv(content);
            case "EXCEL" -> parseExcel(content);
            default -> throw new IllegalArgumentException("Formato no compatible. Usa CSV, XLS o XLSX.");
        };
    }

    private ParsedBankStatement parseCsv(byte[] content) {
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true).build().parse(reader)) {
            Map<String, String> headers = resolveHeaders(parser.getHeaderMap().keySet());
            List<BankMovement> rows = new java.util.ArrayList<>();
            List<BankImportFailurePreview> failures = new java.util.ArrayList<>();
            for (CSVRecord record : parser) {
                int rowNumber = Math.toIntExact(record.getRecordNumber() + 1);
                try {
                    rows.add(toMovement(
                            rowNumber,
                            record.get(headers.get("date")),
                            record.get(headers.get("description")),
                            record.get(headers.get("amount")),
                            record.get(headers.get("type")),
                            record.get(headers.get("currency"))));
                } catch (RuntimeException exception) {
                    failures.add(new BankImportFailurePreview(
                            rowNumber,
                            safeCsvValue(record, headers.get("description")),
                            failureReason(exception)));
                }
            }
            requireRows(rows, failures, "El archivo CSV no contiene movimientos.");
            return new ParsedBankStatement("CSV", List.copyOf(rows), List.copyOf(failures), List.of());
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible leer el archivo CSV.", exception);
        }
    }

    private ParsedBankStatement parseExcel(byte[] content) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("El archivo Excel no contiene hojas.");
            }
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = firstNonEmptyRow(sheet);
            if (headerRow == null) {
                throw new IllegalArgumentException("El archivo Excel está vacío.");
            }
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Map<String, Integer> rawHeaders = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                rawHeaders.put(formatter.formatCellValue(cell), cell.getColumnIndex());
            }
            Map<String, String> headers = resolveHeaders(rawHeaders.keySet());
            Map<String, Integer> indexes = new HashMap<>();
            headers.forEach((key, value) -> indexes.put(key, rawHeaders.get(value)));
            List<BankMovement> movements = new java.util.ArrayList<>();
            List<BankImportFailurePreview> failures = new java.util.ArrayList<>();
            for (int rowNumber = headerRow.getRowNum() + 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                Row row = sheet.getRow(rowNumber);
                if (row == null || isEmpty(row, formatter)) {
                    continue;
                }
                try {
                    movements.add(toMovement(
                            rowNumber + 1,
                            cell(row, indexes.get("date"), formatter),
                            cell(row, indexes.get("description"), formatter),
                            cell(row, indexes.get("amount"), formatter),
                            cell(row, indexes.get("type"), formatter),
                            cell(row, indexes.get("currency"), formatter)));
                } catch (RuntimeException exception) {
                    failures.add(new BankImportFailurePreview(
                            rowNumber + 1,
                            cell(row, indexes.get("description"), formatter),
                            failureReason(exception)));
                }
            }
            requireRows(movements, failures, "El archivo Excel no contiene movimientos.");
            return new ParsedBankStatement(
                    "EXCEL", List.copyOf(movements), List.copyOf(failures), List.of());
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible leer el archivo Excel.", exception);
        }
    }

    private Map<String, String> resolveHeaders(Set<String> rawHeaders) {
        Map<String, String> result = new HashMap<>();
        for (String header : rawHeaders) {
            String normalized = normalize(header);
            if (DATE_HEADERS.contains(normalized)) result.put("date", header);
            if (DESCRIPTION_HEADERS.contains(normalized)) result.put("description", header);
            if (AMOUNT_HEADERS.contains(normalized)) result.put("amount", header);
            if (TYPE_HEADERS.contains(normalized)) result.put("type", header);
            if (CURRENCY_HEADERS.contains(normalized)) result.put("currency", header);
        }
        if (!result.keySet().containsAll(Set.of("date", "description", "amount", "type", "currency"))) {
            throw new IllegalArgumentException(
                    "Se requieren las columnas fecha, descripción, importe, tipo y moneda.");
        }
        return result;
    }

    private BankMovement toMovement(int rowNumber, String date, String description,
            String amount, String type, String currency) {
        return new BankMovement(rowNumber, parseDate(date), validDescription(description), parseAmount(amount).abs(),
                parseType(type), normalizeCurrency(currency));
    }

    private LocalDate parseDate(String value) {
        String date = value.trim();
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/uuuu"), DateTimeFormatter.ofPattern("dd-MM-uuuu"))) {
            try {
                return LocalDate.parse(date, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported bank date format.
            }
        }
        throw new IllegalArgumentException("Fecha inválida.");
    }

    private BigDecimal parseAmount(String value) {
        try {
            String amount = value.replace("$", "").replace(" ", "").trim();
            if (amount.contains(",") && amount.contains(".")) {
                amount = amount.lastIndexOf(',') > amount.lastIndexOf('.')
                        ? amount.replace(".", "").replace(',', '.')
                        : amount.replace(",", "");
            } else if (amount.matches("[-+]?\\d{1,3}(,\\d{3})+")) {
                amount = amount.replace(",", "");
            } else {
                amount = amount.replace(',', '.');
            }
            BigDecimal parsed = new BigDecimal(amount);
            if (parsed.signum() == 0) {
                throw new IllegalArgumentException("El importe debe ser mayor que cero.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El importe no es válido.");
        }
    }

    private MovementType parseType(String value) {
        MovementType type = TYPES.get(normalize(value).toUpperCase(Locale.ROOT));
        if (type == null) {
            throw new IllegalArgumentException("Tipo inválido.");
        }
        return type;
    }

    private String normalizeCurrency(String value) {
        String currency = value == null ? "MXN" : value.trim().toUpperCase(Locale.ROOT);
        if (!CurrencyCode.supports(currency)) {
            throw new IllegalArgumentException("Moneda no compatible.");
        }
        return currency;
    }

    private String validDescription(String value) {
        String description = value == null ? "" : value.trim();
        if (description.isEmpty() || description.length() > 256) {
            throw new IllegalArgumentException("Descripción inválida.");
        }
        return description;
    }

    private String detectFormat(byte[] content, String filename) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (name.endsWith(".xlsx") || name.endsWith(".xls")
                || (content.length >= 4 && content[0] == (byte) 0xD0 && content[1] == (byte) 0xCF)
                || (content.length >= 2 && content[0] == 'P' && content[1] == 'K')) {
            return "EXCEL";
        }
        if (name.endsWith(".csv")) {
            return "CSV";
        }
        throw new IllegalArgumentException("Formato no compatible. Usa CSV, XLS o XLSX.");
    }

    private Row firstNonEmptyRow(Sheet sheet) {
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        for (Row row : sheet) {
            if (!isEmpty(row, formatter)) return row;
        }
        return null;
    }

    private boolean isEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) return false;
        }
        return true;
    }

    private String cell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell);
    }

    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private String safeCsvValue(CSVRecord record, String header) {
        try {
            String value = record.get(header);
            return value == null || value.isBlank() ? "Sin descripción" : value.trim();
        } catch (RuntimeException exception) {
            return "Sin descripción";
        }
    }

    private String failureReason(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "La fila contiene datos no válidos."
                : exception.getMessage();
    }

    private void requireRows(List<BankMovement> movements, List<BankImportFailurePreview> failures, String message) {
        if (movements.isEmpty() && failures.isEmpty()) throw new IllegalArgumentException(message);
    }
}
