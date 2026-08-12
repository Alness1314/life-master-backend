package com.alness.lifemaster.operations.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

@Component
public class ReportCsvWriter {
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public byte[] write(ReportDocument document) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            output.write(UTF8_BOM);
            try (CSVPrinter csv = new CSVPrinter(new OutputStreamWriter(output, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.builder().setRecordSeparator("\r\n").build())) {
                csv.printRecord("Reporte", safe(document.title()));
                csv.printRecord("Periodo", periodLabel(document.range()));
                if (document.currency() != null && !document.currency().isBlank()) {
                    csv.printRecord("Moneda", safe(document.currency()));
                }
                for (Map.Entry<String, Object> entry : document.summary().entrySet()) {
                    csv.printRecord(safe(entry.getKey()), safe(ReportValueFormatter.text(entry.getValue())));
                }
                csv.println();
                csv.printRecord(document.columns().stream().map(this::safe).toList());
                for (List<Object> row : document.rows()) {
                    csv.printRecord(row.stream().map(ReportValueFormatter::text).map(this::safe).toList());
                }
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el archivo CSV.", exception);
        }
    }

    private String periodLabel(ReportRange range) {
        return ReportValueFormatter.text(range.from()) + " - " + ReportValueFormatter.text(range.to());
    }

    private String safe(String value) {
        if (value == null || value.isEmpty()) return "";
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' ? "'" + value : value;
    }
}
