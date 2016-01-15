package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

// a "searcher" to find appropriate places to apply the theorem of cosines
public class SCosineTheorem extends GeoSearcher {
	
	// the graph the search is conducted
	private Graph graph;
	
	// the attribute identifier for detecting already visited nodes
	private static final String VISITED = "%VIS_COS";
	
	// construct from the graph to search on
	public SCosineTheorem ( Graph g)
	{
		this.graph = g;
	}
	
	// processes a given node to apply the cosine theorem, taking
	// the properties of the node into account
	@Override
	public void processNode ( Node node, ArrayList<Node> toBeProcessed)
	{
		if ( node.getId().startsWith( GraphHelpers.Keys.ANGLE) && !node.hasAttribute( VISITED)) // an unvisited angle node
		{
			// note that we visited this node
			node.addAttribute( VISITED);
			
			workOnAngleNode ( node, toBeProcessed);
		}
		else if ( node.getId().startsWith( GraphHelpers.Keys.LENGTH) && !node.hasAttribute( VISITED)) // an unvisited length node
			// TODO these parts are not tested!!
		{
			// note that we visited this node
			node.addAttribute( VISITED);

			// find any related angle nodes, revisit them
			ArrayList<Node> neighbors;
			neighbors = GraphHelpers.getCommonNeighbors( GraphHelpers.getEdgeOtherSide( node.getEdge( 0), node),
														 GraphHelpers.getEdgeOtherSide( node.getEdge( 1), node));
			
			// re-process the angle nodes related to this length node
			for ( Node n : neighbors)
			{
				if ( n.getId().startsWith( GraphHelpers.Keys.ANGLE))
				{
					n.addAttribute( VISITED);
					workOnAngleNode ( n, toBeProcessed);
				}
			}
			
		}
		
	}
	
	//////////////////////// graph processing related
	
	// takes an angle node to work on node 
	public void workOnAngleNode ( Node node, ArrayList<Node> toBeProcessed)
	{
		// the node which are on one side of the angle
		ArrayList<Node> sides1 = new ArrayList<Node>();
		// node on the center on the angle
		Node sideCenter;
		// nodes on the other side of the angle
		ArrayList<Node> sides3 = new ArrayList<Node>();

		sideCenter = null;

		// divide the neighbor nodes of this angle according to their position
		for ( Edge edgeToSide : node.getEdgeSet())
		{
			switch ( (int) edgeToSide.getAttribute( "typ"))
			{
			case 1:
				sides1.add( GraphHelpers.getEdgeOtherSide( edgeToSide, node));
				break;
			case 2: // center node
				sideCenter = GraphHelpers.getEdgeOtherSide( edgeToSide, node);
				break;
			case 3:
				sides3.add( GraphHelpers.getEdgeOtherSide( edgeToSide, node));
				break;
			}
		}

		// iterate through all the divided edges for triples, process them searching for places to apply the theorem
		for ( Node n1 : sides1)
		{
			for ( Node n2 : sides3)
			{
				processTriple ( node, n1, sideCenter, n2, toBeProcessed);
			}
		}
	}
	
