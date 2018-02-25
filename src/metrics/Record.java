package metrics;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */


public class Record implements Comparable<Record> {
	int wins, losses, draws;
	
	Record() { this(0,0,0); }
	
	Record(int wins, int losses, int draws)
	{
		this.wins = wins;
		this.losses = losses;
		this.draws = draws;
	}
	
	public void win() { wins++; }
	public void lose() { losses++; }
	public void draw() { draws++; }
	
	
	public double trueWinPercentage()
	{
		if (wins + losses + draws == 0)
			return 0;
		return wins / (double)getGames();
	}
	public double winPercentage() 
	{
		if (wins + losses + draws == 0)
			return 0;
		return (wins + .5 * draws) / (double)getGames();
	}
	
	public void add(Record record)
	{
		add(record.wins, record.losses, record.draws);
	}
	
	public void add(int wins, int losses, int draws)
	{
		this.wins += wins;
		this.losses += losses;
		this.draws += draws;
	}
	
	public void subtract(Record record)
	{
		subtract(record.wins, record.losses, record.draws);
	}
	
	public void subtract(int wins, int losses, int draws)
	{
		this.wins -= wins;
		this.losses -= losses;
		this.draws -= draws;
	}
	
	public Record inverse()
	{
		return new Record(this.losses, this.wins, this.draws);
	}
	
	public int getWins() { return wins; }
	public int getLosses() { return losses; }
	public int getDraws() { return draws; }
	public int getGames() { return wins + losses + draws; }
	
	public void setRecord(int wins, int losses, int draws)
	{
		setWins(wins);
		setLosses(losses);
		setDraws(draws);
	}
	
	public void setWins(int wins)
	{
		this.wins = wins;
	}
	
	public void setLosses(int losses)
	{
		this.losses = losses;
	}
	
	public void setDraws(int draws)
	{
		this.draws = draws;
	}
	
	@Override
	public String toString()
	{
		if (draws == 0)
		{
			return String.format("%d-%d", wins, losses);
		}
		else {
			return String.format("%d-%d-%d", wins, losses, draws);
		}
	}

	@Override
	public int compareTo(Record record) {
		int cmp = Double.compare(this.winPercentage(), record.winPercentage());
		if (cmp == 0)
		{
			cmp = Integer.compare(this.wins, record.wins);
		}
		if (cmp == 0)
		{
			cmp = -1 * Integer.compare(this.losses, record.losses);
		}
		
		if (cmp == 0)
		{
			cmp = Integer.compare(this.getGames(), record.getGames()); 
		}
		return cmp;
	}
}

