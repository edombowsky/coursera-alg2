import java.util.HashMap;
import java.util.Map;

public class BaseballElimination
{
    private int numTeams;
    private int vertNum;
    private int betweenTeamVertNum;
    private Map<String, Integer> teamNamesReverse; // int -> TeamName
    private TeamStat[] teamStats;
    private int[][] teamPairVertNum;
    private Map<Integer, TeamPair> teamPairVertNumReverse;
    private TeamStat highestWinTeam;

    private static class TeamStat
    {
        private int index;
        private String teamName;
        private int wins;
        private int losses;
        private int remaining;
        private int[] remAgainst;
        private boolean elCalculated;
        private boolean isEliminated;
        private boolean elCertCalculated;
        private Bag<String> elCertificate;

        private static TeamStat(int index,
                         String teamName,
                         int wins,
                         int losses,
                         int remaining,
                         int[] remAgainst)
        {
            this.index      = index;
            this.teamName   = teamName;
            this.wins       = wins;
            this.losses     = losses;
            this.remaining  = remaining;
            this.remAgainst = remAgainst;
        }

        private TeamStat()
        {
        }
    }

    private static class TeamPair
    {
        private int t1;
        private int t2;

        public TeamPair(int t1, int t2)
        {
            this.t1 = t1;
            this.t2 = t2;
        }
    }

    public BaseballElimination(String filename)
    {
        highestWinTeam = new BaseballElimination.TeamStat();

        // create a baseball division from given filename in format specified below
        In in = new In(filename);

        numTeams           = Integer.parseInt(in.readLine());
        betweenTeamVertNum = numTeams*(numTeams - 1) / 2;
        vertNum            = betweenTeamVertNum + numTeams + 2;

        teamStats        = new TeamStat[numTeams];
        teamNamesReverse = new HashMap<String, Integer>();

        int[] tempTeamRem;
        int currentTeam = 0;
        String line;
        String[] chopped;
        TeamStat teamStat;

        teamPairVertNum        = new int[numTeams][numTeams];
        teamPairVertNumReverse = new HashMap<Integer, BaseballElimination.TeamPair>();

        while (currentTeam < numTeams)
        {
            line = in.readLine();
            chopped = line.trim().split("\\s+");

            teamNamesReverse.put(chopped[0], currentTeam);
            teamStat           = new TeamStat();
            tempTeamRem        = new int[numTeams];
            teamStat.index     = currentTeam;
            teamStat.teamName  = chopped[0];
            teamStat.wins      = Integer.parseInt(chopped[1]);
            teamStat.losses    = Integer.parseInt(chopped[2]);
            teamStat.remaining = Integer.parseInt(chopped[3]);

            for (int i = 0; i < numTeams; i++)
            {
                tempTeamRem[i] = Integer.parseInt(chopped[i+4]);
            }

            teamStat.remAgainst = tempTeamRem;

            if (teamStat.wins > highestWinTeam.wins) highestWinTeam = teamStat;

            teamStats[currentTeam] = teamStat;
            currentTeam++;
        }

        TeamPair teamPair;
        int teamVertN;

        for (int team1 = 0; team1 < numTeams; team1++)
        {
            for (int team2 = team1 + 1; team2 < numTeams; team2++)
            {
                teamPair = new TeamPair(team1, team2);
                teamVertN = getTeamVertNum(team1, team2);
                teamPairVertNum[team1][team2] = teamVertN;
                teamPairVertNumReverse.put(teamVertN, teamPair);
            }
        }
    }

    private int getFinalVertNum()
    {
        return vertNum - 1;
    }

    private int getBeginVertNum()
    {
        return 0;
    }

    private int getTeamVertNum(int team)
    {
        return 1 + betweenTeamVertNum + team;
    }

    private int getTeamVertNum(int team1, int team2)
    {
        if (team1 > team2) return getTeamVertNum(team2, team1);

        return betweenTeamVertNum - (numTeams - team1)
                * (numTeams - team1 - 1) / 2
                + team2 - team1;
    }

    public int numberOfTeams()
    {
        return numTeams;
    }

    public Iterable<String> teams()
    {
        return teamNamesReverse.keySet();
    }

    private void checkArgument(String team)
    {
        if (!teamNamesReverse.containsKey(team)) throw new java.lang.IllegalArgumentException();
    }

    public int wins(String team)
    {
        checkArgument(team);

        return teamStats[teamNamesReverse.get(team)].wins;
    }

