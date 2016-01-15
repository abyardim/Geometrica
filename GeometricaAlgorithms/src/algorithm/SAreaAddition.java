package algorithm;
import java.util.ArrayList;



import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;


public class SAreaAddition extends GeoSearcher {

	private Graph g;
	private TopologyProvider topology;
	
	public SAreaAddition ( Graph g)
	{
		this.g = g;
		this.topology = ( TopologyProvider) g.getAttribute( "top");
	}
	
	@Override
	public void processNode(Node n, ArrayList<Node> toBeProcessed) {
		if ( n.getId().startsWith( GraphHelpers.Keys.AREA)) // an area relation
		{
			for ( int i = 0; i < n.getDegree(); i++)
			{
				Node commonNode1, commonNode2;
				
				commonNode1 = getAreaNthVertex( n, i + 1);
				commonNode2 = getAreaNthVertex( n, ( i + 1) % n.getDegree() + 1);
				
				// iterate through all neighboring area properties
				for ( Node neighborArea : GraphHelpers.getCommonNeighborsOfType( GraphHelpers.Keys.AREA, commonNode1, commonNode2))
				{
					if ( neighborArea != n && GraphHelpers.getCommonNeighbors( n, neighborArea).size() == 2)
					{
						// get the relative locations of the area
						int location = topology.isPolygonInsidePolygon( commonNode1.getId().substring( 1), commonNode2.getId().substring( 1), 
														getVertexNames( neighborArea), getVertexNames( n));
						
						// the first polygon is inside the second
						if ( location == topology.POLYGON1_INSIDE)
						{
							Node newArea = mergeAreas ( n, neighborArea, commonNode1, commonNode2, true);
							
							if ( newArea != null)
								toBeProcessed.add( newArea);
						}
						// the second polygon is inside the first
						else if ( location == topology.POLYGON2_INSIDE)
						{
							Node newArea = mergeAreas ( neighborArea, n, commonNode1, commonNode2, true);
							
							if ( newArea != null)
								toBeProcessed.add( newArea);
						}
						// the polygons are outside each other
						else if ( location == topology.POLYGONS_OUTSIDE)
						{
							Node newArea = mergeAreas ( neighborArea, n, commonNode1, commonNode2, false);
							
							if ( newArea != null)
								toBeProcessed.add( newArea);
						}
					}
					
				}
			}
			
		}
	} // end of method processNode
	
	// combines the given areas to calculate and add their common area node
	// commonPoint1 and commonPoint2 are the point shared by the areas
	// subtract indicates whether the areas will be summed or their difference will be taken
	private Node mergeAreas ( Node area1, Node area2, Node commonPoint1, Node commonPoint2, boolean subtract)
	{
		// the information relating to the new area node
		Node newArea; 
		SolutionNode solution;
		String explanation;
		
		// the list of point nodes of the new area polygon in order
		ArrayList<Node> vertexNodes;
		Node[] vertexNodesArray;
		
		// the start indices of the common nodes, with respect to both areas
		int startAreaIndice1 = ( int) area1.getEdgeBetween( commonPoint1).getAttribute( "typ");
		int startAreaIndice2 = ( int) area2.getEdgeBetween( commonPoint2).getAttribute( "typ");
		
		vertexNodes = new ArrayList<Node>();
		vertexNodes.ensureCapacity( area1.getDegree() + area2.getDegree() - 2);
		
		for ( int i = 0; i < area1.getDegree(); i++)
		{
			vertexNodes.add( getAreaNthVertex( area1, ( i + startAreaIndice1 - 1) % area1.getDegree() + 1));
		}
		
		// connect the second area
		for ( int i = 1; i < area2.getDegree() - 1; i++)
		{
			vertexNodes.add( getAreaNthVertex( area2, ( i + startAreaIndice2 - 1) % area2.getDegree() + 1));
		}
		
		vertexNodesArray = new Node[area1.getDegree() + area2.getDegree() - 2];
		vertexNodesArray = vertexNodes.toArray( vertexNodesArray);
		
		// if such an area already exists, return
		if ( GraphHelpers.getAreaBetween( vertexNodesArray) != null)
			return null;
		
		// create the new area node
		newArea = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.AREA));
		
		for ( int i = 0; i < vertexNodesArray.length; i++)
		{
			// make the edges between each vertex
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), vertexNodesArray[i], newArea).addAttribute( "typ", i + 1);
		}
		
		if ( subtract)
			newArea.addAttribute( "val", (double) area1.getAttribute( "val") - (double) area2.getAttribute( "val"));
		else
			newArea.addAttribute( "val", (double) area1.getAttribute( "val") + (double) area2.getAttribute( "val"));
		
		explanation = "The area " + getAreaName( newArea) + " = " + getAreaName( area1) + ( subtract ? " - " : "+") + getAreaName( area2);
		explanation += 	" = " + MathHelpers.round( (double) newArea.getAttribute( "val"), 2);
		
		newArea.addAttribute( "sol", new SolutionNode( explanation, ( SolutionNode) area1.getAttribute( "sol"), ( SolutionNode) area2.getAttribute( "sol")));
		
		return newArea;
		
	}
	
	// returns the names of the vertices of an area node in an area
	private String[] getVertexNames ( Node areaNode)
	{
		String[] names = new String[ areaNode.getDegree()];
		
		for ( int i = 1; i <= areaNode.getDegree(); i++)
		{
			names[i - 1] = getAreaNthVertex( areaNode, i).getId().substring( 1);
		}
		
		return names;
	}
	
	// returns the textual representation of an area
	private String getAreaName ( Node areaNode)
	{
		String name = "A( ";
		name += getAreaNthVertex( areaNode, 1).getId().substring( 1);
		
		for ( int i = 2; i <= areaNode.getDegree(); i++)
		{
			name += ", " + getAreaNthVertex( areaNode, i).getId().substring( 1);
		}
		
		name += ")";
		
		return name;
	}
	
	// returns the 'no'th corner of the area node
	private static Node getAreaNthVertex ( Node areaNode, int no)
	{
		if ( no > areaNode.getDegree())
			return null;
		
		Iterator<Edge> edgeIterator = areaNode.getEdgeIterator();
		while ( edgeIterator.hasNext())
		{
			Edge e = edgeIterator.next();
			if ( (int) e.getAttribute( "typ") == no)
				return GraphHelpers.getEdgeOtherSide( e, areaNode);
		}
		
		return null;
	}

}
