package algorithm;
// Created: 26.04.2015, Ali Batuhan Yardým

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// contains every detail related to a geometric system in the program
public class GeoConstruct {
	// the unprocessed points and geometric objects of a construction
	private ArrayList<GeoPoint> points;
	private ArrayList<GeoObject> rawObjects;

	// the geometric relationships
	private ArrayList<GeoConstraint> constraints;
	
	private HashMap<String,GeoPoint> pointDictionary;
	
	// the script this construct originated from
	private String script;
	
	// the list of features the user asks for
	private ArrayList<String> queries;
	
	public GeoConstruct ()
	{
		rawObjects = new ArrayList<GeoObject>();
		points = new ArrayList<GeoPoint>();
		constraints = new ArrayList<GeoConstraint>();
		
		pointDictionary = new HashMap<String,GeoPoint>();
		
		queries = new ArrayList<String>();
	} 

	// getters / setters
	public ArrayList<GeoPoint> getPoints ( ) 
	{
		return points;
	}

	public void addPoints ( ArrayList<GeoPoint> points) 
	{
		for ( GeoPoint p : points)
		{
			this.points.add( p);
			this.rawObjects.add( p);
			this.pointDictionary.put( p.name, p);
		}
	}
	
	public void addPoint ( GeoPoint point) 
	{
		this.points.add( point);
		this.rawObjects.add( point);
		this.pointDictionary.put( point.name, point);
	}

	public ArrayList<GeoObject> getRawObjects() 
	{
		return rawObjects;
	}

	public void addRawObject ( GeoObject rawObject) 
	{
		this.rawObjects.add( rawObject);
	}
	
	public ArrayList<GeoConstraint> getConstraints () 
	{
		return constraints;
	}

	public void addConstraint ( GeoConstraint constraint) 
	{
		this.constraints.add( constraint);
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
	public void addQuery ( String s)
	{
		queries.add( s);
	}
	
	public ArrayList<String> getQueries ( )
	{
		return queries;
	}	
	
	// point query from name, returns null if point does not exist
	public GeoPoint findPoint ( String name)
	{
		return pointDictionary.get( name);
	}	
	
	////// methods to load constructs from files
	
	// saves a construct to a file
	public static void exportToFile ( GeoConstruct cons, File file) throws ParserConfigurationException, TransformerException
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
		Document out = docBuilder.newDocument();
		
		// xml nodes of the file
		Element root;
		Element scriptElement;
		Element p1;
		Element pointsElement;
		
		root = out.createElement( "construct");
		out.appendChild( root);
		
		// add the script of this construct
		scriptElement = out.createElement( "script");
		scriptElement.appendChild( out.createTextNode( cons.getScript()));
		root.appendChild( scriptElement);
		
		pointsElement = out.createElement( "points");
		
		// save the data of all points in the construct
		for ( GeoPoint p : cons.getPoints())
		{
			p1 = out.createElement( "point");
			p1.setAttribute( "x", "" + p.getX());
			p1.setAttribute( "y", "" + p.getY());
			p1.appendChild( out.createTextNode( p.getName()));
			
			pointsElement.appendChild( p1);
		}
		
		root.appendChild( pointsElement);

		// attempt to save this file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(out);
		StreamResult result = new StreamResult( file);
		transformer.transform( source, result);


	}
	
	// loads the specified construct from a given file
	// returns null if file is not valid
	public static GeoConstruct loadFromFile ( File file)
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc;
		
		String script;
		GeoConstruct construct;
		GeoInterpreter interpreter;
		
		NodeList pointsNode;
		NodeList pointList;
		
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse ( file);
			
			// read the script and create the construct
			script = doc.getElementsByTagName( "script").item( 0).getTextContent();
			/*interpreter = new GeoInterpreter( script);
			interpreter.interpretString();
			construct = interpreter.getConstruct();*/
			
			construct = new GeoConstruct();
			construct.setScript( script);
			
			// read the data of all points, if they exist
			pointsNode = doc.getElementsByTagName( "points");

			if ( pointsNode.getLength() > 0 && pointsNode.item( 0).getNodeType() == Node.ELEMENT_NODE)
			{				
				pointList = ( ( Element) pointsNode.item( 0)).getElementsByTagName( "point");
				
				for ( int i = 0; i < pointList.getLength(); i++)
				{
					GeoPoint pointObject;
					
					String name =  pointList.item( i).getTextContent();
					
					// if this point does not exist yet
					if ( null == construct.findPoint( pointList.item( i).getTextContent()))
					{
						pointObject = new GeoPoint( pointList.item( i).getTextContent());
						
						// update the coordinates of this point
						pointObject.setX( Double.parseDouble( pointList.item( i).getAttributes().getNamedItem("x").getTextContent()));
						pointObject.setY( Double.parseDouble( pointList.item( i).getAttributes().getNamedItem("y").getTextContent()));
						
						construct.addPoint( pointObject);
					}
				}
			}
			
		} catch ( Exception e) {
			e.printStackTrace();
			
			return null;
		}

		return construct;
	}

}
