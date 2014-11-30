import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class BaseballElimination
{
    private HashMap<String, Integer> name2ID;
    private String[] id2Name;
    private int[] wins;
    private int[] loss;
    private int[] remaining;
    private int[][] games;
    // for trivial elimination
    private int maxWins;
    private String maxWinsTeam;
    private Iterable<String> eliminations;

    /**
     * Create a baseball division from given filename in format specified below
     *
     * @param filename
     */
    public BaseballElimination(String filename)
    {
        In fileIn   = new In(filename);
        int teamNum = Integer.parseInt(fileIn.readLine());

        name2ID      = new HashMap<String, Integer>(teamNum);
        id2Name      = new String[teamNum];
        wins         = new int[teamNum];
        maxWins      = 0;
        maxWinsTeam  = null;
        loss         = new int[teamNum];
        remaining    = new int[teamNum];
        games        = new int[teamNum][teamNum];
        eliminations = null;

        // initialize
        for (int i = 0; i < teamNum; i++)
        {
            String[] teamInfos = fileIn.readLine().trim().split(" +");

            name2ID.put(teamInfos[0], i);

            id2Name[i] = teamInfos[0];
            wins[i]    = Integer.parseInt(teamInfos[1]);

            if (wins[i] > maxWins)
            {
                maxWins     = wins[i];
                maxWinsTeam = teamInfos[0];
            }

            loss[i]      = Integer.parseInt(teamInfos[2]);
            remaining[i] = Integer.parseInt(teamInfos[3]);

            for (int j = 0; j < teamNum; j++)
            {
                games[i][j] = Integer.parseInt(teamInfos[4 + j]);
            }
        }
    }

    /**
     * Number of teams
     *
     * @return
     */
    public int numberOfTeams()
    {
        return name2ID.size();
    }

    /**
     * All teams
     *
     * @return
     */
    public Iterable<String> teams()
    {
        return name2ID.keySet();
    }

    /**
     * Number of wins for given team
     *
     * @param team
     * @return
     */
    public int wins(String team)
    {
        if (!name2ID.containsKey(team)) throw new IllegalArgumentException();

        return wins[name2ID.get(team)];
    }

    /**
     * Number of losses for given team
     *
     * @param team
     * @return
     */
    public int losses(String team)
    {
        if (!name2ID.containsKey(team)) throw new IllegalArgumentException();

        return loss[name2ID.get(team)];
    }

    /**
     * Number of remaining games for given team
     *
     * @param team
     * @return
     */
    public int remaining(String team)
    {
        if (!name2ID.containsKey(team)) throw new IllegalArgumentException();

        return remaining[name2ID.get(team)];
    }

    /**
     * Number of remaining games between team1 and team2
     *
     * @param team1
     * @param team2
     * @return
     */
    public int against(String team1, String team2)
    {
        if (!name2ID.containsKey(team1) || !name2ID.containsKey(team2))
            throw new IllegalArgumentException();

        return games[name2ID.get(team1)][name2ID.get(team2)];
    }

    /**
     * Is given team eliminated?
     *
     * @param team
     * @return
     */
    public boolean isEliminated(String team)
    {
        if (!name2ID.containsKey(team)) throw new IllegalArgumentException();

        // current id of team
        int id = name2ID.get(team);

        // trivial elimination
        if (wins[id] + remaining[id] < maxWins)
        {
            ArrayList<String> eliminationsList = new ArrayList<String>();
            eliminationsList.add(maxWinsTeam);
            eliminations = eliminationsList;

            return true;
        }

        //nontrivial elimination
        // # of vertices = 2 + n-1 + C(n-1,2)
        FlowNetwork baseball = new FlowNetwork(2 + numberOfTeams() - 1
                                               + (numberOfTeams() - 1)
                                               * (numberOfTeams() - 2) / 2);

        // max total wins for each team
        int maxIdWins = wins[id] + remaining[id];

        // add edge to t( id = 1)
        for (int i = 0; i < numberOfTeams(); i++) {
            if (i != id)
            {
                baseball.addEdge(new FlowEdge(teamId2EdgeId(i, id), 1,
                                              maxIdWins - wins[i]));
            }
        }

        // number of edges already added
        int numOfEdges = 2 + numberOfTeams() - 1;

        // add edges from s( id = 0) and to 0~n-1
        for (int j = 0; j < numberOfTeams(); j++)
        {
            if (j != id)
            {
                for (int k = j + 1; k < numberOfTeams(); k++)
                {
                    if (k != id)
                    {
                        baseball.addEdge(new FlowEdge(0, numOfEdges, games[j][k]));
                        baseball.addEdge(new FlowEdge(numOfEdges, teamId2EdgeId(j, id), games[j][k]));
                        baseball.addEdge(new FlowEdge(numOfEdges, teamId2EdgeId(k, id), games[j][k]));
                        numOfEdges++;
                    }
                }
            }
        }

        //Max flow
        FordFulkerson baseballFordFulkerson = new FordFulkerson(baseball, 0, 1);

        for (FlowEdge e:baseball.adj(0))
        {
            if (e.flow() != e.capacity())
            {
                findSubsets(baseball, baseballFordFulkerson, id);

                return true;
            }
        }

        eliminations = null;

        return false;
    }

    private int teamId2EdgeId(int teamId, int desTeamId)
    {
        return teamId < desTeamId ? teamId + 2 : teamId + 2 - 1;
    }

    private int edgeId2TeamId(int edgeId, int desTeamId)
    {
        return edgeId - 2 < desTeamId ? edgeId - 2 : edgeId - 2 + 1;
    }

    private void findSubsets(FlowNetwork fn, FordFulkerson ff, int desTeamId)
    {
        HashSet<Integer> ids = new HashSet<Integer>();

        for (FlowEdge e:fn.adj(0))
        {
            int matchId = e.other(0);

            // belongs to the s side
            if (ff.inCut(matchId))
            {
                for (FlowEdge adj:fn.adj(matchId))
                {
                    int edgeId = adj.other(matchId);

                    if (edgeId != 0) ids.add(edgeId2TeamId(edgeId, desTeamId));
                }
            }
        }

        ArrayList<String> eliminationsList = new ArrayList<String>();

        for (int id:ids)
        {
            eliminationsList.add(id2Name[id]);
        }

        eliminations = eliminationsList;
    }

    /**
     * Subset R of teams that eliminates given team; null if not eliminated
     *
     * @param team
     * @return
     */
    public Iterable<String> certificateOfElimination(String team)
    {
        if (!name2ID.containsKey(team)) throw new IllegalArgumentException();

        return isEliminated(team) ? eliminations : null;
    }

    // test client
    public static void main(String[] args)
    {
        BaseballElimination division = new BaseballElimination(args[0]);

        for (String team : division.teams())
        {
            if (division.isEliminated(team))
            {
                StdOut.print(team + " is eliminated by the subset R = { ");

                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");

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
