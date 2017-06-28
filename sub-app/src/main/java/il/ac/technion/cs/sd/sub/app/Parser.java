package il.ac.technion.cs.sd.sub.app;


import java.io.IOException;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class Parser {
    private static String DELIMITER = ",";

    private SortedMap<String, Subscription> subscriptions = new TreeMap<>(
            Comparator
                    .comparing((String s) -> s.split(DELIMITER)[0])
                    .thenComparing((String s)-> s.split(DELIMITER)[1])
            );

    private SortedMap<String, String> journals = new TreeMap<>();

    public static enum ParserType {CSV, JSON};

    public Parser(String input, ParserType parserType) {
        if (parserType.equals(ParserType.CSV)) {
            input = csvToJson(input);
        }

        JsonElement jsonElement = new JsonParser().parse(input);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        parseProducts(jsonArray);
        parseSubscriptions(jsonArray);
    }

    String csvToJson(String csvData)  {
        CSVParser parser = null;
        try {
            parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String csvResult;

        try {
            csvResult = "[\n" +
                    parser
                    .getRecords()
                    .stream()
                    .map(csvRecord -> {
                        switch (csvRecord.get(0)) {
                            case "journal":
                                return "  " + handleCsvJournal(csvRecord);
                            case "subscriber":
                                return "  " + handleCsvSubscriber(csvRecord);
                            case "cancel":
                                return "  " + handleCsvCancel(csvRecord);
                            default:
                                return null;
                        }
                    })
                    .collect(Collectors.joining(",\n")) + "\n]";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return csvResult;
    }

    private String handleCsvCancel(CSVRecord csvRecord) {
        return "{\"type\": \"cancel\", \"user-id\": \""
                + csvRecord.get(1)
                + "\", \"journal-id\": \""
                + csvRecord.get(2)
                + "\"}";
    }

    private String handleCsvSubscriber(CSVRecord csvRecord) {
        return "{\"type\": \"subscription\", \"user-id\": \""
                + csvRecord.get(1)
                + "\", \"journal-id\": \""
                + csvRecord.get(2)
                + "\"}";
    }

    private String handleCsvJournal(CSVRecord csvRecord) {
        return "{\"type\": \"journal\", \"journal-id\": \""
                + csvRecord.get(1)
                + "\", \"price\": "
                + csvRecord.get(2)
                +"}";
    }


    public SortedMap<String, Subscription> getSubscriptions() {
        return subscriptions;
    }

    private void parseProducts(JsonArray jsonArray){
        for (JsonElement element : jsonArray){
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.get("type").getAsString().equals("journal")){
                journals.put(jsonObject.get("journal-id").getAsString(), jsonObject.get("price").getAsString());
            }
        }
    }

    private void parseSubscriptions(JsonArray jsonArray){
        for (JsonElement element : jsonArray){
            JsonObject jsonObject = element.getAsJsonObject();
            switch (jsonObject.get("type").getAsString()) {
                case "cancel":
                    handleCancel(jsonObject);
                    break;
                case "subscription":
                    handleSubscribe(jsonObject);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleSubscribe(JsonObject jsonObject){
        String journalId = jsonObject.get("journal-id").getAsString();
        if (!journals.containsKey(journalId)) {
            return;
        }
        String userId = jsonObject.get("user-id").getAsString();

        String subscriptionId = String.join(",", "asfdasdf", "asfasd");

//        orders.put(orderId, );
    }

    private void handleCancel(JsonObject jsonObject){
        String orderId = jsonObject.get("order-id").getAsString();
//        if (!orders.containsKey(orderId)) {
//            return;
//        }
//        orders.get(orderId).setCancelled(true);
    }
}
