/**
 * 
 */
package gov.state.cst.bpel_analyzer;

/**
 * @author BaydushLC
 *
 */
public class AppException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean fatal;
    /**
     * 
     */
    public AppException() {
        this.fatal = true;
    }

    /**
     * @param arg0
     */
    public AppException( String arg0 ) {
        super( arg0 );
        this.fatal = true;
    }

    /**
     * @param arg0
     */
    public AppException( Throwable arg0 ) {
        super( arg0 );
        this.fatal = true;
    }

    /**
     * @param arg0
     * @param fatal
     */
    public AppException( Throwable arg0, boolean fatal ) {
        super( arg0 );
        this.fatal = fatal;
    }

    /**
     * @param arg0
     * @param arg1
     */
    public AppException( String arg0, Throwable arg1 ) {
        super( arg0, arg1 );
        this.fatal = true;
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public AppException( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super( arg0, arg1, arg2, arg3 );
        this.fatal = true;
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal( boolean fatal ) {
        this.fatal = fatal;
    }
    
}
