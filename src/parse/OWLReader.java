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

public class OWLReader extends GameReader<OWLGame, Team, String> {
	
	public OWLReader(File teamDataSource, File gameDataSource) throws ClassNotFoundException, IOException
	{
		super(teamDataSource, gameDataSource);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadCompetitorData(File teamDataSource) throws ClassNotFoundException, IOException
	{
		ObjectInputStream istream = new ObjectInputStream(new FileInputStream(teamDataSource));
		competitorGraph = (Graph<String>)istream.readObject();
		istream.close();
		
		Collection<Node<String>> nodes = competitorGraph.getNodesAtLevel(2);
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
		byte stageNum, weekNum;
		String team1Str, team2Str;
		Team team1, team2;
		Byte team1Score, team2Score;
		boolean isPlayoffGame;
		
		boolean eof = false;
		OWLGame game;
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
			stageNum = istream.readByte();
			weekNum = istream.readByte();
			team1Str = istream.readUTF();
			team1 = competitorMap.get(team1Str);
			team1Score = istream.readByte();
			if (team1Score < 0)
				team1Score = null;
			team2Str = istream.readUTF();
			team2 = competitorMap.get(team2Str);
			team2Score = istream.readByte();
			if (team2Score < 0)
				team2Score = null;
			isPlayoffGame = istream.readBoolean();
			game = new OWLGame(team1, team1Score, team2, team2Score, date, stageNum, weekNum);
			game.setPlayoff(isPlayoffGame);
			gameList.add(game);
		}
		
		istream.close();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		File teamDataSource = new File("resources/owl/owl-graph.dat");
		File gameDataSource = new File("resources/owl/owl-clean.dat");
		
		OWLReader reader = new OWLReader(teamDataSource, gameDataSource);
		Map<String, Team> teamMap = reader.competitorMap;
		List<OWLGame> gameList = reader.gameList;
		teamMap.forEach((shortName, team) ->
		{
			System.out.printf("%s: %s\n", shortName, team);
		});
		gameList.forEach((game) ->
		{
			System.out.printf("%s%s\n", game, game.isPlayoffGame() ? " *" : "");
		});
		
	}
}
