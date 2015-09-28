package gov.state.cst.bpel_analyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BPELFile {
	private Path bpelPath;
	private HashMap<String, CompositeComponentProperty> properties;
	
	@SuppressWarnings("unused")
	private BPELFile() {
	}
	
	public BPELFile(String basePath, Node componentNode) {
		try {
	    	if( !(componentNode instanceof Element) ) {
	    		throw new IllegalArgumentException();
	    	}
	
	    	XPath xpath = XPathFactory.newInstance().newXPath();
	    	Node fileNameNode = (Node)xpath.evaluate( "implementation.bpel/@src", componentNode, XPathConstants.NODE );
	    	if( fileNameNode == null ) {
	    		throw new IllegalArgumentException();
	    	}

	    	this.bpelPath = Paths.get( basePath, fileNameNode.getNodeValue() );
			this.properties = new HashMap<String, CompositeComponentProperty>();

			NodeList propertyNodes = (NodeList)xpath.evaluate( "property", componentNode, XPathConstants.NODESET );
    		for( Node propertyNode : Utilities.asList( propertyNodes ) ) {
    			assert propertyNode instanceof Element;
    			CompositeComponentProperty property = new CompositeComponentProperty( (Element)propertyNode );
    			this.properties.put( property.getName(), property );
    		}
		}
		catch( Exception e ) {
			throw new IllegalArgumentException( e );
		}
   	}

	public Path getPath() {
		return this.bpelPath;
	}
	
	public <T> T property( String name, T defaultValue ) throws IllegalConversionException {
		if( !properties.containsKey( name ) ) {
			return defaultValue;
		}
		CompositeComponentProperty ccp = properties.get( name );
		return ccp.convertTo( defaultValue );
	}

	public HashMap<String, CompositeComponentProperty> getProperties() {
		return this.properties;
	}

	public void setProperties( HashMap<String, CompositeComponentProperty> properties ) {
		this.properties = properties;
	}
	
	
}
