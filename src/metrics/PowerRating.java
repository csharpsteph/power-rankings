package metrics;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.*;

import nodes.Competitor;
import parse.AbstractGame;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* Reads games and returns Elo ratings. */
public class PowerRating<Game extends AbstractGame<?>> implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4617428167340107822L;

	private Map<Competitor, Double> m_ratings;
	private GameList<Game> gameList;
	private BiFunction<RatingPair, Game, RatingPair> updateFunction;	// Where the math happens
	private double defaultRating;
	private boolean trackChanges;	// Allows tracking of ratings on a game-by-game basis.
	// TreeMap automatically sorts by key, putting game indices in chronological order.
	private Map<Competitor, TreeMap<Integer, Double>> teamRatingsOverTime = null;
	private int gameIndex;
	
	PowerRating(Map<Competitor, Double> initialRatings, double defaultRating,
			BiFunction<RatingPair, Game, RatingPair> updateFunction, GameList<Game> gameList,
			boolean trackChanges)
	{
	
		m_ratings = new HashMap<>();
		if (initialRatings != null)
			m_ratings.putAll(initialRatings);
		this.gameList = gameList;
		this.updateFunction = updateFunction;
		this.defaultRating = defaultRating;
		if (trackChanges)
		{
			this.trackChanges = true;
			teamRatingsOverTime = new HashMap<>();
		}
	}
	
	// Process all the games in gameList.
	public void process()
	{	
		for (gameIndex = 0; gameIndex < gameList.size(); gameIndex++)
			processGame(gameList.get(gameIndex));
	}
	
	// Update ratings on a game-by-game basis.
	private void processGame(Game game)
	{
		Competitor comp1, comp2;
		double comp1Rating, comp2Rating;
		RatingPair pair, newPair;
		
		comp1 = game.getCompetitor1();
		comp2 = game.getCompetitor2();
		m_ratings.putIfAbsent(comp1, defaultRating);
		m_ratings.putIfAbsent(comp2, defaultRating);
		comp1Rating = m_ratings.get(comp1);
		comp2Rating = m_ratings.get(comp2);
				
		pair = new RatingPair(comp1Rating, comp2Rating);
		newPair = updateFunction.apply(pair, game);
		
		m_ratings.put(comp1, newPair.rating1);
		m_ratings.put(comp2, newPair.rating2);
		
		if (this.trackChanges)
		{	// Add a map of the team's ratings. The key is the game number; value is the rating.
			TreeMap<Integer, Double> map1 = teamRatingsOverTime.get(comp1);
			if (map1 == null) 
			{
				map1 = new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
				map1.put(-1, pair.rating1);
				teamRatingsOverTime.put(comp1, map1);
			}
			map1.put(gameIndex, newPair.rating1);
			
			
			TreeMap<Integer, Double> map2 = teamRatingsOverTime.get(comp2);
			if (map2 == null)
			{
				map2 = new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
				map2.put(-1, pair.rating2);
				teamRatingsOverTime.put(comp2, map2);
			}
			map2.put(gameIndex, newPair.rating2);
		}
	}
	
	public Map<Competitor, TreeMap<Integer, Double>> getRatingsOverTime()
	{
		return new HashMap<>(teamRatingsOverTime);
	}
	
	// Returns a map of ratings after the final game in gameList.
	public Map<Competitor, Double> getFinalRatings()
	{
		Map<Competitor, Double> ratingsMap = new HashMap<>();
		TreeMap<Integer, Double> m_gameToRating;	
		for (Competitor c: teamRatingsOverTime.keySet())
		{
			m_gameToRating = teamRatingsOverTime.get(c);
			ratingsMap.put(c, m_gameToRating.lastEntry().getValue());
		}
		
		return ratingsMap;
	}
	
	// Returns a map of ratings before the specified (0-based) game index.
	public Map<Competitor, Double> getRatingsBeforeGame(int index)
	{
		Map<Competitor, Double> ratingsMap = new HashMap<>();
		Map.Entry<Integer, Double> ratingEntry;
		for (Competitor c: teamRatingsOverTime.keySet())
		{
			ratingEntry = teamRatingsOverTime.get(c).lowerEntry(index);
			if (ratingEntry != null)
			{
				ratingsMap.put(c, ratingEntry.getValue());
			}
		}
		return ratingsMap;
	}

}

// Convenience class, pairing two ratings. Subject to update after application of Elo function.
class RatingPair
{
	double rating1, rating2;
	RatingPair(double rating1, double rating2)
	{
		this.rating1 = rating1;
		this.rating2 = rating2;
	}
}