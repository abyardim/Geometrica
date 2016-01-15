package algorithm;
// Created: 28.04.2015, Ali Batuhan Yardým

// a class that based on a geometric drawing, provides
// topological relations between a set of points
public class TopologyProvider {
	// constants for the return values of functions
	public static final int POINT_ON_SEGMENT = 1;
	public static final int POINT_OUTSIDE_1 = 2;
	public static final int POINT_OUTSIDE_2 = 3;
	
	private GeoConstruct construct;
	
	public TopologyProvider ( GeoConstruct construct)
	{
		this.construct = construct;
	}
	
	// checks the relative position of a point with a line segment
	// returns POINT_ON_SEGMENT if the point is in the inner area between
	// the line points, POINT_OUTSIDE_1 or POINT_OUTSIDE_2 if it is
	// outside, depending on which point ( lp1 or lp2) it is closer to
	public int pointOnLineSegment ( String pointName, String linePoint1Name, String linePoint2Name)
	{
		GeoPoint point;
		GeoPoint lp1, lp2;
		// the position vectors:
		Vector2 vPoint;
		Vector2 vLine1;
		Vector2 vLine2;
		
		double dotProduct;
		
		// get the point objects from the names
		point = construct.findPoint( pointName);
		lp1 = construct.findPoint( linePoint1Name);
		lp2 = construct.findPoint( linePoint2Name);
		
		vPoint = point.getPositionVector();
		vLine1 = lp1.getPositionVector();
		vLine2 = lp2.getPositionVector();
		
		dotProduct = vLine2.substract( vLine1).dotProduct( vPoint.substract( vLine1));
		
		if ( dotProduct < 0) // on the side of point 1
			return POINT_OUTSIDE_1;
		else if ( dotProduct > vLine2.substract( vLine1).lengthSquared()) // on the side of point 2
			return POINT_OUTSIDE_2;

		// else, it is in the line segment in between
		return POINT_ON_SEGMENT;
	}
	
	
	public static final int POLYGON1_INSIDE = 1;
	public static final int POLYGON2_INSIDE = 2;
	public static final int POLYGONS_OUTSIDE = 4;
	public static final int NO_RESULT = 3;
	// tests whether two polygons cross each other
	// possibly except a set of common points
	public int isPolygonInsidePolygon ( String common1, String common2, String[] polygon1, String[] polygon2)
	{
		int polygon1Inside = 0;
		
		for ( String p1 : polygon1)
		{
			if ( !p1.equals( common1) && !p1.equals( common2))
			{
				int result;
				
				if ( isPointInPolygon(p1, polygon2))
					result = 1;
				else
					result = 2;
				
				if ( polygon1Inside == 0)
					polygon1Inside = result;
				else
					if ( polygon1Inside != result)
						return NO_RESULT;
			}
		}
		
		int polygon2Inside = 0;
		
		for ( String p2 : polygon2)
		{
			if ( !p2.equals( common1) && !p2.equals( common2))
			{
				int result;
				
				if ( isPointInPolygon(p2, polygon1))
					result = 1;
				else
					result = 2;

				if ( polygon2Inside == 0)
					polygon2Inside = result;
				else
					if ( polygon2Inside != result)
						return NO_RESULT;
			}
		}
		
		if ( polygon1Inside == 1)
		{
			return POLYGON1_INSIDE;
		}
		else if ( polygon2Inside == 1)
			return POLYGON2_INSIDE;	
		else
		{
			// check if the two polygons are on the opposite sides of the common line
			int ccw1 = 0;
			
			for ( String n : polygon1)
			{
				if ( !n.equals( common1) && !n.equals( common2))
				{
					ccw1 = ccw( construct.findPoint( n).getPositionVector(), 
								construct.findPoint( common1).getPositionVector(),
								construct.findPoint( common2).getPositionVector());
					break;
				}
			}
			
			for ( String n : polygon2)
			{
				if ( !n.equals( common1) && !n.equals( common2))
				{
					if ( ccw1 == ccw( construct.findPoint( n).getPositionVector(), 
									  construct.findPoint( common1).getPositionVector(),
									  construct.findPoint( common2).getPositionVector()))
								return NO_RESULT;
				}
			}
			
			
			return POLYGONS_OUTSIDE;
		}
	}
	
