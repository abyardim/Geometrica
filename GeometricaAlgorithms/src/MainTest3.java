import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import algorithm.GeoConstruct;
import algorithm.GeoPoint;
import algorithm.GeoRenderer;
import algorithm.TopologyProvider;


public class MainTest3 {

	public static void main(String[] args) {
		GeoPoint p1, p2, p3, p4, p5, po1, po2, pi, px, px2;
		
		p1 = new GeoPoint( "A", 74, 55, true);
		p2 = new GeoPoint( "B", 239, 76, true);
		p3 = new GeoPoint( "C", 328, 256, true);
		p4 = new GeoPoint( "D", 170, 177, true);
		p5 = new GeoPoint( "E", 40, 167, true);
		
		po1 = new GeoPoint( "P1", 229, 40, true);
		po2 = new GeoPoint( "P2", 366, 311, true);
		pi = new GeoPoint( "P3", 259, 109, true);
		px = new GeoPoint( "X", 137, 137, true);
		px2 = new GeoPoint( "X2", 282, 216, true);
		
		GeoConstruct cons = new GeoConstruct();
		cons.addPoint( p1);
		cons.addPoint( p2);
		cons.addPoint( p3);
		cons.addPoint( p4);
		cons.addPoint( p5);
		cons.addPoint( po1);
		cons.addPoint( po2);
		
		cons.addPoint( pi);
		cons.addPoint( px);
		cons.addPoint( px2);
		
		TopologyProvider top = new TopologyProvider( cons);
		
		long startTime = System.nanoTime() / 1000;
		
		top.isPointInPolygon( "X", "A", "B", "C", "D", "E");
		top.isPointInPolygon( "X2", "A", "B", "C", "D", "E");
		top.isPointInPolygon( "P1", "A", "B", "C", "D", "E");
		top.isPointInPolygon( "P2", "A", "B", "C", "D", "E");
		
		top.pointOnLineSegment( "P1", "B", "C");
		top.pointOnLineSegment( "P2", "B", "C");
		top.pointOnLineSegment( "P3", "B", "C");
		
		top.triangleInPolygon( "X", "D", "E", "A", "B", "C", "D", "E");
		top.triangleInPolygon( "P1", "D", "E", "A", "B", "C", "D", "E");
		top.triangleInPolygon( "P1", "A", "B", "A", "B", "C", "D", "E");
		
		long endTime = System.nanoTime() / 1000;
		
		System.out.println( top.isPointInPolygon( "X", "A", "B", "C", "D", "E"));
		System.out.println( top.isPointInPolygon( "X2", "A", "B", "C", "D", "E"));
		System.out.println( top.isPointInPolygon( "P1", "A", "B", "C", "D", "E"));
		System.out.println( top.isPointInPolygon( "P2", "A", "B", "C", "D", "E"));
		
		System.out.println( top.pointOnLineSegment( "P1", "B", "C"));
		System.out.println( top.pointOnLineSegment( "P2", "B", "C"));
		System.out.println( top.pointOnLineSegment( "P3", "B", "C"));
		
		System.out.println( "tripolygon: \n" + top.triangleInPolygon( "P1", "A", "B", "A", "B", "C", "D", "E"));
		System.out.println( top.triangleInPolygon( "X", "D", "E", "A", "B", "C", "D", "E"));
		System.out.println( top.triangleInPolygon( "P1", "D", "E", "A", "B", "C", "D", "E"));
		
		System.out.println( "processed in: " + ( endTime - startTime));
		
		
		
		GeoRenderer renderer = new GeoRenderer ( cons);
		
		JFrame frame = new JFrame( "render result");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		frame.setSize( 500, 500);
		frame.add( new RenderSurface( renderer));
		frame.setVisible( true);
		
		
		
	}

}
