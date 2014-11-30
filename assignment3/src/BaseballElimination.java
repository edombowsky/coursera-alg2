import java.util.ArrayList;
import java.util.HashMap;

public class BaseballElimination
{
    private static final double EPSILON = 1E-6;

    private int maxWinCountCurrent;
    private String maxWinTeamCurrent;
    private int teamCount;
    private ArrayList<String> teamNames;
    private HashMap<String, Integer> team2Index;
    private int[] winCounts;
    private int[] lossCounts;
    private int[] remainingCounts;
    private int[][] remainGames;

    /**
     * Create a baseball division from given filename in format specified below
     *
     * @param filename
     */
    public BaseballElimination(String filename)
    {
        In in = new In(filename);
        teamCount = in.readInt();

        // Init members
        teamNames       = new ArrayList<String>();
        team2Index      = new HashMap<String, Integer>();
        winCounts       = new int[teamCount];
        lossCounts      = new int[teamCount];
        remainingCounts = new int[teamCount];
        remainGames     = new int[teamCount][teamCount];

        // read data
        for (int i = 0; i < teamCount; i++)
        {
            String teamName = in.readString();

            teamNames.add(teamName);
            team2Index.put(teamName, i);
            winCounts[i] = in.readInt();

            if (winCounts[i] > maxWinCountCurrent)
            {
                maxWinCountCurrent = winCounts[i];
                maxWinTeamCurrent = teamNames.get(i);
            }

            lossCounts[i]      = in.readInt();
            remainingCounts[i] = in.readInt();

            for (int j = 0; j < teamCount; j++)
            {
                remainGames[i][j] = in.readInt();
            }
        }
    }

    /**
     * Number of teams
     *
     * @return the number of teams
     */
    public int numberOfTeams()
    {
        return teamCount;
    }

    /**
     * All teams
     *
     * @return all teams
     */
    public Iterable<String> teams()
    {
        return teamNames;
    }

    private void checkArgument(String team)
    {
        if (!team2Index.containsKey(team)) throw new IllegalArgumentException();
    }

    /**
     * Number of wins for given team
     *
     * @param team
     * @return the numer of wins for given team
     */
    public int wins(String team)
    {
        checkArgument(team);

        return winCounts[team2Index.get(team)];
    }

    /**
     * Number of losses for given team
     *
     * @param team
     * @return the number of losses fir given team
     */
    public int losses(String team)
    {
        checkArgument(team);

        return lossCounts[team2Index.get(team)];
    }

    /**
     * Number of remaining games for given team
     *
     * @param team
     * @return the number of games remaining for given team
     */
    public int remaining(String team)
    {
        checkArgument(team);

        return remainingCounts[team2Index.get(team)];
    }

    /**
     * Number of remaining games between team1 and team2
     *
     * @param team1
     * @param team2
     * @return the number of remaining games between team1 and team2
     */
    public int against(String team1, String team2)
    {
        checkArgument(team1);
        checkArgument(team2);

        return remainGames[team2Index.get(team1)][team2Index.get(team2)];
    }

