package il.ac.technion.cs.sd.sub.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;
import il.ac.technion.cs.sd.sub.library.TryUntilSuccess;
import javafx.util.Pair;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;


public class SubscriberReaderImpl implements SubscriberReader {
    private static final String DELIMITER = ",";

    private CompletableFuture<Reader> users;
    private CompletableFuture<Reader> usersJournals;
    private CompletableFuture<Reader> journals;
    private CompletableFuture<Reader> journalsUsers;

    @Inject
    public SubscriberReaderImpl(
            ReaderFactory readerFactory,
            @Named("usersFileName") String usersFileName,
            @Named("usersJournalsFileName") String usersJournalsFileName,
            @Named("journalsFileName") String journalsFileName,
            @Named("journalsUsersFileName") String journalsUsersFileName) {

        this.users = readerFactory.create(usersFileName).getFuture();
        this.usersJournals = readerFactory.create(usersJournalsFileName).getFuture();
        this.journals = readerFactory.create(journalsFileName).getFuture();
        this.journalsUsers = readerFactory.create(journalsUsersFileName).getFuture();
    }

    @Override
    public CompletableFuture<Optional<Boolean>> isSubscribed(String userId, String journalId) {
        return handleBooleanQuery(userId, journalId, Subscription::isSubscribed);
    }

    @Override
    public CompletableFuture<Optional<Boolean>> wasSubscribed(String userId, String journalId) {
        return handleBooleanQuery(userId, journalId, Subscription::wasSubscribed);
    }

    @Override
    public CompletableFuture<Optional<Boolean>> isCanceled(String userId, String journalId) {
        return handleBooleanQuery(userId, journalId, Subscription::isCanceled);
    }

    @Override
    public CompletableFuture<Optional<Boolean>> wasCanceled(String userId, String journalId) {
        return handleBooleanQuery(userId, journalId, Subscription::wasCanceled);
    }

    @Override
    public CompletableFuture<List<String>> getSubscribedJournals(String userId) {
        return getSubscribed(userId, usersJournals, Subscription::getJournalId);
    }

