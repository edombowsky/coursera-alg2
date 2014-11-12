import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class WordNetTest
{
    private static WordNet wordNet;
    private static WordNet wordNetLarge;

    @Before
    public void setup() throws Exception
    {
        wordNet = new WordNet("resources/synsets15.txt",
                              "resources/hypernyms15Path.txt");


        wordNetLarge = new WordNet("resources/synsets.txt",
                                   "resources/hypernyms.txt");
    }

    @Test
    public void testConstructor()
    {
        assertTrue("a is a noun", wordNet.isNoun("a"));
//        assertThat(wordNet.isNoun("a"), is("a is a noun"));
    }

    @Test
    public void testAllNouns()
    {
        assertEquals("f", wordNet.nouns().iterator().next());
    }


    @Test
    public void testLarge()
    {
        assertThat(wordNetLarge.distance("horse", "cat"), is(7));
    }

    @Test
    public void testLargeIsNoun()
    {
        assertTrue(wordNetLarge.isNoun("'s_Gravenhage"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDistanceInvalid()
    {
        assertEquals(wordNetLarge.distance("horse", "eleventeen"), is(7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSapInvalid()
    {
        assertEquals("", wordNetLarge.sap("horse", "eleventeen"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCyclesInvalid()
    {
        WordNet wordNetInvalidCycle = new WordNet("resources/synsets3.txt",
                                                  "resources/hypernyms3InvalidCycle.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTwoRootsInvalid()
    {
        WordNet wordNetTwoRoots = new WordNet("resources/synsets3.txt",
                                              "resources/hypernyms3InvalidTwoRoots.txt");
    }
}
