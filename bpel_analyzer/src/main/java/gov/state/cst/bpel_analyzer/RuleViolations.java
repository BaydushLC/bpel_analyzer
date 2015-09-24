package gov.state.cst.bpel_analyzer;

import java.util.ArrayList;

public abstract class RuleViolations {
	private ArrayList<Violation> violations;
	private ArrayList<AppException> exceptions;
	
	public RuleViolations() {
		this.violations = new ArrayList<Violation>();
		this.exceptions = new ArrayList<AppException>();
	}
	
	public abstract boolean evaluate( String bpelPath );
	
	public void clear() {
		if( this.violations.size() != 0 )
		{
			this.violations = new ArrayList<Violation>();
		}
		if( this.exceptions.size() != 0 )
		{
			this.exceptions = new ArrayList<AppException>();
		}
	}

	public boolean hasViolations()
	{
		return !this.violations.isEmpty();
	}
	
	public boolean hasExceptions()
	{
		return !this.exceptions.isEmpty();
	}
	
	public ArrayList<Violation> getViolations() {
		return violations;
	}

	public ArrayList<AppException> getExceptions() {
		return exceptions;
	}
	
	public void add( Violation violation ) {
		violations.add( violation );
	}
	
	public void add( AppException exception ) {
		exceptions.add( exception );
	}
	
	public boolean hasProblems()
	{
		return( this.hasExceptions() | this.hasViolations() );
	}
	
	public void printOutput( String description )
	{
		Utilities.println( description + " [" + Integer.toString( violations.size() ) + " Violations]");
		Utilities.indent();
		printExceptions();
		printViolations();
		Utilities.outdent();
	}

	public void printViolations() {
		for( Violation v : violations ) {
			Utilities.println( v.toString() );
		}
	}

	public void printExceptions() {
		if( exceptions.size() > 0 ) {
			Utilities.println( "---v" );
			for( AppException e : exceptions ) {
				Utilities.println( Utilities.StringifyAppException( e ) );
			}
			Utilities.println( "  ---^" );
		}
	}
}
