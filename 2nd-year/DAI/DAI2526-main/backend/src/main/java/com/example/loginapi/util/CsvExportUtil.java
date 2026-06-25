package com.example.loginapi.util;

import java.util.List;

/**
 * Simple CSV exporter. Produces UTF-8 CSV from a list of string arrays.
 * The first array is treated as headers.
 */
public class CsvExportUtil {

    private CsvExportUtil() {}

    /**
     * Converts headers + rows into a UTF-8 CSV string.
     *
     * @param headers  column header names
     * @param rows     data rows (each row is an array of strings)
     * @return the full CSV content as a String
     */
    public static String toCsv(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(escape(headers)).append("\r\n");
        for (String[] row : rows) {
            sb.append(escape(row)).append("\r\n");
        }
        return sb.toString();
    }

    private static String escape(String[] cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            String val = cols[i] == null ? "" : cols[i];
            // Wrap in quotes if contains comma, quote, or newline
            if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
                val = "\"" + val.replace("\"", "\"\"") + "\"";
            }
            sb.append(val);
        }
        return sb.toString();
    }
}
