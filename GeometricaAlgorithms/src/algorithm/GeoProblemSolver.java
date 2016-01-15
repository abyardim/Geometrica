package algorithm;
import java.util.ArrayList;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

// class responsible of managing the problem solution process
public class GeoProblemSolver {
	private GeoConstruct construct;
	
	// the graph representing the relations in the problem
	private Graph problemGraph;
	
	// the root nodes of the solutions for each query
	private ArrayList<SolutionNode> solutions;
	
	// the queries to solve
	private ArrayList<Query> queries;
	
	public GeoProblemSolver ( GeoConstruct construct)
	{
		this.construct = construct;
		
		solutions = new ArrayList<SolutionNode>();
		queries = new ArrayList<Query>();

		problemGraph = new MultiGraph( "Problem Graph");
	}
	
	// return codes for solve:
	public static final int SOLVE_SUCCESSFUL = 1;
	public static final int SOLVE_UNSUCCESSFUL = 2;
	public static final int SOLVE_PARTIALLY_SUCCESSFUL = 3;
	public static final int SYSTEM_INCONSISTENT = 4;
	public static final int NO_QUERIES = 5;
	
	public int solve ( )
	{
		// the "searcher" objects that search steps of the solution
		ArrayList<GeoSearcher> searchers;
		// two arrays which alternate as the list of nodes to search
		// to keep track of points to search
		ArrayList<Node> toBeProcessed = new ArrayList<Node>();
		ArrayList<Node> processedLater =  new ArrayList<Node>();

		// algorithm initialization // TODO
		//this.constructProblem();
		
		// if there are no queries, return
		if ( this.queries.isEmpty())
			return NO_QUERIES;

		// initialize solver objects
		searchers = new ArrayList<GeoSearcher>();
		searchers.add( new SRegularPolygon( problemGraph));
		searchers.add( new SAngleAddition( problemGraph));
		searchers.add( new SAreaAddition( problemGraph));
		searchers.add( new SLengthAddition( problemGraph));
		searchers.add( new SAngleExtender( problemGraph));
		searchers.add( new SCosineTheorem( problemGraph));
		searchers.add( new SAreaSineFormula( problemGraph));
		searchers.add( new SInverseCosineTheorem( problemGraph));
		
		// populate the starting points
		for ( Node n : problemGraph)
		{
			toBeProcessed.add( n);
		}
		
		// signal to stop iterations
		boolean endProcess = false;
		for ( int i = 0; i < 100 && !endProcess; i++) // max. 100 iterations
		{
			// if we found the answer or have no more nodes to process left, exit
			if ( toBeProcessed.isEmpty() || isCompleted() == SOLVE_SUCCESSFUL)
			{
				endProcess = true;
			}
			
			// process each node marked to be searched
			for ( Node n : toBeProcessed)
			{
				for ( GeoSearcher searcher : searchers)
				{
					searcher.processNode( n, processedLater);
				}
			}
			
			// check for inconsistencies
			if ( !isConsistent( processedLater))
				return SYSTEM_INCONSISTENT;
			
			// swap the lists
			ArrayList<Node> swap;
			swap = toBeProcessed;
			toBeProcessed = processedLater;
			processedLater = swap;
			processedLater.clear();
		}
		
		createSolutions();
		
		return isCompleted();
	}
	
	// for debug only, shows the graph on the screen
	public void updateVisualizationData ( )
	{
		// visualization
		for (Node node : problemGraph) {
			node.addAttribute("ui.label", node.getId() + " " + ( node.hasAttribute( "val") ? node.getAttribute("val") : ""));
		}

		for (Edge e : problemGraph.getEachEdge()) {
			if ( e.hasAttribute( "typ"))
				e.addAttribute( "ui.label", e.getAttribute( "typ"));		    	
		}
	}
	
	// for debug only, shows the graph on the screen
	public Viewer showGraph ( )
	{
		Viewer v;
		
		updateVisualizationData();
		
		v = problemGraph.display();
		v.setCloseFramePolicy( CloseFramePolicy.CLOSE_VIEWER);
		return v;
	}
	
	// getters
	
