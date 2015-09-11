package gov.state.cst.bpel_analyzer;

public class Violation {
	private String violationDescription;
	private ViolationLocation violationLocation;

	public Violation() {
		violationDescription = null;
		violationLocation = new ViolationLocation();
	}
	
	@Override
	public String toString() {
	    StringBuilder result = new StringBuilder();

	    if( violationDescription != null ) {
	    	result.append( violationDescription + System.getProperty("line.separator") );
	    }
	    result.append( violationLocation.toString() );

	    return result.toString();
	  }
	
	/**
	 * @return the violationLocation
	 */
	public ViolationLocation getViolationLocation() {
		return violationLocation;
	}

	/**
	 * @param violationLocation the violationLocation to set
	 */
	public void setViolationLocation(ViolationLocation violationLocation) {
		this.violationLocation = violationLocation;
	}

	/**
	 * @return the violationDescription
	 */
	public String getViolationDescription() {
		return violationDescription;
	}

	/**
	 * @param violationDescription the violationDescription to set
	 */
	public void setViolationDescription(String violationDescription) {
		this.violationDescription = violationDescription;
	}
	
}
