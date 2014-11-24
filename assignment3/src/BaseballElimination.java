public class BaseballElimination
{
    /**
     * Create a baseball division from given filename in format specified below
     *
     * @param filename
     */
    public BaseballElimination(String filename)
    {

    }

    /**
     * Number of teams
     *
     * @return the number of teams
     */
    public int numberOfTeams()
    {

    }

    /**
     * All teams
     *
     * @return
     */
    public Iterable<String> teams()
    {

    }

    /**
     * Number of wins for given team
     *
     * @param team
     *
     * @return the number of winds for a given team
     *
     * @throws java.lang.IllegalArgumentException if the input team is an invalid team
     */
    public int wins(String team)
    {

    }

    /**
     * Number of losses for given team
     *
     * @param team
     *
     * @return the number of losses for a team
     *
     * @throws java.lang.IllegalArgumentException if the input team is an invalid team
     */
    public int losses(String team)
    {

    }

    /**
     * Number of remaining games for given team
     *
     * @param team
     *
     * @return the number of remaining games for a team
     *
     * @throws java.lang.IllegalArgumentException if the input team is an invalid team
     */
    public int remaining(String team)
    {

    }

    /**
     * Number of remaining games between team1 and team2
     *
     * @param team1
     * @param team2
     *
     * @return the number of remaining games between team1 and team2
     *
     * @throws java.lang.IllegalArgumentException if one (or both) of the input arguments are invalid teams.
     */
    public int against(String team1, String team2)
    {

    }

    /**
     * Is given team eliminated?
     *
     * @param team
     *
     * @return true if team is eliminated, false otherwise
     *
     * @throws java.lang.IllegalArgumentException if input team is an invalid team
     */
    public boolean isEliminated(String team)
    {

    }

    /**
     * Subset R of teams that eliminates given team; null if not eliminated
     *
     * @param team
     *
     * @return
     *
     * @throws java.lang.IllegalArgumentException if input team is an invalid team
     */
    public Iterable<String> certificateOfElimination(String team)
    {

    }

    /**
     * Used to test the class
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