	// returns true if a point is inside the region of an angle formed by three other points
	public boolean isPointInsideAngle ( String pointName, String anglePointName1, String angleCenterPointName, String anglePointName3)
	{
		// the position vectors of the points
		Vector2 vPoint;
		Vector2 vAngle1;
		Vector2 vAngle3;
		Vector2 vCenter;
		
		// the ray vectors starting from the center of the angle
		Vector2 vRay1;
		Vector2 vRay2;
		Vector2 vPointRay;
		
		// the components of the point in the new basis
		double a, b;
		
		vPoint = construct.findPoint( pointName).getPositionVector();
		vAngle1 = construct.findPoint( anglePointName1).getPositionVector();
		vAngle3 = construct.findPoint( anglePointName3).getPositionVector();
		vCenter = construct.findPoint( angleCenterPointName).getPositionVector();
		
		vRay1 = vAngle1.substract( vCenter);
		vRay2 = vAngle3.substract( vCenter);
		vPointRay = vPoint.substract( vCenter);
		
		// writer the point in the basis formed by the two ray vectors
		a = (vPointRay.getX() * vRay2.getY() - vRay2.getX() * vPointRay.getY()) / ( vRay1.getX() * vRay2.getY() - vRay1.getY() * vRay2.getX());
		b = (vPointRay.getX() * vRay1.getY() - vRay1.getX() * vPointRay.getY()) / ( vRay2.getX() * vRay1.getY() - vRay2.getY() * vRay1.getX());
		
		// the point is inside the angle if the components are positive
		return ( a > 0 && b > 0);
	}
	
	// uses the crossing number algorithm to determine whether
	// a point is inside a polygon, specified by the names of the points
	// assumed that the points exist
	public boolean isPointInPolygon ( String pointName, String... polygonPoints) // TODO test this method for extreme cases
	{
		// the locations of the point and the polygon vertices
		Vector2 point;
		Vector2[] polygonVertices;
		
		// get the locations:
		point = construct.findPoint( pointName).getPositionVector();
		polygonVertices = new Vector2[ polygonPoints.length];
		for ( int i = 0; i < polygonVertices.length; i++)
		{
			polygonVertices[i] = construct.findPoint(  polygonPoints[i]).getPositionVector();
		}
		
		// iterate, while alternating the state of the point
		// at each intersection of the vertical ray with the polygon
		boolean result = false;
		int i, j;
		
		// TODO expand this part to be more clear
		for ( i = 0, j = polygonVertices.length - 1; i < polygonVertices.length; j = i++) {
			if ( ( ( polygonVertices[i].getY() > point.getY()) != ( polygonVertices[j].getY() > point.getY())) &&
				 ( point.getX() < ( polygonVertices[j].getX() - polygonVertices[i].getX()) * ( point.getY() - 
				   polygonVertices[i].getY()) / ( polygonVertices[j].getY()- polygonVertices[i].getY()) + polygonVertices[i].getX()) )
				// intersection, flip the state
				result = !result;
		}

		return result;
	}
	
	
	
	
	public static final int TRIANGLE_INTERSECTS_POLYGON = 1;
	public static final int TRIANGLE_INSIDE_POLYGON = 2;
	public static final int TRIANGLE_OUTSIDE_POLYGON = 3;
	public static final int TRIANGLE_OUTSIDE_CONTAINING_POLYGON = 4;
	
