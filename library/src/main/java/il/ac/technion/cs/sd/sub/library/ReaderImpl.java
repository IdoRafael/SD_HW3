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

    @AssistedInject
    public ReaderImpl(
            FutureLineStorageFactory st_factory,
            @Assisted String filename) {
        fls = TryUntilSuccess.of(
                () -> st_factory.open(filename)
        );
    }

    @Override
    public CompletableFuture<Reader> getFuture() {
        return fls.thenApply(v -> this);
    }

    private CompletableFuture<Void> insertInFuture(FutureLineStorage fls, Collection<String> stringsCollection) {
        CompletableFuture<Boolean> currentWrite = completedFuture(null);

        for (String string : stringsCollection) {
            currentWrite = currentWrite.thenCompose(b -> TryUntilSuccess.ofBoolean(() -> fls.appendLine(string)));
        }

        return currentWrite.thenCompose(lastWrite -> completedFuture(null));
    }

    @Override
    public CompletableFuture<Reader> insertStrings(Collection<String> stringsCollection) {
        fls = fls
                .thenCompose(fls -> insertInFuture(fls, stringsCollection))
                .thenCompose(v -> fls);
        return getFuture();
    }

    private CompletableFuture<OptionalInt> futureBinarySearch(int first, int last,String id , Comparator<String> comparator) {
        if (first > last)
            return completedFuture(OptionalInt.empty());
        int middle = (last + first) / 2;
        return fls
                .thenCompose(fls -> TryUntilSuccess.of(() -> fls.read(middle)))
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
        return fls
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
        return fls.thenCompose(ls -> TryUntilSuccess.of(() -> ls.read(lineNum)));
    }

    @Override
    public CompletableFuture<Integer> numberOfLines() {
        return fls.thenCompose(ls -> TryUntilSuccess.ofOptionalInt(() -> ls.numberOfLines()));
    }


}

