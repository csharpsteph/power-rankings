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

import nodes.Graph;
import nodes.Node;
import nodes.Team;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

public class NBAReader extends GameReader<NBAGame, Team, String> {
	
	public NBAReader(File teamDataSource, File gameDataSource) throws ClassNotFoundException, IOException
	{
		super(teamDataSource, gameDataSource);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadCompetitorData(File teamDataSource) throws FileNotFoundException, IOException, 
		ClassNotFoundException 
	{
		ObjectInputStream istream = new ObjectInputStream(new FileInputStream(teamDataSource));
		competitorGraph = (Graph<String>)istream.readObject();
		istream.close();
		
		Collection<Node<String>> nodes = competitorGraph.getNodesAtLevel(3);
		competitorMap = new HashMap<>();
		for (Node<String> n: nodes) 
		{
			Team t = (Team)n;
			competitorMap.put(t.getShortName(), t);
		}
		
	}
	
	protected void loadGameData(File gameDataSource) throws FileNotFoundException, IOException,
		ClassNotFoundException
	{
		ObjectInputStream istream = new ObjectInputStream(new FileInputStream(gameDataSource));
		
		Date date;
		String team1Str, team2Str;
		Team team1, team2;
		Short team1Score, team2Score;
		
		boolean eof = false;
		NBAGame game;
		gameList = new ArrayList<>();
		// Read games
		while (!eof)
		{
			date = (Date)istream.readObject();
			if (date == null)
			{
				eof = true;
				continue;
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
			game = new NBAGame(team1, team1Score, team2, team2Score, date);
			gameList.add(game);
		}
		
		istream.close();
		
	}
}
