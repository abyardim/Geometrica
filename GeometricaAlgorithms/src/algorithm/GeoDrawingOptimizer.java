// Created: 10.05.2015, Ali Batuhan Yardým

package algorithm;

import java.util.ArrayList;

// a class that given a geometric construct object,
// optimizes the locations of the points contained to
// match the constraints on them
public class GeoDrawingOptimizer {
	private GeoConstruct construct;
	
	public GeoDrawingOptimizer ( GeoConstruct cons)
	{
		this.construct = cons;
	}
	
	// run the optimization process
	// returns true only if a solution was found
	public boolean optimize ( boolean randomize)
	{
		// the set of objects and constraints that the numeric solver will work on
		ArrayList<GeoObject> objects;
		ArrayList<GeoConstraint> constraints;
		
		// the new regular polygon objects to be created, and its vertices:
		GeoRigidObject newRegularPolygon;
		GeoPoint[] newPolygonVertices;
		int pointId;
		
		// the numeric algorithm
		GeoNumericSolver solver;
		
		objects = new ArrayList<GeoObject>();
		constraints = new ArrayList<GeoConstraint>();
		
		pointId = 0;
		
		// process each constraint on the construct:
		for ( GeoConstraint constraint : construct.getConstraints())
		{
			// if this is a "regular polygon" constraint, make it a rigid object
			if ( constraint.getType() == GeoConstraint.C_REGULAR_POLYGON)
			{
				newPolygonVertices = new GeoPoint[ constraint.getPoints().length];
				
				for ( int i = 0; i < constraint.getPoints().length; i++)
				{
					GeoPoint p;
					p = constraint.getPoints()[i];
					
					if ( p.isParametrized() || p.isFixed())
					{
						// if this point has already been parameterized, create a virtual version of it
						GeoPoint vp = new GeoPoint( "#" + p.getName() + pointId);
						vp.setX( p.getX());
						vp.setY( p.getY());
						pointId++;
						
						constraints.add( new GeoConstraint.CPointToPointDistance( vp, p, 0));
						newPolygonVertices[i] = vp;
					}
					else
					{
						p.setParametrized( true);
						newPolygonVertices[i] = p;						
					}
				}
				
				newRegularPolygon = GeoRigidObject.createRegularPolygon( newPolygonVertices);
				objects.add( newRegularPolygon);
			}
			else
			{
				// a regular constraint, add it to the list for the numeric solver to process
				constraints.add( constraint);
			}
		}
		
		// add the unparameterized points as independent objects
		for ( GeoPoint p : construct.getPoints())
		{
			if ( !p.isParametrized())
			{
				objects.add( p);
			}
		}
		
		// solve the set of objects and constraints
		solver = new GeoNumericSolver();
		solver.addConstraints( constraints);
		solver.addObjects( objects);

		int trialCounter = 0;
		while ( trialCounter < 100 && !solver.findSolution( true, trialCounter < 3 ? randomize : true))
		{
			trialCounter++;
		}
		
		// debug
		System.out.println( "Final error: " + solver.calculateError());
		System.out.println( "Iterations: " + trialCounter);
		
		return solver.calculateError() < 1e-9;
	}

}
