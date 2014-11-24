/**
 * Content-aware picture resizing: iteratively remove the least noticable
 * vertical or horizontal seam. A <em>seam</em> is a path from the top to bottom
 * (or left to right) that moves only one pixel left or right (up or down) for
 * each pixel it moves down (to the right).
 *
 * The basic idea is to think of the image as an edge-weighted, directed, acyclic
 * graph. In the vertical version, downward edges point left, striaght, and
 * right from each non-bottom pixel. (The transpose is true in the horizontal
 * case.) We then assign weights to each pixel (really, to each in-edge) equal
 * to an energy function. The one defined here is a double gradient
 * function. Finally, we find the shortest path through this graph and remove
 * the corresponding pixels.
 *
 * Because this is an image-manipulation class, the origin pixel is in the top
 * left and coordinates are given in (column, row) order.
 *
 * Dependencies: Picture.class
 *
 */

public class SeamCarver
{
    private static final double BORDER_ENERGY = 3 * (255 * 255);
    private Picture pic;
    private double[] weights;
    private double[] distTo;
    private int[] edgeTo;

    /**
     * Construct a new SeamCarver from a <code>Picture</code> object.
     */
    public SeamCarver(Picture picture)
    {
        this.pic = new Picture(picture);
    }

    /**
     *
     * @return a copy of the current picture.
     */
    public Picture picture()
    {
        return new Picture(pic);
    }

    /**
     *
     * @return the width of the current picture.
     */
    public int width()
    {
        return pic.width();
    }

    /**
     *
     * @return the height of the current picture.
     */
    public int height()
    {
        return pic.height();
    }

    /**
     * Find the square color gradient in one dimension.
     *
     * @param a
     * @param b
     *
     * @return
     */
    private int gradient(java.awt.Color a, java.awt.Color b)
    {
        int red   = a.getRed()   - b.getRed();
        int green = a.getGreen() - b.getGreen();
        int blue  = a.getBlue()  - b.getBlue();

        return red*red + green*green + blue*blue;
    }

    /**
     * Returns the energy of pixel at column x and row y (origin in top left)
     * which is calculated as a double gradient for interior pixels. Thus, each
     * pixel's energy depends on the difference in color of all eight of its
     * neighbors. The energy is set to set to <code>3 * (255 * 255)</code> for
     * border pixels.
     *
     * @param x
     * @param y
     *
     * @return the energy of pixel at column x and row y (origin in top left).
     */
    public double energy(int x, int y)
    {
        if (x < 0 || x >= width() || y < 0 || y >= height())
        {
            String msg = Integer.toString(x) + ", " + Integer.toString(y);
            throw new IndexOutOfBoundsException(msg);
        }

        if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1) return BORDER_ENERGY;

