package nodes;

import java.util.Stack;

public class PrintGraph {
	
	public static void printGraphDepthFirst(Node root)
	{
		Stack<Node> stack = new Stack<>();
		stack.add(root);
		while (!stack.isEmpty())
		{
			System.out.printf("%s -> %s\n", stack.peek().getParent(), stack.peek());
			stack.addAll(stack.pop().getChildren());
		}
	}
	
	public static void printGraphBreadthFirst(Node root)
	{
		java.util.Queue<Node> forest = new java.util.ArrayDeque<>();
		forest.add(root);
		while (!forest.isEmpty())
		{
			System.out.printf("%s -> %s\n", forest.peek().getParent(), forest.peek());
			forest.addAll(forest.remove().getChildren());
		}
	}
}
