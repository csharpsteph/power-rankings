package nodes;

// @author Cody J. Stephens ({@code https://github.com/csharpsteph/})

// An extension of the Competitor class, representing a team with a city and nickname/mascot.
public class Team extends Competitor implements Comparable<Competitor> {

	private static final long serialVersionUID = -5409591027771819436L;
	private String shortName, city, nickname;
	
	public Team(String shortName, String city, String nickname)
	{
		super(shortName);
		setKey("TEAM_" + shortName);
		this.shortName = shortName;
		this.city = city;
		this.nickname = nickname;
	}
	
	public String getFullName()
	{
		return String.format("%s %s", city, nickname);
	}
		
	public String getNickname()
	{
		return nickname;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	public String getCity()
	{
		return city;
	}

	@Override
	public String toString()
	{
		return getFullName();
	}
	
	@Override
	public int compareTo(Competitor t)
	{
		return t_key.compareTo(t.t_key);
	}

	@Override
	public String getName() {
		return getFullName();
	}

}
