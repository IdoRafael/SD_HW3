package il.ac.technion.cs.sd.sub.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class ParserTest {

    public static String getFilesContent(String fileName) throws FileNotFoundException {
        return new Scanner(new File(ParserTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    }

    public Parser getCsvParser(String fileName)throws FileNotFoundException {
        return new Parser(getFilesContent(fileName + ".csv"), Parser.ParserType.CSV);
    }

    public Parser getJsonParser(String fileName)throws FileNotFoundException {
        return new Parser(getFilesContent(fileName + ".json"), Parser.ParserType.JSON);
    }

    @Test
    public void journalsJsonTest() throws Exception{
        Parser parser = getJsonParser("journals");

        Map<String, Long> shouldEqual = new HashMap<>();
        shouldEqual.put("a", 12L);
        shouldEqual.put("foo1234", 100L);

        assertEquals(shouldEqual, parser.getJournalPrices());
    }

    @Test
    public void journalsCsvTest() throws Exception{
        Parser parser = getCsvParser("journals");

        Map<String, Long> shouldEqual = new HashMap<>();
        shouldEqual.put("a", 12L);
        shouldEqual.put("foo1234", 100L);
        shouldEqual.put("b", 2L);

        assertEquals(shouldEqual, parser.getJournalPrices());
    }

    private void bigTest(Parser parser) throws Exception{
        Map<String, Long> expectedPrices = new HashMap<>();
        expectedPrices.put("a", 12L);
        expectedPrices.put("b", 101L);
        expectedPrices.put("c", 1L);
        expectedPrices.put("d", 2L);
        expectedPrices.put("e", 3L);
        assertEquals(expectedPrices, parser.getJournalPrices());

        SortedSet<String> users = new TreeSet<>();
        users.addAll(Arrays.asList("A", "B", "C", "D"));
        assertEquals(users, parser.getSortedUsers());

        SortedMap<String, Subscription> expectedSubscriptions =
                new TreeMap<>(Comparator
                        .comparing((String s) -> s.split(",")[0])
                        .thenComparing((String s)-> s.split(",")[1]));
        expectedSubscriptions.put("A,a", new Subscription("A,a,12,0,1"));
        expectedSubscriptions.put("A,b", new Subscription("A,b,101,0,1,0"));
        expectedSubscriptions.put("A,c", new Subscription("A,c,1,1,0"));
        expectedSubscriptions.put("A,d", new Subscription("A,d,2,1"));
        expectedSubscriptions.put("A,e", new Subscription("A,e,3,0"));

        expectedSubscriptions.put("D,d", new Subscription("D,d,2,0"));

        assertEquals(expectedPrices, parser.getJournalPrices());
    }

    @Test
    public void bigJsonTest() throws Exception{
        bigTest(getJsonParser("big"));
    }

    @Test
    public void bigCsvTest() throws Exception{
        bigTest(getCsvParser("big"));
    }

}