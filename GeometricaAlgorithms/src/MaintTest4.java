import java.io.File;
import java.io.IOException;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 

public class MaintTest4 {
	
	public static void main ( String[] args) throws SAXException, IOException, ParserConfigurationException, TransformerException
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (new File("exampleFile.txt"));
		
		System.out.println( doc.getDocumentElement().getNodeName());
		NodeList points = doc.getElementsByTagName( "points");
		System.out.println( "point node name = " + points.item(0).getNodeName());
		// NodeList points = listPoints.item(0).getChildNodes();
		
		if ( points.item( 0).getNodeType() == Node.ELEMENT_NODE)
		{
			Element epoints = ( Element) points.item( 0);
			
			NodeList plist = epoints.getElementsByTagName( "point");
			
			for ( int i = 0; i < plist.getLength(); i++)
			{
				System.out.println( "\nPoint: " + plist.item( i).getTextContent() + " - ");
				System.out.println( "x = " + plist.item( i).getAttributes().getNamedItem("x") + " & y = " + plist.item( i).getAttributes().getNamedItem("y"));
			}
		}

		
		System.out.println( "\nScript:");
		
		System.out.println( doc.getElementsByTagName( "script").item( 0).getTextContent());
		
		DocumentBuilderFactory docBuilderFactory2 = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder2 = docBuilderFactory.newDocumentBuilder();
		
		Document out = docBuilder2.newDocument();
		
		Element root = out.createElement( "construct");
		out.appendChild( root);
		
		Element scriptElement = out.createElement( "script");
		scriptElement.appendChild( out.createTextNode( "point A B \nset-dist A B 300"));
		root.appendChild( scriptElement);
		
		Element pointsElement = out.createElement( "points");
		Element p1, p2;
		p1 = out.createElement( "point");
		p2 = out.createElement( "point");
		
		p1.setAttribute( "x", "123x");
		p1.setAttribute( "y", "456y");
		
		p2.setAttribute( "x", "777x");
		p2.setAttribute( "y", "888y");
		
		pointsElement.appendChild( p1);
		pointsElement.appendChild( p2);
		
		root.appendChild( pointsElement);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(out);
		StreamResult result = new StreamResult(new File("exampleOut.txt"));
		transformer.transform( source, result);
	}
	
}
