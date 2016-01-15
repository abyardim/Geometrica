package algorithm;
// Created: 26.94.2015, Ali Batuhan Yardým

// representing an abstract geometric object
// which can be updated according to new parameters
public abstract class GeoObject {
	String name;
	String type;
	
	public GeoObject ( String name, String type)
	{
		this.name = name;
		this.type = type;
	}
	
	// mutators
	
	// the formal name of this object, if there is any
	public String getName ( )
	{
		return name;
	}
	
	// returns a string representing the type
	public String getType ( )
	{
		return type;
	}
	
	// the methods responsible for updating the object according to new parameters
	public abstract int getNumParameters ( );
	public abstract void updateParameters ( double[] source, int startOffset);
	public abstract double[] getParameters ( );
}
