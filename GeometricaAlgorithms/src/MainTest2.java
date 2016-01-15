import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import algorithm.GraphHelpers;
import algorithm.SAreaSineFormula;
import algorithm.SCosineTheorem;
import algorithm.SInverseCosineTheorem;
import algorithm.SolutionNode;
import algorithm.Vector2;

// a temporary class to test the solver algorithm
public class MainTest2 {

	public static void main(String[] args) {

		// test vector functions
		Vector2 v1 = new Vector2 ( 1, 0);
		Vector2 v2 = new Vector2 ( 5, 4);

		System.out.println( v1.rotate( Math.PI / 6));
		System.out.println( v1.rotateAroundPoint( Math.PI / 6, v2));
		System.out.println( v1.dotProduct( v2));
		System.out.println( v2.crossProduct( v1));

		problem2();
		return;
	}

	// problem for testing topological 
	public static void problem1 ( )
	{
		// construct a graph to represent a simple pythagorean theorem question
		// involving two consequent applications of the theorem
		Graph testGraph = new SingleGraph( "test2");

		testGraph.addNode( "!A");
		testGraph.addNode( "!B");
		testGraph.addNode( "!C");
		testGraph.addNode( "!D");

		String len1 = GraphHelpers.Keys.LENGTH + genNodeNo();
		String len2 = GraphHelpers.Keys.LENGTH + genNodeNo();
		String len3 = GraphHelpers.Keys.LENGTH + genNodeNo();
		String ang1 = GraphHelpers.Keys.ANGLE  + genNodeNo();
		String ang2 = GraphHelpers.Keys.ANGLE  + genNodeNo();

		testGraph.addNode( len1);
		testGraph.addNode( len2);
		testGraph.addNode( len3);
		testGraph.addNode( ang1);
		testGraph.addNode( ang2);

		/// LENGTHS
		testGraph.getNode( len1).addAttribute( "val", 3.0);
		testGraph.getNode( len1).addAttribute( "sol", new SolutionNode( "Given |BD| = 3"));
		testGraph.getNode( len2).addAttribute( "val", 4.0);
		testGraph.getNode( len2).addAttribute( "sol", new SolutionNode( "Given |AD| = 4"));
		testGraph.getNode( len3).addAttribute( "val", 12.0);
		testGraph.getNode( len3).addAttribute( "sol", new SolutionNode( "Given |BC| = 12"));
		testGraph.getNode( ang1).addAttribute( "val", Math.PI / 2);
		testGraph.getNode( ang1).addAttribute( "sol", new SolutionNode( "Given m(ADB)=90"));
		testGraph.getNode( ang2).addAttribute( "val", Math.PI / 2);
		testGraph.getNode( ang2).addAttribute( "sol", new SolutionNode( "Given m(ABC)=90"));

		testGraph.addEdge( genEdgeId(), "!D", len1);
		testGraph.addEdge( genEdgeId(), len1, "!B");
		testGraph.addEdge( genEdgeId(), "!A", len2);
		testGraph.addEdge( genEdgeId(), "!D", len2);
		testGraph.addEdge( genEdgeId(), "!B", len3);
		testGraph.addEdge( genEdgeId(), "!C", len3);


		/// ANGLE 1
		String angEdge11 = genEdgeId();
		String angEdge12 = genEdgeId();
		String angEdge13 = genEdgeId();

		testGraph.addEdge( angEdge11, "!A", ang1);
		testGraph.addEdge( angEdge12, "!D", ang1);
		testGraph.addEdge( angEdge13, "!B", ang1);

		testGraph.getEdge( angEdge11).setAttribute( "typ", 1);
		testGraph.getEdge( angEdge12).setAttribute( "typ", 2);
		testGraph.getEdge( angEdge13).setAttribute( "typ", 3);

		/// ANGLE 2
		String angEdge21 = genEdgeId();
		String angEdge22 = genEdgeId();
		String angEdge23 = genEdgeId();

		testGraph.addEdge( angEdge21, "!A", ang2);
		testGraph.addEdge( angEdge22, "!B", ang2);
		testGraph.addEdge( angEdge23, "!C", ang2);

		testGraph.getEdge( angEdge21).setAttribute( "typ", 2);
		testGraph.getEdge( angEdge22).setAttribute( "typ", 1);
		testGraph.getEdge( angEdge23).setAttribute( "typ", 3);
	}

