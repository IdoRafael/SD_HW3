package il.ac.technion.cs.sd.sub.app;


import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FutureLineStorageTestImpl implements FutureLineStorage {
    ArrayList<String> lines = new ArrayList<>();

    private static final int NUMBER_OF_LINES_SLEEP_DURATION = 100;
    private static final Double PROBABILITY_TO_SUCCEED = 0.5;

    @Override
    public CompletableFuture<Boolean> appendLine(String s) {
        if( Math.random() <= PROBABILITY_TO_SUCCEED) {
            lines.add(s);
            return completedFuture(true);
        } else {
            return completedFuture(false);
        }
    }

    @Override
    public CompletableFuture<Optional<String>> read(int i) {
        String toReturn = lines.get(i);
        try {
            Thread.sleep(toReturn.length());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if( Math.random() <= PROBABILITY_TO_SUCCEED) {
            return CompletableFuture.completedFuture(Optional.of(toReturn));
        } else {
            return completedFuture(Optional.empty());
        }
    }

    @Override
    public CompletableFuture<OptionalInt> numberOfLines() {
        try {
            Thread.sleep(NUMBER_OF_LINES_SLEEP_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if( Math.random() <= PROBABILITY_TO_SUCCEED) {
            return CompletableFuture.completedFuture(OptionalInt.of(lines.size()));
        } else {
            return completedFuture(OptionalInt.empty());
        }
    }
}
