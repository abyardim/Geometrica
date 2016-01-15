package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class GraphHelpers {
	
	// id keys for distinguishing graph nodes
	public static class Keys {
		public static final String EQUAL_LENGTH 			= "#EQL";
		public static final String ANGLE 					= "#ANG";
		public static final String EQUAL_ANGLE 				= "#EQA";
		public static final String PERPENDICULAR 			= "#PRP";
		public static final String LENGTH 					= "#LEN";
		public static final String PARALLEL 				= "#PAR";
		public static final String AREA 					= "#ARE";
		public static final String COLLINEAR 				= "#COL";
		public static final String POINT_ON_SEGMENT			= "#POS";
		public static final String POINT_TO_LINE_DISTANCE	= "#PLD";
		public static final String REGULAR_POLYGON			= "#RGP";
		
		// returns true if the node is a point node
		public static boolean isPointNode ( Node n)
		{
			return n.getId().startsWith( "!");
		}
		
		// returns true if the node is a point node
		public static boolean isConstraintNode ( Node n)
		{
			return n.getId().startsWith( "#");
		}
	
		// key generators:
		
		// to keep track of used keys;
		private static int lastEdgeId = 0;
		private static int lastNodeId = 0;
		
		// generates an unused edge id
		public static String generateEdgeId ( )
		{
			return "e:" + ( lastEdgeId++);
		}
		
		// generates an unused point id
		public static String generatePointNodeId ( String name)
		{
			return "!" + name /*+ ":" + ( lastNodeId++)*/;
		}
		
		// generates an unused constraint id
		public static String generateConstraintNodeId ( String type)
		{
			return type + ":" + ( lastNodeId++);
		}
	}
	
	// returns the node of a function
	public static Node findPointNode ( String pointName, Graph g)
	{
		for ( Node n : g.getEachNode())
		{
			if ( n.getId().equals( "!" + pointName))
				return n;
		}
		
		return null;
		
	}
	
	// returns the node of an edge that is not the given one
	public static Node getEdgeOtherSide ( Edge e, Node n)
	{
		if ( e.getNode0() == n)
			return e.getNode1();
		return e.getNode0();
	}
	
	// returns the neighbor nodes of a given node
	public static ArrayList<Node> getNeighbors ( Node n)
	{
		Iterator<Node> neighboursA = n.getNeighborNodeIterator();
		ArrayList<Node> list = new  ArrayList<Node>();
		while ( neighboursA.hasNext())
		{
			Node ne = neighboursA.next();

			list.add( ne);
		}
		
		return list;
	}

	// returns the neighbor nodes of a given node having a specified type
	public static ArrayList<Node> getNeighborsOfType ( String type, Node n)
	{
		Iterator<Node> neighboursA = n.getNeighborNodeIterator();
		ArrayList<Node> list = new  ArrayList<Node>();
		while ( neighboursA.hasNext())
		{
			Node ne = neighboursA.next();

			if ( ne.getId().startsWith( type))
				list.add( ne);
		}

		return list;
	}
	
	// returns a list of nodes which are common neighbors of both n1 and n2
	public static ArrayList<Node> getCommonNeighbors ( Node n1, Node n2)
	{
		Iterator<Node> neighboursA = n1.getNeighborNodeIterator();
		ArrayList<Node> list = new  ArrayList<Node>();
		while ( neighboursA.hasNext())
		{
			Node ne = neighboursA.next();
			
			// if is a neighbor to both, add to the list
			if ( ne.hasEdgeBetween( n2))
				list.add( ne);
		}
		
		return list;
	}
	
	// returns a list of nodes which are common neighbors of nodes
	public static ArrayList<Node> getCommonNeighbors ( Node ... nodes)
	{
		ArrayList<Node> commonNeighbors;
		Iterator<Node> commonNeighborIterator;
		
		if ( nodes.length == 0)
			return new ArrayList<Node>();
		else if ( nodes.length == 1)
			return getNeighbors ( nodes[0]);
		else if ( nodes.length == 2)
			return getCommonNeighbors ( nodes[0], nodes[1]);
		
		commonNeighbors = getCommonNeighbors ( nodes[0], nodes[1]);
		
		// loop while finding the effective intersection of the common neighbors
		for ( int i = 2; i < nodes.length; i++)
		{
			commonNeighborIterator = commonNeighbors.iterator();
			
			while ( commonNeighborIterator.hasNext())
			{
				// if the node does not have an edge towards this, remove it from the
				// list of common nodes
				if ( !nodes[i].hasEdgeBetween( commonNeighborIterator.next()))
				{
					commonNeighborIterator.remove();
				}
			}
		}
		
		return commonNeighbors;
	}
	

	
	
	// the iterable object for the neighbors of a node
	public static class NeighborIterable implements Iterable<Node>
	{
		private Iterator<Node> iter;
		public NeighborIterable( Iterator<Node> i)
		{
			iter = i;
		}

		@Override
		public Iterator<Node> iterator() {
			return iter;
		}
	}

	// returns an iterable interface for the neighbors of a node
	public static NeighborIterable getNeighborIterable ( Node n)
	{
		return new NeighborIterable( n.getNeighborNodeIterator());
	}
	
	// returns the corresponding "length" node between the point node n1 and n2
	// if there is no length data, returns null
 	public static Node getLengthBetween ( Node n1, Node n2)
	{
		// check until we find a length node
		for ( Node neighbor : getCommonNeighbors( n1, n2))
		{
			if ( neighbor.getId().startsWith( Keys.LENGTH))
				return neighbor;
		}
		
		return null;
	}
	
	// returns the corresponding "angle" node between the points n1, n2 and m
	// m is the center point of the angle
	// if there is no angle data, returns null
	public static Node getAngleBetween ( Node n1, Node m, Node n2)
	{
		// check until we find the appropriate node
		for ( Node neighbor : getCommonNeighbors( n1, m, n2))
		{
			if ( neighbor.getId().startsWith( Keys.ANGLE) &&
				 ( int) neighbor.getEdgeBetween( m).getAttribute( "typ") == 2)
				return neighbor;
		}

		return null;
	}
	
	// given a set of points, returns the angle nodes shared by these
	public static ArrayList<Node> getAnglesBetween ( Node n1, Node n2)
	{
		ArrayList<Node> angles = new ArrayList<Node>();
		
		for ( Node n : getCommonNeighbors( n1, n2))
		{
			// iterating all common nodes, add to the list if it is an angle node
			if ( n.getId().startsWith( GraphHelpers.Keys.ANGLE))
			{
				angles.add( n);
			}
		}
		
		return angles;
	}

	// given two point of a ray, returns the angle nodes which is placed on it
	// rayStart is the point where the ray originates
	public static ArrayList<Node> getAnglesOnRay ( Node rayStart, Node rayEnd)
	{
		ArrayList<Node> angles = new ArrayList<Node>();

		for ( Node n : getCommonNeighbors( rayStart, rayEnd))
		{
			// iterating all common nodes, add to the list if it is an angle node
			if ( n.getId().startsWith( GraphHelpers.Keys.ANGLE) && ( (int) n.getEdgeBetween( rayStart).getAttribute( "typ")) == 2)
			{
				angles.add( n);
			}
		}

		return angles;
	}
	
	// returns a list of nodes which are common neighbors of nodes
	public static ArrayList<Node> getCommonNeighborsOfType ( String type, Node... nodes)
	{
		ArrayList<Node> commonNeighbors;
		Iterator<Node> commonNeighborIterator;

		if ( nodes.length == 0)
			return new ArrayList<Node>();
		else if ( nodes.length == 1)
			return  getNeighborsOfType ( type, nodes[0]);
		else if ( nodes.length == 2)
			return getCommonNeighborsOfType ( type, nodes[0], nodes[1]);
		
		commonNeighbors = getCommonNeighborsOfType ( type, nodes[0], nodes[1]);

		// loop while finding the effective intersection of the common neighbors
		for ( int i = 2; i < nodes.length; i++)
		{
			commonNeighborIterator = commonNeighbors.iterator();

			while ( commonNeighborIterator.hasNext())
			{
				// if the node does not have an edge towards this, remove it from the
				// list of common nodes
				if ( !nodes[i].hasEdgeBetween( commonNeighborIterator.next()))
				{
					commonNeighborIterator.remove();
				}
			}
		}

		return commonNeighbors;
	}

	// returns the shared neighbors between two nodes that
	// have the specified type
	public static ArrayList<Node> getCommonNeighborsOfType ( String type, Node n1, Node n2)
	{
		ArrayList<Node> result = new ArrayList<Node>();

		for ( Node n : getCommonNeighbors( n1, n2))
		{
			// iterating all common nodes, add to the list if it is an angle node
			if ( n.getId().startsWith( type))
			{
				result.add( n);
			}
		}

		return result;
	}
	
	// given a set of points, returns the area node produced by these
	public static Node getAreaBetween ( Node... points)
	{
		if ( points.length == 0 || points.length == 1)
			return null;

		// get the nodes which are in common
		ArrayList<Node> neighbors = getCommonNeighbors( points);
		
		for ( Node n : neighbors)
		{
			// if this common node is one with an area property and has the appropriate number of
			// nodes, return this node
			if ( n.getId().startsWith( Keys.AREA) && n.getDegree() == points.length)
				return n;
		}
		
		// no common area node
		return null;
	}
	
	public static ArrayList<Node> getAngleConstraintsBetween ( Node n1, Node n2, Node n3)
	{
		ArrayList<Node> angleNodes = new ArrayList<Node>();
		
		for ( Node cn : getCommonNeighbors( n1, n2, n3))
		{
			if ( cn.getId().startsWith( Keys.ANGLE))
				angleNodes.add( cn);
		}
		
		return angleNodes;
	}

	// clears the "visited" markers of a node
	public static void setNodeUnvisited ( Node n)
	{
		Iterator<String> keys = n.getAttributeKeyIterator();
		
		while ( keys.hasNext())
		{
			String key = keys.next();
			if ( key.startsWith( "%VIS"))
			{
				keys.remove();
			}
		}
	}
	
}
