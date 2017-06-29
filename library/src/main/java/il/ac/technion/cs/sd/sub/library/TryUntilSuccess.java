package il.ac.technion.cs.sd.sub.library;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TryUntilSuccess {
    static public  <T> CompletableFuture<T>
    of(
            Supplier<CompletableFuture<Optional<T>>> supplier
    ) {
        return tryUntilSuccessAux(supplier, supplier.get()).thenApply(Optional::get);
    }

    static private <T> CompletableFuture<Optional<T>>
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

    static public CompletableFuture<Integer>
    ofOptionalInt(
            Supplier<CompletableFuture<OptionalInt>> supplier
    ) {
        return tryUntilSuccessOptionalIntAux(supplier, supplier.get()).thenApply(OptionalInt::getAsInt);
    }

    static private CompletableFuture<OptionalInt>
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

    static public CompletableFuture<Boolean>
    ofBoolean(
            Supplier<CompletableFuture<Boolean>> supplier
    ) {
        return tryUntilSuccessBoolAux(supplier, supplier.get());
    }

    static private CompletableFuture<Boolean>
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
