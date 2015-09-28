/**
 * 
 */
package gov.state.cst.bpel_analyzer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author BaydushLC
 *
 */
public class MaxVariablesRule extends RuleViolations {
    private BPELFile bpelFile = null;
    private int max_variables =0;
	private boolean shouldDehydrate = true;

    public MaxVariablesRule() {
    }

    /**
	 * @return the max_variables
	 */
	public int getMax_variables() {
		return max_variables;
	}

	/**
	 * @param max_variables the max_variables to set
	 */
	public void setMax_variables(int max_variables) {
		this.max_variables = max_variables;
	}

	/**
     * Inner class provides DocumentHandler
     */
    class BPELContentHandler extends DefaultHandler {
        private Stack<Integer> level;
        private Locator locator;
        private Collection<String> scopes;
        private Collection<String> dehydrationPoints;
    	
        public BPELContentHandler() {
        	level = new Stack<Integer>();
        	locator = null;
        	
        	// all scopes should be lowercase
    		scopes = new HashSet<String>();
    		scopes.add( "process" );
    		scopes.add( "scope" );
    		
        	// all dehydrationPoints should be lowercase
    		dehydrationPoints = new HashSet<String>();
    		dehydrationPoints.add( "wait" );
    		dehydrationPoints.add( "receive" );
    		dehydrationPoints.add( "onmessage" );
    		dehydrationPoints.add( "onalarm" );
    		dehydrationPoints.add( "dehydrate" );
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
        	localName = localName.toLowerCase();
        	// wait, receive, onMessage, and onAlarm or a call to Async WSDL API force dehydration
        	if( dehydrationPoints.contains( localName ) && !shouldDehydrate ) {
        		Violation v = new Violation();
        		v.setViolationDescription( "BPEL contains " + rawName + " step that forces dehydration but conflicts with composite InMemoryOptimization property!" );
        		ViolationLocation vl = new ViolationLocation( bpelFile.getPath().toString(), locator.getLineNumber(), locator.getColumnNumber() );
        		v.setViolationLocation( vl );
        		add( v );
        		shouldDehydrate = true;
        	}
            if( scopes.contains( localName ) ) {
            	if( level.size() == 0 ) {
            		level.push( new Integer( 0 ) );
            	} else {
            		level.push( new Integer(level.peek()));
            	}
            }
            else if( localName.equals( "variable" ) ) {
            	level.set(level.size()-1, new Integer( level.peek().intValue() + 1 ) );
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @SuppressWarnings( "javadoc" )
        @Override
        public void endElement(String uri, String localName, String qName) {
            if( scopes.contains( localName.toLowerCase() ) )
            {
                int varCount = level.pop().intValue();
            	if( varCount > max_variables && shouldDehydrate )
            	{
            		Violation v = new Violation();
            		v.setViolationDescription( "Variable count of " + Integer.toString(varCount)  + " exceeds allowed limit of " + Integer.toString(max_variables) );
            		ViolationLocation vl = new ViolationLocation( bpelFile.getPath().toString(), locator.getLineNumber(), locator.getColumnNumber() );
            		v.setViolationLocation( vl );
            		add( v );
            	}
                
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
		
		// determine if this BPEL has flags set to not dehydrate
		try {
			if( bpelFile.property( "bpel.config.InMemoryOptimization", new Boolean( false ) ).booleanValue()
					|| bpelFile.property( "bepl.config.completionPersistPolicy", new Boolean( false ) ).booleanValue() )
			{
				shouldDehydrate = false;
			}
		} catch (IllegalConversionException e1) {
			add( new AppException( "BPEL file not found: " + bpelFile.getPath().toString(), e1 ) );
			return !hasProblems();
		}

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
