public class CircularSuffixArray
{
    private static final short CUTOFF = 15;   // cutoff to insertion sort (any value between 0 and 12)

    private final int[] index;   // index[i] = j means text.substring(j) is ith largest suffix
    private final int N;         // number of characters in text

    /**
     * Circular suffix array of s
     * @param s
     */
    public CircularSuffixArray(String s)
    {
        N     = s.length();
        index = new int[N];

        for (int i = 0; i < N; i++)
        {
            index[i] = i;
        }

        // shuffle
        sort(s, 0, N-1, 0);
    }

    private char charAt(String s, int i, int d)
    {
        return s.charAt((i + d) % N);
    }

    // 3-way string quicksort lo..hi starting at dth character
    private void sort(String s, int lo, int hi, int d)
    {
        // cutoff to insertion sort for small subarrays
        if (hi <= lo + CUTOFF)
        {
            insertion(s, lo, hi, d);
            return;
        }

        int lt = lo;
        int gt = hi;
        int v  = charAt(s, index[lo], d);
        int i  = lo + 1;

        while (i <= gt)
        {
            int t = charAt(s, index[i], d);

            if      (t < v) exch(lt++, i++);
            else if (t > v) exch(i, gt--);
            else            i++;
        }

        // a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi].
        sort(s, lo, lt-1, d);
        sort(s, lt, gt, d+1);
        sort(s, gt+1, hi, d);
    }

    // sort from a[lo] to a[hi], starting at the dth character
    private void insertion(String s, int lo, int hi, int d)
    {
        for (int i = lo; i <= hi; i++)
        {
            for (int j = i; j > lo && less(s, j, j - 1, d); j--)
            {
                exch(j, j - 1);
            }
        }
    }

    // is text[i+d..N) < text[j+d..N) ?
    private boolean less(String s, int i, int j, int d)
    {
        int q = index[i];
        int p = index[j];

        for (int cnt = d; cnt < N; cnt++)
        {
            int pv = charAt(s, p, cnt), qv = charAt(s, q, cnt);

            if (qv < pv) return true;
            if (qv > pv) return false;
        }

        return false;
    }

    // exchange index[i] and index[j]
    private void exch(int i, int j)
    {
        int swap = index[i];

        index[i] = index[j];
        index[j] = swap;
    }

    /**
     * Get the length of the input string
     *
     * @return the length of the input string
     */
    public int length()
    {
        return N;
    }

    /**
     *
     * Returns the index into the original string of the <em>i</em>th smallest suffix.
     * That is, <tt>text.substring(sa.index(i))</tt> is the <em>i</em> smallest suffix.
     *
     * @param i an integer between 0 and <em>N</em>-1
     * @return the index into the original string of the <em>i</em>th smallest suffix
     * @throws java.lang.IndexOutOfBoundsException unless 0 &le; <em>i</em> &lt; <Em>N</em>
     */
    public int index(int i)
    {
        if (i < 0 || i >= N) throw new IndexOutOfBoundsException();

        return index[i];
    }

    /**
     * Unit testing of the methods (optional)
     * @param args
     */
    public static void main(String[] args)
    {
//        String s = "ABRACADABRA!";
//        String s = "abcdefghijklmnopqrstuvwxyz0123456789";
//        String s = "*************";
        String s = "zebra";

        int SCREEN_WIDTH = 80;
        int n            = s.length();
        int digits       = (int) Math.log10(n) + 1;
        String fmt       = "%" + (digits == 0 ? 1 : digits) + "d ";

        CircularSuffixArray csa = new CircularSuffixArray(s);

        StdOut.printf("String length: %d\n", n);

        for (int i = 0; i < n; i++)
        {
            StdOut.printf(fmt, i);

            for (int j = 0; j < (SCREEN_WIDTH - digits - 1) && j < n; j++)
            {
                char c = s.charAt((j + csa.index(i)) % n);

                if (c == '\n') c = ' ';

                StdOut.print(c);
            }

            StdOut.printf(fmt, csa.index(i));
            StdOut.println();
        }
    }
}