package il.ac.technion.cs.sd.sub.app;

import java.util.concurrent.CompletableFuture;

public interface SubscriberInitializer {
  /** Saves the CSV data persistently, so that it could be run using SubscriberReader. */
  CompletableFuture<Void> setupCsv(String csvData);
  /** Saves the JSON data persistently, so that it could be run using SubscriberReader. */
  CompletableFuture<Void> setupJson(String jsonData);
}
