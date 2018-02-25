package parse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nodes.Team;

public class NBAGame extends AbstractGame<Short> implements Comparable<NBAGame> {
	
	NBAGame(Team team1, Short team1Score, Team team2, Short team2Score, Date date) {
		super(team1, team1Score, team2, team2Score, date);
		this.date = date;
		isNeutralSite = false;		
	}
	
	public boolean isPlayoffs()
	{
		return date.compareTo(new GregorianCalendar(2018, Calendar.APRIL, 12).getTime()) >= 0;
	}
	
	@Override
	public String toString()
	{
		DateFormat format = new SimpleDateFormat("EEE MMM d yyyy");
		String dateStr = format.format(date);
		return String.format("%s: %s %s, %s %s", 
				dateStr, comp1, comp1Score == null ? "-" : String.valueOf(comp1Score), 
						comp2, comp2Score == null ? "-" : String.valueOf(comp2Score));
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

	@Override
	public int compareTo(NBAGame game)
	{
		int cmp = 0;
		if (this.isComplete()) cmp -= 1;
		if (game.isComplete()) cmp += 1;
		if (cmp != 0) return cmp;
		
		cmp = this.date.compareTo(game.date);
		return cmp;
	}

}
