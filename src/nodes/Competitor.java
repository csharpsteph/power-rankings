package nodes;

public class Competitor extends Node<String> implements Comparable<Competitor> {

	private static final long serialVersionUID = -7333953472615249059L;
	
	public Competitor(String name)
	{
		super("COMP_" + name);
	}
	
	public String getName()
	{
		return t_key;
	}
	
	public String getFullName()
	{
		return t_key;
	}
	
	@Override
	public String toString()
	{
		return t_key;
	}
	
	@Override
	public int compareTo(Competitor comp)
	{
		return this.t_key.compareTo(comp.t_key);
	}
	

}
