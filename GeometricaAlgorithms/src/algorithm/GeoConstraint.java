package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.util.Arrays;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

// TODO
// TODO
// TODO
// TODO		// add checks for conflicting graphs nodes
// TODO
// TODO

// represents a geometric constraint, a "rule" on the system
public abstract class GeoConstraint {
	public abstract double calculateError();
	public abstract int getType();
	public abstract boolean involvesPoint ( GeoPoint p);
	public abstract boolean involvesPoint ( String p);
	public abstract GeoPoint[] getPoints ( );
	// for use by the graph solver:
	public abstract Node generateGraphNode ( Graph g);

	public static final int C_P2P_DISTANCE = 0;
	public static final int C_INTERIOR_ANGLE = 1;
	public static final int C_EQUAL_LENGTH = 2;
	public static final int C_POINT_ON_LINE = 3;
	public static final int C_EQUAL_ANGLE = 4;
	public static final int C_PARALLEL_LINE = 5;
	public static final int C_POINT_TO_LINE_DISTANCE = 6;
	public static final int C_POINT_ON_LINE_SEGMENT = 7;
	public static final int C_TRIANGULAR_AREA = 8;
	public static final int C_REGULAR_POLYGON = 9;
	
	// constraint for the distance between two points
	public static class CPointToPointDistance extends GeoConstraint {
		GeoPoint p1;
		GeoPoint p2;
		double distance;

		public CPointToPointDistance ( GeoPoint p1, GeoPoint p2, double distance)
		{
			this.p1 = p1;
			this.p2 = p2;
			this.distance = distance;
		}

		@Override
		public double calculateError() {
			double x = Math.hypot( (p1.getX() - p2.getX()), (p1.getY() - p2.getY())) - distance;
			return x * x;
		}

		@Override
		public int getType() {
			return C_P2P_DISTANCE;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p1 == p || p2 == p;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( p1.getName()) || p.equals( p2.getName());
		}

		@Override
		public GeoPoint[] getPoints( ) {
			GeoPoint[] points = new GeoPoint[2];
			points[0] = p1;
			points[1] = p2;
			return points;
		}

		@Override
		public Node generateGraphNode( Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.LENGTH));
			n.addAttribute( "sol", new SolutionNode( "Given |" + p1.getName() + "-" + p2.getName() + "| = " + MathHelpers.round( distance, 2)));
			n.addAttribute( "val", distance);
			
