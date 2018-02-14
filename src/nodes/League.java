package nodes;

public class League extends Node<String> implements Comparable<League>
{
	private static final long serialVersionUID = 1839518024935441460L;
	private String name, shortName;
	
	public League(String name, String shortName)
	{
		super("LEAGUE_" + shortName);
		this.name = name;
		this.shortName = shortName;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(League league) {
		return this.t_key.compareTo(league.t_key);
	}

}