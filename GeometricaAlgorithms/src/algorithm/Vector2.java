package algorithm;
// Created: 28.04.2015, Ali Batuhan Yardým

// represents a 2 dimensional vector
public final class Vector2 {
	// coordinates
	private double x, y;
	
	public Vector2 ( double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector2 ( )
	{
		this.x = 0;
		this.y = 0;
	}
	
	// getters
	public double getX ( )
	{
		return x;
	}
	
	public double getY ( )
	{
		return y;
	}
	
	
	// vector arithmetic:
	
	public Vector2 add ( Vector2 v)
	{
		return new Vector2 ( v.x + x, v.y + y);
	}
	
	// subtract v from this vector
	public Vector2 substract ( Vector2 v)
	{
		return new Vector2 ( x - v.x, y - v.y);
	}
	
	// scale this vector by a scalar
	public Vector2 scale ( double scalar)
	{
		return new Vector2 ( x * scalar, y * scalar);
	}
	
	// the size of this vector
	public double length ( )
	{
		return Math.sqrt( x * x + y * y);
	}
	
	// the size squared
	public double lengthSquared ( )
	{
		return x * x + y * y;
	}
	
	public double distanceTo ( Vector2 p1)
	{
		return Math.hypot( p1.x - x, p1.y - y);
	}
	
	// the scalar product of two vectors
	public double dotProduct ( Vector2 v)
	{
		return v.x * x + v.y * y;
	}
	
	// returns the projection of this vector on v
	public Vector2 projectionOnto ( Vector2 v)
	{
		return this.scale( this.dotProduct( v) / this.lengthSquared());
	}
	
	// returns the x component of the cross product this x v
	public double crossProduct ( Vector2 v)
	{
		return ( this.x * v.y) - ( this.y * v.x);
	}
	
	// returns the angle between two vectors in radians
	public double angleBetween ( Vector2 v)
	{
		return Math.acos( this.dotProduct( v) / ( v.length() * this.length()));
	}
	
	// rotates the vector by an angle in radians
	// in the counterclockwise direction
	public Vector2 rotate ( double theta)
	{
		double cosTheta, sinTheta;
		
		cosTheta = Math.cos( theta);
		sinTheta = Math.sin( theta);
		
		return new Vector2 ( x * cosTheta - y * sinTheta, x * sinTheta + y * cosTheta);
	}
	
	// rotates the end point of a vector around a pivot
	public Vector2 rotateAroundPoint ( double theta, Vector2 pivot)
	{
		Vector2 fromPivot;
		
		fromPivot = this.substract( pivot);
		return fromPivot.rotate( theta).add( pivot);
	}

	// returns a vector with coefficients of the basis formed by two linearly
	// independent vectors
	public Vector2 expressInBasis ( Vector2 u1, Vector2 u2)
	{
		return null;
	}
	
	
	public String toString ( )
	{
		return "( " + x + ", " + y + ")";
	}
	
	
}
