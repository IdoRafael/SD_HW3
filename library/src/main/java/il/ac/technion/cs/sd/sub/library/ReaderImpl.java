package il.ac.technion.cs.sd.sub.library;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class ReaderImpl implements Reader {
    private CompletableFuture<FutureLineStorage> fls;
    private CompletableFuture<Boolean> currentWrite;

    @AssistedInject
    public ReaderImpl(
            FutureLineStorageFactory st_factory,
            @Assisted String filename) {
        fls = TryUntilSuccess.of(
                () -> st_factory.open(filename)
        );
        currentWrite = fls.thenCompose(ls -> completedFuture(null));
    }

    @Override
    public CompletableFuture<Reader> getFuture() {
        return getFutureFutureLineStorage().thenApply(v -> this);
    }

    private CompletableFuture<FutureLineStorage> getFutureFutureLineStorage() {
        return currentWrite.thenCompose(v -> fls);
    }

    @Override
    public CompletableFuture<Reader> insertStrings(Collection<String> stringsCollection) {
        for (String string : stringsCollection) {
            currentWrite = currentWrite
                    .thenCompose(b -> fls)
                    .thenCompose(ls -> TryUntilSuccess.ofBoolean(() -> ls.appendLine(string)));
        }

        return getFuture();
    }

    private CompletableFuture<OptionalInt> futureBinarySearch(int first, int last,String id , Comparator<String> comparator) {
        if (first > last)
            return completedFuture(OptionalInt.empty());
        int middle = (last + first) / 2;
        return getFutureFutureLineStorage()
                .thenCompose(ls -> TryUntilSuccess.of(() -> ls.read(middle)))
                .thenCompose(valueRead -> {
                    int compareResult = comparator.compare(valueRead, id);
                    if (compareResult == 0)
                        return completedFuture(OptionalInt.of(middle));
                    if (compareResult > 0)
                        return futureBinarySearch(first, middle - 1,id, comparator);
                    else
                        return futureBinarySearch(middle + 1, last,id, comparator);
                });
    }

    @Override
    public CompletableFuture<OptionalInt> findIndex(String id, Comparator<String> comparator)  {
        return getFutureFutureLineStorage()
                .thenCompose(ls -> TryUntilSuccess.ofOptionalInt(() -> ls.numberOfLines()))
                .thenCompose(lineNumbers -> futureBinarySearch(0, lineNumbers - 1,id, comparator));
    }

    @Override
    public CompletableFuture<Optional<String>> find(String id, Comparator<String> comparator)  {
        return findIndex(id, comparator).thenCompose(foundIndex -> {
                    if (foundIndex.isPresent()) {
                        return read(foundIndex.getAsInt()).thenApply(Optional::of);
                    } else  {
                        return completedFuture(Optional.empty());
                    }
                });
    }

    @Override
    public CompletableFuture<String> read(int lineNum){
        return getFutureFutureLineStorage()
                .thenCompose(ls -> TryUntilSuccess.of(() -> ls.read(lineNum)));
    }

    @Override
    public CompletableFuture<Integer> numberOfLines() {
        return getFutureFutureLineStorage()
                .thenCompose(ls -> TryUntilSuccess.ofOptionalInt(() -> ls.numberOfLines()));
    }


}

