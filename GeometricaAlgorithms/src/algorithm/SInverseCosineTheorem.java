package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

// a searcher class to use the inverse of the law of cosines
// to find an angle
public class SInverseCosineTheorem extends GeoSearcher {
	// the graph the search is conducted
	private Graph graph;
	
	// the attribute identifier for detecting already visited nodes
	private static final String VISITED = "VIS_INVCOS";
	
	// construct from the graph to search on
	public SInverseCosineTheorem ( Graph g)
	{
		this.graph = g;
	}
	
	// processes a given node to apply the cosine theorem, taking
	// the properties of the node into account
	@Override	
	public void processNode ( Node node, ArrayList<Node> toBeProcessed)
	{
		// works on length nodes
		if ( node.getId().startsWith( GraphHelpers.Keys.LENGTH) && !node.hasAttribute( VISITED))
		{
			// the points of the length
			Node p1 = GraphHelpers.getEdgeOtherSide( node.getEdge( 0), node);
			Node p2 = GraphHelpers.getEdgeOtherSide( node.getEdge( 1), node);
			
			// iterate the length nodes of p1
			for ( Node n : GraphHelpers.getNeighbors( p1))
			{
				// if we have a new length node
				if ( n.getId().startsWith( GraphHelpers.Keys.LENGTH) && n != node)
				{
					// the new point with the common length
					Node p_temp;
					// the third length node
					Node len_temp;
					
					p_temp = GraphHelpers.getEdgeOtherSide( n.getEdge( 0), n) == p2 || GraphHelpers.getEdgeOtherSide( n.getEdge( 0), n) == p1 
							 ? GraphHelpers.getEdgeOtherSide( n.getEdge( 1), n)
							 : GraphHelpers.getEdgeOtherSide( n.getEdge( 0), n);
					
					System.out.println( "" + p1.getId() + p2.getId() + p_temp.getId());
					
					// check if we also know the third length in the triangle p1 - p_temp - p2
					len_temp = GraphHelpers.getLengthBetween( p_temp, p2);
					if ( len_temp != null)
					{
						processTriple ( p1, p2, p_temp, node, n, len_temp, toBeProcessed);
					}
				}
			}
			
		}
	}
	
	// given a triple of length, applies the cosine theorem
	// takes three point nodes, and the corresponding three length nodes between them
	private void processTriple ( Node p1, 	Node p2,	Node p3,
								 Node lp1p2, Node lp1p3, Node lp2p3, ArrayList<Node> toBeProcessed)
	{
		// the angle nodes, to check if they exist
		Node ang1 = GraphHelpers.getAngleBetween( p1, p2, p3);
		Node ang2 = GraphHelpers.getAngleBetween( p1, p3, p2);
		Node ang3 = GraphHelpers.getAngleBetween( p3, p1, p2);
				
		// if the angle p1-p2-p3 is unknown
		if ( ang1 == null)
		{
			// create a angle new node to add to the graph
			Node angNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
			double newAngle;
			
			System.out.println( "" + p1.getId() + p2.getId() + p3.getId());
			
			newAngle = getAngleOnTriangle( ( double) lp1p2.getAttribute( "val"), ( double) lp1p3.getAttribute( "val"), ( double) lp2p3.getAttribute( "val"));
			angNode.addAttribute( "val", newAngle);
			angNode.addAttribute( "sol", generateSolutionNode ( p1, p2,	p3, lp1p2, lp1p3, lp2p3, newAngle));

			// add the new edges
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p1).setAttribute( "typ", 1);
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p2).setAttribute( "typ", 2);
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p3).setAttribute( "typ", 3);
			
			// add the new angle node to the list of the nodes to be processed
			toBeProcessed.add( angNode);
		}
		
		// if the angle p1-p3-p2 is unknown
		if ( ang2 == null)
		{
			// create a angle new node to add to the graph
			Node angNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
			double newAngle;
			
			newAngle = getAngleOnTriangle( ( double) lp1p3.getAttribute( "val"), ( double) lp1p2.getAttribute( "val"), ( double) lp2p3.getAttribute( "val"));
			angNode.addAttribute( "val", newAngle);
			angNode.addAttribute( "sol", generateSolutionNode ( p1, p3,	p2, lp1p3, lp1p2, lp2p3, newAngle));

			// add the new edges
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p1).setAttribute( "typ", 1);
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p2).setAttribute( "typ", 3);
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p3).setAttribute( "typ", 2);
			
			// add the new angle node to the list of the nodes to be processed
			toBeProcessed.add( angNode);
		}
		
		// if the angle p2-p1-p3 is unknown
		if ( ang3 == null)
		{
			// create a angle new node to add to the graph
			Node angNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
			double newAngle;
			
			newAngle = getAngleOnTriangle( ( double) lp1p2.getAttribute( "val"), ( double) lp2p3.getAttribute( "val"), ( double) lp1p3.getAttribute( "val"));
			angNode.addAttribute( "val", newAngle);
			angNode.addAttribute( "sol", generateSolutionNode ( p2, p1,	p3, lp1p2, lp2p3, lp1p3, newAngle));

			// add the new edges
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p1).setAttribute( "typ", 2);
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p2).setAttribute( "typ", 1);
			graph.addEdge( GraphHelpers.Keys.generateEdgeId(), angNode, p3).setAttribute( "typ", 3);
			
			// add the new angle node to the list of the nodes to be processed
			toBeProcessed.add( angNode);
		}
				
	}
	
	// using the law of cosines, returns the angle ABC on a triangle
	// where all the lengths are known
	private double getAngleOnTriangle ( double lenAB, double lenAC, double lenBC)
	{
		return Math.acos( ( lenAC * lenAC - lenAB * lenAB - lenBC * lenBC) / ( -2 * lenBC * lenAB));
	}
	
	// construct the solution for an angle
	private SolutionNode generateSolutionNode ( Node p1, 	Node p2,	Node p3,
												Node lp1p2, Node lp1p3, Node lp2p3, double result)
	{
		String explanation;
		
		// build the explanation String for this step
		explanation = "Apply the inverse law of cosines on the triangle " + p1.getId().substring( 1) + "-" + p2.getId().substring( 1) +
					  "-" + p3.getId().substring( 1) + "\n";
		explanation += "So the value of the angle " + p1.getId().substring( 1) + "-" + p2.getId().substring( 1) + "-" +
					   p3.getId().substring( 1) + " is " + MathHelpers.round( MathHelpers.toDegrees( result), 2);
		
		return new SolutionNode ( explanation, (SolutionNode) lp1p2.getAttribute( "sol"), (SolutionNode) lp2p3.getAttribute( "sol"), 
				(SolutionNode) lp1p3.getAttribute( "sol"));
	}
}