    public int losses(String team)
    {
        checkArgument(team);

        return teamStats[teamNamesReverse.get(team)].losses;
    }

    public int remaining(String team)
    {
        checkArgument(team);

        return teamStats[teamNamesReverse.get(team)].remaining;
    }

    public int against(String team1, String team2)
    {
        checkArgument(team1);
        checkArgument(team2);

        return teamStats[teamNamesReverse.get(team1)]
                .remAgainst[teamNamesReverse.get(team2)];
    }

    private boolean isTriviallyEliminated(String team)
    {
        TeamStat thisTeam = teamStats[teamNamesReverse.get(team)];

        if (thisTeam.wins + thisTeam.remaining < highestWinTeam.wins)
        {
            return true;
        }

        return false;
    }

    public boolean isEliminated(String team)
    {
        checkArgument(team);

        //Trivial elimination
        TeamStat thisTeam = teamStats[teamNamesReverse.get(team)];

        if (thisTeam.elCalculated)
        {
            return thisTeam.isEliminated;
        }


        if (isTriviallyEliminated(team))
        {
            thisTeam.elCalculated = true;
            thisTeam.isEliminated = true;
            thisTeam.elCertCalculated = true;
            Bag<String> elCert = new Bag<String>();
            elCert.add(highestWinTeam.teamName);
            thisTeam.elCertificate = elCert;

            return true;
        }

        FlowNetwork flowNetwork = new FlowNetwork(vertNum);
        int beginVert = getBeginVertNum();
        int finalVert = getFinalVertNum();
        FlowEdge tempEdge;
        TeamStat teamStat1, teamStat2;

        for (int team1 = 0; team1 < numTeams; team1++)
        {
            teamStat1 = teamStats[team1];
            tempEdge = new FlowEdge(getTeamVertNum(team1), finalVert,
                                    thisTeam.wins + thisTeam.remaining - teamStat1.wins);
            flowNetwork.addEdge(tempEdge);

            for (int team2 = team1 + 1; team2 < numTeams; team2++)
            {
                teamStat2 = teamStats[team2];
                tempEdge = new FlowEdge(beginVert, teamPairVertNum[team1][team2],
                                        teamStat1.remAgainst[team2]);
                flowNetwork.addEdge(tempEdge);
                tempEdge = new FlowEdge(teamPairVertNum[team1][team2],
                                        getTeamVertNum(team1),
                                        Double.POSITIVE_INFINITY);
                flowNetwork.addEdge(tempEdge);
                tempEdge = new FlowEdge(teamPairVertNum[team1][team2],
                                        getTeamVertNum(team2),
                                        Double.POSITIVE_INFINITY);
                flowNetwork.addEdge(tempEdge);
            }
        }

        FordFulkerson ff = new FordFulkerson(flowNetwork, beginVert, finalVert);
        Bag<String> elCert;

        for (FlowEdge edge: flowNetwork.adj(beginVert))
        {
            if (edge.capacity() - edge.flow() > 0.001)
            {
                thisTeam.elCalculated = true;
                thisTeam.isEliminated = true;
                elCert = new Bag<String>();

                for (int i = 0; i < numTeams; i++)
                {
                    if (ff.inCut(getTeamVertNum(i))) elCert.add(teamStats[i].teamName);
                }

                thisTeam.elCertCalculated = true;
                thisTeam.elCertificate = elCert;

                return true;
            }

        }

        thisTeam.elCalculated     = true;
        thisTeam.isEliminated     = false;
        thisTeam.elCertCalculated = true;
        thisTeam.elCertificate    = null;

        return false;
    }

    public Iterable<String> certificateOfElimination(String team)
    {
        checkArgument(team);

        TeamStat thisTeam = teamStats[teamNamesReverse.get(team)];

        if (thisTeam.elCertCalculated) return thisTeam.elCertificate;

        isEliminated(team);

        return thisTeam.elCertificate;
    }


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
}


