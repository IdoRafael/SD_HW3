package il.ac.technion.cs.sd.sub.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Yaniv on 10/06/2017.
 */
public class ParserTest {

    public static String getFilesContent(String fileName) throws FileNotFoundException {
        return new Scanner(new File(ParserTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    }

    public void justRun(String fileName)throws FileNotFoundException {
        String csv = getFilesContent(fileName);
        new Parser(csv, Parser.ParserType.JSON).csvToJson(csv);
    }

    @Test
    public void smallTest() throws Exception{
        justRun("small.csv");
    }

}