package gov.state.cst.bpel_analyzer;

public class ViolationLocation {
	public String file;
	public String filePosition;
	
	public ViolationLocation() {
		file = null;
		filePosition = null;
	}
	
	public ViolationLocation( String file ) {
		this.file = file;
		filePosition = null;
	}

	public ViolationLocation( String file, String filePosition ) {
		this.file = file;
		this.filePosition = filePosition;
	}

	public ViolationLocation( String file, int line, int position ) {
		this.file = file;
		this.filePosition = String.format( "Line: " + Integer.toString( line ) + " Position: " + Integer.toString(position) );
	}
	
	@Override
	public String toString() {
	    StringBuilder result = new StringBuilder();
	    String NEW_LINE = System.getProperty("line.separator");

	    result.append( "Filename: " + file + NEW_LINE );
	    result.append( "  " + filePosition + NEW_LINE );

	    return result.toString();
	}

}
