import java.util.List;
import java.util.ArrayList;

public class BaseballElimination
{
    private final short numberOfTeams;
    private final List<String> teams;
    private final short[] wins, losses, remaining;
    private final short[][] games;
    private List<Integer> R;
    private boolean isEliminatedExec;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename)
    {
        In in = new In(filename);

        // initial variable
        numberOfTeams = in.readShort();
        teams         = new ArrayList<String>();
        wins          = new short[numberOfTeams];
        losses        = new short[numberOfTeams];
        remaining     = new short[numberOfTeams];
        games         = new short[numberOfTeams][numberOfTeams];

        int i = 0;
        while (!in.isEmpty())
        {
            teams.add(in.readString());
            wins[i]      = in.readShort();
            losses[i]    = in.readShort();
            remaining[i] = in.readShort();

            for (int j = 0; j < numberOfTeams; j++)
            {
                games[i][j] = in.readShort();
            }

            i++;
        }
    }

    /**
     * Number of teams
     *
     * @return the nuymber of teams
     */
    public int numberOfTeams()
    {
        return numberOfTeams;
    }

    /**
     * All the teams
     *
     * @return all the teams as an iterable
     */
    public Iterable<String> teams()
    {
        return teams;
    }

    //

    /**
     * The number of wins for a given team
     *
     * @param team
     * @return the number of wins for given team
     */
    public int wins(String team)
    {
        int i = checkTeam(team);

        return wins[i];
    }

    /**
     * The numer of losses for given team
     *
     * @param team
     * @return the number of losses for given team
     */
    public int losses(String team)
    {
        int i = checkTeam(team);

        return losses[i];
    }

    /**
     * The number of remaining games for given team
     * @param team
     * @return the number of remaining games for given team
     */
    public int remaining(String team)
    {
        int i = checkTeam(team);

        return remaining[i];
    }

    /**
     * The number of remaining games between one team and another team
     *
     * @param team1
     * @param team2
     * @return the number of remaining games between team1 and team2
     */
    public int against(String team1, String team2)
    {
        int i = checkTeam(team1);
        int j = checkTeam(team2);

        return games[i][j];
    }

    /**
     * Is a given team eliminated?
     *
     * @param team
     * @return true if given team eliminated, false otherwise
     */
    public boolean isEliminated(String team)
    {
        int x = checkTeam(team);
        isEliminatedExec = true;

        return computeR(x);
    }

    /**
     *
     * @param team
     * @return the subset R of teams that eliminates given team; null if not eliminated
     */
    public Iterable<String> certificateOfElimination(String team)
    {
        int x = checkTeam(team);
        if (!isEliminatedExec) computeR(x);

        isEliminatedExec = false;
        if (R.isEmpty()) return null;

        List<String> result = new ArrayList<String>();

        for (int i : R)
        {
            result.add(teams.get(i));
        }

        return result;
    }

    // ----------------------------
    // Helper methods
    // ----------------------------
    private boolean computeR(int x)
    {
        R = new ArrayList<Integer>();

        // trivial eliminated solution
        for (int i = 0; i < numberOfTeams; i++)
        {
            if (i != x && wins[x] + remaining[x] < wins[i])
            {
                R.add(i);

                return true;
            }
        }

        // nontrivial eliminated solution
        int count = numberOfTeams * numberOfTeams + numberOfTeams + 2;
        FlowNetwork fn = buildFlowNetwork(x, count);

        // compute min cut with ford-fulkerson method
        FordFulkerson FF = new FordFulkerson(fn, count-2, count-1);

        for (int i = 0; i < numberOfTeams; i++)
        {
            if (FF.inCut(i)) R.add(i);
        }

        return wins[x] + remaining[x] < averageR(x);
    }

    private FlowNetwork buildFlowNetwork(int x, int count)
    {
        boolean[][] gMarked = new boolean[numberOfTeams][numberOfTeams];
        FlowNetwork fn = new FlowNetwork(count);

        // connect team vertices to t
        for (int i = 0; i < numberOfTeams; i++)
        {
            if (i != x)
                fn.addEdge(new FlowEdge(i, count - 1, wins[x] + remaining[x] - wins[i]));
        }

        // connect s to game vertices and game vertices to team vertices
        for (int i = numberOfTeams; i < numberOfTeams * numberOfTeams + numberOfTeams; i++)
        {
            int m = (i - numberOfTeams) % numberOfTeams;
            int n = (i - numberOfTeams) / numberOfTeams;
            int cap = games[m][n];

            if (x != m && x != n && m != n && !gMarked[n][m])
            {
                fn.addEdge(new FlowEdge(count-2, i, cap));
                fn.addEdge(new FlowEdge(i, m, Double.POSITIVE_INFINITY));
                fn.addEdge(new FlowEdge(i, n, Double.POSITIVE_INFINITY));
                gMarked[m][n] = true;
            }
        }

        return fn;
    }

    private int checkTeam(String team)
    {
        if (!teams.contains(team))
            throw new java.lang.IllegalArgumentException("Invalid Team:" + team);

        return teams.indexOf(team);
    }

    private double averageR(int x)
    {
        int wr = 0, gr = 0;
        boolean[][] gMarked = new boolean[numberOfTeams][numberOfTeams];

        for (int i : R)
        {
            wr += wins[i];
            for (int j = 0; j < numberOfTeams; j++)
            {
                if (i != j && j != x && !gMarked[j][i])
                {
                    gr += games[i][j];
                    gMarked[i][j] = true;
                }
            }
        }

        return (double) (wr + gr) / R.size();
    }

    /**
     * Test method
     *
     * @param args
     */
    public static void main(String[] args)
    {
        BaseballElimination division = new BaseballElimination(args[0]);

        for (String team : division.teams())
        {
            if (division.isEliminated(team))
            {
                StdOut.print(team + " is eliminated by the subset R = { ");

                for (String t : division.certificateOfElimination(team))
                {
                    StdOut.print(t + " ");
                }

                StdOut.println("}");
            }
            else
            {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}