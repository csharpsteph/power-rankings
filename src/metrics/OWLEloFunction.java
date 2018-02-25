package metrics;

import parse.OWLGame;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* An Elo ratings update function for the Overwatch League. I have not come across any
 * Elo ratings for Overwatch League teams, though they may exist, so the calculations here are
 * somewhat novel. The default K-factor (20) comes from common K-factors for other leagues' Elo ratings, 
 * and the margin of victory multiplier is based on that of the World Football Elo Ratings, described
 * on the web page http://www.eloratings.net/about.
 * 
 * This function has not been backtested and is subject to change once I do that.
 */
public class OWLEloFunction extends EloFunction<OWLGame> implements java.io.Serializable {
	
	private static final long serialVersionUID = 8105012269383962057L;

	/* Default K-factor 20. No home advantage expected, as all games take place in Los Angeles
	 * and the 'home' team receives no perceivable competitive advantage. */
	public OWLEloFunction()
	{
		this(20, .5);
	}
	
	public OWLEloFunction(double K, double homeExpectedWinRate)
	{
		super(K, homeExpectedWinRate);
	}

	@Override
	public RatingPair apply(RatingPair pair, OWLGame game) {
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
		 * MoV	->	MoV K-factor multiplier
		 * 1	->	x1	(no bonus)
		 * 2	->  x2	
		 * 3 	-> 	x3	
		 * 4 	-> 	x3.5
		 */
		double movMultiplier = marginOfVictory <= 1 ? 0 :
			(Math.pow (2, marginOfVictory - 1) - 1) / Math.pow(2, marginOfVictory - 3) - 1;
		
		/* Base value of K increases by 5 for stage playoffs and a further 15 for 
		 * post-season playoffs, to reflect perceived importance of these games.
		 */
		double actualK = K;
		if (game.isPlayoffGame()) 
		{ 
			actualK += 5; 
			if (!game.isStagePlay()) { actualK += 15; }
		}
			
		double delta = (1 + movMultiplier) * actualK * (actualScore1 - expectedScore1);
		double newRating1 = pair.rating1 + delta;
		double newRating2 = pair.rating2 - delta;
		RatingPair newPair = new RatingPair(newRating1, newRating2); 
		return newPair;
	}
}
