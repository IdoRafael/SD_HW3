package il.ac.technion.cs.sd.sub.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.sub.app.SubscriberInitializer;
import il.ac.technion.cs.sd.sub.app.SubscriberReader;
import il.ac.technion.cs.sd.sub.ext.FutureLineStorage;
import il.ac.technion.cs.sd.sub.ext.LineStorageModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ExampleTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(30);

  private static Injector setupAndGetInjector(String fileName) throws Exception {
      String fileContents =
        new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    Injector injector = Guice.createInjector(new SubscriberModule(), new LineStorageModule());
    SubscriberInitializer si = injector.getInstance(SubscriberInitializer.class);
    CompletableFuture<Void> setup =
        fileName.endsWith("csv") ? si.setupCsv(fileContents) : si.setupJson(fileContents);
    setup.get();
    return injector;
  }

  @Test
  public void testSimpleCsv() throws Exception {
    Injector injector = setupAndGetInjector("small.csv");
    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
    assertEquals(Arrays.asList(true, true, false), reader.getAllSubscriptions("foo1234").get().get("foo1234"));
    assertEquals(0, reader.getMonthlyBudget("foo1234").get().getAsInt());
    assertEquals(100, reader.getMonthlyIncome("foo1234").get().getAsInt());
  }

  @Test
  public void testSimpleJson() throws Exception {
    Injector injector = setupAndGetInjector("small.json");
    SubscriberReader reader = injector.getInstance(SubscriberReader.class);
    assertEquals(100, reader.getMonthlyBudget("foo1234").get().getAsInt());
    assertFalse(reader.getMonthlyBudget("bar1234").get().isPresent());
  }
}
