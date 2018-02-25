package metrics;

import parse.CFLGame;
/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* An Elo ratings update function for the Canadian Football League. The default K-factor (20) and the 
 * margin of victory multiplier both come from the Elo function used by fivethirtyeight.com
 * for the National Football League (https://fivethirtyeight.com/features/introducing-nfl-elo-ratings/).
 * Unlike 538's NFL Elo rating function, this CFL function does not confer a home-field advantage,
 * as CFL teams, on the average, win just about as much at home as they do on the road.
 */
public class CFLEloFunction extends EloFunction<CFLGame> implements java.io.Serializable {

	private static final long serialVersionUID = 8569587913551574751L;

	/* Default K-factor = 20. No home-field advantage expected.*/
	public CFLEloFunction()
	{
		this(20, .5);
	}
	
	public CFLEloFunction(double K, double homeAdvRatio)
	{
		super(K, homeAdvRatio);
	}

	/* Main body of the function */
	@Override
	public RatingPair apply(RatingPair pair, CFLGame game) {
		
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
		
				
		int marginOfVictory = Math.abs(game.getScore1().intValue() - game.getScore2().intValue());
		/* 
		 * Taken from the margin of victory multiplier described on fivethirtyeight.com
		 * URL: https://fivethirtyeight.com/features/introducing-nfl-elo-ratings/ 
		 */
		double movMultiplier = Math.log(marginOfVictory + 1);
		
		/* 
		 * Base value of K increases for playoff games to reflect their perceived importance.
		 * */
		double actualK = K;
		if (game.isPlayoffs()) { actualK += 15; }
		double delta = (1 + movMultiplier) * actualK * (actualScore1 - expectedScore1);
		
		// Updates the power ratings
		double newRating1 = pair.rating1 + delta;
		double newRating2 = pair.rating2 - delta;
		RatingPair newPair = new RatingPair(newRating1, newRating2); 
		return newPair;
	}
}
