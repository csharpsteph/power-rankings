package metrics;

import metrics.EloFunction;
import parse.NBAGame;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* An Elo ratings update function for the National Basketball Association (NBA).
 * The default K-factor (20) and the margin of victory multiplier both come from the Elo function
 * used by fivethirtyeight.com for the NBA 
 * (https://fivethirtyeight.com/features/how-we-calculate-nba-elo-ratings/).
 * This NBA Elo function also implements 538's calculate home court advantage because, on average,
 * NBA teams win about 60% of their home games.
 */
public class NBAEloFunction extends EloFunction<NBAGame> implements java.io.Serializable {
	
	private static final long serialVersionUID = -4965557664623704441L;
	
	// Default K-factor = 20. Expected home win rate = 60%.
	public NBAEloFunction()
	{
		this(20, .6);
	}
	
	public NBAEloFunction(double K, double homeExpectedWinRate)
	{
		super(K, homeExpectedWinRate);
	}

	@Override
	public RatingPair apply(RatingPair pair, NBAGame game) {
		
		double homeAdvantageElo = getHomeAdvantageElo();
		
		if (!game.isComplete())
		{
			return pair;
		}
		
		/* eloDifference is negative when competitor 1 is favored
		 * and positive when competitor 2 is favored.
		 */
		double eloDifference = pair.rating2 - pair.rating1;
		if (!game.isNeutralSite())
		{
			// Advantage home advantage
			if (game.homeCompetitor() == game.getCompetitor1())
				eloDifference -= homeAdvantageElo;
			else if (game.homeCompetitor() == game.getCompetitor2());
				eloDifference += homeAdvantageElo;
		}
		double expectedScore1 = 1.0 / (1.0 + Math.pow(10, eloDifference / 400));
		// expectedScore2 would be 1 - expectedScore1
		
		double actualScore1;
		if (game.isADraw()) { actualScore1 = 0.5; }
		else if (game.winner() == game.getCompetitor1()) { actualScore1 = 1; }
		else { actualScore1 = 0; }
		// actualScore2 would be 1 - actualScore 1
		
		
		// Increase MoV multiplier for upset victories by decreasing its denominator
		if (eloDifference < 0 && game.winner() == game.getCompetitor2()
				|| eloDifference > 0 && game.winner() == game.getCompetitor1())
		{
			eloDifference *= -1;
		}
		
		int marginOfVictory = Math.abs(game.getScore1().intValue() - game.getScore2().intValue());
		/* 
		 * Taken from the margin of victory multiplier described on fivethirtyeight.com
		 * URL: https://fivethirtyeight.com/features/how-we-calculate-nba-elo-ratings/ 
		 */
		double movMultiplier = Math.pow(marginOfVictory + 3, .8) / (7.5 + .006 * eloDifference);	
		movMultiplier = Math.max(0, movMultiplier); // Ensures multiplier never turns negative
		
		double delta = (1 + movMultiplier) * K * (actualScore1 - expectedScore1);
		double newRating1 = pair.rating1 + delta;
		double newRating2 = pair.rating2 - delta;
		RatingPair newPair = new RatingPair(newRating1, newRating2); 
		return newPair;
	}
}
