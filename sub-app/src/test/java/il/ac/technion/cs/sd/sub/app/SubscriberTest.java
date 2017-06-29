package il.ac.technion.cs.sd.sub.app;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderImpl;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static il.ac.technion.cs.sd.sub.app.SubscriberReaderImpl.getAllStringsById;
import static il.ac.technion.cs.sd.sub.app.SubscriberReaderImpl.getSomeStringBySingleId;
import static il.ac.technion.cs.sd.sub.app.SubscriberReaderImpl.getStringByIds;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.*;

public class SubscriberTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    private static final Integer LINE_STORAGE_SIZE = 100;
    private static final Double PROBABILITY_TO_SUCCEED = 0.8;

    private final int AMOUNT_TO_RETURN = 10;

    private static FutureLineStorageFactory setupLineStorageFactoryMock(final FutureLineStorage lineStorage) throws InterruptedException {
        final int SLEEP_DURATION = 100;
        Mockito.doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {

            }

            if( Math.random() <= PROBABILITY_TO_SUCCEED) {
                return completedFuture(OptionalInt.of(LINE_STORAGE_SIZE));
            } else {
                return completedFuture(OptionalInt.empty());
            }
        }).when(lineStorage).numberOfLines();


        Mockito.doAnswer(invocationOnMock -> {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {

            }
            int i = (int)invocationOnMock.getArguments()[0];

            if( Math.random() <= PROBABILITY_TO_SUCCEED) {
                return completedFuture(Optional.of(String.join(",", "" + i/10, "" + i%10, "" + i%10)));
            } else {
                return completedFuture(Optional.empty());
            }
        }).when(lineStorage).read(Mockito.anyInt());

        Mockito.doAnswer(invocationOnMock -> {
            if( Math.random() <= PROBABILITY_TO_SUCCEED) {
                return completedFuture(true);
            } else {
                return completedFuture(false);
            }
        }).when(lineStorage).appendLine(Mockito.anyString());

        return s -> {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {

            }
            if( Math.random() <= PROBABILITY_TO_SUCCEED) {
                return completedFuture(Optional.of(lineStorage));
            } else {
                return completedFuture(Optional.empty());
            }
        };
    }

    private static CompletableFuture<Reader> setupStringStorage(final FutureLineStorage lineStorage) throws InterruptedException {
        return completedFuture(new ReaderImpl(
                setupLineStorageFactoryMock(lineStorage),
                ""
        ));
    }

    private static CompletableFuture<SubscriberReader> setup(String fileName) throws Exception {
        String fileContents =
                new Scanner(new File(SubscriberTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
        Injector injector = Guice.createInjector(new SubscriberModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(FutureLineStorageFactory.class).toInstance(new FutureLineStorageFactoryTestImpl());
            }
        });
        SubscriberInitializer si = injector.getInstance(SubscriberInitializer.class);
        return (fileName.endsWith("csv") ? si.setupCsv(fileContents) : si.setupJson(fileContents))
                .thenApply(v -> injector.getInstance(SubscriberReader.class));
    }

    @Test
    public void testSimpleCsv() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("small.csv");
        assertEquals(
                Arrays.asList(true, true, false),
                futureReader
                        .thenCompose(reader -> reader.getAllSubscriptions("foo1234"))
                        .get().get("foo1234"));

        assertEquals(
                0,
                futureReader
                        .thenCompose(reader -> reader.getMonthlyBudget("foo1234"))
                        .get()
                        .getAsInt()
        );
        assertEquals(
                100,
                futureReader
                        .thenCompose(reader -> reader.getMonthlyIncome("foo1234"))
                        .get()
                        .getAsInt()
        );


    }

    @Test
    public void testSimpleJson() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("small.json");
        assertEquals(
                100,
                futureReader
                        .thenCompose(reader -> reader.getMonthlyBudget("foo1234"))
                        .get()
                        .getAsInt()
        );
        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.getMonthlyBudget("bar1234"))
                        .get()
                        .isPresent()
        );
    }

    private void lastPrice(String filename) throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup(filename);

        assertEquals(
                12,
                futureReader
                        .thenCompose(subscriberReader -> subscriberReader.getMonthlyIncome("a"))
                        .get().getAsInt()
        );
    }

    @Test
    public void lastPriceShouldMatterJson() throws Exception {
        lastPrice("big.json");
    }

    @Test
    public void lastPriceShouldMatterCsv() throws Exception {
        lastPrice("big.csv");
    }

    private void nonExistentJournals(String filename) throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup(filename);

        assertTrue(
                futureReader
                        .thenCompose(subscriberReader -> subscriberReader.getSubscribers("nonExistent"))
                        .get().isEmpty()
        );
    }

    @Test
    public void shouldIgnoreSubAndCancelNonExistextJournalsJson() throws Exception {
        nonExistentJournals("big.json");
    }

    @Test
    public void shouldIgnoreSubAndCancelNonExistextJournalsCsv() throws Exception {
        nonExistentJournals("big.csv");
    }


    @Test
    public void shouldAcceptSubAndCancelEvenBeforeDefinition() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("bigFinal.csv");

        Map<String, List<Boolean>> u0Subscriptions = futureReader
                .thenCompose(reader -> reader.getAllSubscriptions("u0"))
                .get();

        assertTrue(u0Subscriptions.containsKey("j5"));
        assertTrue(u0Subscriptions.containsKey("j6"));
    }

    @Test
    public void shouldNotHaveMultipleCancelsTogether() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("bigFinal.csv");

        Map<String, List<Boolean>> j6Subscribers = futureReader
                .thenCompose(reader -> reader.getSubscribers("j6"))
                .get();

        assertThat(
                j6Subscribers.get("u0"),
                IsIterableContainingInOrder.contains(false, true, true, true, false, true, true, false, true)
        );
    }

    @Test
    public void shouldCountSubOnceForBudget() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("bigFinal.csv");

        assertThat(
                futureReader
                        .thenCompose(reader -> reader.getMonthlyIncome("j6"))
                        .get().getAsInt(),
                IsEqual.equalTo(5)
        );
    }

    @Test
    public void testIsSubscribed() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("bigFinal.csv");

        assertTrue(
                futureReader
                        .thenCompose(reader -> reader.isSubscribed("u0","j6"))
                        .get().get()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.isSubscribed("u0","j1"))
                        .get().get()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.isSubscribed("NonExistentUser","j6"))
                        .get()
                        .isPresent()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.isSubscribed("u2","DoesntExist"))
                        .get().get()
        );
    }

    @Test
    public void testWasSubscribed() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("bigFinal.csv");

        assertTrue(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("u0","j6"))
                        .get().get()
        );

        assertTrue(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("u0","j1"))
                        .get().get()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("NonExistentUser","j6"))
                        .get()
                        .isPresent()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("u0","j4"))
                        .get().get()
        );
    }

    @Test
    public void testIsCanceled() throws Exception {
        CompletableFuture<SubscriberReader> futureReader = setup("bigFinal.csv");

       /* assertTrue(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("u0","j6"))
                        .get().get()
        );

        assertTrue(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("u0","j1"))
                        .get().get()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("NonExistentUser","j6"))
                        .get()
                        .isPresent()
        );

        assertFalse(
                futureReader
                        .thenCompose(reader -> reader.wasSubscribed("u0","j4"))
                        .get().get()
        );*/
    }



    private void existTest(String id0, String id1, boolean exists) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        assertEquals(exists, SubscriberReaderImpl.exists(stringStorage, id0, id1).get().booleanValue());

    }

    private void getSingleStringsByIdTest(String id0, String id1) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<Optional<String>> futureResult = getStringByIds(stringStorage,id0, id1);
        Optional<String> result = futureResult.get();
        String[] stringResults = result.get().split(",");

        assertTrue(result.isPresent());
        assertEquals(id0, stringResults[0]);
        assertEquals(id1, stringResults[1]);
    }

    private void getAllStringsByIdTest(String id) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<List<String>> futureResult = getAllStringsById(stringStorage,id);
        List<String> resultList = futureResult.get();
        List<String> expectedList = IntStream.range(0, AMOUNT_TO_RETURN)
                .mapToObj(i -> String.join(",", id.toString(), "" + i, "" + i))
                .collect(Collectors.toList());

        assertEquals(expectedList, resultList);
    }

    private void getSomeStringByIdTest(String id) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<Optional<String>> futureResult = getSomeStringBySingleId(stringStorage,id);
        Optional<String> result = futureResult.get();

        assertEquals(id, result.get().split(",")[0]);
    }

    private void missSingleStringsByIdTest(String id0, String id1) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<Optional<String>> futureResult = getStringByIds(stringStorage,id0, id1);
        Optional<String> result = futureResult.get();

        assertFalse(result.isPresent());

    }

    private void missAllStringsByIdTest(String id) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<List<String>> futureResult = getAllStringsById(stringStorage,id);
        List<String> resultList = futureResult.get();

        assertTrue(resultList.isEmpty());
    }

    private void missSomeStringsByIdTest(String id) throws InterruptedException, ExecutionException {
        FutureLineStorage lineStorage = Mockito.mock(FutureLineStorage.class);
        CompletableFuture<Reader> stringStorage = setupStringStorage(lineStorage);

        CompletableFuture<Optional<String>> futureResult = getSomeStringBySingleId(stringStorage,id);
        Optional<String> result = futureResult.get();

        assertTrue(!result.isPresent());
    }

    @Test
    public void shouldExistInStart() throws InterruptedException, ExecutionException {
        existTest("0", "0", true);
    }

    @Test
    public void shouldExistInMiddle() throws InterruptedException, ExecutionException {
        existTest("5" , "5", true);
    }

    @Test
    public void shouldExistInEnd() throws InterruptedException, ExecutionException {
        existTest("9", "9", true);
    }

    @Test
    public void shouldntExistInStart() throws InterruptedException, ExecutionException {
        existTest("", "0", false);
    }

    @Test
    public void shouldntExistInMiddle() throws InterruptedException, ExecutionException {
        existTest("50" , "5", false);
    }

    @Test
    public void shouldntExistInEnd() throws InterruptedException, ExecutionException {
        existTest("9", "999", false);
    }

    @Test
    public void shouldFindSingleInStart() throws InterruptedException, ExecutionException {
        getSingleStringsByIdTest("0", "0");
    }

    @Test
    public void shouldFindSingleInMiddle() throws InterruptedException, ExecutionException {
        getSingleStringsByIdTest("5" , "5");
    }

    @Test
    public void shouldFindSingleInEnd() throws InterruptedException, ExecutionException {
        getSingleStringsByIdTest("9", "9");
    }

    @Test
    public void shouldMissSingleInStart() throws InterruptedException, ExecutionException {
        missSingleStringsByIdTest("", "0");
    }

    @Test
    public void shouldMissSingleInMiddle() throws InterruptedException, ExecutionException {
        missSingleStringsByIdTest("50" , "5");
    }

    @Test
    public void shouldMissSingleInEnd() throws InterruptedException, ExecutionException {
        missSingleStringsByIdTest("9", "999");
    }

    @Test
    public void shouldFindGroupInStart() throws InterruptedException, ExecutionException {
        getAllStringsByIdTest("0");
    }

    @Test
    public void shouldFindGroupInMiddle() throws InterruptedException, ExecutionException {
        getAllStringsByIdTest("5");
    }

    @Test
    public void shouldFindGroupInEnd() throws InterruptedException, ExecutionException {
        getAllStringsByIdTest("9");
    }

    @Test
    public void shouldMissGroupInStart() throws InterruptedException, ExecutionException {
        missAllStringsByIdTest("");
    }

    @Test
    public void shouldMissGroupInMiddle() throws InterruptedException, ExecutionException {
        missAllStringsByIdTest("50");
    }

    @Test
    public void shouldMissGroupInEnd() throws InterruptedException, ExecutionException {
        missAllStringsByIdTest("999");
    }

    @Test
    public void shouldFindSomeInStart() throws InterruptedException, ExecutionException {
        getSomeStringByIdTest("0");
    }

    @Test
    public void shouldFindSomeInMiddle() throws InterruptedException, ExecutionException {
        getSomeStringByIdTest("5");
    }

    @Test
    public void shouldFindSomeInEnd() throws InterruptedException, ExecutionException {
        getSomeStringByIdTest("9");
    }

    @Test
    public void shouldMissSomeStringInStart() throws InterruptedException, ExecutionException {
        missSomeStringsByIdTest("");
    }

    @Test
    public void shouldMissSomeStringInMiddle() throws InterruptedException, ExecutionException {
        missSomeStringsByIdTest("50");
    }

    @Test
    public void shouldMissSomeStringInEnd() throws InterruptedException, ExecutionException {
        missSomeStringsByIdTest("999");
    }
}
