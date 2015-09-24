package gov.state.cst.bpel_analyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

public class BPELComposite {
	private Path compositePath;
	private Document document;
	
	@SuppressWarnings("unused")
	private BPELComposite() {
	}
	
	public BPELComposite(Path compositePath) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
		this.compositePath = compositePath;
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.document = parser.parse( new FileInputStream( this.compositePath.toFile() ) );
	}

	public ArrayList<BPELFile> getBPELFiles() throws XPathExpressionException {
        ArrayList<BPELFile> result = new ArrayList<BPELFile>();
        
        // Evaluate the XPath expression against the Document
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile( "/composite/component/implementation.bpel/@src" );
        NodeList nodes = (NodeList)expr.evaluate( this.document, XPathConstants.NODESET );
        String basePath = compositePath.getParent().toString();
        for( int i=0; i < nodes.getLength(); i++ ) {
            result.add( new BPELFile( Paths.get( basePath, nodes.item( i ).getNodeValue() ) ) );
        }
        return result;
	}

}
