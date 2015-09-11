/**
 * 
 */
package gov.state.cst.bpel_analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private String basePath;
    private String projectFile;

    public JDeveloperProject( String basePath, String projectFile ) {
        this.basePath = basePath;
        this.projectFile = projectFile;
    }

    public ArrayList<String> getBPELFiles() throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
        ArrayList<String> result = new ArrayList<String>();
        
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse( new FileInputStream( Paths.get( basePath, projectFile ).toString() ) );
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
