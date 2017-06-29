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
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class Reader {
    private CompletableFuture<FutureLineStorage> fls;

    @AssistedInject
    public Reader(
            FutureLineStorageFactory st_factory,
            @Assisted String filename) {
        fls = tryUntilSuccess(
                () -> st_factory.open(filename)
        );
    }

    public CompletableFuture<Reader> getFuture() {
        return fls.thenApply(v -> this);
    }

    private CompletableFuture<Void> insertInFuture(FutureLineStorage fls, Collection<String> stringsCollection) {
        CompletableFuture<Boolean> currentWrite = completedFuture(null);

        for (String string : stringsCollection) {
            currentWrite = currentWrite.thenCompose(b -> tryUntilSuccessBool(() -> fls.appendLine(string)));
        }

        return currentWrite.thenCompose(lastWrite -> null);
    }

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
                .thenCompose(fls -> tryUntilSuccess(() -> fls.read(middle)))
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

    public CompletableFuture<OptionalInt> findIndex(String id, Comparator<String> comparator)  {
        return fls
                .thenCompose(ls -> tryUntilSuccessOptionalInt(() -> ls.numberOfLines()))
                .thenCompose(lineNumbers -> futureBinarySearch(0, lineNumbers - 1,id, comparator));
    }

    public CompletableFuture<Optional<String>> find(String id, Comparator<String> comparator)  {
        return findIndex(id, comparator).thenCompose(foundIndex -> {
                    if (foundIndex.isPresent()) {
                        return read(foundIndex.getAsInt()).thenApply(Optional::of);
                    } else  {
                        return completedFuture(Optional.empty());
                    }
                });
    }

    public CompletableFuture<String> read(int lineNum){
        return fls.thenCompose(ls -> tryUntilSuccess(() -> ls.read(lineNum)));
    }

    public CompletableFuture<Integer> numberOfLines() {
        return fls.thenCompose(ls -> tryUntilSuccessOptionalInt(() -> ls.numberOfLines()));
    }

    private <T> CompletableFuture<T>
    tryUntilSuccess(
            Supplier<CompletableFuture<Optional<T>>> supplier
    ) {
        return tryUntilSuccessAux(supplier, supplier.get()).thenApply(Optional::get);
    }

    private <T> CompletableFuture<Optional<T>>
    tryUntilSuccessAux(
            Supplier<CompletableFuture<Optional<T>>> supplier,
            CompletableFuture<Optional<T>> futureOptional
    ) {
        return futureOptional.thenCompose(optional -> {
            if (optional.isPresent()) {
                return completedFuture(optional);
            } else {
                return tryUntilSuccessAux(supplier, supplier.get());
            }
        });
    }

    private CompletableFuture<Integer>
    tryUntilSuccessOptionalInt(
            Supplier<CompletableFuture<OptionalInt>> supplier
    ) {
        return tryUntilSuccessOptionalIntAux(supplier, supplier.get()).thenApply(OptionalInt::getAsInt);
    }

    private CompletableFuture<OptionalInt>
    tryUntilSuccessOptionalIntAux(
            Supplier<CompletableFuture<OptionalInt>> supplier,
            CompletableFuture<OptionalInt> futureOptional
    ) {
        return futureOptional.thenCompose(optional -> {
            if (optional.isPresent()) {
                return completedFuture(optional);
            } else {
                return tryUntilSuccessOptionalIntAux(supplier, supplier.get());
            }
        });
    }

    private CompletableFuture<Boolean>
    tryUntilSuccessBool(
            Supplier<CompletableFuture<Boolean>> supplier
    ) {
        return tryUntilSuccessBoolAux(supplier, supplier.get());
    }

    private CompletableFuture<Boolean>
    tryUntilSuccessBoolAux(
            Supplier<CompletableFuture<Boolean>> supplier,
            CompletableFuture<Boolean> futureBool
    ) {
        return futureBool.thenCompose(result -> {
            if (result) {
                return completedFuture(true);
            } else {
                return tryUntilSuccessBoolAux(supplier, supplier.get());
            }
        });
    }
}

