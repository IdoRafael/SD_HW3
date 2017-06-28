package il.ac.technion.cs.sd.sub.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;

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

        assertEquals(shouldEqual, parser.getJournals());
    }

    @Test
    public void journalsCsvTest() throws Exception{
        Parser parser = getCsvParser("journals");

        Map<String, Long> shouldEqual = new HashMap<>();
        shouldEqual.put("a", 12L);
        shouldEqual.put("foo1234", 100L);
        shouldEqual.put("b", 2L);

        assertEquals(shouldEqual, parser.getJournals());
    }
}