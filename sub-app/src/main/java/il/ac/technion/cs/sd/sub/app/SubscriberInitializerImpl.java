package il.ac.technion.cs.sd.sub.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    private CompletableFuture<Void> setupUsers(Parser parser) {
        Map<String, Long> journalPrices = parser.getJournalPrices();
        SortedSet<String> sortedUsers = parser.getSortedUsers();
        SortedMap<String, Subscription> sortedSubscriptions = parser.getSortedSubscriptions();

        List<String> lines = new ArrayList<>();
        /*sortedUsers.forEach(userId -> lines.add(
                userId,
                sortedSubscriptions
                        .values()
                        .stream()
                        .filter(with id of userId)
                        .filter(active)
                        .map(price)
                        .sum()
        ));

        return readerFactory.create(usersFileName).insertStrings();
        */
        return null;
    }

    private CompletableFuture<Void> setupUsersJournals(Parser parser) {
        return null;
    }

    private CompletableFuture<Void> setupJournals(Parser parser) {
        return null;
    }

    private CompletableFuture<Void> setupJournalsUsers(Parser parser) {
        return null;
    }
}
