package algorithm;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class SAngleAddition extends GeoSearcher {
	private Graph g;
	private TopologyProvider top;
	
	private final static String VISITED = "%VIS_ANG_ADD2";
	
	public SAngleAddition ( Graph g)
	{
		this.g = g;
		this.top = g.getAttribute( "top");
	}
	
	@Override
	public void processNode(Node n, ArrayList<Node> toBeProcessed) {
		if ( n.getId().startsWith( GraphHelpers.Keys.ANGLE) && !n.hasAttribute( VISITED)) // an unvisited angle node
		{
			n.addAttribute( VISITED);
			
			Node angleNode = n;
			
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
			
			// search for angles to add for each opposing point pair
			for ( Node point1 : sides1)
			{
				for ( Node point2 : sides3)
				{
					if ( point1 != point2)
					{
						searchForAnglesToAdd( angleNode, sideCenter, point1, point2, sides1, sides3, toBeProcessed);
						searchForAnglesToAdd( angleNode, sideCenter, point2, point1, sides3, sides1, toBeProcessed);
					}
				}
			}	
		}		
	}

	// searches for angles to merge having pointCommon as a
	// common point with this angle
	private void searchForAnglesToAdd ( Node angleNode, Node pointCenter, Node pointCommon, Node pointOpposite, 
										ArrayList<Node> sidesCommon, ArrayList<Node> sidesOpposite, ArrayList<Node> toBeProcessed)
	{
		// possible angles which can be merged
		ArrayList<Node> angleCandidates;
		
		angleCandidates = GraphHelpers.getAnglesOnRay( pointCenter, pointCommon);
		
		for ( Node neighborAngleNode : angleCandidates)
		{
			if ( (int) neighborAngleNode.getEdgeBetween( pointCenter).getAttribute( "typ") == 2 && neighborAngleNode != angleNode)
			{
				int commonCode;
				commonCode = (int) neighborAngleNode.getEdgeBetween( pointCommon).getAttribute( "typ");
				
				for ( Node newPoint : GraphHelpers.getNeighbors( neighborAngleNode))
				{
					if ( commonCode != (int) neighborAngleNode.getEdgeBetween( newPoint).getAttribute( "typ") && 
						 (int) neighborAngleNode.getEdgeBetween( newPoint).getAttribute( "typ") != 2)
					{
						Node newAngleNode = addNode ( neighborAngleNode, angleNode, pointCenter, pointCommon, newPoint, pointOpposite);
						
						if ( newAngleNode != null)
							toBeProcessed.add( newAngleNode);
					}
				}
			}
		}
	}
	
	private Node addNode ( Node angleNode1, Node angleNode2, Node pointCenter, Node pointCommon, Node pointOpposite1, Node pointOpposite2)
	{
		// the data of the angle to be created
		Node newNode;
		SolutionNode solutionStep;
		String explanation;
		
		// the relative positions of the angles and points
		boolean isPoint1Inside, isPoint2Inside;
		
		// if the angle we are attempting to add already exists, return
		if ( GraphHelpers.getAngleBetween( pointOpposite1, pointCenter, pointOpposite2) != null)
			return null;
		
		isPoint1Inside = top.isPointInsideAngle( pointOpposite1.getId().substring(1), pointCommon.getId().substring(1), 
												 pointCenter.getId().substring(1), pointOpposite2.getId().substring(1));
		isPoint2Inside = top.isPointInsideAngle( pointOpposite2.getId().substring(1), pointCommon.getId().substring(1), 
				 								 pointCenter.getId().substring(1), pointOpposite1.getId().substring(1));
		
		if ( !isPoint1Inside && !isPoint2Inside)
		{
			// angles can be summed, if their sum is smaller that 180 degrees
			if ( (double) angleNode1.getAttribute( "val") + (double) angleNode2.getAttribute( "val") < Math.PI)
			{
				newNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
				newNode.addAttribute( "val", (double) angleNode1.getAttribute( "val") + (double) angleNode2.getAttribute( "val"));
				
				explanation = "\u2220" + pointOpposite1.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointOpposite2.getId().substring( 1);
				explanation += " = \u2220" + pointOpposite1.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointCommon.getId().substring( 1);
				explanation += " + \u2220" + pointOpposite2.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointCommon.getId().substring( 1);
				explanation += " = " + MathHelpers.round( MathHelpers.toDegrees( (double) angleNode1.getAttribute( "val") + (double) angleNode2.getAttribute( "val")), 2);
				
				solutionStep = new SolutionNode( explanation, (SolutionNode) angleNode1.getAttribute( "sol"), (SolutionNode) angleNode2.getAttribute( "sol"));
				
				newNode.addAttribute( "sol", solutionStep);
				
				// connect the angle to the corner points
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointOpposite1).addAttribute( "typ", 1);
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointOpposite2).addAttribute( "typ", 3);
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointCenter).addAttribute( "typ", 2);
				
				return newNode;
			}
		}
		else if ( isPoint1Inside && !isPoint2Inside) // point 1 is inside the second angle
		{
			// the new angle should be the difference between the two older ones
			newNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
			newNode.addAttribute( "val", (double) angleNode2.getAttribute( "val") - (double) angleNode1.getAttribute( "val"));
			
			explanation = "\u2220" + pointOpposite1.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointOpposite2.getId().substring( 1);
			explanation += " = \u2220" + pointOpposite2.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointCommon.getId().substring( 1);
			explanation += " - \u2220" + pointOpposite1.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointCommon.getId().substring( 1);
			explanation += " = " + MathHelpers.round( MathHelpers.toDegrees( (double) angleNode2.getAttribute( "val") - (double) angleNode1.getAttribute( "val")), 2);
			
			solutionStep = new SolutionNode( explanation, (SolutionNode) angleNode1.getAttribute( "sol"), (SolutionNode) angleNode2.getAttribute( "sol"));
			
			newNode.addAttribute( "sol", solutionStep);
			
			// connect the angle to the corner points
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointOpposite1).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointOpposite2).addAttribute( "typ", 3);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointCenter).addAttribute( "typ", 2);
			
			return newNode;
		}
		else if ( !isPoint1Inside && isPoint2Inside) // point 2 is inside the first angle
		{
			// the new angle should be the difference between the two older ones
			newNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
			newNode.addAttribute( "val", (double) angleNode1.getAttribute( "val") - (double) angleNode2.getAttribute( "val"));
			
			explanation = "\u2220" + pointOpposite1.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointOpposite2.getId().substring( 1);
			explanation += " = \u2220" + pointOpposite1.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointCommon.getId().substring( 1);
			explanation += " - \u2220" + pointOpposite2.getId().substring( 1) + "-" + pointCenter.getId().substring( 1) + "-" + pointCommon.getId().substring( 1);
			explanation += " = " + MathHelpers.round( MathHelpers.toDegrees( (double) angleNode1.getAttribute( "val") - (double) angleNode2.getAttribute( "val")), 2);
			
			solutionStep = new SolutionNode( explanation, (SolutionNode) angleNode1.getAttribute( "sol"), (SolutionNode) angleNode2.getAttribute( "sol"));
			
			newNode.addAttribute( "sol", solutionStep);
			
			// connect the angle to the corner points
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointOpposite1).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointOpposite2).addAttribute( "typ", 3);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newNode, pointCenter).addAttribute( "typ", 2);
			
			return newNode;
		}
		
		// no angle added
		return null;
	}
}
