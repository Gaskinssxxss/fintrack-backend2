import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReportService {

    public String buildCsv(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,USERNAME,DATE,DESCRIPTION,CATEGORY,TYPE,AMOUNT\n");

        for (Transaction t : transactions) {
            sb.append(t.getId()).append(",")
              .append(nullSafe(t.getUsername())).append(",")
              .append(nullSafe(t.getDate())).append(",")
              .append(escapeCsv(nullSafe(t.getDescription()))).append(",")
              .append(escapeCsv(nullSafe(t.getCategory()))).append(",")
              .append(nullSafe(t.getType())).append(",")
              .append(t.getAmount())
              .append("\n");
        }

        return sb.toString();
    }

    public byte[] buildCsvBytes(List<Transaction> transactions) {
        String csv = buildCsv(transactions);
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String text) {
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String nullSafe(String text) {
        return text == null ? "" : text;
    }
}
