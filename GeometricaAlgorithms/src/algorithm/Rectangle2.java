package algorithm;
// Created: 27.04.2015, Ali Batuhan Yardým

// to represent a 2D axis aligned box
public class Rectangle2 {
	// the point with the smaller coordinates
	private Vector2 start;
	// the point with the larger coordinates
	private Vector2 end;

	public Rectangle2 ( )
	{
		start = new Vector2();
		end = new Vector2();
	}

	// construct from points
	public Rectangle2 ( Vector2 v1, Vector2 v2)
	{
		start 	= new Vector2 ( Math.min( v1.getX(), v2.getX()), Math.min( v1.getY(), v2.getY()));
		end		= new Vector2 ( Math.max( v1.getX(), v2.getX()), Math.max( v1.getY(), v2.getY()));
	}
	
	// construct from coordinates
	public Rectangle2 ( double x1, double y1, double x2, double y2)
	{
		start 	= new Vector2 ( Math.min( x1, x2), Math.min( y1, y2));
		end 	= new Vector2 ( Math.max( x1, x2), Math.max( y1, y2));
	}
	
	// construct from size and the upper left point
	public Rectangle2 ( Vector2 p, double w, double h)
	{
		this( p.getX(), p.getY(), p.getX() + w, p.getY() + h);
	}
	
	// getters / setters
	
	public Vector2 getStart() {
		return start;
	}

	public void setStart(Vector2 start) {
		this.start = start;
	}

	public Vector2 getEnd() {
		return end;
	}

	public void setEnd(Vector2 end) {
		this.end = end;
	}
	
	public double getHeight ( )
	{
		return end.getY() - start.getY();
	}
	
	public double getWidth ( )
	{
		return end.getX() - start.getX();
	}
	
	// returns true only if the vector is contained in the rectangle
	public boolean contains ( Vector2 v)
	{
		return ( v.getX() < end.getX() && v.getX() > start.getX()) &&
				( v.getY() < end.getY() && v.getY() > start.getY());
	}
	
	public boolean intersects ( Rectangle2 rect)
	{
		 return ( Math.abs(this.start.getX() - rect.start.getX()) * 2 < (this.getWidth() + rect.getWidth())) &&
		        ( Math.abs(this.start.getY() - rect.start.getY()) * 2 < (this.getHeight() + rect.getHeight()));
	}
}