        return gradient(pic.get(x - 1, y), pic.get(x + 1, y))
                + gradient(pic.get(x, y - 1), pic.get(x, y + 1));
    }

    // Mapping between node ID numbers and (col, row) notation. No bounds
    // checking is performed so use with caution.
    private int node(int col, int row)
    {
        return row * width() + col;
    }

    private int col(int node)
    {
        return node % width();
    }

    private int row(int node)
    {
        return node / width();
    }

    /**
     * Return a sequence of indices of a minimum-energy horizontal seam.
     *
     * If the return value is <code>a</code> then <code>a[column]</code> equals
     * the row number of the pixel in the seam at column number <code>column</code>.
     *
     * @return
     */
    public int[] findHorizontalSeam()
    {
        int row, col, width = width(), height = height();
        init(node(0, 0), node(0, height - 1), width);

        for (int startRow = height - 1; startRow > -width; startRow--)
        {
            if (startRow >= 0)
            {
                col = 0;
                row = startRow;
            }
            else
            {
                col = -startRow;
                row = 0;
            }

            for (; col < width - 1 && row < height; col++)
            {
                int v = node(col, row);

                if (row > 0) relax(v, node(col + 1, row - 1));

                relax(v, node(col + 1, row));

                if (row < height - 1) relax(v, node(col + 1, row + 1));

                row++;
            }
        }

        int endOfSeam = argmin(distTo, node(width - 1, 0), width * height, width);

        return horizontalPath(endOfSeam);
    }

    /**
     * Return a sequence of indices of a minimum-energy vertical seam.
     *
     * If the return value is <code>a</code> then <code>a[row]</code> equals
     * the column number of the pixel in the seam at row number <code>row</code>.
     *
     * @return
     */
    public int[] findVerticalSeam()
    {
        int row, col, width = width(), height = height();
        init(node(0, 0), node(width - 1, 0), 1);

        for (int startCol = width - 1; startCol > -height; startCol--)
        {
            if (startCol >= 0)
            {
                row = 0;
                col = startCol;
            }
            else
            {
                row = -startCol;
                col = 0;
            }

            for (; row < height - 1 && col < width; row++)
            {
                int v = node(col, row);

                if (col > 0) relax(v, node(col - 1, row + 1));

                relax(v, node(col, row + 1));

                if (col < width - 1) relax(v, node(col + 1, row + 1));

                col++;
            }
        }

        int endOfSeam = argmin(distTo, node(0, height - 1), width * height, 1);

        return verticalPath(endOfSeam);
    }


    /**
     * Initialize the search vectors. start, stop, and skip give the range of
     * nodes in which the search should begin (i.e., set the distance to zero).
     *
     * @param start
     * @param stop
     * @param skip
     */
    private void init(int start, int stop, int skip)
    {
        int width = width(), height = height(), size = width * height;

        if (weights == null || weights.length != size) weights = new double[size];
        if (distTo == null  || distTo.length  != size) distTo  = new double[size];
        if (edgeTo == null  || edgeTo.length  != size) edgeTo  = new int[size];

        for (int v = node(0, 0); v < size; v++)
        {
            if (v >= start && v < stop && (v - start) % skip == 0)
                distTo[v] = 0.0;
            else
                distTo[v] = Double.POSITIVE_INFINITY;

            edgeTo[v] = -1;
            weights[v] = energy(col(v), row(v));
        }
    }

    /**
     * The core of the shortest-path algorithm: at each pass, distTo contains
     * the minimum weight from the source to the indexed node found so far.
     *
     * @param from
     * @param to
     */
    private void relax(int from, int to)
    {
        if (distTo[to] > distTo[from] + weights[to])
        {
            distTo[to] = distTo[from] + weights[to];
            edgeTo[to] = from;
        }
    }

    /**
     * Return the index of the least element of an array of doubles in a range.
     *
     * @param a
     * @param start
     * @param stop
     * @param skip
     *
     * @return the index of the least element of an array of doubles in a range.
     */
    private int argmin(double[] a, int start, int stop, int skip)
    {
        if (stop <= start || start < 0 || a.length == 0) throw new IllegalArgumentException();

        double min    = Double.POSITIVE_INFINITY;
        int argmin    = start;
        int stopIndex = stop;

        if (stop > a.length) stopIndex = a.length;

        for (int i = start; i < stopIndex; i += skip)
        {
            if (a[i] < min)
            {
                min = a[i];
                argmin = i;
            }
        }

        return argmin;
    }

    /**
     * Translate the edgeTo array into the output format of findVerticalSeam.
     *
     * @param end
     *
     * @return
     */
    private int[] verticalPath(int end)
    {
        int[] seam = new int[height()];
        seam[row(end)] = col(end);

        for (int prev = edgeTo[end]; prev >= 0; prev = edgeTo[prev])
            seam[row(prev)] = col(prev);

        return seam;
    }

    /**
     * Translate the edgeTo array into the output format of findHorizontalSeam.
     *
     * @param end
     *
     * @return
     */
    private int[] horizontalPath(int end)
    {
        int[] seam = new int[width()];
        seam[col(end)] = row(end);

        for (int prev = edgeTo[end]; prev >= 0; prev = edgeTo[prev])
            seam[col(prev)] = row(prev);

        return seam;
    }

    /**
     * Remove any horizontal seam from the current picture.
     *
     * The parameter should be in <code>a[col] = row</code> notation.
     *
     * Throw an <code>IllegalArgumentException</code> if the picture has no
     * height from which to make the picture shorter, or if the parameter is not
     * a seam.
     *
     * Throw an <code>IndexOutofBoundsException</code> if the parameter attempts
     * to reference a row that does not exist.
     *
     * @param
     */
    public void removeHorizontalSeam(int[] a)
    {
        if (height() == 0)
            throw new IllegalArgumentException("Cannot remove horizontal seam from 0-height picture");
        if (a.length != width())
            throw new IllegalArgumentException("Horizontal seam narrower than width of picture");

        Picture p = new Picture(width(), height() - 1);
        int lastrow = a[0];

        for (int col = 0; col < width(); col++)
        {
            if (a[col] < lastrow - 1 || a[col] > lastrow + 1)
                throw new IllegalArgumentException("Non-valid seam");
            if (a[col] < 0 || a[col] >= height())
                throw new IndexOutOfBoundsException(Integer.toString(a[col]));

            lastrow = a[col];

            for (int row = 0; row < height() - 1; row++)
            {
                if (row < lastrow)
                    p.set(col, row, pic.get(col, row));
                else
                    p.set(col, row, pic.get(col, row + 1));
            }
        }

        pic = p;

        // Free up memory that init() will need to reallocate anyway.
        weights = null;
        distTo  = null;
        edgeTo  = null;
    }

    /**
     * Remove any vertical seam from the current picture.
     *
     * The parameter should be in <code>a[row] = col</code> notation.
     *
     * Throw an <code>IllegalArgumentException</code> if the picture has no
     * width from which to make the picture narrower, or if the parameter is not
     * a seam.
     *
     * Throw an <code>IndexOutofBoundsException</code> if the parameter attempts
     * to reference a column that does not exist.
     */
    public void removeVerticalSeam(int[] a)
    {
        if (width() == 0)
            throw new IllegalArgumentException("Cannot remove vertical seam from zero-width picture");
        if (a.length != height())
            throw new IllegalArgumentException("Vertical seam shorter than height of picture");

        Picture p = new Picture(width() - 1, height());

        int lastcol = a[0];

        for (int row = 0; row < height(); row++)
        {
            if (a[row] < lastcol - 1 || a[row] > lastcol + 1)
                throw new IllegalArgumentException("Non-valid seam");
            if (a[row] < 0 || a[row] >= width())
                throw new IndexOutOfBoundsException(Integer.toString(a[row]));

            lastcol = a[row];

            for (int col = 0; col < width() - 1; col++)
            {
                if (col < lastcol)
                    p.set(col, row, pic.get(col, row));
                else
                    p.set(col, row, pic.get(col + 1, row));
            }
        }

        pic = p;

        // Free up memory that init() will need to reallocate anyway.
        weights = null;
        distTo  = null;
        edgeTo  = null;
    }
}