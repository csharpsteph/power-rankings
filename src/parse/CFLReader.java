package parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nodes.Graph;
import nodes.Node;
import nodes.Team;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})
/*
* An extension of the GameReader class for the Canadian Football League.
*/
public class CFLReader extends GameReader<CFLGame, Team, String> {
	
	
	public CFLReader(File teamDataSource, File gameDataSource) throws ClassNotFoundException, IOException
	{
		super(teamDataSource, gameDataSource);
	}

	protected void loadGameData(File gameDataSource) throws FileNotFoundException, IOException
	{
		ObjectInputStream istream = new ObjectInputStream(new FileInputStream(gameDataSource));
		
		Date date = null;
		String weekStr;
		String team1Str, team2Str;
		Team team1, team2;
		Short team1Score, team2Score;
		
		boolean eof = false;
		CFLGame game;
		gameList = new ArrayList<>();
		// Read games
		while (!eof)
		{
			
			weekStr = istream.readUTF();
			if (weekStr.equals("~"))
			{
				eof = true;
				continue;
			}
			try {
				date = (Date)istream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			team1Str = istream.readUTF();
			team1 = competitorMap.get(team1Str);
			team1Score = istream.readShort();
			if (team1Score < 0)
				team1Score = null;
			team2Str = istream.readUTF();
			team2 = competitorMap.get(team2Str);
			team2Score = istream.readShort();
			if (team2Score < 0)
				team2Score = null;
			game = new CFLGame(team1, team1Score, team2, team2Score, date, weekStr);
			gameList.add(game);
		}
		
		istream.close();
		
	}

	@SuppressWarnings("unchecked")
	protected void loadCompetitorData(File teamDataSource) throws FileNotFoundException, IOException 
	{
		ObjectInputStream istream = new ObjectInputStream(new FileInputStream(teamDataSource));
		try {
			competitorGraph = (Graph<String>)istream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		istream.close();
		
		Collection<Node<String>> nodes = competitorGraph.getNodesAtLevel(2);
		competitorMap = new HashMap<>();
		for (Node<String> n: nodes) 
		{
			Team t = (Team)n;
			competitorMap.put(t.getShortName(), t);
		}
		
	}
	

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		File teamDataSource = new File("resources/cfl/cfl-graph.dat");
		File gameDataSource = new File("resources/cfl/cfl-clean.dat");
		
		CFLReader reader = new CFLReader(teamDataSource, gameDataSource);
		Map<String, Team> teamMap = reader.competitorMap;
		List<CFLGame> gameList = reader.gameList;
		
		teamMap.forEach((shortName, team) ->
		{
			System.out.printf("%s: %s\n", shortName, team);
		});
		gameList.forEach((game) ->
		{
			System.out.printf("%s\n", game);
		});
		
		//System.out.println(reader.teamGraph() == null);
		//PrintGraph.printGraphDepthFirst(reader.teamGraph().getRoot());
	}

}
