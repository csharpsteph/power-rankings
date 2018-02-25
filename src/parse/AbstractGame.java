package parse;

import java.util.Date;

import nodes.*;

// @author Cody J. Stephens ({@code https://github.com/csharpsteph/)

/* An abstract class representing a game between two Competitors.
 * 
 */
public abstract class AbstractGame<ScoreType extends Number> {
	protected Competitor comp1, comp2;
	protected ScoreType comp1Score, comp2Score;
	protected boolean isNeutralSite = true;
	protected Date date;
	
	AbstractGame(Competitor comp1, ScoreType comp1Score, Competitor comp2, ScoreType comp2Score, Date date)
	{
		
		this.comp1 = comp1;
		this.comp2 = comp2;
		this.comp1Score = comp1Score;
		this.comp2Score = comp2Score;
		this.date = date;
	}
	
	public Date getDate()
	{
		return date;
	}
	
	public Competitor getCompetitor1()
	{
		return comp1;
	}
	
	public Competitor getCompetitor2()
	{
		return comp2;
	}
	
	public Competitor otherCompetitor(Competitor competitor)
	{
		boolean isC1 = competitor == comp1,
				isC2 = competitor == comp2;
		
		if (isC1) return comp2;
		if (isC2) return comp1;
		else return null;
	}
	
	public ScoreType getScore1()
	{
		return comp1Score;
	}
	
	public ScoreType getScore2()
	{
		return comp2Score;
	}
	
	public boolean isComplete()
	{
		return comp1Score != null && comp2Score != null;
	}
	
	public boolean isNeutralSite()
	{
		return isNeutralSite;
	}
	
	public boolean isADraw()
	{
		return isComplete() && comp1Score == comp2Score;
	}
	
	public Competitor winner()
	{
		if (!isComplete())
			return null;
		else if (isADraw()) return null;
		else if (comp1Score.doubleValue() > comp2Score.doubleValue()) return comp1;
		else return comp2;
	}
	
	public Competitor loser()
	{
		if (!isComplete())
			return null;
		else if (isADraw()) return null;
		else if (comp1Score.doubleValue() > comp2Score.doubleValue()) return comp2;
		else return comp1;
	}
	
	public Competitor homeCompetitor()
	{
		if (isNeutralSite) return null;
		else return comp2;
	}
	
	public Competitor awayCompetitor()
	{
		if (isNeutralSite) return null;
		else return comp1;
	}
	
	// Aliases, for cases in which the competitors are teams
	public Competitor homeTeam()
	{
		return homeCompetitor();
	}
	
	public Competitor awayTeam()
	{
		return awayCompetitor();
	}
	
	public void setNeutralSite(boolean isNeutralSite)
	{
		this.isNeutralSite = isNeutralSite;
	}
	
}
