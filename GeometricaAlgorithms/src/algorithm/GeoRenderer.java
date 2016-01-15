package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.awt.Graphics;

// temporary class to visualize geometric constructions
// for testing
public class GeoRenderer {
	private GeoConstruct construct;
	
	public GeoRenderer ( GeoConstruct construct)
	{
		this.construct = construct;
	}
	
	public void renderTo ( Graphics g2d)
	{
		// first, render points
		for ( GeoPoint p : construct.getPoints())
		{
			g2d.drawString( p.name, (int) ( p.getX() - 10), (int) ( p.getY() - 6));
			g2d.drawOval( (int) ( p.getX() - 3), (int) ( p.getY() - 3), 6, 6);
		}
		
		// render some constraints, according to their types
		for ( GeoConstraint cons : construct.getConstraints())
		{
			if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
				GeoConstraint.CPointToPointDistance p2p = (GeoConstraint.CPointToPointDistance) cons;
        		
        		// draw line segment:
        		g2d.drawLine( (int) p2p.p1.getX() , (int) p2p.p1.getY(),(int) p2p.p2.getX(),(int) p2p.p2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_INTERIOR_ANGLE)
        	{
        		GeoConstraint.CInteriorAngle p2p = (GeoConstraint.CInteriorAngle) cons;
        		
        		// draw line segment:
        		g2d.drawLine( (int) p2p.p1.getX() , (int) p2p.p1.getY(),(int) p2p.p2.getX(),(int) p2p.p2.getY());
        		g2d.drawLine( (int) p2p.p3.getX() , (int) p2p.p3.getY(),(int) p2p.p2.getX(),(int) p2p.p2.getY());
        		
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CEqualLength eqlen = (GeoConstraint.CEqualLength) cons;
        		
        		g2d.drawLine( (int) eqlen.l1p1.getX() , (int) eqlen.l1p1.getY(),(int) eqlen.l1p2.getX(),(int) eqlen.l1p2.getY());
        		g2d.drawLine( (int) eqlen.l2p1.getX() , (int) eqlen.l2p1.getY(),(int) eqlen.l2p2.getX(),(int) eqlen.l2p2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CPointOnLine eqlen = (GeoConstraint.CPointOnLine) cons;
      
        		g2d.drawLine( (int) eqlen.lp1.getX() , (int) eqlen.lp1.getY(),(int) eqlen.lp2.getX(),(int) eqlen.lp2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CEqualAngle p2p = (GeoConstraint.CEqualAngle) cons;
        		
        		// draw line segment:
        		g2d.drawLine( (int) p2p.a1.getX() , (int) p2p.a1.getY(),(int) p2p.a2.getX(),(int) p2p.a2.getY());
        		g2d.drawLine( (int) p2p.a3.getX() , (int) p2p.a3.getY(),(int) p2p.a2.getX(),(int) p2p.a2.getY());
        		
        		// draw line segment 2:
        		g2d.drawLine( (int) p2p.b1.getX() , (int) p2p.b1.getY(),(int) p2p.b2.getX(),(int) p2p.b2.getY());
        		g2d.drawLine( (int) p2p.b3.getX() , (int) p2p.b3.getY(),(int) p2p.b2.getX(),(int) p2p.b2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CParallelLine eqlen = (GeoConstraint.CParallelLine) cons;
        		
        		g2d.drawLine( (int) eqlen.l1p1.getX() , (int) eqlen.l1p1.getY(),(int) eqlen.l1p2.getX(),(int) eqlen.l1p2.getY());
        		g2d.drawLine( (int) eqlen.l2p1.getX() , (int) eqlen.l2p1.getY(),(int) eqlen.l2p2.getX(),(int) eqlen.l2p2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CPointToLineDistance distCons = (GeoConstraint.CPointToLineDistance) cons;
        		
        		g2d.drawLine( (int) distCons.lp1.getX() , (int) distCons.lp1.getY(),(int) distCons.lp2.getX(),(int) distCons.lp2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CPointOnLineSegment eqlen = (GeoConstraint.CPointOnLineSegment) cons;
        	      
        		g2d.drawLine( (int) eqlen.lp1.getX() , (int) eqlen.lp1.getY(),(int) eqlen.lp2.getX(),(int) eqlen.lp2.getY());
        	}
        	else if ( cons.getType() == GeoConstraint.C_P2P_DISTANCE)
        	{
        		GeoConstraint.CTriangularArea car = (GeoConstraint.CTriangularArea) cons;
        		
        		// draw the triangle
        		g2d.drawLine( (int) car.p1.getX() , (int) car.p1.getY(),(int) car.p2.getX(),(int) car.p2.getY());
        		g2d.drawLine( (int) car.p1.getX() , (int) car.p1.getY(),(int) car.p3.getX(),(int) car.p3.getY());
        		g2d.drawLine( (int) car.p2.getX() , (int) car.p2.getY(),(int) car.p3.getX(),(int) car.p3.getY());
        	}
		}
	}
}