	// returns true if a line segment crosses a polygon's sides
	public int triangleInPolygon ( String triangleTip, String common1, String common2, String... polygonVertexNames)
	{
		Vector2 pointTip;
		Vector2 pointCommon1, pointCommon2;
		Vector2[] polygonVertices;
		
		// get the position vectors:
		pointTip = construct.findPoint( triangleTip).getPositionVector();
		pointCommon1 = construct.findPoint( common1).getPositionVector();
		pointCommon2 = construct.findPoint( common2).getPositionVector();
		
		polygonVertices = new Vector2[ polygonVertexNames.length];
		for ( int i = 0; i < polygonVertices.length; i++)
		{
			polygonVertices[i] = construct.findPoint(  polygonVertexNames[i]).getPositionVector();
		}
		
		if ( isPointInPolygon( triangleTip, polygonVertexNames))
		{
			// if the tip of the triangle is inside the polygon, check for additional intersections
			for ( int i = 0; i < polygonVertices.length - 1; i++)
			{
				if ( !polygonVertexNames[i].equals( common1) && !polygonVertexNames[i].equals( common2) &&
					 !polygonVertexNames[i + 1].equals( common1) && !polygonVertexNames[i + 1].equals( common2))
				{
					if ( lineSegmentsCross(pointCommon1, pointTip, polygonVertices[i], polygonVertices[i + 1]) ||
						 lineSegmentsCross(pointCommon2, pointTip, polygonVertices[i], polygonVertices[i + 1]))
					{
						// the triangle is inside the polygon but intersects it
						return TRIANGLE_INTERSECTS_POLYGON;
					}
				}
			}
			
			return TRIANGLE_INSIDE_POLYGON;
		}
		else
		{
			// if the tip of the triangle is outside the polygon, check for additional intersections again
			for ( int i = 0; i < polygonVertices.length - 1; i++)
			{
				if ( !polygonVertexNames[i].equals( common1) && !polygonVertexNames[i].equals( common2) &&
					 !polygonVertexNames[i + 1].equals( common1) && !polygonVertexNames[i + 1].equals( common2))
				{
					if ( lineSegmentsCross(pointCommon1, pointTip, polygonVertices[i], polygonVertices[i + 1]) ||
						 lineSegmentsCross(pointCommon2, pointTip, polygonVertices[i], polygonVertices[i + 1]))
					{
						// the triangle is inside the polygon but intersects it
						return TRIANGLE_INTERSECTS_POLYGON;
					}
				}
			}
			
			//check if the triangle contains the polygon
			for ( int i = 0; i < polygonVertices.length - 1; i++)
			{
				if ( !polygonVertexNames[i].equals( common1) && !polygonVertexNames[i].equals( common2) &&
					 !polygonVertexNames[i + 1].equals( common1) && !polygonVertexNames[i + 1].equals( common2))
				{
					if ( isPointInPolygon( polygonVertexNames[i], common1, common2, triangleTip))
						return TRIANGLE_OUTSIDE_CONTAINING_POLYGON;
					else
						return TRIANGLE_OUTSIDE_POLYGON;

				}
			}

			return TRIANGLE_OUTSIDE_POLYGON;
		}
		
	}
	
	
	// returns the axis aligned bounding box of a set of points
	/*private static Rectangle2 getBoundingRectangle ( Vector2[] points)
	{
		double minx, miny, maxx, maxy;
		
		minx = points[0].getX();
		maxx = points[0].getY();
		miny = points[0].getY();
		maxy = points[0].getY();
		
		// iterating through all the points, find the maximum and minimum coordinate values
		for ( Vector2 p : points)
		{
			if ( p.getX() < minx)
				minx = p.getX();
			if ( p.getX() > maxx)
				maxx = p.getX();
			if ( p.getY() < miny)
				miny = p.getY();
			if ( p.getY() > maxy)
				maxy = p.getY();
		}
		
		// return the bounding box
		return new Rectangle2( minx, miny, maxx, maxy);
	}*/
	
	// returns true if two line segments cross each other
	private static boolean lineSegmentsCross ( Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2)
	{
		if ( ccw( start1, end1, start2) * ccw( start1, end1, end2) > 0)
			return false;
		if ( ccw(start2, end2, start1) * ccw( start2, end2, end1) > 0)
			return false;
		return true;
	}

	// returns +1 if three vectors are arranged in counterclockwise direction
	// 0 if collinear, -1 if neither
	private static int ccw( Vector2 a, Vector2 b, Vector2 c) {
		double area2 = (b.getX() - a.getX()) * (c.getY() - a.getY()) - (c.getX() - a.getX()) * (b.getY() - a.getY());
		if      (area2 < 0) return -1;
		else if (area2 > 0) return +1;
		else                return  0;
	}
}
