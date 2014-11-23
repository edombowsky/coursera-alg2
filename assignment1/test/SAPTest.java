import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SAPTest
{
    private static SAP sap1;
    private static SAP sap2;
    private static SAP sap3;
    private static SAP sap4;
    private static SAP sap5;
    private static SAP sap6;
    private static SAP sap7;

    @Before
    public void setup() throws Exception
    {
        In in = new In("resources/digraph1.txt");
        Digraph G1 = new Digraph(in);
        sap1 = new SAP(G1);

        in = new In("resources/digraph2.txt");
         Digraph G2 = new Digraph(in);
        sap2 = new SAP(G2);

        in = new In("resources/digraph3.txt");
        Digraph G3 = new Digraph(in);
        sap3 = new SAP(G3);

        in = new In("resources/digraph4.txt");
        Digraph G4 = new Digraph(in);
        sap4 = new SAP(G4);

        in = new In("resources/digraph5.txt");
        Digraph G5 = new Digraph(in);
        sap5 = new SAP(G5);

        in = new In("resources/digraph6.txt");
        Digraph G6 = new Digraph(in);
        sap6 = new SAP(G6);

        in = new In("resources/digraph-ambiguous-ancestor.txt");
        Digraph G7 = new Digraph(in);
        sap7 = new SAP(G7);
    }

    @Test
    public void testDigraph1()
    {
        assertThat(sap1.length(3, 11),   is(4));
        assertThat(sap1.length(9, 12),   is(3));
        assertThat(sap1.length(7, 2),    is(4));
        assertThat(sap1.length(1, 6),    is(-1));
        assertThat(sap1.ancestor(3, 11), is(1));
        assertThat(sap1.ancestor(9, 12), is(5));
        assertThat(sap1.ancestor(7, 2),  is(0));
        assertThat(sap1.ancestor(1, 6),  is(-1));
//        assertEquals(4, sap1.length(3, 11));
    }

    @Test
    public void testDigraph1Anc()
    {
        assertThat(sap1.ancestor(3, 11), is(1));
    }

    @Test
    public void testDigraph1Ex2()
    {
        assertThat(sap1.length(9, 12), is(3));
        assertThat(sap1.ancestor(9, 12), is(5));
    }

    @Test
    public void testDigraph1Ex3()
    {
        assertThat(sap1.length(7, 2), is(4));
        assertThat(sap1.ancestor(7, 2), is(0));
    }

    @Test
    public void testDigraph1Ex4Negative()
    {
        assertThat(sap1.length(1, 6), is(-1));
        assertThat(sap1.ancestor(1, 6), is(-1));
    }

    @Test
    public void testDigraph1WithOneBeingAncestor()
    {
        assertThat(sap1.length(3, 0), is(2));
        assertThat(sap1.ancestor(3, 0), is(0));
    }

    @Test
    public void testDigraph1WithAnotherBeingAncestor()
    {
        assertThat(sap1.length(1, 5), is(1));
        assertThat(sap1.ancestor(1, 5), is(1));
    }

    @Test
    public void testDigraph1WithSources()
    {
        Integer [] v = new Integer []{7,8};
        Integer [] w = new Integer []{11,12}; 
        assertThat(sap1.length(Arrays.asList(v), Arrays.asList(w)), is(5));
        assertThat(sap1.ancestor(Arrays.asList(v), Arrays.asList(w)), is(1));
    }

    @Test
    public void testDigraph1WithSourcesContainingAncestors()
    {
        Integer [] v = new Integer []{7,3};
        Integer [] w = new Integer []{11,10};

        assertThat(sap1.length(Arrays.asList(v), Arrays.asList(w)), is(3));
        assertThat(sap1.ancestor(Arrays.asList(v), Arrays.asList(w)), is(1));
    }

    @Test
    public void testDigraph2()
    {
        assertThat(sap2.length(1, 5), is(2));
    }

    @Test
    public void testDigraph2Anc()
    {
        assertThat(sap2.ancestor(1, 5), is(0));
    }

    @Test
    public void testDigraph3()
    {
        assertThat(sap3.length(1, 5), is(2));
    }

    @Test
    public void testDigraph3Anc()
    {
        assertThat(sap3.ancestor(1, 5), is(1));
    }

    @Test
    public void testDigraph3FromDiffSubgraph()
    {
        assertThat(sap3.ancestor(1, 0), is(-1));
        assertThat(sap3.length(1, 0), is(-1));
    }

    @Test
    public void testDigraph4()
    {
        assertThat(sap4.length(1, 9), is(4));
    }

    @Test
    public void testDigraph4Anc()
    {
        assertThat(sap4.ancestor(1, 9), is(8));
    }

    @Test
    public void testDigraph5()
    {
        assertThat(sap5.length(7, 19), is(6));
    }

    @Test
    public void testDigraph5Anc()
    {
        assertThat(sap5.ancestor(7, 19), is(9));
    }

    @Test
    public void testDigraph6()
    {
        assertThat(sap6.length(2, 7), is(2));
    }

    @Test
    public void testDigraph6Anc()
    {
        assertThat(sap6.ancestor(2, 7), is(3));
    }

    @Test
    public void testDigraph7()
    {
        assertThat(sap7.length(0, 6), is(5));
    }

    @Test
    public void testDigraph7Anc()
    {
        assertThat(sap7.ancestor(0, 6), is(2));
    }
}