	public ArrayList<SolutionNode> getSolutions ( )
	{
		return solutions;
	}
	
	public int getStatus ( )
	{
		return isCompleted();
	}
	
	// check if everything is solved
	private int isCompleted ( )
	{
		boolean someNotCompleted;
		boolean someCompleted;
		
		someNotCompleted = false;
		someCompleted = false;
		
		for ( Query q : queries)
		{
			// if there is at least one completed query
			if ( q.isCompleted())
			{
				someCompleted = true;
			}
			// if there is at least one query not yet completed
			else
			{
				someNotCompleted = true;
			}
		}
		
		if ( someCompleted & !someNotCompleted)
			return SOLVE_SUCCESSFUL;
		else if ( !someCompleted)
			return SOLVE_UNSUCCESSFUL;
		else // some queries completed, some not
			return SOLVE_PARTIALLY_SUCCESSFUL;
	}
	
	// checks the consistency of the added nodes
	private boolean isConsistent ( ArrayList<Node> toBeChecked)
	{
		for ( Node n : toBeChecked)
		{
			// if n has a numerical value associated with it, it should
			// be valid number (not NaN)
			if ( n.hasAttribute( "val") && ((Double) n.getAttribute( "val")).isNaN())
				return false;
		}
		
		// if every value verified, the system is currently consistent
		return true;
	}
	
	// helper function to construct the initial state of the problem
	// with its graph and queries
	public void constructProblem ( ) // TODO change this to private
	{
		TopologyProvider topology = new TopologyProvider( construct);
		
		problemGraph.addAttribute( "top", topology);
		
		// first create the graph of this problem
		
		// create point nodes
		for ( GeoPoint p : construct.getPoints())
		{
			Node pointNode;
			
			pointNode = problemGraph.addNode( GraphHelpers.Keys.generatePointNodeId( p.getName()));
			
			if ( p.isFixed())
			{
				pointNode.addAttribute( "coor", new Vector2( p.getX(), p.getY()));
				pointNode.addAttribute( "sol", new SolutionNode ( "Given the cooradinates of " + p.getName() + "."));
			}
		}
		
		// create constraint nodes
		for ( GeoConstraint c : construct.getConstraints())
		{
			c.generateGraphNode( problemGraph);
		}
		
		// create the query objects
		
		for ( String queryStr : construct.getQueries())
		{
			if ( queryStr.startsWith( "are")) // an area query
			{
				String[] points = new String[StringHelpers.countWords( queryStr, "#") - 1];
				
				// look for each vertex of this area
				for ( int i = 0; i < points.length; i++)
				{
					points[i] = StringHelpers.getWord( queryStr, i + 2, "#");
				}
				
				queries.add( new AreaQuery( points));
			}
			else if ( queryStr.startsWith( "ang")) // an angle query
			{
				queries.add( new AngleQuery( StringHelpers.getWord( queryStr, 2, "#"),
											 StringHelpers.getWord( queryStr, 3, "#"),
											 StringHelpers.getWord( queryStr, 4, "#")));		
			}
			else if ( queryStr.startsWith( "len"))
			{
				queries.add( new LengthQuery( StringHelpers.getWord( queryStr, 2, "#"),
						 					  StringHelpers.getWord( queryStr, 3, "#")));
			}
		}
		
		System.out.println( "...");
		// TODO remove duplicates
	}

	private void createSolutions ( )
	{
		// the node to be added
		SolutionNode node;
		
		for ( Query query : queries)
		{
			node = query.getSolution();
			
			if ( node != null)
				solutions.add( node);
		}
	}
	
	////////////// internal query types
	
	// internal classes to distinguish
	private abstract class Query
	{
		// supposed to return true is the answer of
		// this query was found
		public abstract boolean isCompleted ( );
		
		// returns the solution node relating to this query if any exist
		public abstract SolutionNode getSolution ( );
	}
	
	private class AreaQuery extends Query
	{
		// the list of points to find the area between
		Node[] points;
		
		public AreaQuery ( String... points)
		{
			this.points = new Node[points.length];
			
			String[] pointert = points;
			
			for ( int i = 0; i < points.length; i++)
			{
				this.points[i] = GraphHelpers.findPointNode( points[i], problemGraph);
			}
		}
		
