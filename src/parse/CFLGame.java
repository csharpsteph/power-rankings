package parse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import nodes.Team;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})
/*
 * A class representing a Canadian Football League game.
 */

public class CFLGame extends AbstractGame<Short> implements Comparable<CFLGame> {
	
	private byte week;

	public CFLGame(Team team1, Short team1Score, Team team2, Short team2Score,
			Date date, String weekStr) {
		super(team1, team1Score, team2, team2Score, date);
		this.week = week(weekStr);
		if (!isGreyCup())
		{
			isNeutralSite = false;
		}
	}
	
	public Date getDate()
	{
		return (Date)date.clone();
	}
	
	public Team getTeam1()
	{
		return (Team)getCompetitor1();
	}
	
	public Team getTeam2()
	{
		return (Team)getCompetitor2();
	}
	
	private byte week(String weekStr)
	{
		byte b;
		if (weekStr.startsWith("P"))
		{
			b = (byte)20;
			b += Byte.parseByte(weekStr.substring(1, 2));
		}
		else if (weekStr.startsWith("G"))
		{
			b = 23;
		}
		else {
			b = Byte.parseByte(weekStr);
		}
		return b;
	}
	
	public byte getWeekNumber()
	{
		return week;
	}
	
	public boolean isPlayoffs()
	{
		return week > 20;
	}
	
	public boolean isGreyCup()
	{
		return week == 23;
	}
	
	public boolean isRegularSeason()
	{
		return week < 21;
	}

	@Override
	public int compareTo(CFLGame game)
	{
		int cmp = 0;
		if (this.isComplete()) cmp -= 1;
		if (game.isComplete()) cmp += 1;
		if (cmp != 0) return cmp;
		
		cmp = this.date.compareTo(game.date);
		return cmp;
	}
	
	@Override
	public String toString()
	{
		
		DateFormat format = new SimpleDateFormat("EEE MMM d yyyy");
		String dateStr = format.format(date);
		String weekStr;
		if (isGreyCup())
			weekStr = "Grey Cup";
		else if (isPlayoffs())
			weekStr = "Playoffs, Round " + (week - 20);
		else
			weekStr = "Week " + String.valueOf(week);
		return String.format("%s: (%s) %s %d, %s %d", 
				dateStr, weekStr, comp1, comp1Score, comp2, comp2Score);
	}
}
