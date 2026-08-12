package com.alness.lifemaster.operations.report;

public record ReportExportFile(byte[] content, String contentType, String fileName) {
}
