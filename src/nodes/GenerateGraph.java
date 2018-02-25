package nodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */
public class GenerateGraph implements java.io.Serializable, Runnable {

	
	private static final long serialVersionUID = -2839416749758944677L;
	private File source, output;

	public void writeOutput() throws FileNotFoundException, IOException
	{
		ObjectOutputStream ostream = new ObjectOutputStream(new FileOutputStream(this.output));
		Scanner istream = new Scanner(source);
		String line, type;
		int typeEndIndex, numTabs;
		int currentLevel = -1;
		NodeLevelPair prevNodeEntry = null;
		Map<Integer, Node<String>> parentNodes = new HashMap<>();
		TreeMap<Integer, Integer> tabsToLevels = new TreeMap<>();
		
		while (istream.hasNext())
		{
			line = istream.nextLine();
			if (line.isEmpty()) { continue; }
			typeEndIndex = line.indexOf(':');
			if (typeEndIndex < 0)
			{
				ostream.close();
				istream.close();
				throw new RuntimeException("No node type found. Type must be followed by a colon (:).");
			}
			numTabs = 0;
			{
				int i = 0;
				while (line.charAt(i++) == '\t')
				{
					numTabs++;
				}
			}
			
			// Disregard tab-level mappings for out-of-scope nodes/lines
			Set<Integer> keySet = new HashSet<>(tabsToLevels.keySet());
			for (Integer prevKeyTabs: keySet)
			{
				if (prevKeyTabs > numTabs)
				{
					tabsToLevels.remove(prevKeyTabs);
				}
			}
			
			// Set level of current node (at most one greater than the previous level)
			Map.Entry<Integer, Integer> prevTabLevelEntry = tabsToLevels.floorEntry(numTabs);
			if (prevTabLevelEntry == null)
			{	// This entry has no parent; set level to 0.
				currentLevel = 0;
			} 
			else if (prevTabLevelEntry.getKey() < numTabs)
			{	// This entry is a child of the previous entry; increase level by 1.
				currentLevel = prevTabLevelEntry.getValue() + 1;
			}
			else { // This entry is a sibling of the previous entry; use the same level.
				currentLevel = prevTabLevelEntry.getValue();
			}
			
			tabsToLevels.put(numTabs, currentLevel);
			
			// Detect type of node
			type = line.substring(numTabs, typeEndIndex);
			
			// Process information from the line
			{
				String[] parts = line.substring(numTabs + type.length() + 2).split(",\\s*");
				if (type.equals("League"))
				{	
					prevNodeEntry = new NodeLevelPair(new League(parts[0], parts[1]), currentLevel);
				}
				else if (type.equals("Division"))
				{
					prevNodeEntry = new NodeLevelPair(
							new Division(parts[0], parts[1], parts[2]), currentLevel);
				}
				else if (type.equals("Team"))
				{
					prevNodeEntry = new NodeLevelPair(new Team(parts[0], parts[1], parts[2]), currentLevel);
				}
			}
			
			NodeLevelPair justAdded = prevNodeEntry;
			parentNodes.put(justAdded.level, justAdded.node);
			
			if (justAdded.level > 0)
			{
				parentNodes.get(justAdded.level - 1).addChild(justAdded.node);
			}
		}
		istream.close();
		
		Node<String> root = parentNodes.get(0);
		Graph<String> graph = new Graph<>();
		graph.setRoot(root);
		ostream.writeObject(graph);
		ostream.close();
	}

	public static void main(String[] args) throws IOException {
		new GenerateGraph().run();
	}

	@Override
	public void run() {
		String leagueNames[] = {"cfl", "owl", "nba"};
		
		for (String s: leagueNames)
		{
			File source = new File("resources/" + s + "/" + s + "-teams.txt");
			File output = new File("resources/" + s + "/" + s + "-graph.dat");
			try {
				this.source = source;
				this.output = output;
				if (!source.exists())
				{
					System.out.printf("Could not find specified file: %s\n", source.getName());
				}
				writeOutput();
			} catch (FileNotFoundException e) {
				e.printStackTrace();	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

// Convenience class, pairing a node with a tab level
class NodeLevelPair {
	Node<String> node;
	int level;
	
	NodeLevelPair(Node<String> node, int level)
	{
		this.node = node;
		this.level = level;
	}
	
}