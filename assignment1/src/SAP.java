//import java.util.Iterator;
//
//public class SAP {
//    private final Digraph digraph;
//    private final BFSCache vcache, wcache;
//
//    private class BFSCache implements Iterable<Integer> {
//        private final boolean[] visited;
//        private final int[] distanceTo;
//        private final Queue<Integer> modified = new Queue<Integer>();
//        private final Queue<Integer> queue = new Queue<Integer>();
//
//        public BFSCache(int size) {
//            visited = new boolean[size];
//            distanceTo = new int[size];
//
//            for (int i = 0; i < size; i++) {
//                visited[i] = false;
//                distanceTo[i] = -1;
//            }
//        }
//
//        public Iterator<Integer> iterator() {
//            return modified.iterator();
//        }
//
//        public void clear() {
//            while (!modified.isEmpty()) {
//                int v = modified.dequeue();
//                visited[v] = false;
//                distanceTo[v] = -1;
//            }
//        }
//
//        public void bfs(int v) {
//            visited[v] = true;
//            distanceTo[v] = 0;
//
//            modified.enqueue(v);
//            queue.enqueue(v);
//
//            while (!queue.isEmpty()) {
//                int w = queue.dequeue();
//
//                for (int next : digraph.adj(w)) {
//                    if (!visited[next]) {
//                        visited[next] = true;
//                        distanceTo[next] = distanceTo[w] + 1;
//                        modified.enqueue(next);
//                        queue.enqueue(next);
//                    }
//                }
//            }
//        }
//
//        public void bfs(Iterable<Integer> v) {
//            for (int w : v) {
//                visited[w] = true;
//                distanceTo[w] = 0;
//
//                modified.enqueue(w);
//                queue.enqueue(w);
//            }
//
//            while (!queue.isEmpty()) {
//                int w = queue.dequeue();
//
//                for (int next : digraph.adj(w)) {
//                    if (!visited[next]) {
//                        visited[next] = true;
//                        distanceTo[next] = distanceTo[w] + 1;
//                        modified.enqueue(next);
//                        queue.enqueue(next);
//                    }
//                }
//            }
//        }
//
//        public boolean canReach(int v) {
//            return visited[v];
//        }
//
//        public int distanceTo(int v) {
//            return distanceTo[v];
//        }
//    }
//
//    public SAP(Digraph G) {
//        this.digraph = new Digraph(G);
//        this.vcache = new BFSCache(G.V());
//        this.wcache = new BFSCache(G.V());
//    }
//
//    public int length(int v, int w) {
//        precalc(v, w);
//
//        return findDistance();
//    }
//
//    public int ancestor(int v, int w) {
//        precalc(v, w);
//
//        return findAncestor();
//    }
//
//    public int length(Iterable<Integer> v, Iterable<Integer> w) {
//        precalc(v, w);
//
//        return findDistance();
//    }
//
//    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
//        precalc(v, w);
//
//        return findAncestor();
//    }
//
//    private int findDistance() {
//        int result = -1;
//        BFSCache[] caches = { vcache, wcache };
//
//        for (BFSCache cache : caches) {
//            for (int v : cache) {
//                if (vcache.canReach(v) && wcache.canReach(v)) {
//                    int distance = vcache.distanceTo(v) + wcache.distanceTo(v);
//
//                    if (result == -1 || distance < result) {
//                        result = distance;
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private int findAncestor() {
//        int minDistance = -1;
//        int ancestor = -1;
//        BFSCache[] caches = { vcache, wcache };
//
//        for (BFSCache cache : caches) {
//            for (int v : cache) {
//                if (vcache.canReach(v) && wcache.canReach(v)) {
//                    int distance = vcache.distanceTo(v) + wcache.distanceTo(v);
//
//                    if (minDistance < 0 || distance < minDistance) {
//                        minDistance = distance;
//                        ancestor = v;
//                    }
//                }
//            }
//        }
//
//        return ancestor;
//    }
//
//    private void precalc(int v, int w) {
//        verifyInput(v);
//        verifyInput(w);
//
//        vcache.clear();
//        wcache.clear();
//
//        vcache.bfs(v);
//        wcache.bfs(w);
//    }
//
//    private void precalc(Iterable<Integer> v, Iterable<Integer> w) {
//        verifyInput(v);
//        verifyInput(w);
//
//        vcache.clear();
//        wcache.clear();
//
//        vcache.bfs(v);
//        wcache.bfs(w);
//    }
//
//    private void verifyInput(int v) {
//        if (v < 0 || v >= digraph.V())
//            throw new java.lang.IndexOutOfBoundsException();
//    }
//
//    private void verifyInput(Iterable<Integer> v) {
//        for (int w : v) {
//            verifyInput(w);
//        }
//    }
//
//    public static void main(String[] args) {
//        In in = new In(args[0]);
//        Digraph G = new Digraph(in);
//        SAP sap = new SAP(G);
//
//        while (!StdIn.isEmpty()) {
//            int v = StdIn.readInt();
//            int w = StdIn.readInt();
//            int length = sap.length(v, w);
//            int ancestor = sap.ancestor(v, w);
//            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
//        }
//    }
//}


import java.util.ArrayList;


public class SAP
{
    private Digraph graph;

    /**
     * constructor takes a digraph (not necessarily a DAG)
     *
     * @param G a digraph
     */
    public SAP(Digraph G)
    {
        graph = new Digraph(G);
    }

    private void validate(int i)
    {
        if (i < 0 || i >= graph.V())
        {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * @param v first vertex
     * @param w second vertex
     * @return length of shortest ancestral path between v and w; -1 if no such path
     */
    public int length(int v, int w)
    {
        ArrayList<Integer> vv = new ArrayList<Integer>();
        vv.add(v);
        ArrayList<Integer> ww = new ArrayList<Integer>();
        ww.add(w);

        return internalVisit(vv, ww)[1];
    }

    private int[] internalVisit(Iterable<Integer> vs, Iterable<Integer> ws)
    {
        BreadthFirstDirectedPaths bfsv = new BreadthFirstDirectedPaths(graph, vs);
        BreadthFirstDirectedPaths bfsw = new BreadthFirstDirectedPaths(graph, ws);
        int minDistance = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0, size = graph.V(); i < size; i++)
        {
            if (bfsv.hasPathTo(i) && bfsw.hasPathTo(i))
            {
                int distance = bfsv.distTo(i) + bfsw.distTo(i);

                if (distance < minDistance)
                {
                    minIndex = i;
                    minDistance = distance;
                }
            }
        }

        if (minDistance == Integer.MAX_VALUE)
        {
            return new int[]{-1, -1};
        }

        return new int[]{minIndex, minDistance};
    }

    /**
     * a common ancestor of v and w that participates in a shortest ancestral path;
     * -1 if no such path
     */
    public int ancestor(int v, int w)
    {
        ArrayList<Integer> vv = new ArrayList<Integer>();
        vv.add(v);
        ArrayList<Integer> ww = new ArrayList<Integer>();
        ww.add(w);

        return internalVisit(vv, ww)[0];
    }

    /**
     * length of shortest ancestral path between any vertex in v and any vertex in w;
     * -1 if no such path
     */
    public int length(Iterable<Integer> v, Iterable<Integer> w)
    {
        return internalVisit(v, w)[1];
    }

    /**
     * a common ancestor that participates in shortest ancestral path;
     * -1 if no such path
     */
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w)
    {
        return internalVisit(v, w)[0];
    }

    public static void main(String[] args)
    {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);

        while (!StdIn.isEmpty())
        {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
// 4152044054  3913739241 }




/*
import java.util.Arrays;


public class SAP
{
    private Digraph g = null;
    private boolean[] marked = null;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G)
    {
        g = new Digraph(G);
        marked = new boolean[g.V()];
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w)
    {
        if (v < 0 || v >= g.V()|| w < 0 || w >= g.V())
        {
            throw new IndexOutOfBoundsException();
        }

        Arrays.fill(marked, false);
        BreadthFirstDirectedPaths pathV = new BreadthFirstDirectedPaths(g, v);
        BreadthFirstDirectedPaths pathW = new BreadthFirstDirectedPaths(g, w);

        int length = Integer.MAX_VALUE;
        Queue<Integer> ancestorsV = new Queue<Integer>();
        ancestorsV.enqueue(v);
        while (!ancestorsV.isEmpty())
        {
            Integer ancestor = ancestorsV.dequeue();
            if (pathW.hasPathTo(ancestor))
            {
                int tempLength = pathV.distTo(ancestor) + pathW.distTo(ancestor);
                if (tempLength < length)
                {
                    length = tempLength;
                }
            }
            for (Integer adj:g.adj(ancestor))
            {
                if (!marked[adj])
                {
                    ancestorsV.enqueue(adj);
                    marked[adj] = true;
                }
            }
        }
        if (length < Integer.MAX_VALUE)
            return length;
        else
            return -1;
    }

    // a common ancestor of v and w that participates in a shortest ancestral
    // path; -1 if no such path
    public int ancestor(int v, int w)
    {
        Arrays.fill(marked, false);
        if (v < 0 || v >= g.V()|| w < 0 || w >= g.V())
        {
            throw new IndexOutOfBoundsException();
        }

        BreadthFirstDirectedPaths pathV = new BreadthFirstDirectedPaths(g, v);
        BreadthFirstDirectedPaths pathW = new BreadthFirstDirectedPaths(g, w);

        int length = Integer.MAX_VALUE;
        int commonAncestor = -1;
        Queue<Integer> ancestorsV = new Queue<Integer>();
        ancestorsV.enqueue(v);

        while (!ancestorsV.isEmpty())
        {
            Integer ancestor = ancestorsV.dequeue();

            if (pathW.hasPathTo(ancestor))
            {
                int tempLength = pathV.distTo(ancestor) + pathW.distTo(ancestor);

                if (tempLength < length)
                {
                    length = tempLength;
                    commonAncestor = ancestor;
                }
            }

            for (Integer adj:g.adj(ancestor))
            {
                if (!marked[adj])
                {
                    ancestorsV.enqueue(adj);
                    marked[adj] = true;
                }
            }
        }
        return commonAncestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w)
    {
        int length = Integer.MAX_VALUE;

        for (Integer vInt:v)
        {
            for (Integer wInt:w)
            {
                int tempLength = length(vInt, wInt);

                if (tempLength!= -1 && tempLength < length)
                {
                    length = tempLength;
                }
            }
        }

        if (length < Integer.MAX_VALUE)
            return length;
        else
            return -1;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w)
    {
        int length = Integer.MAX_VALUE;
        int pathV = -1;
        int pathW = -1;

        for (Integer vInt:v)
        {
            for (Integer wInt:w)
            {
                int tempLength = length(vInt, wInt);

                if (tempLength!= -1 && tempLength < length)
                {
                    length = tempLength;
                    pathV = vInt;
                    pathW = wInt;
                }
            }
        }

        if (pathV != -1)
        {
            return ancestor(pathV, pathW);
        }
        else
        {
            return -1;
        }
    }

    // for unit testing of this class (such as the one below)
    public static void main(String[] args)
    {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);

        while (!StdIn.isEmpty())
        {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
*/