package algorithm;
import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

// searches for ways to extend an angle
// using collinearity
public class SAngleExtender extends GeoSearcher {
	private Graph g;
	private TopologyProvider topology;
	
	public SAngleExtender ( Graph g)
	{
		this.g = g;
		this.topology = g.getAttribute( "top");
	}
	
	private final static String VISITED = "%VIS_ANG_EXT";

	@Override
	public void processNode(Node n, ArrayList<Node> toBeProcessed) {
		if ( n.getId().startsWith( GraphHelpers.Keys.ANGLE) && !n.hasAttribute( VISITED)) // unvisited angle node
		{
			n.addAttribute( VISITED);
			
			workOnAngleNode( n, toBeProcessed);			
		}
		else if ( n.getId().startsWith( GraphHelpers.Keys.COLLINEAR) && !n.hasAttribute( VISITED)) // unvisited collinearity node
		{
			ArrayList<Node> neighbors = GraphHelpers.getNeighbors( n);
			
			// process each neighboring angle node
			for ( Node nodePoint : neighbors)
			{
				for ( Node consNode : GraphHelpers.getNeighbors( nodePoint))
				{
					// if this is an angle node, process this.
					if ( consNode.getId().startsWith( GraphHelpers.Keys.ANGLE))
					{
						consNode.addAttribute( VISITED);
						workOnAngleNode( consNode, toBeProcessed);
					}
				}
			}
		}
	}
	
	// takes a node assumed to be an angle node, searches for ways to extends the angle
	private void workOnAngleNode ( Node angleNode, ArrayList<Node> toBeProcessed)
	{
		// the node which are on one side of the angle
		ArrayList<Node> sides1 = new ArrayList<Node>();
		// node on the center on the angle
		Node sideCenter;
		// nodes on the other side of the angle
		ArrayList<Node> sides3 = new ArrayList<Node>();

		sideCenter = null;

		// divide the neighboring nodes of this angle according to their positions
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
		sides1.add( sideCenter);
		sides3.add( sideCenter);
		
		// inside each direction, check for collinear relationships
		
		// inside first ray:
		for ( Node p1 : sides1)
		{
			for ( Node p2 : sides1)
			{
				if ( p1 != p2)
				{
					ArrayList<Node> collinearityNodes = GraphHelpers.getCommonNeighborsOfType( GraphHelpers.Keys.COLLINEAR, p1, p2);
					
					for ( Node col : collinearityNodes)
						extendAngle ( angleNode, sides3.get( 0), sideCenter, p1, p2, col, toBeProcessed);				
					
				}
			}
		}
		
		// the second ray
		// inside first ray:
		for ( Node p1 : sides3)
		{
			for ( Node p2 : sides3)
			{
				if ( p1 != p2)
				{
					ArrayList<Node> collinearityNodes = GraphHelpers.getCommonNeighborsOfType( GraphHelpers.Keys.COLLINEAR, p1, p2);

					for ( Node col : collinearityNodes)
						extendAngle ( angleNode, sides1.get( 0), sideCenter, p1, p2, col, toBeProcessed);				

				}
			}
		}
	}
	
	// extends an angle node taking the points' relative position with each other into account
	// the node anglePoint3 represents a point on the opposite ray of the angle
	// p1 and p2 are nodes on the ray to be extended
	private void extendAngle ( Node angleNode, Node anglePointOpposite, Node centerPoint, Node collinearPoint1, Node collinearPoint2, 
							   Node collinearity, ArrayList<Node> toBeProcessed)
	{
		// the collinear node which is not the center of the angle
		Node nodeRayOut;
		
		if ( collinearPoint1 == centerPoint)
			nodeRayOut = collinearPoint2;
		else
			nodeRayOut = collinearPoint1;
		
		for ( Node newPoint : GraphHelpers.getNeighbors( collinearity))
		{
			if ( newPoint != collinearPoint1 && newPoint != collinearPoint2 && newPoint != centerPoint)
			{
				// find where the new point is, relative to the ray points
				int posCode = topology.pointOnLineSegment( newPoint.getId().substring( 1), centerPoint.getId().substring( 1), nodeRayOut.getId().substring( 1));
				
				// on the same direction, inside the angle
				if ( posCode == TopologyProvider.POINT_ON_SEGMENT || posCode == TopologyProvider.POINT_OUTSIDE_2)
				{
					if ( !angleNode.hasEdgeBetween( newPoint))
					{
						// the code of the ray this point is located on
						int sideType;
						
						// TODO instead of adding an edge, can add a new angle node
						// TODO replicating the properties of the first one
						
						sideType = (int) nodeRayOut.getEdgeBetween( angleNode).getAttribute( "typ");
						
						g.addEdge( GraphHelpers.Keys.generateEdgeId(), newPoint, angleNode).addAttribute( "typ", sideType);
						
						// make the angle node unvisited so that other searchers notice
						GraphHelpers.setNodeUnvisited( angleNode);
						
						toBeProcessed.add( angleNode);
					}			
				}
				// the point is outside, it is this one's supplementary
				else
				{
					// create the angle nodes not already present
					if ( GraphHelpers.getAngleBetween( anglePointOpposite, centerPoint, newPoint) == null)
					{
						toBeProcessed.add( addSupplementaryAngle ( angleNode, newPoint, centerPoint, collinearPoint1, anglePointOpposite, collinearity));
					}
					if ( GraphHelpers.getAngleBetween( anglePointOpposite, centerPoint, newPoint) == null)
					{
						toBeProcessed.add( addSupplementaryAngle ( angleNode, newPoint, centerPoint, collinearPoint2, anglePointOpposite, collinearity));
					}
					
				}
			}
			
		}
	}
	
	// add the supplementary angle node with the appropriates solution data
	// centerPoint is the point of the center of the angle, p2 is the point
	// shared by the two angle on their common rays
	private Node addSupplementaryAngle (Node angleNode, Node newPoint, Node centerPoint, Node collinearPoint, Node anglePointOpposite, Node collinearity)
	{
		SolutionNode solution;
		String explanation;
		
		// the supplementary angle
		Node newAngleNode;
		
		// build the explanation for supplementary angle step
		explanation = "\u2220" + newPoint.getId().substring( 1) + "- "+ centerPoint.getId().substring( 1) + " - " + collinearPoint.getId().substring( 1);
		explanation += " = " + " 180 - " + "\u2220" + anglePointOpposite.getId().substring( 1) + "- "+ centerPoint.getId().substring( 1) + " - " + collinearPoint.getId().substring( 1);
		explanation += " = " + MathHelpers.round(( 180 - MathHelpers.toDegrees( (double) angleNode.getAttribute( "val"))), 2);
		
		solution = new SolutionNode ( explanation, ( SolutionNode) angleNode.getAttribute( "sol"), ( SolutionNode) collinearity.getAttribute( "sol"));
		
		// prepare the new angle node and add the appropriate edges
		newAngleNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
		
		newAngleNode.addAttribute( "val", Math.PI - ((Double) angleNode.getAttribute( "val")));
		newAngleNode.addAttribute( "sol", solution);
		
		g.addEdge( GraphHelpers.Keys.generateEdgeId(), newAngleNode, newPoint).addAttribute( "typ", 1);
		g.addEdge( GraphHelpers.Keys.generateEdgeId(), newAngleNode, anglePointOpposite).addAttribute( "typ", 3);
		g.addEdge( GraphHelpers.Keys.generateEdgeId(), newAngleNode, centerPoint).addAttribute( "typ", 2);
		
		return newAngleNode;
	}
		
	

}
