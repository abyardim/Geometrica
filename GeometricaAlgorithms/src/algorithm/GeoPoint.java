package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

// a class representing a single point
public class GeoPoint extends GeoObject {
	
	private double x, y;
	private boolean fixed;
	boolean parametrized;

	public GeoPoint( String name ) {
		super(name, "point");
		
		x = y = 0;
		fixed = false;
	}
	
	// construct from the name and initial coordinates
	public GeoPoint ( String name, double x, double y)
	{
		super(name, "point");
		
		this.x = x;
		this.y = y;
		fixed = false;
	}
	
	// construct from the initial coordinates and the fixed state
	public GeoPoint ( String name, double x, double y, boolean fixed)
	{
		super(name, "point");
	
		this.x = x;
		this.y = y;
		this.fixed = fixed;
	}

	@Override
	public int getNumParameters ( ) 
	{
		// a point has two parameters, a x coordinate and a y coordinate
		// but if it is fixed, it does not have any
		if ( fixed)
		{
			return 0;
		}
		
		return 2;
	}

	// update the points coordinates from an array
	// it is assumed that the array is large enough
	@Override
	public void updateParameters ( double[] source, int startOffset) 
	{
		if ( fixed )
			return;
		
		// if the point is not fixed, update the coordinates from the given array
		this.x = source[startOffset];
		this.y = source[startOffset + 1];
	}

	@Override
	public double[] getParameters() {
		if ( this.isFixed())
			return new double[]{};
		
		return new double[] { x, y};
	}
	
	// getters / setters
	
	public double getX ( )
	{
		return x;
	}
	
	public double getY ( )
	{
		return y;
	}
	
	public void setX ( double x)
	{
		this.x = x;
	}
	
	public void setY ( double y)
	{
		this.y = y;
	}
	
	public Vector2 getPositionVector ( )
	{
		return new Vector2 ( x, y);
	}
	
	// is the point fixed at a coordinate by the user?
	public boolean isFixed ( )
	{
		return fixed;
	}
	
	public void setFixed ( boolean fixed)
	{
		this.fixed = fixed;
	}

	// fixes a point at a given coordinate
	public void fixPointAt ( double x, double y)
	{
		fixed = true;
		this.x = x;
		this.y = y;
	}
	
	public boolean isParametrized ( )
	{
		return parametrized;
	}
	
	public void setParametrized ( boolean param)
	{
		this.parametrized = param;
	}

	
	
}