	// processes triple of points given for an angle
	private void processTriple ( Node angle, Node p1, Node m, Node p2, ArrayList<Node> toBeProcessed)
	{
		// the "length" nodes between the points, if there is one
		Node distN1M;
		Node distMN2;
		Node distN1N2;
		
		// the length of the angle
		
		
		distN1M  = GraphHelpers.getLengthBetween( p1, m);
		distMN2  = GraphHelpers.getLengthBetween( m, p2);
		distN1N2 = GraphHelpers.getLengthBetween( p1, p2);
		
		// hypotenuse unknown
		if ( distN1N2 == null)
		{
			// if one of the sides are unknown, we don't have enough data
			if ( distMN2 == null || distN1M == null)
			{
				// not enough data
				return;
			}
			// apply the cosine theorem if we know the other two sides
			else
			{
				double newLength;
				Node newLengthNode;
				
				newLengthNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));
				
				// prepare the node to be added
				newLength =  calculateSideOpposite( ( double) distMN2.getAttribute( "val"), ( double) distN1M.getAttribute( "val"),
							 ( double) angle.getAttribute( "val"));
				newLengthNode.addAttribute( "val", newLength);
				newLengthNode.addAttribute( "sol", generateSolutionNodeOpposite( distMN2, distN1M, angle, new String[] { p1.getId().substring( 1), 
											m.getId().substring( 1), p2.getId().substring( 1)}, newLength));
				
				// connect the new length node appropriately
				graph.addEdge( GraphHelpers.Keys.generateEdgeId(), p1, newLengthNode);
				graph.addEdge( GraphHelpers.Keys.generateEdgeId(), p2, newLengthNode);
				
				toBeProcessed.add( newLengthNode);
			}
		}
		else // hypotenuse known
		{
			// if the angle is different from 90 degrees, return, as
			// there is no definite result from the law of cosines
			if ( Math.abs( ( double ) angle.getAttribute( "val") - Math.PI / 2 ) > 0.01)
				return; // TODO
			
			if ( distMN2 == null && distN1M != null) // side 2 unknown
			{
				
				System.out.println( "alert1");
				// we know the hypotenuse, but not the sides
				
				// the value of the new length
				double newLength;
				// the length node to be added
				Node newLengthNode;

				// create the new length node
				newLengthNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));

				newLength = calculateSideAdjacent ( ( double) distN1M.getAttribute( "val"), ( double) distN1N2.getAttribute( "val"));
				newLengthNode.addAttribute( "val", newLength);
				
				// generate and add the solution node
				newLengthNode.addAttribute( "sol", generateSolutionNodeAdjacent( distN1M, distN1N2, angle, new String[] { 
											p2.getId().substring( 1), m.getId().substring( 1), p1.getId().substring( 1)	}, newLength));

				// connect the new length
				graph.addEdge( GraphHelpers.Keys.generateEdgeId(), m, newLengthNode);
				graph.addEdge( GraphHelpers.Keys.generateEdgeId(), p2, newLengthNode);
				
				toBeProcessed.add( newLengthNode);
			}
			else if ( distN1M == null && distMN2 != null) // side 1 unknown
			{
				System.out.println( "alert2");
				
				// we know the hypotenuse, but not the sides
				
				// the value of the new length
				double newLength;
				// the length node to be added
				Node newLengthNode;

				// create the new length node
				newLengthNode = graph.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));

				newLength = calculateSideAdjacent ( ( double) distMN2.getAttribute( "val"), ( double) distN1N2.getAttribute( "val"));
				newLengthNode.addAttribute( "val", newLength);
				
				// generate and add the solution node
				newLengthNode.addAttribute( "sol", generateSolutionNodeAdjacent( distMN2, distN1N2, angle, new String[] { 
											p1.getId().substring( 1), m.getId().substring( 1), p2.getId().substring( 1)	}, newLength));

				// connect the new length
				graph.addEdge( GraphHelpers.Keys.generateEdgeId(), m, newLengthNode);
				graph.addEdge( GraphHelpers.Keys.generateEdgeId(), p1, newLengthNode);
				
				toBeProcessed.add( newLengthNode);
			}
		}
	}
	
	/////////////////// solution related
	
	
	// calculates the value of the side of a triangle opposite to the given angle using the law of cosines
	// where the lengths of the adjacent sides are len1 and len1
	private static double calculateSideOpposite ( double len1, double len2, double angle)
	{
		return Math.sqrt( ( len1 * len1) + ( len2 * len2) - 2 * Math.cos( angle) * len1 * len2);
	}
	
	// generates the appropriate solution node for the law of cosines applied to find
	// the opposite side of an angle, trianglePoints is the names of the points producing this triangle
	private static SolutionNode generateSolutionNodeOpposite ( Node len1, Node len2, Node angle, String[] trianglePoints, double result)
	{
		// the explanation
		String solutionText;
		
		// build the explanation text:
		
		if ( Math.abs( ( double) angle.getAttribute( "val") - Math.PI / 2) > 0.01)
		{
			solutionText  = "Apply the law of cosines on the triangle " + trianglePoints[0] + "-" +
							trianglePoints[1] + "-" + trianglePoints[2] + "\n";
			solutionText += "The length of " + trianglePoints[0] + "-" + trianglePoints[2] + " is given by ";
			solutionText += " \u221A( " + len1.getAttribute( "val") + "\u00B2 + " + len2.getAttribute( "val") + 
							"\u00B2 - 2 \u00D7 " + len1.getAttribute( "val") + " \u00D7 " + len1.getAttribute( "val") + 
							" \u00D7 cos ( " + MathHelpers.round(( ( double) angle.getAttribute( "val") * 180 / Math.PI), 2) + " ))";
		}
		else // if we have a right angle, this is the equivalent of the Pythagorean theorem
		{
			solutionText  = "Apply the Pythagorean theorem on the triangle " + trianglePoints[0] + "-" +
							trianglePoints[1] + "-" + trianglePoints[2] + "\n";
			solutionText += "The length of " + trianglePoints[0] + "-" + trianglePoints[2] + " is given by ";
			solutionText += " \u221A( " + len1.getAttribute( "val") + "\u00B2 + " + len2.getAttribute( "val") + "\u00B2 )";
		}
		
		solutionText += "\nSo the length between " + trianglePoints[0] + " and " + trianglePoints[2] + " is " + MathHelpers.round( result, 2);
		
		// construct and return the solution node
		return new SolutionNode( solutionText, ( SolutionNode) len1.getAttribute( "sol"), 
				( SolutionNode) len2.getAttribute( "sol"), ( SolutionNode) angle.getAttribute( "sol"));
		
	}
	
	// TODO generalize to use the law of cosines
	// calculates the value of the side of a right triangle adjacent to the given angle using the law of cosines
	private static double calculateSideAdjacent ( double adjacent, double opposite)
	{
		return Math.sqrt( opposite * opposite - adjacent * adjacent);
	}
	
	// TODO find a way of delaying the string generation until the results are displayed
	
	// generates the appropriate solution node for the law of cosines applied to find the opposite
	// side of an right angle, trianglePoints is the names of the points producing this triangle
	private static SolutionNode generateSolutionNodeAdjacent ( Node adjacent, Node opposite, Node angle, String[] trianglePoints, double result)
	{
		// the explanation
		String solutionText;

		// build the explanation text:

		if ( ( double) angle.getAttribute( "val") - Math.PI / 2 < 0.001)
		{
			solutionText  = "Apply the Pythagorean theorem on the triangle " + trianglePoints[0] + "-" +
							trianglePoints[1] + "-" + trianglePoints[2] + "\n";
			solutionText += "The length of " + trianglePoints[0] + "-" + trianglePoints[1] + " is given by ";
			solutionText += " \u221A( " + opposite.getAttribute( "val") + "\u00B2 - " + adjacent.getAttribute( "val") + "\u00B2 )\n";
		}
		else
		{
			// no solution for this case
			return null;
		}

		solutionText += "So the length between " + trianglePoints[0] + " and " + trianglePoints[1] + " is " + MathHelpers.round( result, 2);

		// construct and return the solution node
		return new SolutionNode( solutionText, ( SolutionNode) adjacent.getAttribute( "sol"), 
				( SolutionNode) opposite.getAttribute( "sol"), ( SolutionNode) angle.getAttribute( "sol"));

	}
}