//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class BaseballElimination
//{
//    private static final double EPSILON = 1E-6;
//
//    private int maxWinCountCurrent;
//    private String maxWinTeamCurrent;
//    private int teamCount;
//    private ArrayList<String> teamNames;
//    private HashMap<String, Integer> team2Index;
//    private int[] winCounts;
//    private int[] lossCounts;
//    private int[] remainingCounts;
//    private int[][] remainGames;
//
//    /**
//     * Create a baseball division from given filename in format specified below
//     *
//     * @param filename
//     */
//    public BaseballElimination(String filename)
//    {
//        In in = new In(filename);
//        teamCount = in.readInt();
//
//        // Init members
//        teamNames       = new ArrayList<String>();
//        team2Index      = new HashMap<String, Integer>();
//        winCounts       = new int[teamCount];
//        lossCounts      = new int[teamCount];
//        remainingCounts = new int[teamCount];
//        remainGames     = new int[teamCount][teamCount];
//
//        // read data
//        for (int i = 0; i < teamCount; i++)
//        {
//            String teamName = in.readString();
//
//            teamNames.add(teamName);
//            team2Index.put(teamName, i);
//            winCounts[i] = in.readInt();
//
//            if (winCounts[i] > maxWinCountCurrent)
//            {
//                maxWinCountCurrent = winCounts[i];
//                maxWinTeamCurrent = teamNames.get(i);
//            }
//
//            lossCounts[i]      = in.readInt();
//            remainingCounts[i] = in.readInt();
//
//            for (int j = 0; j < teamCount; j++)
//            {
//                remainGames[i][j] = in.readInt();
//            }
//        }
//    }
//
//    /**
//     * Number of teams
//     *
//     * @return the number of teams
//     */
//    public int numberOfTeams()
//    {
//        return teamCount;
//    }
//
//    /**
//     * All teams
//     *
//     * @return all teams
//     */
//    public Iterable<String> teams()
//    {
//        return teamNames;
//    }
//
//    private void checkArgument(String team)
//    {
//        if (!team2Index.containsKey(team)) throw new IllegalArgumentException();
//    }
//
//    /**
//     * Number of wins for given team
//     *
//     * @param team
//     * @return the numer of wins for given team
//     */
//    public int wins(String team)
//    {
//        checkArgument(team);
//
//        return winCounts[team2Index.get(team)];
//    }
//
//    /**
//     * Number of losses for given team
//     *
//     * @param team
//     * @return the number of losses fir given team
//     */
//    public int losses(String team)
//    {
//        checkArgument(team);
//
//        return lossCounts[team2Index.get(team)];
//    }
//
//    /**
//     * Number of remaining games for given team
//     *
//     * @param team
//     * @return the number of games remaining for given team
//     */
//    public int remaining(String team)
//    {
//        checkArgument(team);
//
//        return remainingCounts[team2Index.get(team)];
//    }
//
//    /**
//     * Number of remaining games between team1 and team2
//     *
//     * @param team1
//     * @param team2
//     * @return the number of remaining games between team1 and team2
//     */
//    public int against(String team1, String team2)
//    {
//        checkArgument(team1);
//        checkArgument(team2);
//
//        return remainGames[team2Index.get(team1)][team2Index.get(team2)];
//    }
//
//    /**
//     * Is given team eliminated?
//     *
//     * @param team
//     * @return true if given team is eliminated, false otherwise
//     */
//    public boolean isEliminated(String team)
//    {
//        checkArgument(team);
//
//        int teamIndex           = team2Index.get(team);
//        int maxPossibleWinCount = remainingCounts[teamIndex] + winCounts[teamIndex];
//
//        if (maxPossibleWinCount < maxWinCountCurrent) return true;
//
//        int nodeCount = 2                           // s, t
//                + teamCount * (teamCount - 1) / 2   // game vertices
//                + teamCount;                        // team vertices
//
//        // 0..N-1 for team vertices
//        // N..N*(N+1)/2 for game vertices
//        // last 2 node for s and t
//        FlowNetwork network = new FlowNetwork(nodeCount);
//        int s = nodeCount - 2;
//        int t = nodeCount - 1;
//        double sCapacity = 0;
//
//        // edge from team vertices to t
//        for (int i = 0; i < teamCount; i++)
//        {
//            if (i != teamIndex)
//            {
//                double weight = maxPossibleWinCount - winCounts[i];
//                FlowEdge edge = new FlowEdge(i, t, weight);
//
//                network.addEdge(edge);
//            }
//        }
//
//        int gameNodeIndex = teamCount - 1;
//
//        for (int i = 0; i < teamCount; i++)
//        {
//            for (int j = i + 1; j < teamCount; j++)
//            {
//                gameNodeIndex++;
//
//                if (i == teamIndex || j == teamIndex) continue;
//
//                // edge from s to game vertices
//                double weight = remainGames[i][j];
//                FlowEdge edge = new FlowEdge(s, gameNodeIndex, weight);
//
//                network.addEdge(edge);
//                sCapacity += weight;
//
//                // edges from game vertices to team vertices
//                edge = new FlowEdge(gameNodeIndex, i, Double.POSITIVE_INFINITY);
//                network.addEdge(edge);
//                edge = new FlowEdge(gameNodeIndex, j, Double.POSITIVE_INFINITY);
//                network.addEdge(edge);
//            }
//        }
//
//        FordFulkerson alg = new FordFulkerson(network, s, t);
//
//        return !(Math.abs(alg.value() - sCapacity) < 1E-6);
//    }
//
//    /**
//     * Provide certification of elimination.
//     *
//     * @param team
//     * @return the subset R of teams that eliminates given team; null if not eliminated
//     */
//    public Iterable<String> certificateOfElimination(String team)
//    {
//        checkArgument(team);
//
//        int teamIndex = team2Index.get(team);
//        int maxPossibleWinCount = remainingCounts[teamIndex] + winCounts[teamIndex];
//
//        if (maxPossibleWinCount < maxWinCountCurrent)
//        {
//            ArrayList<String> result = new ArrayList<String>();
//            result.add(maxWinTeamCurrent);
//
//            return result;
//        }
//
//        int nodeCount = 2                           // s, t
//                + teamCount * (teamCount - 1) / 2   // game vertices
//                + teamCount;                        // team vertices
//
//        // 0..N-1 for team vertices
//        // N..N*(N+1)/2 for game vertices
//        // last 2 node for s and t
//        FlowNetwork network = new FlowNetwork(nodeCount);
//        int s            = nodeCount - 2;
//        int t            = nodeCount - 1;
//        double sCapacity = 0;
//
//        // edge from team vertices to t
//        for (int i = 0; i < teamCount; i++)
//        {
//            if (i != teamIndex)
//            {
//                double weight = maxPossibleWinCount - winCounts[i];
//                FlowEdge edge = new FlowEdge(i, t, weight);
//
//                network.addEdge(edge);
//            }
//        }
//
//        int gameNodeIndex = teamCount - 1;
//
//        for (int i = 0; i < teamCount; i++)
//        {
//            for (int j = i + 1; j < teamCount; j++)
//            {
//                gameNodeIndex++;
//
//                if (i == teamIndex || j == teamIndex) continue;
//
//                // edge from s to game vertices
//                double weight = remainGames[i][j];
//                FlowEdge edge = new FlowEdge(s, gameNodeIndex, weight);
//
//                network.addEdge(edge);
//                sCapacity += weight;
//
//                // edges from game vertices to team vertices
//                edge = new FlowEdge(gameNodeIndex, i, Double.POSITIVE_INFINITY);
//                network.addEdge(edge);
//                edge = new FlowEdge(gameNodeIndex, j, Double.POSITIVE_INFINITY);
//                network.addEdge(edge);
//            }
//        }
//
//        FordFulkerson alg = new FordFulkerson(network, s, t);
//
//        if (Math.abs(alg.value() - sCapacity) < EPSILON) return null;
//
//        ArrayList<String> teams = new ArrayList<String>();
//
//        for (int i = 0; i < teamCount; i++)
//        {
//            if (alg.inCut(i)) teams.add(teamNames.get(i));
//        }
//
//        return teams;
//    }
//
//    /**
//     * Test client
//     *
//     * @param args
//     */
//    public static void main(String[] args)
//    {
//        BaseballElimination division = new BaseballElimination(args[0]);
//
//        for (String team : division.teams())
//        {
//            if (division.isEliminated(team))
//            {
//                StdOut.print(team + " is eliminated by the subset R = { ");
//
//                for (String t : division.certificateOfElimination(team))
//                {
//                    StdOut.print(t + " ");
//                }
//
//                StdOut.println("}");
//            }
//            else
//            {
//                StdOut.println(team + " is not eliminated");
//            }
//        }
//    }
//
////    % java BaseballElimination teams4.txt
////    Atlanta is not eliminated
////    Philadelphia is eliminated by the subset R = { Atlanta New_York }
////    New_York is not eliminated
////    Montreal is eliminated by the subset R = { Atlanta }
////
////    % java BaseballElimination teams5.txt
////    New_York is not eliminated
////    Baltimore is not eliminated
////    Boston is not eliminated
////    Toronto is not eliminated
////    Detroit is eliminated by the subset R = { New_York Baltimore Boston Toronto }
//}
