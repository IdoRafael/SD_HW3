package il.ac.technion.cs.sd.sub.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;
import il.ac.technion.cs.sd.sub.library.ReaderImpl;
import il.ac.technion.cs.sd.sub.library.TryUntilSuccess;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class SubscriberInitializerImpl implements SubscriberInitializer {
    private static final String DELIMITER = ",";

    private ReaderFactory readerFactory;

    private String usersFileName;
    private String usersJournalsFileName;
    private String journalsFileName;
    private String journalsUsersFileName;

    @Inject
    public SubscriberInitializerImpl(
            ReaderFactory readerFactory,
            @Named("usersFileName") String usersFileName,
            @Named("usersJournalsFileName") String usersJournalsFileName,
            @Named("journalsFileName") String journalsFileName,
            @Named("journalsUsersFileName") String journalsUsersFileName
    ) {
        this.readerFactory = readerFactory;
        this.usersFileName = usersFileName;
        this.usersJournalsFileName = usersJournalsFileName;
        this.journalsFileName = journalsFileName;
        this.journalsUsersFileName = journalsUsersFileName;
    }

    @Override
    public CompletableFuture<Void> setupCsv(String csvData) {
        return setup(new Parser(csvData, Parser.ParserType.CSV));
    }

    @Override
    public CompletableFuture<Void> setupJson(String jsonData) {
        return setup(new Parser(jsonData, Parser.ParserType.JSON));
    }

    private CompletableFuture<Void> setup(Parser parser) {
        return CompletableFuture.allOf(
                setupUsers(parser),
                setupUsersJournals(parser),
                setupJournals(parser),
                setupJournalsUsers(parser)
        );
    }

    private CompletableFuture<Reader> setupUsers(Parser parser) {
        SortedSet<String> sortedUsers = parser.getSortedUsers();
        SortedMap<String, Subscription> sortedSubscriptions = parser.getSortedSubscriptions();

        List<String> lines = new ArrayList<>();
        sortedUsers.forEach(userId -> lines.add(String.join(
                DELIMITER,
                userId,
                String.valueOf(sortedSubscriptions
                        .values()
                        .stream()
                        .filter(subscription -> subscription.getUserId().equals(userId))
                        .map(Subscription::getJournalPriceIfSubscribed)
                        .mapToLong(optionalPrice -> optionalPrice.orElse(0L))
                        .sum())
                )
        ));

        return readerFactory.create(usersFileName).insertStrings(lines);
    }

    private CompletableFuture<Reader> setupUsersJournals(Parser parser) {
        SortedMap<String, Subscription> sortedSubscriptions = parser.getSortedSubscriptions();

        List<String> lines = new ArrayList<>();
        sortedSubscriptions.forEach((key, subscription) -> lines.add(String.join(
                DELIMITER,
                subscription.getUserId(),
                subscription.getJournalId(),
                subscription.toString()
        )));

        return readerFactory.create(usersJournalsFileName).insertStrings(lines);
    }

    private CompletableFuture<Reader> setupJournals(Parser parser) {
        Map<String, Long> journalPrices = parser.getJournalPrices();
        SortedMap<String, Subscription> sortedSubscriptions = parser.getSortedSubscriptions();

        List<String> lines = new ArrayList<>();
        journalPrices.forEach((journalId, journalPrice) -> lines.add(String.join(
                DELIMITER,
                journalId,
                String.valueOf(sortedSubscriptions
                        .values()
                        .stream()
                        .filter(subscription -> subscription.getJournalId().equals(journalId))
                        .map(Subscription::getJournalPriceIfSubscribed)
                        .mapToLong(optionalPrice -> optionalPrice.orElse(0L))
                        .sum())
                )
        ));

        return readerFactory.create(journalsFileName).insertStrings(lines);
    }

    private CompletableFuture<Reader> setupJournalsUsers(Parser parser) {
        SortedMap<String, Subscription> sortedSubscriptions = parser.getSortedSubscriptions();

        List<String> lines = new ArrayList<>();
        sortedSubscriptions.forEach((key, subscription) -> lines.add(String.join(
                DELIMITER,
                subscription.getJournalId(),
                subscription.getUserId(),
                subscription.toString()
        )));

        return readerFactory.create(journalsUsersFileName).insertStrings(lines);
    }
}