			// create the edges of this length node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p1.getName(), g));
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p2.getName(), g));
			return n;
		}	
	}

	public static class CInteriorAngle extends GeoConstraint {
		GeoPoint p1;
		GeoPoint p2; // corner
		GeoPoint p3;
		double angle;

		public CInteriorAngle ( GeoPoint p1, GeoPoint p2, GeoPoint p3, double angle)
		{
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
			this.angle = angle;
		}

		@Override
		public double calculateError() {
			double dx = p1.getX() - p2.getX();
			double dy = p1.getY() - p2.getY();

			double dx2 = p3.getX() - p2.getX();
			double dy2 = p3.getY() - p2.getY();

			double hyp1 = Math.hypot( dx, dy);
			double hyp2 = Math.hypot( dx2, dy2);

			dx /= hyp1;
			dy /= hyp1;
			dx2 /= hyp2;
			dy2 /= hyp2;

			double temp = dx * dx2 + dy * dy2;
			double temp2 = Math.cos( Math.PI - angle);

			return ( temp + temp2) * ( temp + temp2);
		}

		@Override
		public int getType() {
			return C_INTERIOR_ANGLE;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == p1 || p == p2 || p == p3;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( p1.getName()) || p.equals( p2.getName()) || p.equals( p3.getName());
		}

		@Override
		public GeoPoint[] getPoints( ) {
			return new GeoPoint[] { p1, p2, p3};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.ANGLE));
			n.addAttribute( "sol", new SolutionNode( "Given \u2220" + p1.getName() + "-" + p2.getName() + "-" 
							+ p3.getName() + " = " + MathHelpers.round( MathHelpers.toDegrees( angle), 2)));
			n.addAttribute( "val", angle);
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p1.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p2.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p3.getName(), g)).addAttribute( "typ", 3);
			return n;
		}
	}
	
	public static class CEqualLength extends GeoConstraint
	{
		// the points of the first line segment
		GeoPoint l1p1;
		GeoPoint l1p2;
		// the points of the second line segment
		GeoPoint l2p1;
		GeoPoint l2p2;
		
		//construct from the line segments to be equal
		public CEqualLength( GeoPoint l1p1,
							 GeoPoint l1p2,
							 GeoPoint l2p1,
							 GeoPoint l2p2)
		{
			this.l1p1 = l1p1;
			this.l1p2 = l1p2;
			this.l2p1 = l2p1;
			this.l2p2 = l2p2;
		}
		
		@Override
		public double calculateError() {
			double temp = Math.hypot( l1p2.getX() - l1p1.getX() , l1p2.getY() - l1p1.getY()) - 
						  Math.hypot( l2p2.getX() - l2p1.getX() , l2p2.getY() - l2p1.getY());
			return temp * temp;
		}

		@Override
		public int getType() {
			return C_EQUAL_LENGTH;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == l1p1 || p == l1p2 || p == l2p2 || p == l2p1;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( l1p1.getName()) || p.equals( l1p2.getName()) 
				|| p.equals( l2p1.getName()) || p.equals( l2p2.getName()) ;
		}

		@Override
		public GeoPoint[] getPoints( ) {
			return new GeoPoint[] { l1p1, l2p2, l1p2, l2p1 };
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.EQUAL_LENGTH));
			n.addAttribute( "sol", new SolutionNode( "Given |" + l1p1.getName() + "-" + l1p2.getName() + "| = |" + 
													 l2p1.getName() + "-" + l2p2.getName() + "|"));
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l1p1.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l1p2.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l2p1.getName(), g)).addAttribute( "typ", 3);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l2p2.getName(), g)).addAttribute( "typ", 4);
			return n;
		}
	}
	
	public static class CPointOnLine extends GeoConstraint
	{
		GeoPoint p;
		// points of the line
		GeoPoint lp1;
		GeoPoint lp2;
		
		public CPointOnLine ( GeoPoint A, GeoPoint lp1, GeoPoint lp2)
		{
			this.p = A;
			this.lp1 = lp1;
			this.lp2 = lp2;
		}
		
		@Override
		public double calculateError() {
			double dx, dy;
			double m, n;
			double Ey, Ex;
			
			dx = lp1.getX() - lp2.getX();
			dy = lp1.getY() - lp2.getY();
			
			m = dy / dx;
			n = dx / dy;
			
			if ( m <= 1 && m >= -1)
			{
				// calculate the expected y point given the x coordinate of the point
				Ey = lp1.getY() + m * ( p.getX() - lp1.getX());
				return ( Ey - p.getY()) * ( Ey - p.getY());
			}
			else
			{
				// calculate the expected x point given the y coordinate of the point
				Ex = lp1.getX() + n * ( p.getY() - lp1.getY());
				return ( Ex - p.getX()) * ( Ex - p.getX());
			}
		}

		@Override
		public int getType() {
			return C_POINT_ON_LINE;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == this.p || p == lp1 || p == lp2;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( this.p.getName()) || p.equals( this.lp1.getName()) 
				|| p.equals( this.lp2.getName());
		}

		@Override
		public GeoPoint[] getPoints( ) {
			return new GeoPoint[] { this.p, this.lp1, this.lp2};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.COLLINEAR));
			n.addAttribute( "sol", new SolutionNode( "Given " + p.getName() + " \u2208 " + lp1.getName() + " - " + 
													 lp2.getName()));
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( lp1.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( lp2.getName(), g)).addAttribute( "typ", 3);
			return n;
		}
	}

	public static class CEqualAngle extends GeoConstraint {
		GeoPoint a1, a2, a3; // first angle
		GeoPoint b1, b2, b3; // second angle
		
		public CEqualAngle ( GeoPoint a1, GeoPoint a2, GeoPoint a3,
							 GeoPoint b1, GeoPoint b2, GeoPoint b3)
		{
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			
			this.b1 = b1;
			this.b2 = b2;
			this.b3 = b3;
		}
		
		@Override
		public double calculateError() {
			// angle between the second pair:
			double cos_angle1;
			cos_angle1  = ( b1.getX() - b2.getX()) * ( b3.getX() - b2.getX());
			cos_angle1 += ( b1.getY() - b2.getY()) * ( b3.getY() - b2.getY());
			cos_angle1 /= Math.hypot( b1.getX() - b2.getX(), b1.getY() - b2.getY()) * 
						  Math.hypot( b3.getX() - b2.getX(), b3.getY() - b2.getY());
					
			double dx = a1.getX() - a2.getX();
			double dy = a1.getY() - a2.getY();
			double dx2 = a3.getX() - a2.getX();
			double dy2 = a3.getY() - a2.getY();
			
			double hyp1 = Math.hypot( dx, dy);
			double hyp2 = Math.hypot( dx2, dy2);
			
			dx /= hyp1;
			dy /= hyp1;
			dx2 /= hyp2;
			dy2 /= hyp2;
			
			double temp = dx * dx2 + dy * dy2;
			double temp2 = -cos_angle1;
			
			return ( temp + temp2) * ( temp + temp2);
		}

		@Override
		public int getType() {
			return C_EQUAL_ANGLE;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return a1 == p || a2 == p || a3 == p ||
					b1 == p || b2 == p || b3 == p;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( a1.getName()) || p.equals( a2.getName()) || p.equals( a3.getName())
					|| p.equals( b3.getName()) || p.equals( b2.getName()) || p.equals( b1.getName());
		}

		@Override
		public GeoPoint[] getPoints() {
			return new GeoPoint[] { a1, a2, a3, b1, b2, b3};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.EQUAL_ANGLE));
			n.addAttribute( "sol", new SolutionNode( "Given \u2220" + a1.getName() + "-" + a2.getName() + "-" + a3.getName() + " = " +
													 "\u2220" + b1.getName() + "-" + b2.getName() + "-" + b3.getName()));
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( a1.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( a2.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( a3.getName(), g)).addAttribute( "typ", 3);
			
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( b1.getName(), g)).addAttribute( "typ", 4);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( b2.getName(), g)).addAttribute( "typ", 5);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( b3.getName(), g)).addAttribute( "typ", 6);
			
			return n;
		}
	}

	public static class CParallelLine extends GeoConstraint
	{
		// the points of the first line segment
		GeoPoint l1p1;
		GeoPoint l1p2;
		// the points of the second line segment
		GeoPoint l2p1;
		GeoPoint l2p2;
		
		//construct from the line segments to be equal
		public CParallelLine ( GeoPoint l1p1, GeoPoint l1p2,
							   GeoPoint l2p1, GeoPoint l2p2)
		{
			this.l1p1 = l1p1;
			this.l1p2 = l1p2;
			this.l2p1 = l2p1;
			this.l2p2 = l2p2;
		}
		
		@Override
		public double calculateError() {
			double dx  = l1p2.getX() - l1p1.getX();
			double dy  = l1p2.getY() - l1p1.getY();
			double dx2 = l2p2.getX() - l2p1.getX();
			double dy2 = l2p2.getY() - l2p1.getY();

			double hyp1 = Math.hypot( dx, dy);
			double hyp2 = Math.hypot( dx2, dy2);

			dx  = dx  / hyp1;
			dy  = dy  / hyp1;
			dx2 = dx2 / hyp2;
			dy2 = dy2 / hyp2;

			double temp = dx * dx2 + dy * dy2 - 1;
			
			return temp * temp;
		}

		@Override
		public int getType() {
			return C_PARALLEL_LINE;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == l1p1 || p == l2p2 || p == l1p2 || p == l2p1;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( l1p1.getName()) || p.equals( l1p2.getName()) || p.equals( l2p1.getName()) || p.equals( l2p2.getName());
		}

		@Override
		public GeoPoint[] getPoints() {
			return new GeoPoint[] { l1p1, l1p2, l2p2, l2p1};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId(GraphHelpers.Keys.PARALLEL));
			n.addAttribute( "sol", new SolutionNode( "Given " + l1p1.getName() + "-" + l1p2.getName() + " \u2225 " +
													 l2p1.getName() + "-" + l2p2.getName()));
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l1p1.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l1p2.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l2p1.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( l2p2.getName(), g)).addAttribute( "typ", 2);
			return n;
		}
	}

	public static class CPointToLineDistance extends GeoConstraint
	{
		GeoPoint pointA;
		GeoPoint lp1;
		GeoPoint lp2;
		
		double distance;
		
		public CPointToLineDistance ( GeoPoint pointA, GeoPoint lp1, GeoPoint lp2, double distance)
		{
			this.pointA = pointA;
			this.lp1 = lp1;
			this.lp2 = lp2;
			
			this.distance = distance;
		}
		
		@Override
		public double calculateError() {
			double dx = lp2.getX() - lp1.getX();
			double dy = lp2.getY() - lp1.getY();
			
			double Xint,Yint;
			double t = -( lp1.getX() * dx - pointA.getX() * dx + lp1.getY() * dy - pointA.getY() * dy) / ( dx * dx + dy * dy);
			Xint = lp1.getX() + dx * t;
			Yint = lp1.getY() + dy * t;
			double temp = Math.hypot( ( pointA.getX() - Xint), ( pointA.getY() - Yint)) - distance;
			
			return temp * temp / 10;
		}

		@Override
		public int getType() {
			return C_POINT_TO_LINE_DISTANCE;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == pointA || p == lp1 || p == lp2;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( pointA.getName()) || p.equals( lp1.getName()) || p.equals( lp2.getName());
		}

		@Override
		public GeoPoint[] getPoints() {
			return new GeoPoint[] { pointA, lp1, lp2};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.POINT_TO_LINE_DISTANCE));
			n.addAttribute( "sol", new SolutionNode( "Given that distance between " + pointA.getName() + " and the line" + 
													 lp1.getName() + "-" + lp2.getName() + " is " + MathHelpers.round( distance, 2)));
			n.addAttribute( "val", distance);
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( pointA.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( lp2.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( lp1.getName(), g)).addAttribute( "typ", 2);
			return n;
		}
	}

	public static class CPointOnLineSegment extends GeoConstraint {
		// points of the line
		GeoPoint lp1, lp2;
		// the point to be on the line
		GeoPoint p1;
		
		public CPointOnLineSegment ( GeoPoint p1, GeoPoint lp1, GeoPoint lp2)
		{
			this.lp1 = lp1;
			this.lp2 = lp2;
			this.p1  = p1;
		}

		@Override
		public double calculateError() {
			
			// TODO improve the functioning of this part
			
			// slopes of the line and its negative reciprocal
			double m1, m2;
			// the values of the line function
			double res1, res2;
			
			// the coordinates of the point and line segment
			double line1X, line1Y;
			double line2X, line2Y;
			double pointX, pointY;
			
			pointX = p1.getX();
			pointY = p1.getY();
			line1X = lp1.getX();
			line1Y = lp1.getY();
			line2X = lp2.getX();
			line2Y = lp2.getY();
			
			// calculate slopes
			m1 = ( line1Y - line2Y) / ( line1X - line2X);
			m2 = -1 / m1;
			
			// first, find which side the point is on
			// calculate the results for the perpendicular functions
			res1 = pointY - m2 * ( pointX - line1X) - line1Y;
			res2 = pointY - m2 * ( pointX - line2X) - line2Y;
			
			// decide where the point is:
			if ( res1 * res2 > 0) // outside the perpendiculars
			{
				// compute distance to both points
				double dist1, dist2;
				
				dist1 = ( pointX - line1X) * ( pointX - line1X) + ( pointY - line1Y) * ( pointY - line1Y);
				dist2 = ( pointX - line2X) * ( pointX - line2X) + ( pointY - line2Y) * ( pointY - line2Y);
				
				// return the distance to the closest point
				return Math.min( dist1, dist2) / 10;
			}
			else // between the perpendiculars
			{
				// return the distance to the infinite line
				return ( m1 * ( pointX - line1X) + line1Y - pointY) * ( m1 * ( pointX - line1X) + line1Y - pointY)
						 / ( ( m1 * m1 + 1) * 10);
			}
		}

		@Override
		public int getType() {
			return C_POINT_ON_LINE_SEGMENT;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == p1 || p == lp1 || p == lp2;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( p1) || p.equals( lp1) || p.equals( lp2);
		}

		@Override
		public GeoPoint[] getPoints() {
			return new GeoPoint[] { p1, lp1, lp2};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.COLLINEAR));
			n.addAttribute( "sol", new SolutionNode( "Given " + p1.getName() + " \u2208 " + lp1.getName() + " - " + 
													 lp2.getName()));
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p1.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( lp1.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( lp2.getName(), g)).addAttribute( "typ", 3);
			return n;
		}
	}

	public static class CTriangularArea extends GeoConstraint {
		// three points of the triangle
		GeoPoint p1, p2, p3;
		double expectedArea;

		public CTriangularArea ( GeoPoint p1, GeoPoint p2, GeoPoint p3, double area)
		{
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
			
			this.expectedArea = area;
		}
		
		@Override
		public double calculateError() {
			
			// TODO improve the functioning of this part
			// TODO ideally use the distance to line code
			
			// the current value and expectation value of the height
			double h;
			double hExpected;
			// slope
			double m1;
			
			m1 = ( p2.getY() - p1.getY()) / ( p2.getX() - p1.getX());
			h = ( m1 * ( p3.getX() - p2.getX()) + p2.getY() - p3.getY()) * ( m1 * ( p3.getX() - p2.getX()) + p2.getY() - p3.getY())
					 / ( m1 * m1 + 1);
			h = Math.sqrt( h);
			
			hExpected = 2 * expectedArea / Math.hypot( p2.getY() - p1.getY(), p2.getX() - p1.getX());
			
			return ( h - hExpected) * ( h - hExpected) / 10;
		}

		@Override
		public int getType() {
			return C_TRIANGULAR_AREA;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			return p == p1 || p == p2 || p == p3;
		}

		@Override
		public boolean involvesPoint(String p) {
			return p.equals( p1.getName()) || p.equals( p2.getName()) || p.equals( p3.getName());
		}

		@Override
		public GeoPoint[] getPoints() {
			return new GeoPoint[] { p1, p2, p3};
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node n = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.AREA));
			n.addAttribute( "sol", new SolutionNode( "Given Area( " + p1.getName() + "-" + p2.getName() + "-" + 
													 p3.getName() + " = " + MathHelpers.round( expectedArea, 2)));
			n.addAttribute( "val", expectedArea);
			
			// create the edges of this angle node
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p1.getName(), g)).addAttribute( "typ", 1);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p2.getName(), g)).addAttribute( "typ", 2);
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), n, GraphHelpers.findPointNode( p3.getName(), g)).addAttribute( "typ", 3);
			return n;
		}
	}

	public static class CRegularPolygon extends GeoConstraint {
		// each vertice of the regular polygon
		GeoPoint[] vertices;

		public CRegularPolygon ( GeoPoint... vertices)
		{
			this.vertices = vertices;
		}
		
		@Override
		public double calculateError() {
			// does not have and error function, must be parameterized in terms of its position
			return 0;
		}

		@Override
		public int getType() {
			return C_REGULAR_POLYGON;
		}

		@Override
		public boolean involvesPoint(GeoPoint p) {
			for ( GeoPoint v : vertices)
				if ( v == p)
					return true;
			
			return false;
		}

		@Override
		public boolean involvesPoint(String p) {
			for ( GeoPoint v : vertices)
				if ( v.getName().equals( p))
					return true;
			
			return false;
		}

		@Override
		public GeoPoint[] getPoints() {
			GeoPoint[] points;
			
			// return a copy of the vertices array:			
			points = new GeoPoint[ vertices.length];
			
			for ( int i = 0; i < vertices.length; i++)
				points[i] = vertices[i];
			
			return points;
		}

		@Override
		public Node generateGraphNode(Graph g) {
			Node regularPolygonNode;
			String solutionExplanation;
			
			regularPolygonNode = g.addNode( GraphHelpers.Keys.generateConstraintNodeId( GraphHelpers.Keys.REGULAR_POLYGON));
			
			// add the first corner
			solutionExplanation = "Given that " + vertices[0].getName();
			g.addEdge( GraphHelpers.Keys.generateEdgeId(), regularPolygonNode, GraphHelpers.findPointNode( vertices[0].getName(), g)).addAttribute( "typ", 1);
			
			// create and add the new "regular polygon" constraint node
			for ( int i = 1; i < vertices.length; i++)
			{
				solutionExplanation += "-" + vertices[i].getName();
				g.addEdge( GraphHelpers.Keys.generateEdgeId(), regularPolygonNode, GraphHelpers.findPointNode( vertices[i].getName(), g)).addAttribute( "typ", i + 1);
			}
			
			solutionExplanation += " is a regular polygon.";
			
			regularPolygonNode.addAttribute( "sol", new SolutionNode( solutionExplanation));
			
			return regularPolygonNode;
		}
	}
}
