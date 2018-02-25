package parse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import nodes.Team;

public class OWLGame extends AbstractGame<Byte> implements Comparable<OWLGame> {
	
	private Byte stageNum, weekNum;
	private boolean isPlayoffGame = true;
	
	public OWLGame(Team team1, Byte team1Score, Team team2, Byte team2Score, 
			Date date, Byte stageNum, Byte weekNum) {
		super(team1, team1Score, team2, team2Score, date);
		this.date = date;
		this.stageNum = stageNum;
		this.weekNum = weekNum;
	}
	
	public Team getTeam1()
	{
		return (Team)getCompetitor1();
	}
	
	public Team getTeam2()
	{
		return (Team)getCompetitor2();
	}
	
	@Override
	public String toString()
	{
		DateFormat format = new SimpleDateFormat("EEE MMM d yyyy");
		String dateStr = format.format(date);
		return String.format("%s (Stage %d, week %d): %s %s, %s %s", 
				dateStr, stageNum, weekNum, comp1, comp1Score == null ? "-" : String.valueOf(comp1Score),
				comp2, comp2Score == null ? "-" : String.valueOf(comp2Score));
	}
	
	public Date getDate()
	{
		return (Date)date.clone();
	}
	
	public byte getStageNumber()
	{
		return stageNum;
	}
	
	public byte getWeekNumber()
	{
		return weekNum;
	}
	
	public boolean isPlayoffGame()
	{
		return isPlayoffGame;
	}
	
	public boolean isStagePlay()
	{
		return stageNum > 4;
	}
	
	public void setPlayoff(boolean isPlayoffGame)
	{
		this.isPlayoffGame = isPlayoffGame;
	}
	
	@Override
	public int compareTo(OWLGame game)
	{
		int cmp = 0;
		if (this.isComplete()) cmp -= 1;
		if (game.isComplete()) cmp += 1;
		if (cmp != 0) return cmp;
		
		cmp = this.date.compareTo(game.date);
		if (cmp != 0) return cmp;
		
		if (this.isPlayoffGame()) cmp += 1;
		if (game.isPlayoffGame()) cmp -= 1;
		return cmp;
	}

}