    private CompletableFuture<List<String>> getSubscribed(String userId, CompletableFuture<Reader> futureReader, Function<Subscription, String> toGet) {
        return getAllStringsById(futureReader, userId)
                .thenApply(
                        strings -> strings
                                .stream()
                                .map(s-> s.split(DELIMITER, 3)[2])
                                .map(Subscription::new)
                                .filter(Subscription::isSubscribed)
                                .map(toGet::apply)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public CompletableFuture<Map<String, List<Boolean>>> getAllSubscriptions(String userId) {
        return getMap(userId, usersJournals, Subscription::getJournalId);
    }



    @Override
    public CompletableFuture<OptionalInt> getMonthlyBudget(String userId) {
        return getMonthly(userId, users);
    }

    @Override
    public CompletableFuture<List<String>> getSubscribedUsers(String journalId) {
        return getSubscribed(journalId, journalsUsers, Subscription::getUserId);
    }

    @Override
    public CompletableFuture<OptionalInt> getMonthlyIncome(String journalId) {
        return getMonthly(journalId, journals);
    }

    @Override
    public CompletableFuture<Map<String, List<Boolean>>> getSubscribers(String journalId) {
        return getMap(journalId, journalsUsers, Subscription::getUserId);
    }

    private CompletableFuture<Optional<Boolean>> handleBooleanQuery(String userId, String journalId, Function<Subscription, Boolean> booleanFunction) {
        return existsBySingleId(users, userId)
                .thenCombine(getStringByIds(usersJournals, userId, journalId), (exists, optionalString) -> {
                    if (exists) {
                        Optional<Boolean> aBoolean = Optional.of(
                                optionalString
                                        .map(s -> s.split(DELIMITER, 3)[2])
                                        .map(Subscription::new)
                                        .map(booleanFunction::apply)
                                        .orElse(false)
                        );
                        return aBoolean;
                    } else {
                        return Optional.empty();
                    }
                });
    }

    private CompletableFuture<OptionalInt> getMonthly(String journalId, CompletableFuture<Reader> futureReader) {
        return getSomeStringBySingleId(futureReader, journalId)
                .thenApply(optionalString -> {
                    if (optionalString.isPresent()) {
                        return OptionalInt.of(
                                optionalString
                                        .map(s -> s.split(DELIMITER)[1])
                                        .map(Long::parseLong)
                                        .map(Long::intValue)
                                        .get());
                    } else {
                        return OptionalInt.empty();
                    }
                });
    }

    private CompletableFuture<Map<String, List<Boolean>>>
    getMap(String id, CompletableFuture<Reader> futureReader, Function<Subscription, String> toGet) {
        return getAllStringsById(futureReader, id)
                .thenApply(
                        strings -> strings
                                .stream()
                                .map(s-> s.split(DELIMITER, 3)[2])
                                .map(Subscription::new)
                                .map(subscription ->
                                        new Pair<>(
                                                toGet.apply(subscription),
                                                subscription.getHistory()
                                                        .stream()
                                                        .map(Subscription.Subscribed::toBoolean)
                                                        .collect(Collectors.toList())
                                        )
                                )
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                );
    }

    static CompletableFuture<Boolean> exists(CompletableFuture<Reader> reader, String id0, String id1) {
        return getStringByIds(reader, id0, id1)
                .thenApply(Optional::isPresent);
    }

    static CompletableFuture<Boolean> existsBySingleId(CompletableFuture<Reader> reader, String id0) {
        return getSomeStringBySingleId(reader, id0)
                .thenApply(Optional::isPresent);
    }

    static CompletableFuture<Optional<String>> getStringByIds(CompletableFuture<Reader> futureReader, String id0, String id1) {
        return futureReader.thenCompose(reader -> findIndexByTwoKeys(completedFuture(reader), id0, id1)
                .thenCompose(indexFound -> {
                    if (indexFound.isPresent()) {
                        return reader.read(indexFound.getAsInt()).thenApply(Optional::of);
                    } else {
                        return completedFuture(Optional.empty());
                    }
                }));
    }

    static CompletableFuture<Optional<String>> getSomeStringBySingleId(CompletableFuture<Reader> futureReader, String id0) {
        return futureReader.thenCompose(reader -> findIndexBySingleKey(completedFuture(reader), id0)
                .thenCompose(indexFound -> {
                    if (indexFound.isPresent()) {
                        return reader.read(indexFound.getAsInt()).thenApply(Optional::of);
                    } else {
                        return completedFuture(Optional.empty());
                    }
                }));
    }

    static CompletableFuture<List<String>> getAllStringsById(CompletableFuture<Reader> futureReader, String id) {
        return futureReader.thenCompose(reader -> findIndexBySingleKey(completedFuture(reader), id).thenCompose(indexFound -> {
            if (indexFound.isPresent()) {
                CompletableFuture<LinkedList<String>> before = getAllBeforeWithSameKey(completedFuture(reader), indexFound.getAsInt(), id);
                CompletableFuture<LinkedList<String>> after = getAllAfterWithSameKey(completedFuture(reader), indexFound.getAsInt() + 1, id);
                return before.thenCombine(after, (beforeList, afterList) -> {
                    beforeList.addAll(afterList);
                    return beforeList;
                });
            }
            return completedFuture(new LinkedList<>());
        }));
    }

    static private CompletableFuture<LinkedList<String>> getAllBeforeWithSameKey(CompletableFuture<Reader> futureReader, int i, String key) {
        if (i < 0) {
            return completedFuture(new LinkedList<>());
        }
        return futureReader.thenCompose(reader -> reader.read(i).thenCompose(found -> {
            if (found.split(DELIMITER)[0].equals(key)) {
                return getAllBeforeWithSameKey(completedFuture(reader), i-1, key).thenApply(list -> {list.addLast(found); return list;});
            } else {
                return completedFuture(new LinkedList<>());
            }
        }));
    }

    static private CompletableFuture<LinkedList<String>> getAllAfterWithSameKey(CompletableFuture<Reader> futureReader, int i, String key) {
        return futureReader.thenCompose(reader -> reader.numberOfLines().thenCompose(size -> {
            if (i >= size) {
                return completedFuture(new LinkedList<>());
            } else {
                return reader.read(i).thenCompose(found -> {
                    if (found.split(DELIMITER)[0].equals(key)) {
                        return getAllAfterWithSameKey(completedFuture(reader), i+1, key).thenApply(list -> {list.addFirst(found); return list;});
                    } else {
                        return completedFuture(new LinkedList<>());
                    }
                });
            }
        }));
    }

    static private CompletableFuture<OptionalInt> findIndexByTwoKeys(CompletableFuture<Reader> futureReader, String key0, String key1) {
        return futureReader.thenCompose(
                reader -> reader.findIndex(
                        String.join(DELIMITER, key0, key1),
                        Comparator.comparing((String s) -> s.split(DELIMITER)[0])
                                .thenComparing((String s)-> s.split(DELIMITER)[1])
                )
        );
    }

    static private CompletableFuture<OptionalInt> findIndexBySingleKey(CompletableFuture<Reader> futureReader, String key) {
        return futureReader.thenCompose(
                reader -> reader.findIndex(
                        key,
                        Comparator.comparing((String s) -> s.split(DELIMITER)[0])
                )
        );

    }
}
