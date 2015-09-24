/**
 * 
 */
package gov.state.cst.bpel_analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author BaydushLC
 *
 */
public class JDeveloperProject {
    private Path projectpath;
    private Document document;

    @SuppressWarnings("unused")
	private JDeveloperProject()
    {
    }
    
    public JDeveloperProject( Path projectpath ) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
        this.projectpath = projectpath;
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.document = parser.parse( new FileInputStream( projectpath.toFile() ) );
    }
    
    public String getName()
    {
    	return projectpath.getFileName().toString();
    }
    
    public boolean hasComposite()
    {
    	return Files.exists( getCompositePath(), LinkOption.NOFOLLOW_LINKS );
    }
    
    public Path getCompositePath()
    {
    	return Paths.get( projectpath.getParent().toString(), "composite.xml" );
    }

    public ArrayList<String> getBPELFiles() throws XPathExpressionException {
        ArrayList<String> result = new ArrayList<String>();
        
        // Evaluate the XPath expression against the Document
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile( "/project/url['_bpelFileURL' = substring( @n, string-length(@n) - string-length('_bpelFileURL') + 1 )]/@path" );
        NodeList nodes = (NodeList)expr.evaluate( document, XPathConstants.NODESET );
        for( int i=0; i < nodes.getLength(); i++ ) {
            result.add( nodes.item( i ).getNodeValue() );
        }
        return result;
    }

}
