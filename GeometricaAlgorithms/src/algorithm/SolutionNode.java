package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

public class SolutionNode  {
	private ArrayList<SolutionNode> parents;
	private String explanation;
	
	public SolutionNode ()
	{
		parents = new ArrayList<SolutionNode>();
		explanation = "-";
	}
	
	public SolutionNode ( String e)
	{
		parents = new ArrayList<SolutionNode>();
		explanation = e;
	}
	
	public SolutionNode ( String e, SolutionNode ... nodes)
	{
		parents = new ArrayList<SolutionNode>();
		explanation = e;
		
		for ( SolutionNode n : nodes)
		{
			parents.add( n);
		}
	}
	
	
	// mutators for the class:
	
	public void setExplanation ( String e)
	{
		explanation = e;
	}
	
	public String getExplanation ( )
	{
		return explanation;
	}
	
	public ArrayList<SolutionNode> getParents ( )
	{
		return parents;
	}
	
	public void addParent ( SolutionNode n)
	{
		parents.add( n);
	}
	
	public boolean isLeaf ( )
	{
		return parents.isEmpty();
	}
	
	// the textual description for the step:
	public String toString ( ) // TODO implement a proper step by step description of the solution, presumably returning a String array
	{
		String result;
		
		result = "";
		for ( SolutionNode n : parents)
		{
			result += n + "\n\n";
		}
		
		return result + explanation;
	}
	
	
}
