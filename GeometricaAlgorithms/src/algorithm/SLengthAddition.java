package algorithm;
import java.util.ArrayList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;


public class SLengthAddition extends GeoSearcher {
	private Graph g;
	private TopologyProvider topology;
	
	private static final String VISITED = "%VIS_LEN_ADD";
	
	public SLengthAddition ( Graph g)
	{
		this.g = g;
		this.topology = (TopologyProvider) g.getAttribute( "top");
	}

	@Override
	public void processNode ( Node n, ArrayList<Node> toBeProcessed) {
		
		if ( n.getId().startsWith( GraphHelpers.Keys.LENGTH) && !n.hasAttribute( VISITED))
		{
			n.addAttribute( VISITED);
			
			// if this is a length node, process nearby collinearity nodes
			
			// the points of the length
			Node point1, point2;
			
			point1 = GraphHelpers.getEdgeOtherSide( n.getEdge( 0), n);
			point2 = GraphHelpers.getEdgeOtherSide( n.getEdge( 0), n);
			ArrayList<Node> commonNeighbors = GraphHelpers.getCommonNeighbors( point1, point2);
			
			for ( Node commonNode : commonNeighbors)
			{
				if ( commonNode.getId().startsWith( GraphHelpers.Keys.COLLINEAR))
					processCollinearityNode( n, toBeProcessed);
			}
			
		}
		if ( n.getId().startsWith( GraphHelpers.Keys.COLLINEAR) && !n.hasAttribute( VISITED))
		{
			n.addAttribute( VISITED);
			processCollinearityNode( n, toBeProcessed);
		}
	}
	
	private void processCollinearityNode ( Node n, ArrayList<Node> toBeProcessed)
	{
		// the list of the nodes related to this line
		ArrayList<Node> points;
		
		points = GraphHelpers.getNeighbors( n);
		
		for ( Node point1 : points)
		{
			ArrayList<Node> lengthNodes = new ArrayList<Node>();
			
			// populate the list of nodes originating from here
			for ( Node point2 : points)
			{
				if ( point2 != point1)
				{
					Node l = GraphHelpers.getLengthBetween( point1, point2);
					
					if ( l != null)
						lengthNodes.add( l);
				}
			}
			
			for ( Node l1 : lengthNodes)
			{
				for ( Node l2 : lengthNodes)
				{
					// for each pairs of the length nodes, process for length additions
					if ( l1 != l2)
					{
						Node processedPoint1, processedPoint2;
						
						// choose the right point-length pairs:
						if ( GraphHelpers.getEdgeOtherSide( l1.getEdge( 0), l1) != point1)
							processedPoint1 = GraphHelpers.getEdgeOtherSide( l1.getEdge( 0), l1);
						else
							processedPoint1 = GraphHelpers.getEdgeOtherSide( l1.getEdge( 1), l1);
						
						if ( GraphHelpers.getEdgeOtherSide( l2.getEdge( 0), l2) != point1)
							processedPoint2 = GraphHelpers.getEdgeOtherSide( l2.getEdge( 0), l2);
						else
							processedPoint2 = GraphHelpers.getEdgeOtherSide( l2.getEdge( 1), l2);
						
						processTriple ( point1, processedPoint1, processedPoint2, 
										l1, l2, toBeProcessed);
					}
				}
			}			
			
		}
		
	}
	
	// processes a triple of points where the two lengths are known
	private void processTriple ( Node commonPoint, Node point1, Node point2,
								 Node length1, Node length2, ArrayList<Node> toBeProcessed)
	{
		// if the length is already known, return
		if ( GraphHelpers.getLengthBetween( point1, point2) != null)
		{
			return;
		}
		
		// whether or not the points intersect
		int intersectionStatus;
		
		intersectionStatus = topology.pointOnLineSegment( point1.getId().substring( 1), commonPoint.getId().substring( 1), 
														  point2.getId().substring( 1));
		
		
		if ( intersectionStatus == TopologyProvider.POINT_OUTSIDE_1) 
		{
			// point 1 is outside the segment and is in the side of the common point
			Node newLength = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));
			
			double lengthValue1, lengthValue2;
			
			// setup the new length
			lengthValue1 = length1.getAttribute( "val");
			lengthValue2 = length2.getAttribute( "val");
			
			newLength.addAttribute( "val", lengthValue1 + lengthValue2);
			newLength.addAttribute( "sol", generateSolution ( commonPoint, point1, point2,
		 							length1, length2, true, lengthValue1 + lengthValue2));
			
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newLength, point1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newLength, point2);
			
			toBeProcessed.add( newLength);
			
		}
		else if ( intersectionStatus == TopologyProvider.POINT_OUTSIDE_2)
		{
			// point 1 is outside the segment and is in the side of the point 2
			Node newLength = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));
			
			double lengthValue1, lengthValue2;
			
			// setup the new length
			lengthValue1 = length1.getAttribute( "val");
			lengthValue2 = length2.getAttribute( "val");
			
			newLength.addAttribute( "val", lengthValue1 + lengthValue2);
			newLength.addAttribute( "sol", generateSolution ( commonPoint, point1, point2,
		 							length1, length2, false, lengthValue1 - lengthValue2));
			
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newLength, point1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newLength, point2);
			
			toBeProcessed.add( newLength);
		}
		else if ( intersectionStatus == TopologyProvider.POINT_ON_SEGMENT)
		{
			// point 1 lies between point 2 and the common point
			Node newLength = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));
			
			double lengthValue1, lengthValue2;
			
			// setup the new length
			lengthValue1 = length1.getAttribute( "val");
			lengthValue2 = length2.getAttribute( "val");
			
			newLength.addAttribute( "val", lengthValue1 + lengthValue2);
			newLength.addAttribute( "sol", generateSolution ( commonPoint, point1, point2,
		 							length2, length1, false, lengthValue2 - lengthValue1));
			
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newLength, point1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), newLength, point2);
			
			toBeProcessed.add( newLength);
		}
		
	}

	// generates the solution for this operation
	private SolutionNode generateSolution ( Node commonPoint, Node point1, Node point2, Node length1, 
											Node length2, boolean plus, double result)
	{
		String explanation;
		
		explanation = "The length " + point1.getId().substring( 1) + "-" + point2.getId().substring( 1);
		explanation += " is equal to |" + commonPoint.getId().substring( 1) + "-" + point1.getId().substring( 1) + "| ";
		explanation += ( plus ? " + " : " - ") + point2.getId().substring( 1) + "-" + commonPoint.getId().substring( 1);
		explanation += " = " + MathHelpers.round( result, 2);
		
		return new SolutionNode ( explanation, ( SolutionNode) length1.getAttribute( "sol"), 
								 ( SolutionNode) length1.getAttribute( "sol"));
		
	}
}
