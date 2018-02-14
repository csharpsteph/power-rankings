package nodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayDeque;
import java.util.Collection;

public class Graph<T extends Comparable<T>> implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2887166626086791276L;
	protected Node<T> root;
	
	public Graph()
	{
		setRoot(null);
	}
	
	public Node<T> getRoot()
	{
		return root;
	}
	
	public void setRoot(Node<T> node)
	{
		if (node != null) node.setParent(null);
		root = node;
	}
	
	public boolean isRoot(Node<T> node)
	{
		return node == root;
	}
	
	public Node<T> findNode(T key)
	{
		return findNode(root, key);
	}
	
	public Node<T> findNode(Node<T> root, T key)
	{
		if (root == null) return null;
		Collection<Node<T>> forest = new ArrayDeque<>();
		forest.add(root);
		while (!forest.isEmpty())
		{
			for (Node<T> node: forest)
			{
				if (node.getKey().equals(key))
				{
					return node;
				}
			}
			forest = getSuccessors(forest);
		}
		
		return null;
	}
	
	public Collection<Node<T>> getSuccessors(Collection<Node<T>> nodes)
	{
		Collection<Node<T>> successors = new ArrayDeque<>();
		for (Node<T> n: nodes)
		{
			if (n == null) continue;
			successors.addAll(n.getChildren());
		}
		return successors;
	}
	
	public Collection<Node<T>> getNodesAtLevel(int level)
	{
		if (root == null) 
		{
			throw new RuntimeException("Root is null");
		}
		
		Collection<Node<T>> forest = new ArrayDeque<>();
		forest.add(root);
		int currentLevel = 0;
		
		while (currentLevel++ < level)
		{
			forest = getSuccessors(forest);
			if (forest.isEmpty())
			{
				throw new RuntimeException(String.format(
					"Graph does not go up to level %d (stops at %d).", level, currentLevel-1));
			}
		}
		
		return forest;
	}
	/*
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		java.io.File teamDataSource = new java.io.File("C:/Users/codys/resource-dump/nba-graph.dat");
		ObjectInputStream istream = new ObjectInputStream(new FileInputStream(teamDataSource));
		Graph<String>teamGraph = (Graph<String>)istream.readObject();
		istream.close();
		
		Node<String> atl = teamGraph.findNode(teamGraph.getRoot(), "TEAM_ATL");
		System.out.println(atl);
	}*/
	
}
