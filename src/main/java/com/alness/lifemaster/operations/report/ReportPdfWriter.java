package com.alness.lifemaster.operations.report;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class ReportPdfWriter {
    private static final Color PURPLE = new Color(103, 85, 217);
    private static final Color LIGHT_PURPLE = new Color(238, 235, 255);
    private static final Color GRID = new Color(205, 205, 215);
    private static final float MARGIN = 34;
    private static final float FONT_SIZE = 7.2f;
    private static final float LINE_HEIGHT = 9f;

    public byte[] write(ReportDocument report) {
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PdfState state = new PdfState(pdf, regular, bold, report);
            state.newPage();
            state.writeHeader();
            state.writeSummary();
            state.writeTable();
            state.closeStream();
            state.addPageNumbers();
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el archivo PDF.", exception);
        }
    }

    private static final class PdfState {
        private final PDDocument pdf;
        private final PDFont regular;
        private final PDFont bold;
        private final ReportDocument report;
        private final float pageWidth = PDRectangle.A4.getHeight();
        private final float pageHeight = PDRectangle.A4.getWidth();
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private PdfState(PDDocument pdf, PDFont regular, PDFont bold, ReportDocument report) {
            this.pdf = pdf;
            this.regular = regular;
            this.bold = bold;
            this.report = report;
        }

        private void newPage() throws IOException {
            closeStream();
            page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            pdf.addPage(page);
            stream = new PDPageContentStream(pdf, page);
            y = pageHeight - MARGIN;
        }

        private void writeHeader() throws IOException {
            stream.setNonStrokingColor(PURPLE);
            stream.addRect(MARGIN, y - 31, pageWidth - MARGIN * 2, 31);
            stream.fill();
            writeText(report.title(), MARGIN + 10, y - 20, bold, 15, Color.WHITE);
            y -= 45;
            String period = "Periodo: " + ReportValueFormatter.text(report.range().from()) + " - "
                    + ReportValueFormatter.text(report.range().to());
            if (report.currency() != null && !report.currency().isBlank()) {
                period += "    Moneda: " + report.currency();
            }
            writeText(period, MARGIN, y, regular, 9, Color.DARK_GRAY);
            y -= 18;
        }

        private void writeSummary() throws IOException {
            float cellWidth = (pageWidth - MARGIN * 2) / 4f;
            int index = 0;
            for (Map.Entry<String, Object> entry : report.summary().entrySet()) {
                if (index > 0 && index % 4 == 0) y -= 38;
                float x = MARGIN + (index % 4) * cellWidth;
                stream.setNonStrokingColor(LIGHT_PURPLE);
                stream.addRect(x, y - 29, cellWidth - 5, 29);
                stream.fill();
                writeText(fit(entry.getKey(), cellWidth - 15, bold, 7), x + 5, y - 10, bold, 7, Color.DARK_GRAY);
                writeText(fit(ReportValueFormatter.text(entry.getValue()), cellWidth - 15, bold, 10),
                        x + 5, y - 23, bold, 10, Color.BLACK);
                index++;
            }
            if (!report.summary().isEmpty()) y -= 48;
        }

        private void writeTable() throws IOException {
            if (report.columns().isEmpty()) return;
            float[] widths = columnWidths(report.columns().size());
            writeTableHeader(widths);
            for (List<Object> row : report.rows()) {
                List<List<String>> lines = new ArrayList<>();
                int maxLines = 1;
                for (int index = 0; index < widths.length; index++) {
                    Object value = index < row.size() ? row.get(index) : "";
                    List<String> wrapped = wrap(ReportValueFormatter.text(value), widths[index] - 8, regular, FONT_SIZE, 3);
                    lines.add(wrapped);
                    maxLines = Math.max(maxLines, wrapped.size());
                }
                float height = Math.max(20, maxLines * LINE_HEIGHT + 8);
                if (y - height < MARGIN + 18) {
                    newPage();
                    writeText(report.title() + " (continuación)", MARGIN, y, bold, 11, PURPLE);
                    y -= 17;
                    writeTableHeader(widths);
                }
                drawRow(lines, widths, height);
            }
            if (report.rows().isEmpty()) {
                writeText("Sin registros para el periodo seleccionado.", MARGIN + 5, y - 16, regular, 9, Color.DARK_GRAY);
                y -= 28;
            }
        }

        private void writeTableHeader(float[] widths) throws IOException {
            float x = MARGIN;
            float height = 22;
            for (int index = 0; index < widths.length; index++) {
                stream.setNonStrokingColor(PURPLE);
                stream.addRect(x, y - height, widths[index], height);
                stream.fill();
                writeText(fit(report.columns().get(index), widths[index] - 8, bold, FONT_SIZE),
                        x + 4, y - 14, bold, FONT_SIZE, Color.WHITE);
                x += widths[index];
            }
            y -= height;
        }

        private void drawRow(List<List<String>> cells, float[] widths, float height) throws IOException {
            float x = MARGIN;
            for (int index = 0; index < widths.length; index++) {
                stream.setStrokingColor(GRID);
                stream.addRect(x, y - height, widths[index], height);
                stream.stroke();
                float textY = y - 11;
                for (String line : cells.get(index)) {
                    writeText(line, x + 4, textY, regular, FONT_SIZE, Color.BLACK);
                    textY -= LINE_HEIGHT;
                }
                x += widths[index];
            }
            y -= height;
        }

        private float[] columnWidths(int count) {
            float available = pageWidth - MARGIN * 2;
            float[] widths = new float[count];
            float equal = available / count;
            for (int index = 0; index < count; index++) widths[index] = equal;
            return widths;
        }

        private List<String> wrap(String text, float maxWidth, PDFont font, float size, int maxLines) throws IOException {
            if (text == null || text.isBlank()) return List.of("");
            text = supportedText(text, font);
            List<String> lines = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : text.replace('\n', ' ').split("\\s+")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (width(candidate, font, size) <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    if (!current.isEmpty()) lines.add(current.toString());
                    current.setLength(0);
                    current.append(fit(word, maxWidth, font, size));
                    if (lines.size() == maxLines - 1) break;
                }
            }
            if (!current.isEmpty() && lines.size() < maxLines) lines.add(current.toString());
            if (lines.isEmpty()) lines.add("");
            if (lines.size() == maxLines && width(text, font, size) > maxWidth * maxLines) {
                int last = lines.size() - 1;
                lines.set(last, fit(lines.get(last) + "...", maxWidth, font, size));
            }
            return lines;
        }

        private String fit(String value, float maxWidth, PDFont font, float size) throws IOException {
            if (value == null) return "";
            value = supportedText(value, font);
            String result = value;
            while (result.length() > 1 && width(result, font, size) > maxWidth) {
                result = result.substring(0, result.length() - 1);
            }
            return result.length() < value.length() ? result.replaceFirst("...$", "") + "..." : result;
        }

        private float width(String value, PDFont font, float size) throws IOException {
            return font.getStringWidth(supportedText(value, font)) / 1000f * size;
        }

        private void writeText(String text, float x, float y, PDFont font, float size, Color color) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.setNonStrokingColor(color);
            stream.newLineAtOffset(x, y);
            stream.showText(text == null ? "" : supportedText(text, font));
            stream.endText();
        }

        private String supportedText(String value, PDFont font) {
            StringBuilder result = new StringBuilder();
            for (int codePoint : value.codePoints().toArray()) {
                String character = new String(Character.toChars(codePoint));
                try {
                    font.encode(character);
                    result.append(character);
                } catch (IOException | IllegalArgumentException exception) {
                    result.append('?');
                }
            }
            return result.toString();
        }

        private void addPageNumbers() throws IOException {
            int total = pdf.getNumberOfPages();
            for (int index = 0; index < total; index++) {
                try (PDPageContentStream footer = new PDPageContentStream(pdf, pdf.getPage(index),
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    String label = "Página " + (index + 1) + " de " + total;
                    footer.beginText();
                    footer.setFont(regular, 7);
                    footer.setNonStrokingColor(Color.GRAY);
                    footer.newLineAtOffset(pageWidth - MARGIN - width(label, regular, 7), 17);
                    footer.showText(label);
                    footer.endText();
                }
            }
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
