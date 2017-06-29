package il.ac.technion.cs.sd.sub.app;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorageFactory;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;
import il.ac.technion.cs.sd.sub.library.ReaderImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

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
            .thenApply(aVoid -> injector.getInstance(SubscriberReader.class));
  }

  @Test
  public void testSimpleCsv() throws Exception {

    CompletableFuture<SubscriberReader> futureReader = setup("small.csv");

    futureReader
            .thenCompose(reader -> reader.getAllSubscriptions("foo1234")).get();
//            .thenApply(map -> map.get("foo1234"));

//    assertEquals(
//            Arrays.asList(true, true, false),
//            a
//
//                    .get()
//    );
//    assertEquals(0, reader.getMonthlyBudget("foo1234").get().getAsInt());
//    assertEquals(100, reader.getMonthlyIncome("foo1234").get().getAsInt());
  }

  @Test
  public void testSimpleJson() throws Exception {
    /*Injector injector = setup("small.json");
    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
    assertEquals(100, reader.getMonthlyBudget("foo1234").get().getAsInt());
    assertFalse(reader.getMonthlyBudget("bar1234").get().isPresent());*/
  }
}
