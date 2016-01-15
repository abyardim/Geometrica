package algorithm;
// Created: 24.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

// TODO extend this to process even if the angle is not in the corner

// a searcher class to find areas using the area formula with sines
public class SAreaSineFormula extends GeoSearcher {
	// the graph the search is conducted
	private Graph graph;

	// the attribute identifier for detecting already visited nodes
	private static final String VISITED = "%VIS_AREASIN";

	// construct from the graph to search on
	public SAreaSineFormula ( Graph g)
	{
		this.graph = g;
	}

	@Override
	public void processNode(Node n, ArrayList<Node> toBeProcessed) {
		if ( n.getId().startsWith( GraphHelpers.Keys.ANGLE) && !n.hasAttribute( VISITED)) // an unvisited angle node
		{
			// mark as visited
			n.addAttribute( VISITED);
			
			workOnAngleNode( n, toBeProcessed);
		}
		else if ( n.getId().startsWith( GraphHelpers.Keys.LENGTH) && !n.hasAttribute( VISITED)) // an unvisited length node
		{
			// mark as visited
			n.addAttribute( VISITED);
			
			// re-process each related angle
			for ( Node angleNode : GraphHelpers.getAnglesBetween( GraphHelpers.getEdgeOtherSide( n.getEdge( 0), n), 
																  GraphHelpers.getEdgeOtherSide( n.getEdge( 1), n)))
			{
				angleNode.addAttribute( VISITED);
				workOnAngleNode ( angleNode, toBeProcessed);
			}
		}
	}
	
	// given an angle node, attempts to process lengths and produce a area node
	private void workOnAngleNode ( Node angleNode, ArrayList<Node> toBeProcessed)
	{
		// the node which are on one side of the angle
		ArrayList<Node> sides1 = new ArrayList<Node>();
		// node on the center on the angle
		Node sideCenter;
		// nodes on the other side of the angle
		ArrayList<Node> sides3 = new ArrayList<Node>();

		sideCenter = null;

		// divide the neighbor nodes of this angle according to their position
		for ( Edge edgeToSide : angleNode.getEdgeSet())
		{
			switch ( (int) edgeToSide.getAttribute( "typ"))
			{
			case 1:
				sides1.add( GraphHelpers.getEdgeOtherSide( edgeToSide, angleNode));
				break;
			case 2: // center node
				sideCenter = GraphHelpers.getEdgeOtherSide( edgeToSide, angleNode);
				break;
			case 3:
				sides3.add( GraphHelpers.getEdgeOtherSide( edgeToSide, angleNode));
				break;
			}
		}

		// iterate through all the divided edges for triples, process them searching for places to apply the theorem
		for ( Node n1 : sides1)
		{
			for ( Node n2 : sides3)
			{
				processTriple ( angleNode, n1, sideCenter, n2, toBeProcessed);
			}
		}
	}
	
	// given an angle node and three of its points, processes these to infer the area
	private void processTriple ( Node angleNode, Node p1, Node m, Node p2, ArrayList<Node> toBeProcessed)
	{
		// the length nodes of the triangle
		Node len1, len2;
		
		len1 = GraphHelpers.getLengthBetween( p1, m);
		len2 = GraphHelpers.getLengthBetween( p2, m);
		
		// if we do not have enough information, return
		// we need both the lengths to infer the area
		if ( len1 == null || len2 == null)
			return;
		
		// if this area is already known, return
		if ( GraphHelpers.getAreaBetween( p1, m, p2) != null)
			return;
		
		// create the new triangle area
		double result;
		Node areaNode;
		
		// build the new node object
		areaNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.AREA));
		result = calculateArea ( ( double) len1.getAttribute( "val"), ( double) len2.getAttribute( "val"), ( double) angleNode.getAttribute( "val"));
		areaNode.addAttribute( "val", result);
		areaNode.addAttribute( "sol", generateSolutionNode ( angleNode, p1, m, p2, len1, len2, result));
		
		// create the edges
		graph.addEdge( GraphHelpers.Keys.generateEdgeId(), areaNode, p1).addAttribute( "typ", 1);
		graph.addEdge( GraphHelpers.Keys.generateEdgeId(), areaNode, m ).addAttribute( "typ", 2);
		graph.addEdge( GraphHelpers.Keys.generateEdgeId(), areaNode, p2).addAttribute( "typ", 3);
		
		toBeProcessed.add( areaNode);
	}
	
	// given the angle, point and length nodes, generates the appropriate solutionNode
	private SolutionNode generateSolutionNode ( Node angleNode, Node p1, Node m, Node p2, Node len1, Node len2, double result)
	{
		String explanation;
		
		if ( Math.abs( ( double) angleNode.getAttribute( "val") - Math.PI / 2 ) < 0.001) // if this is a right angle
		{
			explanation = "On the triangle " + p1.getId().substring( 1) + "-" + m.getId().substring( 1) + "-" + p2.getId().substring( 1) + " ";
			explanation += "the area is given by \u00BD \u00D7 " + len1.getAttribute( "val") + " \u00D7 " + len2.getAttribute( "val");
			explanation += "\nTherefore, it is " + MathHelpers.round( result, 2);
		}
		else
		{
			explanation = "On the triangle " + p1.getId().substring( 1) + "-" + m.getId().substring( 1) + "-" + p2.getId().substring( 1) + " ";
			explanation += "the area is given by \u00BD \u00D7 " + len1.getAttribute( "val") + " \u00D7 " + len2.getAttribute( "val") + " \u00D7 " +
						   "sin ( " + MathHelpers.round( ( ( double) angleNode.getAttribute( "val") * 180 / Math.PI),2) + " )";
			explanation += "\nTherefore, it is " + result;
		}
		
		// build the explanation
		
		
		return new SolutionNode( explanation, ( SolutionNode) angleNode.getAttribute( "sol"), ( SolutionNode) len1.getAttribute( "sol"),
								 ( SolutionNode) len2.getAttribute( "sol"));
		
	}
	
	// given the lengths and the angle in between them, returns the area of the triangle formed by the system
	private double calculateArea ( double length1, double length2, double angle)
	{
		return length1 * length2 * Math.sin( angle) / 2;
	}
}
