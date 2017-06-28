package il.ac.technion.cs.sd.sub.app;

import java.util.concurrent.CompletableFuture;

public class SubscriberInitializerImpl implements SubscriberInitializer {
    @Override
    public CompletableFuture<Void> setupCsv(String csvData) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setupJson(String jsonData) {
        return null;
    }
}
