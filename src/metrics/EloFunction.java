package metrics;

import java.util.function.BiFunction;

import parse.AbstractGame;
public abstract class EloFunction<Game extends AbstractGame<?>> 
		implements BiFunction<RatingPair, Game, RatingPair> 
{
	protected double K;
	protected double homeExpectedWinRate;
	
	public EloFunction()
	{
		this(20, .5);
	}
	
	public EloFunction (double K, double homeExpectedWinRate)
	{
		setK(K);
		setHomeAdvantageWinRate(homeExpectedWinRate);
	}
	
	/* Constrains K between 0 and 1000 */
	public void setK(double K)
	{
		K = Math.max(0, K);
		K = Math.min(1000, K);
		this.K = K;
	}
	
	public double getK()
	{
		return K;
	}
	
	public double getHomeAdvantageWinRate()
	{
		return homeExpectedWinRate;
	}
	
	/* Constrains homeAdvantageRatio between 0.00 (impossible for the home team) 
	 * and 0.99 (very favorable to the home team).
	 * In practice, league-wide average home win rate should never be lower than say 40%
	 * or higher than 90%. A rate above 99% would produce near-infinite home advantage Elo points.*/
	public void setHomeAdvantageWinRate(double homeExpectedWins)
	{
		homeExpectedWins = Math.max(0.00, homeExpectedWins);
		homeExpectedWins = Math.min(0.99, homeExpectedWins);
		this.homeExpectedWinRate = homeExpectedWins;
	}
	
	public double getHomeAdvantageRatio()
	{
		return homeExpectedWinRate / (1 - homeExpectedWinRate);
	}
	
	public double getHomeAdvantageElo()
	{
		return 400 * Math.log10(homeExpectedWinRate);
	}
	
	
	@Override
	public abstract RatingPair apply(RatingPair pair, Game game);
}
