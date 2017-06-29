package il.ac.technion.cs.sd.sub.app;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class ExampleTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(30);

  private static CompletableFuture<SubscriberReader> setup(String fileName) throws Exception {
      String fileContents =
        new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
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
}
