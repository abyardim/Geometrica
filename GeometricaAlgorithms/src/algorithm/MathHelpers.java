package algorithm;
// Created: 23.04.2015, Ali Batuhan Yardým

public class MathHelpers {
	
	// rounds a double to the specified number of figures
	// modified version originally taken from
	// http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	public static double round ( double value, int places) {
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	
	// squares a given number
	public static double square ( double x)
	{
		return x * x;
	}
	
	// converts the value of an angle in degrees to radians
	public static double toRadians ( double val)
	{
		return val * Math.PI / 180;
	}
	
	// converts the value of an angle in degrees to radians
	public static double toDegrees ( double val)
	{
		return val * 180 / Math.PI;
	}
		
	
}
