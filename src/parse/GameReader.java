package parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import nodes.Competitor;
import nodes.Graph;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})
/*
* An abstract class which reads object files and returns a game list and a team graph.
*/
public abstract class GameReader<Game extends AbstractGame<?>, N extends Competitor, 
		KeyType extends Comparable<KeyType>> {
	protected Map<String, N> competitorMap;
	protected List<Game> gameList;
	protected Graph<KeyType> competitorGraph;
	
	protected GameReader(File competitorDataSource, File gameDataSource) throws FileNotFoundException, 
		ClassNotFoundException, IOException
	{
		loadCompetitorData(competitorDataSource);
		loadGameData(gameDataSource);
	}
	
	public Map<String, N> competitorMap()
	{
		return competitorMap;
	}
	
	public Map<String, N> teamMap()
	{
		return competitorMap;
	}
	
	public List<Game> gameList()
	{
		return gameList;
	}
	
	public Graph<KeyType> competitorGraph()
	{
		return competitorGraph;
	}
	
	public Graph<KeyType> teamGraph()
	{
		return competitorGraph;
	}
	
	protected abstract void loadCompetitorData(File teamDataSource) throws FileNotFoundException, IOException, 
	ClassNotFoundException;
	protected abstract void loadGameData(File gameDataSource) throws FileNotFoundException, IOException, 
	ClassNotFoundException;
	
	
	
}
