package gov.state.cst.bpel_analyzer;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MaxJavaSnippetLength extends RuleViolations {
    private int max_code_lines;
    private int countOfSnippets;

    public MaxJavaSnippetLength() {
    	this.countOfSnippets = 0;
	}
	
    /**
	 * @return the max_code_lines
	 */
	public int getMax_code_lines() {
		return max_code_lines;
	}

	/**
	 * @param max_code_lines the max_code_lines to set
	 */
	public void setMax_code_lines(int max_code_lines) {
		this.max_code_lines = max_code_lines;
	}

	@Override
	public boolean evaluate( BPELFile bpelFile ) {
		this.clear();

		try {
			File fXmlFile = bpelFile.getPath().toFile();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			XPathFactory xpathfactory = XPathFactory.newInstance();
	        XPath xpath = xpathfactory.newXPath();
	        
	        // do not use the namespace as we have not set dbFactory.setNamespaceAware(true);
	        XPathExpression expr = xpath.compile("//extensionActivity/exec[@language='java']");
	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList)result;
	        for (int i = 0; i < nodes.getLength(); i++) {
	        	Node node = nodes.item(i);
	        	String javaCode = node.getTextContent();
	        	this.countOfSnippets++;
	        	int code_lines = computeLinesOfCode( javaCode );
	        	if( code_lines > max_code_lines ) {
	        		String codeName = node.getAttributes().getNamedItem("name").getNodeValue();
            		Violation v = new Violation();
            		v.setViolationDescription( "Java fragment of " + Integer.toString(code_lines)  + " lines exceeds allowed limit of " + Integer.toString(max_code_lines) );
            		ViolationLocation vl = new ViolationLocation( bpelFile.getPath().toString(), "at bpelx:exec[@name='" + codeName + "']" );
            		v.setViolationLocation( vl );
            		add( v );
	        	}
	        }
		}
		catch( Exception e ) {
			if( e instanceof AppException ) {
				add( (AppException)e );
			} else {
				add( new AppException( e.getMessage(), e ) );
			}
		}
        return !hasProblems();
	}

	private int computeLinesOfCode(String javaCode) {
		// remove comment lines
		String manipulatedCode = javaCode.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1 " );
		// remove lines that have only a block character ({ or })
		manipulatedCode = manipulatedCode.replaceAll( "(?m)^[ \\t]*[\\{|\\}][ \\t]*$", "" );
		// remove empty lines
		manipulatedCode = manipulatedCode.replaceAll( "(?m)^[ \\t]*\\r?\\n", "" ).trim();
		return manipulatedCode.split( "\\r?\\n").length;
	}

	@Override
	public void printOutput( String description )
	{
		this.printSummary(String.format( "%s [%d Violations out of %d snippets]",
				description, getViolations().size(), this.countOfSnippets ));
		this.printDetail();
	}

}
