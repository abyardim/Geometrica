package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.util.ArrayList;

// numerical solver for multiple variable constraints/functions
// using BFGS algorithm

// complete port from the constraint solving project SketchSolve to Java
// can be found at https://code.google.com/p/sketchsolve/
public class GeoNumericSolver {

	// the set of constraints and objects to optimize
	ArrayList<GeoConstraint> constraints;
	ArrayList<GeoObject> objects;

	public GeoNumericSolver ()
	{
		constraints = new ArrayList<GeoConstraint>();
		objects = new ArrayList<GeoObject>();
	}

	///// methods to access solver data
	
	public void addObject ( GeoObject p)
	{
		objects.add( p);
	}
	
	public void addObjects ( ArrayList<GeoObject> list)
	{
		objects.addAll( list);
	}

	public void addConstraint ( GeoConstraint c)
	{
		constraints.add( c);
	}
	
	public void addConstraints ( ArrayList<GeoConstraint> constraints)
	{
		this.constraints.addAll( constraints);
	}

	public ArrayList<GeoObject> getPoints ()
	{
		return objects;
	}

	public ArrayList<GeoConstraint> getConstraints ()
	{
		return constraints;
	}

	///////////////////////////////// the algorithm
	
	// constants of BFGS algorithm
	final double pert				= 1e-11; // -11
	final double XconvergenceRough	= 1e-4;
	final double XconvergenceFine	= 1e-12; // -12
	final double smallF				= 1e-20;
	final double validSolution		= 1e-9; // -12
	int ftimes; // track error calculation calls

	// the set of all parameters to be optimized
	private double[] parameterPool;
	private double[] x = parameterPool;

