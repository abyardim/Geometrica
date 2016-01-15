package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;
import java.util.Collections;

// a set of points whose relative position with each other is known
public class GeoRigidObject extends GeoCompoundObject {
	
	private double[] pointData;
	private boolean scaleable;

	public GeoRigidObject(String name, boolean scaleable) {
		super(name, "rigid");
		
		this.scaleable = scaleable;
		
	}
	
	public GeoRigidObject( boolean scaleable) {
		super( "rigid*", "rigid");
		
		this.scaleable = scaleable;
	}

	@Override
	public int getNumParameters() {
		if ( scaleable)
			return 4;
		return 3;
	}

	@Override
	public void updateParameters(double[] source, int startOffset) {
		ArrayList<GeoPoint> points = getPoints();
		double x, y;
		double rotation;
		double scale;
		
		GeoPoint p;
		double newX, newY;
		 
		x = source[startOffset];
		y = source[startOffset + 1];
		rotation = source[startOffset + 2];
		
		if ( scaleable)
			scale = source[startOffset + 3] / 100;
		else
			scale = 1;
		 
		// place each point appropriately
		for ( int i = 0; i < points.size(); i++)
		{
			p = points.get( i);
			
			// first rotate and scale,
			newX = scale * ( pointData[2 * i] * Math.cos( rotation) - pointData[2 * i + 1] * Math.sin( rotation));
			newY = scale * ( pointData[2 * i] * Math.sin( rotation) + pointData[2 * i + 1] * Math.cos( rotation));
			 
			// then translate
			newX += x;
			newY += y;
			
			p.setX( newX);
			p.setY( newY);
		}
	}
	
	@Override
	public double[] getParameters() {
		
		// the average of the coordinates of each vertice
		double avgX, avgY;
		// an estimate of the scale
		double scale;
		// an estimate of the angle
		double rotation;
		
		avgX = avgY = 0;
		
		// compute the average:
		for ( GeoPoint p : getPoints())
		{
			avgX += p.getX();
			avgY += p.getY();
		}
		
		avgX /= getPoints().size();
		avgY /= getPoints().size();

		scale = 0;
		// find the average value of the scale
		for ( int i = 0; i < getPoints().size(); i++)
		{
			double realDist = getPoints().get( i).getPositionVector().distanceTo( getPoints().get( (i + 1) % getPoints().size()).getPositionVector());
			double expectedDist = new Vector2( pointData[2 * i], pointData[2 * i + 1])
				.distanceTo( new Vector2( pointData[2 * (i + 1) % getPoints().size()], pointData[2 * (i + 1) % getPoints().size() + 1]));
			
			scale += realDist / expectedDist * 100;
		}
		
		scale /= getPoints().size();
		
		
		// check if the system is counterclockwise or clockwise
		ArrayList<GeoPoint> points = getPoints();
		double cwResult = 0;
		for ( int i = 0; i < points.size(); i++)
		{
			Vector2 p1, p2;
			p1 = new Vector2( points.get( i).getX(), points.get( i).getY());
			p2 = new Vector2( points.get( (i + 1) % points.size()).getX(), points.get( (i + 1) % points.size()).getY());
			
			p1 = p1.substract( new Vector2( avgX, avgY));
			p2 = p2.substract( new Vector2( avgX, avgY));
			
			cwResult += ( p2.getX() - p1.getX()) * ( p2.getY() + p1.getY());
		}
		
		if ( cwResult > 0) // clockwise
		{
			Collections.reverse( points);
			System.out.println( "clockwise");
		}
		
		// estimate the rotation
		Vector2 vpoint1 = getPoints().get( 0).getPositionVector().substract( new Vector2( avgX, avgY));
		rotation = Math.atan2( vpoint1.getY(), vpoint1.getX()) - Math.atan2( pointData[1], pointData[0]);
		
		return new double[] { avgX, avgY, rotation, scale};
	}
	
	private void process ( )
	{
		ArrayList<GeoPoint> points = getPoints();
		pointData = new double[points.size() * 2];
		
		for ( int i = 0; i < points.size(); i++)
		{
			pointData[2 * i] = points.get( i).getX();
			pointData[2 * i + 1] = points.get( i).getY();
		}
	}
	
	public static GeoRigidObject createRegularPolygon ( double sideLength, GeoPoint... points)
	{
		GeoRigidObject rigidObject;
		
		rigidObject = new GeoRigidObject( false);
		
		int n = 0;
		double angleDiff;
		double radius;
		
		angleDiff = Math.PI * 2 / points.length;
		radius = sideLength / ( 2 * Math.sin( angleDiff / 2));
		
		rigidObject.pointData = new double[points.length * 2];
		
		for ( int i = 0; i < points.length; i++)
		{
			rigidObject.pointData[2 * i] = radius * Math.cos( angleDiff * n);
			rigidObject.pointData[2 * i + 1] = radius * Math.sin( angleDiff * n);
			
			rigidObject.addPoint( points[i]);
			// p.setParametrized( true);
			
			n++;
		}
		
		//rigidObject.process();
		
		return rigidObject;
	}
	
	public static GeoRigidObject createRegularPolygon ( GeoPoint... points)
	{
		GeoRigidObject rigidObject;
		
		rigidObject = new GeoRigidObject( true);
		
		int n = 0;
		double angleDiff;
		double radius;
		
		angleDiff = Math.PI * 2 / points.length;
		radius = 50;
		
		rigidObject.pointData = new double[points.length * 2];
		
		for ( int i = 0; i < points.length; i++)
		{
			rigidObject.pointData[2 * i] = radius * Math.cos( angleDiff * n);
			rigidObject.pointData[2 * i + 1] = radius * Math.sin( angleDiff * n);
			
			rigidObject.addPoint( points[i]);
			// p.setParametrized( true);
			
			n++;
		}
		
		// rigidObject.process();
		
		return rigidObject;
	}
	
	public static GeoRigidObject createFromPointData ( boolean scaleable, double[] data, GeoPoint... points)
	{
		if ( points.length != data.length * 2)
			return null;
		
		GeoRigidObject rigidObject;
		
		rigidObject = new GeoRigidObject( true);
		
		int i = 0;
		for ( GeoPoint p : points)
		{
			p.setParametrized( true);
			p.setX( data[i * 2]);
			p.setY( data[i * 2 + 1]);
			rigidObject.addPoint ( p);
			
			i++;
		}
		
		rigidObject.process();
		
		return rigidObject;
	}


}
