package nodes;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */

/* A Node which represents a division of teams or group of competitors. 
 * Children may be of any other Node type.
 */
public class Division extends Node<String> implements Comparable<Division> {
	
	private static final long serialVersionUID = -5218345512156431111L;
	protected String typeName, fullName, shortName;
	public Division(String typeName, String fullName, String shortName)
	{
		super(typeName.toUpperCase().substring(0, Math.min(3, typeName.length())) + 
				"_" + shortName);
		this.typeName = typeName;	// e.g. division, conference
		this.fullName = fullName;	
		this.shortName = shortName;
	}
	
	public String getTypeName()
	{
		return typeName;
	}
	
	public String getFullName()
	{
		return fullName;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	@Override
	public String toString()
	{
		return fullName;
	}

	@Override
	public int compareTo(Division div) {
		return this.fullName.compareTo(div.fullName);
	}
	
	
	
	
}
