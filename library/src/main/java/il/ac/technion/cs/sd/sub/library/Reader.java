package il.ac.technion.cs.sd.sub.library;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

public interface Reader {
    CompletableFuture<Reader> getFuture();

    CompletableFuture<Reader> insertStrings(Collection<String> stringsCollection);

    CompletableFuture<OptionalInt> findIndex(String id, Comparator<String> comparator);

    CompletableFuture<Optional<String>> find(String id, Comparator<String> comparator);

    CompletableFuture<String> read(int lineNum);

    CompletableFuture<Integer> numberOfLines();
}
