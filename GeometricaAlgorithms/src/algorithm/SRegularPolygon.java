package algorithm;

import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class SRegularPolygon extends GeoSearcher {
	// the graph the search is conducted
	private Graph g;

	// the attribute identifier for detecting already visited nodes
	private static final String VISITED = "%VIS_REG_POLYGON";
	// notes if a regular polygon's properties are fully known
	private static final String POLYGON_WELL_DETERMINED = "%VIS_DET";
	
	// construct from the graph to search on
	public SRegularPolygon ( Graph g)
	{
		this.g = g;
	}

	@Override
	public void processNode( Node n, ArrayList<Node> toBeProcessed) {
		if ( n.getId().startsWith( GraphHelpers.Keys.REGULAR_POLYGON) && ! n.hasAttribute( VISITED)) // an unvisited regular polygon identifier node
		{
			n.addAttribute( VISITED);
			processRegularPolygon( n, toBeProcessed);
		}
		else if ( n.getId().startsWith( GraphHelpers.Keys.LENGTH) && ! n.hasAttribute( VISITED)) // an unvisited length node
		{
			n.addAttribute( VISITED);
			
			// for length nodes, process each nearby
			for ( Node point : GraphHelpers.getCommonNeighbors( GraphHelpers.getEdgeOtherSide( n.getEdge(0), n), GraphHelpers.getEdgeOtherSide( n.getEdge(1), n)))
			{
				for ( Node nearByConstraint: GraphHelpers.getNeighbors( point))
				{
					if ( nearByConstraint.getId().startsWith( GraphHelpers.Keys.REGULAR_POLYGON))
					{
						nearByConstraint.addAttribute( VISITED);
						
						processRegularPolygon( nearByConstraint, toBeProcessed);
					}
				}
			}
		}	
	}
	
	// processes a graph node that is assumed to be a regular polygon node
	private void processRegularPolygon ( Node regularPolygon, ArrayList<Node> toBeProcessed)
	{
		if ( regularPolygon.hasAttribute( POLYGON_WELL_DETERMINED))
			return;
	
		addInteriorAngles( regularPolygon, toBeProcessed);
		addLengths( regularPolygon, toBeProcessed);
		addArea( regularPolygon, toBeProcessed);
	}
	
	// calculates the area of the rgular polygon if possible
	private void addArea ( Node regularPolygon, ArrayList<Node> toBeProcessed)
	{
		// the length of one side of the polygon, if it is known
		double length = -1;
		Node lengthNode = null;
		
		Node p1, p2;
		
		p1 = p2 = null;
		
		// search for a known side length
		for ( int i = 1; i <= regularPolygon.getDegree(); i++)
		{
			Node lengthTemp;
			
			p1 = getNthVertex( regularPolygon, i);
			p2 = getNthVertex( regularPolygon, i % regularPolygon.getDegree() + 1);
			
			lengthTemp = GraphHelpers.getLengthBetween( p1, p2);
			
			// if we find a known length, store it
			if ( lengthTemp != null)
			{
				lengthNode = lengthTemp;
				length = ( double) lengthTemp.getAttribute( "val");
				
				break;
			}
		}
		
		// if no known length exists, cannot infer other
		if ( lengthNode == null)
			return;
		
		// if the length is known, calculate the area
		Node areaNode;
		SolutionNode solution;
		String explanation;
		
		explanation = "The area of the polygon " + getPolygonName( regularPolygon) + " has area ";
		explanation += regularPolygon.getDegree() + " \u00D7 " + "A( " + p1.getId().substring( 1) + ", O, " 
						+ p2.getId().substring( 1) + ")";
		explanation += " = " + MathHelpers.round( calculateArea( length, regularPolygon.getDegree()), 2);
				
		solution = new SolutionNode( explanation, (SolutionNode) regularPolygon.getAttribute( "sol"), (SolutionNode) lengthNode.getAttribute( "sol"));
		
		// add the area to the problem graph
		areaNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.AREA));
		areaNode.addAttribute( "val", calculateArea( length, regularPolygon.getDegree()));
		areaNode.addAttribute( "sol", solution);
		
		// connect the area node to its vertices
		for ( int i = 0; i < regularPolygon.getDegree(); i++)
		{
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), getNthVertex( regularPolygon, i + 1), areaNode).addAttribute( "typ", i + 1);
		}		
		
		toBeProcessed.add( areaNode);
	}
	
	private double calculateArea ( double sideLength, int sides)
	{
		return sides * 0.5 * sideLength * 0.5 * sideLength * 0.5 * Math.sin( Math.PI * 2 / sides)
				/ ( MathHelpers.square( Math.cos( calculateAngleValue ( sides) / 2)));
	}
	
	// processes a regular polygon to update side lengths
	private void addLengths ( Node regularPolygon, ArrayList<Node> toBeProcessed)
	{
		// the length of one side of the polygon, if it is known
		double length = -1;
		Node lengthNode = null;
		
		// search for a known side length
		for ( int i = 0; i < regularPolygon.getDegree(); i++)
		{
			Node p1, p2;
			Node lengthTemp;
			
			p1 = getNthVertex( regularPolygon, i % regularPolygon.getDegree() + 1);
			p2 = getNthVertex( regularPolygon, (i + 1) % regularPolygon.getDegree() + 1);
			
			lengthTemp = GraphHelpers.getLengthBetween( p1, p2);
			
			// if we find a known length, store it
			if ( lengthTemp != null)
			{
				lengthNode = lengthTemp;
				length = ( double) lengthTemp.getAttribute( "val");
				
				break;
			}
		}
		
		// if no known length exists, cannot infer other
		if ( lengthNode == null)
			return;
		
		String explanation = "On the regular polygon " + getPolygonName( regularPolygon) + " the length " +
							 "|" + GraphHelpers.getNeighbors( lengthNode).get( 0).getId().substring( 1) + "-" + GraphHelpers.getNeighbors( lengthNode).get( 1).getId().substring( 1) +
							 "| = |%REPLACE%| = " + MathHelpers.round( length, 2) ;
		
		// add the known length to every side
		for ( int i = 0; i < regularPolygon.getDegree(); i++)
		{
			Node p1, p2;
			Node lengthTemp;
			
			p1 = getNthVertex( regularPolygon, i % regularPolygon.getDegree() + 1);
			p2 = getNthVertex( regularPolygon, (i + 1) % regularPolygon.getDegree() + 1);
			
			lengthTemp = GraphHelpers.getLengthBetween( p1, p2);
			
			// length not known, add it
			if ( lengthTemp == null)
			{
				lengthTemp = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));
				lengthTemp.addAttribute( "val", length);
				lengthTemp.addAttribute( "sol", new SolutionNode( explanation.replace( "%REPLACE%", p1.getId().substring( 1) + "-" + p2.getId().substring( 1)), 
																  (SolutionNode) lengthNode.getAttribute( "sol"), (SolutionNode) regularPolygon.getAttribute( "sol")));
				
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), lengthTemp, p1);
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), lengthTemp, p2);
				
				toBeProcessed.add( lengthTemp);
			}
		}
		
		// the regular polygon is now well-determined
		regularPolygon.addAttribute( POLYGON_WELL_DETERMINED, true);
	} // end of addLengths()
	
	// processes a regular polygon node for angles
	private void addInteriorAngles ( Node regularPolygon, ArrayList<Node> toBeProcessed)
	{
		for ( int i = 0; i < regularPolygon.getDegree(); i++)
		{
			// the three vertices of the angle
			Node vertex1, vertex2, vertex3;
			
			vertex1 = getNthVertex( regularPolygon, ( i % regularPolygon.getDegree()) + 1);
			vertex2 = getNthVertex( regularPolygon, ( ( i + 1) % regularPolygon.getDegree()) + 1);
			vertex3 = getNthVertex( regularPolygon, ( ( i + 2) % regularPolygon.getDegree()) + 1);
			
			Node angleNode = GraphHelpers.getAngleBetween( vertex1, vertex2, vertex3);
			
			// if this angle is not known, add it
			if ( angleNode == null)
			{
				SolutionNode sol;
				String explanation;
				
				explanation = getPolygonName( regularPolygon) + " is a regular polygon, the angle \u2220" + vertex1.getId().substring( 1) + "-"
							  + vertex2.getId().substring( 1) + "-" + vertex3.getId().substring( 1) + " = 180 \u00D7  ( " + regularPolygon.getDegree() + " - 2) / " + regularPolygon.getDegree() +
							  " = " + MathHelpers.round( MathHelpers.toDegrees( calculateAngleValue( regularPolygon.getDegree())), 2);
				
				sol = new SolutionNode( explanation, ( SolutionNode) regularPolygon.getAttribute( "sol"));
				
				// add the new angle to the graph
				angleNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
				
				angleNode.addAttribute( "val", calculateAngleValue( regularPolygon.getDegree()));
				angleNode.addAttribute( "sol", sol);
				
				// connect the angle node to its points
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), angleNode, vertex1).addAttribute( "typ", 1);
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), angleNode, vertex2).addAttribute( "typ", 2);
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), angleNode, vertex3).addAttribute( "typ", 3);
				
				toBeProcessed.add( angleNode);
			}
		}
	} // end of addInteriorAngles()
	
	// returns the measure of one angle of a regular polygon
	private double calculateAngleValue ( double sides)
	{
		return Math.PI * ( sides - 2) / sides;
	}
	
	// returns the mathematical name of a polygon node
	private String getPolygonName ( Node polygon)
	{
		String result;
		
		result = getNthVertex( polygon, 1).getId().substring( 1);
		
		for ( int i = 2; i <= polygon.getDegree(); i++)
		{
			result += "-" + getNthVertex( polygon, i).getId().substring( 1);
		}
		
		return result;
	}
	

	
	// returns the nth vertex node of the regular polygon
	private Node getNthVertex ( Node regularPolygon, int n)
	{
		if ( n > regularPolygon.getDegree())
			return null;
		
		Iterator<Edge> edgeIterator = regularPolygon.getEdgeIterator();
		while ( edgeIterator.hasNext())
		{
			Edge e = edgeIterator.next();
			if ( (int) e.getAttribute( "typ") == n)
				return GraphHelpers.getEdgeOtherSide( e, regularPolygon);
		}
		
		return null;
	}
	
}
