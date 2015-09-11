package gov.state.cst.bpel_analyzer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.exception.ExceptionUtils;

public class Utilities {
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
		catch (Exception e) {} 		// swallow the exception

		String modifiedName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		try {
			setStringProperty( classDef, obj, modifiedName, value );
			return;
		}
		catch (Exception e) {} 		// swallow the exception
		try {
			setIntProperty( classDef, obj, modifiedName, value );
			return;
		}
		catch (Exception e) {} 		// swallow the exception

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
	
	public static void print( int indentLevel, String... lines ) {
		print( indentLevel, false, lines );
	}

	public static void println( int indentLevel, String... lines ) {
		print( indentLevel, true, lines );
	}

	private static void print( int indentLevel, boolean println, String[] lines ) {
		StringBuilder indent = new StringBuilder();
		for( int level = 0; level < indentLevel; level++ ) {
			indent.append( "  " );
		}
		
		for( String line : lines ) {
			String[] allLines = line.split( "\\r?\\n" );
			for( String printableLine : allLines ) {
				System.out.print( indent.toString() );
				if( println ) {
					System.out.println( printableLine.replaceAll("[\n\r]", "") );
				} else {
					System.out.print( printableLine.replaceAll("[\n\r]", "") );
				}
			}
		}
	}

}