    /**
     * Is given team eliminated?
     *
     * @param team
     * @return true if given team is eliminated, false otherwise
     */
    public boolean isEliminated(String team)
    {
        checkArgument(team);

        int teamIndex           = team2Index.get(team);
        int maxPossibleWinCount = remainingCounts[teamIndex] + winCounts[teamIndex];

        if (maxPossibleWinCount < maxWinCountCurrent) return true;

        int nodeCount = 2                           // s, t
                + teamCount * (teamCount - 1) / 2   // game vertices
                + teamCount;                        // team vertices

        // 0..N-1 for team vertices
        // N..N*(N+1)/2 for game vertices
        // last 2 node for s and t
        FlowNetwork network = new FlowNetwork(nodeCount);
        int s = nodeCount - 2;
        int t = nodeCount - 1;
        double sCapacity = 0;

        // edge from team vertices to t
        for (int i = 0; i < teamCount; i++)
        {
            if (i != teamIndex)
            {
                double weight = maxPossibleWinCount - winCounts[i];
                FlowEdge edge = new FlowEdge(i, t, weight);

                network.addEdge(edge);
            }
        }

        int gameNodeIndex = teamCount - 1;

        for (int i = 0; i < teamCount; i++)
        {
            for (int j = i + 1; j < teamCount; j++)
            {
                gameNodeIndex++;

                if (i == teamIndex || j == teamIndex) continue;

                // edge from s to game vertices
                double weight = remainGames[i][j];
                FlowEdge edge = new FlowEdge(s, gameNodeIndex, weight);

                network.addEdge(edge);
                sCapacity += weight;

                // edges from game vertices to team vertices
                edge = new FlowEdge(gameNodeIndex, i, Double.POSITIVE_INFINITY);
                network.addEdge(edge);
                edge = new FlowEdge(gameNodeIndex, j, Double.POSITIVE_INFINITY);
                network.addEdge(edge);
            }
        }

        FordFulkerson alg = new FordFulkerson(network, s, t);

        return !(Math.abs(alg.value() - sCapacity) < 1E-6);
    }

    /**
     * Provide certification of elimination.
     *
     * @param team
     * @return the subset R of teams that eliminates given team; null if not eliminated
     */
    public Iterable<String> certificateOfElimination(String team)
    {
        checkArgument(team);

        int teamIndex = team2Index.get(team);
        int maxPossibleWinCount = remainingCounts[teamIndex] + winCounts[teamIndex];

        if (maxPossibleWinCount < maxWinCountCurrent)
        {
            ArrayList<String> result = new ArrayList<String>();
            result.add(maxWinTeamCurrent);

            return result;
        }

        int nodeCount = 2                           // s, t
                + teamCount * (teamCount - 1) / 2   // game vertices
                + teamCount;                        // team vertices

        // 0..N-1 for team vertices
        // N..N*(N+1)/2 for game vertices
        // last 2 node for s and t
        FlowNetwork network = new FlowNetwork(nodeCount);
        int s            = nodeCount - 2;
        int t            = nodeCount - 1;
        double sCapacity = 0;

        // edge from team vertices to t
        for (int i = 0; i < teamCount; i++)
        {
            if (i != teamIndex)
            {
                double weight = maxPossibleWinCount - winCounts[i];
                FlowEdge edge = new FlowEdge(i, t, weight);

                network.addEdge(edge);
            }
        }

        int gameNodeIndex = teamCount - 1;

        for (int i = 0; i < teamCount; i++)
        {
            for (int j = i + 1; j < teamCount; j++)
            {
                gameNodeIndex++;

                if (i == teamIndex || j == teamIndex) continue;

                // edge from s to game vertices
                double weight = remainGames[i][j];
                FlowEdge edge = new FlowEdge(s, gameNodeIndex, weight);

                network.addEdge(edge);
                sCapacity += weight;

                // edges from game vertices to team vertices
                edge = new FlowEdge(gameNodeIndex, i, Double.POSITIVE_INFINITY);
                network.addEdge(edge);
                edge = new FlowEdge(gameNodeIndex, j, Double.POSITIVE_INFINITY);
                network.addEdge(edge);
            }
        }

        FordFulkerson alg = new FordFulkerson(network, s, t);

        if (Math.abs(alg.value() - sCapacity) < EPSILON) return null;

        ArrayList<String> teams = new ArrayList<String>();

        for (int i = 0; i < teamCount; i++)
        {
            if (alg.inCut(i)) teams.add(teamNames.get(i));
        }

        return teams;
    }

    /**
     * Test client
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

//    % java BaseballElimination teams4.txt
//    Atlanta is not eliminated
//    Philadelphia is eliminated by the subset R = { Atlanta New_York }
//    New_York is not eliminated
//    Montreal is eliminated by the subset R = { Atlanta }
//
//    % java BaseballElimination teams5.txt
//    New_York is not eliminated
//    Baltimore is not eliminated
//    Boston is not eliminated
//    Toronto is not eliminated
//    Detroit is eliminated by the subset R = { New_York Baltimore Boston Toronto }
}
