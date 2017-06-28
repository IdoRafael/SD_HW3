package il.ac.technion.cs.sd.sub.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;


public class SubscriberReaderImpl implements SubscriberReader {
    private static final String DELIMITER = ",";

    @Inject
    public SubscriberReaderImpl(
            ReaderFactory readerFactory) {

    }

    @Override
    public CompletableFuture<Optional<Boolean>> isSubscribed(String userId, String journalId) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Boolean>> wasSubscribed(String userId, String journalId) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Boolean>> isCanceled(String userId, String journalId) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Boolean>> wasCanceled(String userId, String journalId) {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> getSubscribedJournals(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, List<Boolean>>> getAllSubscriptions(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<OptionalInt> getMonthlyBudget(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<List<String>> getSubscribedUsers(String journalId) {
        return null;
    }

    @Override
    public CompletableFuture<OptionalInt> getMonthlyIncome(String journalId) {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, List<Boolean>>> getSubscribers(String journalId) {
        return null;
    }

    CompletableFuture<Boolean> exists(Reader reader, String id0, String id1) {
        return getStringByIds(reader, id0, id1)
                .thenApply(Optional::isPresent);
    }

    CompletableFuture<Boolean> existsBySingleId(Reader reader, String id0) {
        return getSomeStringBySingleId(reader, id0)
                .thenApply(Optional::isPresent);
    }

    CompletableFuture<Optional<String>> getStringByIds(Reader reader, String id0, String id1) {
        return findIndexByTwoKeys(reader, id0, id1)
                .thenCompose(indexFound -> {
                    if (indexFound.isPresent()) {
                        return reader.read(indexFound.getAsInt()).thenApply(Optional::of);
                    } else {
                        return completedFuture(Optional.empty());
                    }
                });
    }

    CompletableFuture<Optional<String>> getSomeStringBySingleId(Reader reader, String id0) {
        return findIndexBySingleKey(reader, id0)
                .thenCompose(indexFound -> {
                    if (indexFound.isPresent()) {
                        return reader.read(indexFound.getAsInt()).thenApply(Optional::of);
                    } else {
                        return completedFuture(Optional.empty());
                    }
                });
    }

    CompletableFuture<List<String>> getAllStringsById(Reader reader, String id) {
        return findIndexBySingleKey(reader, id).thenCompose(indexFound -> {
            if (indexFound.isPresent()) {
                CompletableFuture<LinkedList<String>> before = getAllBeforeWithSameKey(reader, indexFound.getAsInt(), id);
                CompletableFuture<LinkedList<String>> after = getAllAfterWithSameKey(reader, indexFound.getAsInt() + 1, id);
                return before.thenCombine(after, (beforeList, afterList) -> {
                    beforeList.addAll(afterList);
                    return beforeList;
                });
            }
            return completedFuture(new LinkedList<>());
        });
    }

    private CompletableFuture<LinkedList<String>> getAllBeforeWithSameKey(Reader reader, int i, String key) {
        if (i < 0) {
            return completedFuture(new LinkedList<>());
        }
        return reader.read(i).thenCompose(found -> {
            if (found.split(DELIMITER)[0].equals(key)) {
                return getAllBeforeWithSameKey(reader, i-1, key).thenApply(list -> {list.addLast(found); return list;});
            } else {
                return completedFuture(new LinkedList<>());
            }
        });
    }

    private CompletableFuture<LinkedList<String>> getAllAfterWithSameKey(Reader reader, int i, String key) {
        return reader.numberOfLines().thenCompose(size -> {
            if (i >= size) {
                return completedFuture(new LinkedList<>());
            } else {
                return reader.read(i).thenCompose(found -> {
                    if (found.split(DELIMITER)[0].equals(key)) {
                        return getAllAfterWithSameKey(reader, i+1, key).thenApply(list -> {list.addFirst(found); return list;});
                    } else {
                        return completedFuture(new LinkedList<>());
                    }
                });
            }
        });
    }

    private CompletableFuture<OptionalInt> findIndexByTwoKeys(Reader reader, String key0, String key1) {
        return reader.findIndex(
                String.join(DELIMITER, key0, key1),
                Comparator.comparing((String s) -> s.split(DELIMITER)[0])
                        .thenComparing((String s)-> s.split(DELIMITER)[1])
        );
    }

    private CompletableFuture<OptionalInt> findIndexBySingleKey(Reader reader, String key) {
        return reader.findIndex(
                key,
                Comparator.comparing((String s) -> s.split(DELIMITER)[0])
        );

    }
}
