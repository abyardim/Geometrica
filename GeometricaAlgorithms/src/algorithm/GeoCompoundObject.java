package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

// the base class for geometric objects which consist of multiple children
public abstract class GeoCompoundObject extends GeoObject {
	
	ArrayList<GeoPoint> points;
	
	public GeoCompoundObject ( String name, String type)
	{
		super ( name, type);
		
		points = new ArrayList<GeoPoint>();
	}
	
	public void addPoint ( GeoPoint p)
	{
		points.add( p);
	}
	
	public void removePoint ( GeoPoint p)
	{
		points.remove( p);
	}
	
	public ArrayList<GeoPoint> getPoints ( )
	{
		return points;
	}

}
