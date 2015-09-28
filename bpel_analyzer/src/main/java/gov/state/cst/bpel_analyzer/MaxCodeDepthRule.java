package gov.state.cst.bpel_analyzer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class MaxCodeDepthRule extends RuleViolations {
    private BPELFile bpelFile;
    private int max_code_depth;

    public MaxCodeDepthRule() {
	}
	
    /**
	 * @return the max_code_depth
	 */
	public int getMax_code_depth() {
		return max_code_depth;
	}

	/**
	 * @param max_code_depth the max_code_depth to set
	 */
	public void setMax_code_depth(int max_code_depth) {
		this.max_code_depth = max_code_depth;
	}

	/**
     * Inner class provides DocumentHandler
     */
    class BPELContentHandler extends DefaultHandler {
        private int codeDepth;
        private Locator locator;
        private Collection<String> scopes;
    	
        public BPELContentHandler() {
        	codeDepth = 0;
        	locator = null;
    		scopes = new HashSet<String>();
    		scopes.add( "process" );
    		scopes.add( "if" );
    		scopes.add( "while" );
    		scopes.add( "switch" );
    		scopes.add( "repeatUntil" );
    		scopes.add( "forEach" );
    		scopes.add( "pick" );
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
         */
        @SuppressWarnings( "javadoc" )
        @Override
        public void setDocumentLocator( Locator rhs ) {
            locator = rhs;
        }

        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @SuppressWarnings( "javadoc" )
        @Override
        public void startElement( String nsURI, String localName, String rawName, Attributes attributes ) throws SAXException {
            if( scopes.contains( rawName ) ) {
            	if( ++codeDepth > max_code_depth ) {
	        		Violation v = new Violation();
	        		v.setViolationDescription( "Code depth of " + Integer.toString(codeDepth)  + " exceeds allowed limit of " + Integer.toString(max_code_depth) );
	        		ViolationLocation vl = new ViolationLocation( bpelFile.getPath().toString(), locator.getLineNumber(), locator.getColumnNumber() );
	        		v.setViolationLocation( vl );
	        		add( v );
	            }
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @SuppressWarnings( "javadoc" )
        @Override
        public void endElement(String uri, String localName, String qName) {
            if( scopes.contains( localName ) )
            {
            	codeDepth--;
            }
        }

        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @SuppressWarnings( "javadoc" )
        @Override
        public void characters( char[] ch, int start, int length ) {
            //if( person ) {
            //    System.out.println( "Person: " + new String( ch, start, length ) );
            //    person = false;
            //} else if( email ) {
            //    System.out.println( "Email: " + new String( ch, start, length ) );
            //    email = false;
            //}
        }
    }

	@Override
	public boolean evaluate( BPELFile bpelFile ) {
		this.bpelFile = bpelFile;
		this.clear();

        try {
        	XMLReader parser = XMLReaderFactory.createXMLReader();
        	parser.setContentHandler( new BPELContentHandler() );
            parser.parse( bpelFile.getPath().toString() );
        }
        catch( FileNotFoundException e ) {
        	add( new AppException( "BPEL file not found: " + bpelFile.getPath().toString(), e ) );
        } catch (IOException | SAXException e) {
        	add( new AppException( e ) );
		}

        return !hasProblems();
	}

}
