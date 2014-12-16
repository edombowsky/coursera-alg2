public class BurrowsWheeler
{
    private static final int R = 256;

    // a

    /**
     * Apply Burrows-Wheeler encoding, reading from standard input and writing to standard output
     */
    public static void encode()
    {
        // read the input
        String s = BinaryStdIn.readString();
        int n    = s.length();

        CircularSuffixArray CSA = new CircularSuffixArray(s);

        for (int first = 0; first < n; first++)
        {
            if (CSA.index(first) == 0)
            {
                BinaryStdOut.write(first);

                break;
            }
        }

        for (int i = 0; i < n; i++)
        {
            BinaryStdOut.write(s.charAt((CSA.index(i) + n - 1) % n));
        }

        // print Burrows-Wheeler transform            
        BinaryStdOut.close();
    }

    /**
     * Apply Burrows-Wheeler decoding, reading from standard input and writing to standard output
     */
    public static void decode()
    {
        int first = BinaryStdIn.readInt();
        String t  = BinaryStdIn.readString();
        int n     = t.length();

        // construct the next array with Key-indexed counting
        int[] count = new int[R + 1];
        int[] next  = new int[n];

        for (int i = 0; i < n; i++)
        {
            count[t.charAt(i) + 1]++;
        }

        for (int r = 0; r < R; r++)
        {
            count[r + 1] += count[r];
        }

        for (int i = 0; i < n; i++)
        {
            next[count[t.charAt(i)]++] = i;
        }

        // decode message
        for (int i = next[first], c = 0; c < n; i = next[i], c++)
        {
            BinaryStdOut.write(t.charAt(i));
        }

        BinaryStdOut.close();
    }

    // if args[0] is '-', apply Burrows-Wheeler encoding
    // if args[0] is '+', apply Burrows-Wheeler decoding
    public static void main(String[] args)
    {
        if      (args[0].equals("-")) encode();
        else if (args[0].equals("+")) decode();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
