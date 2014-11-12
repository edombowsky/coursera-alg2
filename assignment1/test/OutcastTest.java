import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OutcastTest
{
    WordNet wordNet;

    @Before
    public void setup() throws Exception
    {
        wordNet = new WordNet("resources/synsets.txt", "resources/hypernyms.txt");
    }

    @Test
    public void testOutcast5()
    {
        Outcast outcast = new Outcast(wordNet);
        In in = new In("resources/outcast5.txt");
        String[] nouns = in.readAllStrings();

        assertThat(outcast.outcast(nouns), is(equalTo("table")));
    }

    @Test
    public void testOutcast8()
    {
        Outcast outcast = new Outcast(wordNet);
        In in = new In("resources/outcast8.txt");
        String[] nouns = in.readAllStrings();

        assertThat(outcast.outcast(nouns), is(equalTo("bed")));
    }


    @Test
    public void testOutcast11()
    {
        Outcast outcast = new Outcast(wordNet);

        In in = new In("resources/outcast11.txt");
        String[] nouns = in.readAllStrings();

        assertThat(outcast.outcast(nouns), is(equalTo("potato")));
    }

    //File contains a single noun
    @Test
    public void testOutcast2()
    {
        Outcast outcast = new Outcast(wordNet);
        In in = new In("resources/outcast2.txt");
        String[] nouns = in.readAllStrings();

        assertThat(outcast.outcast(nouns), is(equalTo("Turing")));
    }
}