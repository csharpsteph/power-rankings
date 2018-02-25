package metrics;

import java.util.HashMap;
import java.util.Map;

import nodes.Competitor;
import nodes.Team;
import parse.AbstractGame;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* Calculates team, home, and away records from a GameList and stores them.
 * Also provides a way to calculate opponent strength (or the sum of opponent records)
 * with a provided set of records.
 */
public class TeamRecords<Game extends AbstractGame<?>, C extends Competitor> {
	
	private Map<String, C> competitorMap;
	private Map<C, Record> overallRecords, awayRecords, homeRecords;
	//private List<Game> gameList;
	private GameList<Game> gameList;
	
	TeamRecords(Map<String, C> teamMap, GameList<Game> gameList)
	{
		this.competitorMap = teamMap;
		this.gameList = gameList;
	}
	
	public Map<C, Record> teamRecords()
	{
		return new HashMap<>(overallRecords);
	}
	
	public Map<C, Record> homeRecords()
	{
		return new HashMap<>(homeRecords);
	}
	
	public Map<C, Record> awayRecords()
	{
		return new HashMap<>(awayRecords);
	}
	
	/* 	Note: A team's opponent record may include games in which this team faced the opponent.
	 */
	public static <N extends AbstractGame<?>, C extends Competitor> 
		Map<C, Record> opponentRecords(Map<C, Record> teamRecords, GameList<N> gameList)
	{
		Map<C, Record> oppRecordMap = new HashMap<>();
		for (C t: teamRecords.keySet())
		{
			oppRecordMap.put(t, new Record());
		}
		
		for (N g: gameList)
		{
			Team team1 = (Team)g.getCompetitor1();
			Team team2 = (Team)g.getCompetitor2();
			
			oppRecordMap.get(team1).add(teamRecords.get(team2));
			oppRecordMap.get(team2).add(teamRecords.get(team1));
		}
		return oppRecordMap;
	}
	// Commences the read.
	public void readGames()
	{
		overallRecords = new HashMap<>();
		awayRecords = new HashMap<>();
		homeRecords = new HashMap<>();
		
		for (C t: competitorMap.values())
		{
			overallRecords.put(t, new Record());
			homeRecords.put(t, new Record());
			awayRecords.put(t, new Record());
		}
		
		for (Game g: gameList)
		{
			Team team1 = (Team)g.getCompetitor1();
			Team team2 = (Team)g.getCompetitor2();
			
			if (g.isComplete())
			{
				if (g.isADraw())
				{
					overallRecords.get(team1).draw();
					overallRecords.get(team2).draw();
					awayRecords.get(team1).draw();
					homeRecords.get(team2).draw();
				} 
				else {
					if (g.winner() == team1)
					{
						overallRecords.get(team1).win();
						overallRecords.get(team2).lose();
						if (!g.isNeutralSite())
						{
							awayRecords.get(team1).win();
							homeRecords.get(team2).lose();
						}
					}
					else
					{
						overallRecords.get(team2).win();
						overallRecords.get(team1).lose();
						if (!g.isNeutralSite())
						{
							awayRecords.get(team1).lose();
							homeRecords.get(team2).win();
						}
					}
				}
			}
		}
	}

}