		public boolean isCompleted ( ) {
			return GraphHelpers.getAreaBetween( points) != null;
		}

		@Override
		public SolutionNode getSolution() {
			Node areaNode = GraphHelpers.getAreaBetween( points);

			if ( areaNode == null)
			{
				String explanation;
				explanation = "The area A( " + points[0].getId().substring( 1);

				for ( int i = 1; i < points.length; i++)
				{
					explanation += 	", " + points[i].getId().substring( 1);
				}

				explanation += ") could not be found, but numerically it is ";

				double area = 0; 
				int numPoints = points.length;
				int j = numPoints-1; 

				for ( int i=0; i < numPoints; i++)
				{ 
					area = area + ( construct.findPoint( points[j].getId().substring( 1)).getX() + construct.findPoint( points[i].getId().substring( 1)).getX()) 
							* ( construct.findPoint( points[j].getId().substring( 1)).getY() - construct.findPoint( points[i].getId().substring( 1)).getY()); 
					j = i;  //j is previous vertex to i
				}


				explanation += MathHelpers.round( area / 2, 2);

				return new SolutionNode( explanation);
			}

			return ( SolutionNode) areaNode.getAttribute( "sol");
		}
	}
	
	private class LengthQuery extends Query
	{
		Node point1, point2;
		
		public LengthQuery ( String pointId1, String pointId2)
		{
			this.point1 = GraphHelpers.findPointNode( pointId1, problemGraph);
			this.point2 = GraphHelpers.findPointNode( pointId2, problemGraph);
		}
		
		public boolean isCompleted ( ) {
			// true if such a length node exists
			return GraphHelpers.getLengthBetween( point1, point2) != null;
		}

		@Override
		public SolutionNode getSolution() {
			Node lengthNode = GraphHelpers.getLengthBetween( point1, point2);
			
			if ( lengthNode == null)
			{
				String explanation;
				
				explanation = "No solution found for |" + point1.getId().substring( 1) + "-" + point2.getId().substring( 1) + "| ";
				explanation += "but numerically the answer is " + MathHelpers.round( construct.findPoint( point1.getId().substring( 1)).getPositionVector()
														.distanceTo( construct.findPoint( point2.getId().substring( 1)).getPositionVector()), 2);
				return new SolutionNode( explanation);
			}
			return ( SolutionNode) lengthNode.getAttribute( "sol");
		}
	}
	
	private class AngleQuery extends Query
	{
		Node point1, point2, point3;
		
		public AngleQuery ( String pointId1, String pointId2, String pointId3)
		{
			this.point1 = GraphHelpers.findPointNode( pointId1, problemGraph);
			this.point2 = GraphHelpers.findPointNode( pointId2, problemGraph);
			this.point3 = GraphHelpers.findPointNode( pointId3, problemGraph);
		}
		
		public boolean isCompleted ( ) {
			// true if such an angle node exists
			return GraphHelpers.getAngleBetween( point1, point2, point3) != null;
		}

		@Override
		public SolutionNode getSolution() {
			Node angleNode =  GraphHelpers.getAngleBetween( point1, point2, point3);
			
			if ( angleNode == null)
			{
				String explanation;
				double angle;
				Vector2 v1, v2;
				
				v1 = construct.findPoint( point1.getId().substring( 1)).getPositionVector().substract( construct.findPoint( point2.getId().substring( 1)).getPositionVector());
				v2 = construct.findPoint( point3.getId().substring( 1)).getPositionVector().substract( construct.findPoint( point2.getId().substring( 1)).getPositionVector());
				
				angle = MathHelpers.toDegrees( v1.angleBetween( v2));
				
				explanation = "No solution found for \u2220" + point1.getId().substring( 1) + "-" + point2.getId().substring( 1) + "-" + point3.getId().substring(1) + " ";
				explanation += "but numerically the answer is " + MathHelpers.round( angle, 2);
				return new SolutionNode( explanation);
				
			}
				
			return ( SolutionNode) angleNode.getAttribute( "sol");
		}
	}
}