	public static void problem2 ( )
	{
		// construct a graph to represent a simple pythagorean theorem question
		// involving two consequent applications of the theorem
		Graph testGraph = new SingleGraph( "test2");

		testGraph.addNode( "!A");
		testGraph.addNode( "!B");
		testGraph.addNode( "!C");
		testGraph.addNode( "!D");

		String len1 = GraphHelpers.Keys.LENGTH + genNodeNo();
		String len2 = GraphHelpers.Keys.LENGTH + genNodeNo();
		String len3 = GraphHelpers.Keys.LENGTH + genNodeNo();
		String ang1 = GraphHelpers.Keys.ANGLE  + genNodeNo();
		String ang2 = GraphHelpers.Keys.ANGLE  + genNodeNo();

		testGraph.addNode( len1);
		testGraph.addNode( len2);
		testGraph.addNode( len3);
		testGraph.addNode( ang1);
		testGraph.addNode( ang2);

		/// LENGTHS
		testGraph.getNode( len1).addAttribute( "val", 3.0);
		testGraph.getNode( len1).addAttribute( "sol", new SolutionNode( "Given |BD| = 3"));
		testGraph.getNode( len2).addAttribute( "val", 4.0);
		testGraph.getNode( len2).addAttribute( "sol", new SolutionNode( "Given |AD| = 4"));
		testGraph.getNode( len3).addAttribute( "val", 12.0);
		testGraph.getNode( len3).addAttribute( "sol", new SolutionNode( "Given |BC| = 12"));
		testGraph.getNode( ang1).addAttribute( "val", Math.PI / 2);
		testGraph.getNode( ang1).addAttribute( "sol", new SolutionNode( "Given m(ADB)=90"));
		testGraph.getNode( ang2).addAttribute( "val", Math.PI / 2);
		testGraph.getNode( ang2).addAttribute( "sol", new SolutionNode( "Given m(ABC)=90"));

		testGraph.addEdge( genEdgeId(), "!D", len1);
		testGraph.addEdge( genEdgeId(), len1, "!B");
		testGraph.addEdge( genEdgeId(), "!A", len2);
		testGraph.addEdge( genEdgeId(), "!D", len2);
		testGraph.addEdge( genEdgeId(), "!B", len3);
		testGraph.addEdge( genEdgeId(), "!C", len3);


		/// ANGLE 1
		String angEdge11 = genEdgeId();
		String angEdge12 = genEdgeId();
		String angEdge13 = genEdgeId();

		testGraph.addEdge( angEdge11, "!A", ang1);
		testGraph.addEdge( angEdge12, "!D", ang1);
		testGraph.addEdge( angEdge13, "!B", ang1);

		testGraph.getEdge( angEdge11).setAttribute( "typ", 1);
		testGraph.getEdge( angEdge12).setAttribute( "typ", 2);
		testGraph.getEdge( angEdge13).setAttribute( "typ", 3);

		/// ANGLE 2
		String angEdge21 = genEdgeId();
		String angEdge22 = genEdgeId();
		String angEdge23 = genEdgeId();

		testGraph.addEdge( angEdge21, "!A", ang2);
		testGraph.addEdge( angEdge22, "!B", ang2);
		testGraph.addEdge( angEdge23, "!C", ang2);

		testGraph.getEdge( angEdge21).setAttribute( "typ", 2);
		testGraph.getEdge( angEdge22).setAttribute( "typ", 1);
		testGraph.getEdge( angEdge23).setAttribute( "typ", 3);

		long startTime = System.nanoTime() / 1000;

		processGraph( testGraph);

		long endTime = System.nanoTime() / 1000;

		System.out.println( "Found in " + ( endTime - startTime) + " microsec.");

		// visualization
		for (Node node : testGraph) {
			node.addAttribute("ui.label", node.getId() + " " + ( node.hasAttribute( "val") ? node.getAttribute("val") : ""));
		}

		for (Edge e : testGraph.getEachEdge()) {
			if ( e.hasAttribute( "typ"))
				e.addAttribute( "ui.label", e.getAttribute( "typ"));		    	
		}


		// test graph utilities
		System.out.println( GraphHelpers.getNeighbors( testGraph.getNode( "!A")) );

		testGraph.addAttribute("ui.quality");
		testGraph.addAttribute("ui.antialias");

		testGraph.display();

		// output the solution
		System.out.println( GraphHelpers.getLengthBetween( testGraph.getNode( "!A"), testGraph.getNode( "!C")).getAttribute( "sol") );

		JOptionPane.showMessageDialog( null, GraphHelpers.getLengthBetween( testGraph.getNode( "!A"), testGraph.getNode( "!C")).getAttribute( "sol"));
		JOptionPane.showMessageDialog( null, GraphHelpers.getAngleBetween( testGraph.getNode( "!C"), testGraph.getNode( "!B"), testGraph.getNode( "!A")).getAttribute( "sol"));
	}


	/////// the algorithm
	static String processGraph( Graph g)
	{
		// initialize the solvers
		SCosineTheorem solver3;
		SInverseCosineTheorem solver4;
		SAreaSineFormula solver5;

		solver3 = new SCosineTheorem( g);
		solver4 = new SInverseCosineTheorem( g);
		solver5 = new SAreaSineFormula( g);

		ArrayList<Node> toBeProcessed = new ArrayList<Node>();
		ArrayList<Node> laterProcessed =  new ArrayList<Node>();

		// populate the starting points
		for ( Node n : g)
		{
			if ( n.getId().startsWith( "#"))
				toBeProcessed.add( n);
		}

		int i = 0;
		// loop until we find a solution
		while ( !solutionFound( g) && i < 100 && toBeProcessed.size() > 0)
		{
			// call strategy designers
			// TODO

			for ( Node n : toBeProcessed)
			{
				/*solver.processNode( g, n, laterProcessed);
				solver2.processNode( g, n, laterProcessed);*/

				solver3.processNode( n, laterProcessed);
				solver4.processNode( n, laterProcessed);
				solver5.processNode( n, laterProcessed);
			}

			toBeProcessed = new ArrayList<Node>( laterProcessed);

			laterProcessed.clear();

			i++;
		}

		return "";
	}

	// temporary test for success
	static boolean solutionFound ( Graph g)
	{
		if ( GraphHelpers.getLengthBetween( g.getNode( "!A"), g.getNode( "!C")) != null)
			return true;
		return false;
	}

	// some helpers to get started
	static int nextEdgeNo = 0;
	public static String genEdgeId ()
	{
		return "E" + ( nextEdgeNo++) + ":" + 0;
	}

	static int nextNodeNo = 0;
	public static String genNodeId ( String data)
	{
		return "N" + genNodeNo() + ":" + data;
	}

	public static int genNodeNo ( )
	{
		return ( nextNodeNo++);
	}
}
