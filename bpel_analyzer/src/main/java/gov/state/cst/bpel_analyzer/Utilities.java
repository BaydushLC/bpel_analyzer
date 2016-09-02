package gov.state.cst.bpel_analyzer;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utilities {
	private static final Logger logger = LoggerFactory.getLogger( Utilities.class );
	static private int indentLevel = 0;
	static PrintStream originalStream = null;
	static PrintStream summaryStream = null;
	static PrintStream detailStream = null;
	static PrintStream activeStream = null;
	
	public static void setProperty( Class<RuleViolations> classDef, RuleViolations obj, String name, String value ) throws Exception {
		Exception failureException = null;
		try {
			setStringProperty( classDef, obj, name, value );
		}
		catch (Exception e) {
			failureException = e;
		}
		try {
			setIntProperty( classDef, obj, name, value );
			return;
		}
		catch (Exception e) {
			// swallow the exception
			logger.warn("", e);
		}

		String modifiedName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		try {
			setStringProperty( classDef, obj, modifiedName, value );
			return;
		}
		catch (Exception e) {
			// swallow the exception
			logger.warn("", e);
		}
		try {
			setIntProperty( classDef, obj, modifiedName, value );
			return;
		}
		catch (Exception e) {
			// swallow the exception
			logger.warn("", e);
		}

		throw failureException;
	}

	private static void setIntProperty(Class<RuleViolations> classDef, RuleViolations obj, String name, String value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] argTypes = { int.class };
		Method setter;
		setter = classDef.getMethod( name, argTypes );
		Object[] args = { Integer.parseInt( value ) };
		setter.invoke( obj, args );
	}

	private static void setStringProperty(Class<RuleViolations> classDef, RuleViolations obj, String name, String value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] argTypes = { String.class };
		Method setter = classDef.getMethod( name, argTypes );
		Object[] args = { value };
		setter.invoke( obj, args );
	}
	
	public static String StringifyAppException( AppException e ) {
	    String NEW_LINE = System.getProperty("line.separator");
    	StringBuilder result = new StringBuilder();
    	
	    for( String line : ExceptionUtils.getStackTrace( e ).split("\\r?\\n") ) {
	    	result.append( " " + line.replaceAll("[\n\r]", "") + NEW_LINE );
	    }
	    return result.toString();
	}
	
	public static int indent() {
		return ++indentLevel;
	}

	public static int outdent() {
		if( indentLevel > 0 ) {
			--indentLevel;
		}
		return indentLevel;
	}

	public static void print( String... lines ) {
		print( false, lines );
	}

	public static void println( String... lines ) {
		print( true, lines );
	}

	private static void print( boolean println, String[] lines ) {
		boolean firstLine = true;
		
		initializeStreams();
		if( activeStream == null )
			return;
		
		StringBuilder indent = new StringBuilder();
		for( int level = 0; level < indentLevel; level++ ) {
			indent.append( "  " );
		}
		
		for( String line : lines ) {
			String[] allLines = line.split( "\\r?\\n" );
			for( String printableLine : allLines ) {
				activeStream.print( indent.toString() );
				if( firstLine ) {
					firstLine = false;
					indent.append( "  " );
				}
				if( println ) {
					activeStream.println( printableLine.replaceAll("[\n\r]", "") );
				} else {
					activeStream.print( printableLine.replaceAll("[\n\r]", "") );
				}
			}
		}
	}
	
	public static void activateDetailStream() {
		activeStream = detailStream;
	}

	public static void activateSummaryStream() {
		activeStream = summaryStream;
	}

	public static void activateOriginalStream() {
		activeStream = originalStream;
	}

	private static void initializeStreams() {
		if (activeStream != null)
			return;
		originalStream = new PrintStream(System.out);
		summaryStream = null;
		detailStream = originalStream;
		activeStream = originalStream;
		CommandLine cl = Cli.getInstance().getCommandLine();
		if (cl.hasOption("s")) {
			String summaryFile = cl.getOptionValue( "s" );
			if( summaryFile==null ) {
				summaryStream = originalStream;
			} else {
				try {
					summaryStream = new PrintStream(summaryFile);
				} catch (FileNotFoundException e) {
					logger.error("", e);
					summaryStream = null;
				}
			}
		}
		if (cl.hasOption("nd")) {
			detailStream = null;
		} else if (cl.hasOption("d")) {
			String detailFile = cl.getOptionValue( "d" );
			if( detailFile!=null ) {
				try {
					detailStream = new PrintStream(detailFile);
				} catch (FileNotFoundException e) {
					logger.error("", e);
					detailStream = originalStream;
				}
			}
		}
	}
	
	public static List<Node> asList( NodeList n ) {
		return n.getLength()==0
				? Collections.<Node>emptyList()
				: new NodeListWrapper(n);
	}

	private static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
		private final NodeList list;
		
		NodeListWrapper(NodeList l) {
			list=l;
		}
		
		public Node get(int index) {
			return list.item(index);
		}
		
		public int size() {
			return list.getLength();
		}
	}

}
