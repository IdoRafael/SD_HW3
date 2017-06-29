package il.ac.technion.cs.sd.sub.app;

import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FutureLineStorageFactoryTestImpl implements FutureLineStorageFactory {
    private static final int SLEEP_DURATION = 100;
    private static final Double PROBABILITY_TO_SUCCEED = 0.5;

    private HashMap<String, CompletableFuture<FutureLineStorage>>
            openLineStorages = new HashMap<>();

    @Override
    public CompletableFuture<Optional<FutureLineStorage>> open(String s) {
        try {
            Thread.sleep(openLineStorages.size() * SLEEP_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if( Math.random() <= PROBABILITY_TO_SUCCEED) {
            openLineStorages.putIfAbsent(s,
                    completedFuture(new FutureLineStorageTestImpl())
            );
            return openLineStorages.get(s).thenApply(Optional::of);
        } else {
            return completedFuture(Optional.empty());
        }
    }
}