	// starts the solution process
	// fine indicates whether or not a more precise but costly process should be applied
	// randomize indicates whether parameters should be randomized beforehand
	public boolean findSolution( boolean fine, boolean randomize)
	{		
		// build the parameter pool
		buildParameterPool();
		
		// TODO implement the option to prevent randomization
		if ( randomize)
			randomizeParameters();
		else
			cachePoints();
		
		// shorthand...
		x = parameterPool;
		final int xLength = x.length;

		/////////////////////////////////////////////////
		// apply BFGS

		////////// setup:
		ftimes = 0;

		// precision of the result, affects performance cost
		double convergence;
		if ( fine)
			convergence = XconvergenceFine;
		else
			convergence = XconvergenceRough;

		double f0 = calculateError(); // initial error value
		if ( f0 < smallF)
			return true; // success

		// calculate gradient vector
		double[] grad = new double[ xLength];
		double norm;
		double f1,f2,f3,alpha1,alpha2,alpha3,alphaStar;
		norm = 0;
		for ( int j = 0; j < xLength;j++)
		{
			x[j] = x[j] + pert;
			grad[j] = (calculateError() - f0) / pert;
			x[j] -= pert;
			norm = norm + (grad[j]*grad[j]);
		}
		norm = Math.sqrt( norm);

		double[] s = new double[xLength]; // search direction
		double[][] N = new double[xLength][xLength]; // Hessian matrix estimate

		// Hessian estimation
		// TODO wrap these in a convenient matrix class?
		for ( int i = 0; i < xLength; i++)
		{
			for ( int j = 0; j < xLength; j++)
			{
				if ( i == j)
				{
					N[i][j] = grad[i] / norm;
					if ( N[i][j] < 0)
						N[i][j] = -N[i][j];

					s[i] = -grad[i] / norm; // search direction
				}
				else
					N[i][j] = 0;
			}
		}

		double fnew = f0 + 1;
		double alpha = 1; // initial value of search vector multiplier

		double[] xold = new double[xLength]; // for old parameter set
		double fold;
		for ( int i = 0; i < xLength; i++) // fill xold with current parameters
		{
			xold[i] = x[i];
		}

		//////////////////// line search:
		alpha1 = 0;
		f1 = f0;

		alpha2 = 1;
		for ( int i = 0; i < xLength; i++)
		{
			x[i] = xold[i] + alpha2 * s[i]; // take a step of alpha2 in search direction
		}
		f2 = calculateError();

		alpha3 = 2 * alpha2;
		for ( int i = 0; i < xLength; i++)
		{
			x[i]=xold[i]+alpha3*s[i]; // take a step of alpha3
		}
		f3 = calculateError();

		// Now reduce or lengthen alpha2 and alpha3 until the minimum is
		// bracketed by the triplet f1>f2<f3
		while( f2 > f1 || f2 > f3)
		{
			if( f2 > f1)
			{
				// If f2 is greater than f1 then we shorten alpha2 and alpha3 closer to f1
				// Effectively both are shortened by a factor of two.
				alpha3 = alpha2;
				f3 = f2;
				alpha2 = alpha2/2;
				for(int i=0; i < xLength; i++)
				{
					x[i] = xold[i] + alpha2 * s[i];//calculate the new x
				}
				f2=calculateError();
			}
			else if(f2>f3)
			{
				//If f2 is greater than f3 then we length alpah2 and alpha3 closer to f1
				//Effectively both are lengthened by a factor of two.
				alpha2=alpha3;
				f2=f3;
				alpha3=alpha3*2;
				for(int i=0;i<xLength;i++)
				{
					x[i]=xold[i]+alpha3*s[i];//calculate the new x
				}
				f3=calculateError();
			}
		}

		// get the alpha for the minimum f of the quadratic approximation
		alphaStar= alpha2+((alpha2-alpha1)*(f1-f3))/(3*(f1-2*f2+f3));

		//Guarantee that the new alphaStar is within the bracket
		if(alphaStar>alpha3 || alphaStar<alpha1) alphaStar=alpha2;

		/// Set the values to alphaStar
		for(int i=0;i<xLength;i++)
		{
			x[i]=xold[i]+alphaStar*s[i];//calculate the new x
		}
		fnew = calculateError();
		fold=fnew;

		///////////////////////// line search end
		double[] deltaX = new double[xLength];
		double[] gradnew = new double[xLength];
		double[] gamma = new double[xLength];
		double bottom=0;
		double deltaXtDotGamma;
		double[][] gammatDotN = new double[1][xLength]; // [1][xLength];

		double gammatDotNDotGamma=0;
		double firstTerm=0;

		double[][] FirstSecond = new double[xLength][xLength]; // [xLength][xLength];
		double[][] deltaXDotGammatDotN = new double[xLength][xLength]; // [xLength][xLength];
		double[][] gammatDotDeltaXt = new double[xLength][xLength]; // [xLength][xLength];
		double[][] NDotGammaDotDeltaXt = new double[xLength][xLength]; // [xLength][xLength];

		double deltaXnorm = 1;

		int iterations = 1;
		int steps;

		for (int i = 0 ; i < xLength; i++)
		{
			deltaX[i] = x[i] - xold[i]; //Calculate the difference in x for the Hessian update
		}

		///////////////////////////////////////////////
		/// Start of main algorithm
		///////////////////////////////////////////////
		while( deltaXnorm > convergence && fnew > smallF && /* avoid too long search times*/ ftimes < 500000)
		{
			bottom=0;
			deltaXtDotGamma = 0;

			for(int i=0;i<xLength;i++)
			{
				//Calculate the new gradient vector
				x[i]=x[i]+pert;
				gradnew[i]=( calculateError() - fnew) / pert;
				x[i]=x[i]-pert;
				//Calculate the change in the gradient
				gamma[i]=gradnew[i]-grad[i];
				bottom+=deltaX[i] * gamma[i];

				deltaXtDotGamma += deltaX[i]*gamma[i];
			}

			//make sure that bottom is never 0
			if (bottom==0) 
				bottom=.0000000001;

			//calculate all (1xn).(nxn)
			for(int i=0;i<xLength;i++)
			{
				gammatDotN[0][i]=0;
				for(int j=0;j<xLength;j++)
				{
					gammatDotN[0][i]+=gamma[j]*N[i][j];//This is gammatDotN transpose
				}

			}

			//calculate all (1xn).(nx1)
			gammatDotNDotGamma=0;
			for(int i=0;i<xLength;i++)
			{
				gammatDotNDotGamma+=gammatDotN[0][i]*gamma[i];
			}

			//Calculate the first term
			firstTerm=0;
			firstTerm=1+gammatDotNDotGamma/bottom;

			//Calculate all (nx1).(1xn) matrices
			for(int i=0;i<xLength;i++)
			{
				for(int j=0;j<xLength;j++)
				{
					FirstSecond[i][j]=((deltaX[j]*deltaX[i])/bottom)*firstTerm;
					deltaXDotGammatDotN[i][j]=deltaX[i]*gammatDotN[0][j];
					gammatDotDeltaXt[i][j]=gamma[i]*deltaX[j];
				}
			}

			//Calculate all (nxn).(nxn) matrices
			for(int i=0;i<xLength;i++)
			{
				for(int j=0;j<xLength;j++)
				{
					NDotGammaDotDeltaXt[i][j]=0;
					for(int k=0;k<xLength;k++)
					{
						NDotGammaDotDeltaXt[i][j]+=N[i][k]*gammatDotDeltaXt[k][j];
					}
				}
			}

			//Now calculate the BFGS update on N
			for(int i=0;i<xLength;i++)
			{
				for(int j=0;j<xLength;j++)
				{
					N[i][j]=N[i][j]+FirstSecond[i][j]-(deltaXDotGammatDotN[i][j]+NDotGammaDotDeltaXt[i][j])/bottom;
				}
			}

			//Calculate s
			for(int i=0;i<xLength;i++)
			{
				s[i]=0;
				for(int j=0;j<xLength;j++)
				{
					s[i]+=-N[i][j]*gradnew[j];
				}
			}

			alpha=1; //Initial search vector multiplier

			//copy newest values to the xold
			for(int i=0;i<xLength;i++)
			{
				xold[i]=x[i];//Copy last values to xold
			}
			steps=0;


			////////////////////////
			// start of line search
			////////////////////////

			//Make the initial position alpha1
			alpha1=0;
			f1 = fnew;

			//Take a step of alpha=1 as alpha2
			alpha2=1;
			for(int i=0;i<xLength;i++)
			{
				x[i]=xold[i]+alpha2*s[i];//calculate the new x
			}
			f2 = calculateError();

			//Take a step of alpha 3 that is 2*alpha2
			alpha3 = alpha2*2;
			for(int i=0;i<xLength;i++)
			{
				x[i]=xold[i]+alpha3*s[i];//calculate the new x
			}
			f3=calculateError();

			//Now reduce or lengthen alpha2 and alpha3 until the minimum is
			//Bracketed by the triplet f1>f2<f3
			steps=0;
			while(f2>f1 || f2>f3)
			{
				if(f2>f1)
				{
					//If f2 is greater than f1 then we shorten alpha2 and alpha3 closer to f1
					//Effectively both are shortened by a factor of two.
					alpha3=alpha2;
					f3=f2;
					alpha2=alpha2/2;
					for(int i=0;i<xLength;i++)
					{
						x[i]=xold[i]+alpha2*s[i];//calculate the new x
					}
					f2= calculateError();

				}

				else if(f2>f3)
				{
					//If f2 is greater than f3 then we length alpah2 and alpha3 closer to f1
					//Effectively both are lengthened by a factor of two.
					alpha2=alpha3;
					f2=f3;
					alpha3=alpha3*2;
					for(int i=0;i<xLength;i++)
					{
						x[i]=xold[i]+alpha3*s[i];//calculate the new x
					}
					f3 = calculateError();
				}
				if(steps==4)
				{
					alpha2=1;
					alpha3=2;

					for(int i=0;i<xLength;i++)
					{
						for(int j=0;j<xLength;j++)
						{
							if(i==j)
							{
								N[i][j]=1;
								s[i]=-gradnew[i]; //Calculate the initial search vector
							}
							else N[i][j]=0;
						}
					}
				}
				steps=steps+1;
			}

			// get the alpha for the minimum f of the quadratic approximation
			alphaStar= alpha2+((alpha2-alpha1)*(f1-f3))/(3*(f1-2*f2+f3));

			//Guarantee that the new alphaStar is within the bracket
			if(alphaStar>alpha3 || alphaStar<alpha1)
			{
				alphaStar=alpha2;
			}

			/// Set the values to alphaStar
			for(int i=0;i<xLength;i++)
			{
				x[i]=xold[i]+alphaStar*s[i];//calculate the new x
			}
			fnew = calculateError();

			////////////////////////
			// end of line search
			////////////////////////

			deltaXnorm=0;
			for(int i=0;i<xLength;i++)
			{
				deltaX[i]=x[i]-xold[i]; // Calculate the difference in x for the hessian update
				deltaXnorm+=deltaX[i]*deltaX[i];
				grad[i]=gradnew[i];
			}
			deltaXnorm=Math.sqrt(deltaXnorm);
			iterations++;
			/////////////////////////////////////////////////////////////
			///End of Main loop
			/////////////////////////////////////////////////////////////
		}

		updatePoints();
		
		System.out.println( "Solved in " + ftimes);
		System.out.println( "Error: " + calculateError());

		///End of function
		if( fnew < validSolution) 
			return true;

		return false; // failure... No solution found
	} // end of method findSolution

	// constructs the parameter pool
	private void buildParameterPool ( )
	{
		// total number of parameters to work on
		int parameterCount;
		
		parameterCount = 0;
		for ( GeoObject c : objects)
		{
			parameterCount += c.getNumParameters();
		}
		
		parameterPool = new double[ parameterCount];
		
		// clear the array to zeros
		for ( int i = 0; i < parameterCount; i++)
		{
			parameterPool[i] = 0;
		}
	}
	
	// randomize variables
	private void randomizeParameters ()
	{
		for ( int i = 0; i < parameterPool.length; i++)
		{
			parameterPool[i] = Math.random() * 500; // TODO make this context specific
		}		
	}

	// sets the current locations of points as parameters
	// useful for getting hints from the user about the construction
	private void cachePoints ( )
	{
		int leftAt = 0;

		for ( int i = 0; i < objects.size(); i++)
		{
			if ( objects.get(i).getType().equals( "point")) // a point object
			{
				GeoPoint p = ( GeoPoint) objects.get(i);
				if ( p.getNumParameters() == 2)
				{
					parameterPool[leftAt] = p.getX();
					parameterPool[leftAt + 1] = p.getY();
				}
			}
			else if ( objects.get(i).getType().equals( "rigid")) // a rigid set of points
			{
				if ( objects.get(i).getNumParameters() == 3)
				{
					double[] params = objects.get(i).getParameters();
					
					parameterPool[ leftAt] = params[0];
					parameterPool[ leftAt + 1] = params[1];
					parameterPool[ leftAt + 2] = params[2];
					
					 // leftAt += 3;
				}
				else
				{
					double[] params = objects.get(i).getParameters();

					parameterPool[ leftAt] = params[0];
					parameterPool[ leftAt + 1] = params[1];
					parameterPool[ leftAt + 2] = params[2];
					parameterPool[ leftAt + 3] = params[3];
					
					// leftAt += 4;
				}
			}
			objects.get( i).updateParameters( parameterPool, leftAt);
			leftAt += objects.get( i).getNumParameters();
		}
	}
	
	// update the parameters of the geometric objects
	private void updatePoints ( )
	{
		int leftAt = 0;

		for ( int i = 0; i < objects.size(); i++)
		{
			objects.get( i).updateParameters( parameterPool, leftAt);
			leftAt += objects.get( i).getNumParameters();
		}
	}

	// calculate the error of the current parameter setup // TODO should be private
	public double calculateError( )
	{
		ftimes++;

		updatePoints();

		double err = 0;
		for (int i = 0; i < constraints.size(); i++)
		{
			err += constraints.get( i).calculateError();
		}

		return err;
	}
}
