/**
 * 
 */
package gov.state.cst.bpel_analyzer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BaydushLC
 *
 */
public class Settings {
	private static final Logger logger = LoggerFactory
			.getLogger(Settings.class);

    /**
     * Singleton instance of the object
     */
    private static Settings instance;
    private static String fileName = "bpel_analyzer.config";
    @SuppressWarnings( "javadoc" )
    private XMLConfiguration config;

    /** A private Constructor prevents any other class from instantiating. 
     * @throws AppException */
    private Settings() throws AppException {
    	try {
    		config = new XMLConfiguration( fileName );
        }
    	catch (ConfigurationException e) {
    		logger.error("", e);
            throw new AppException( e, true );
		}
    }

    /**
     * The Static initializer constructs the instance at class loading time;
     * this is to simulate a more involved construction process (it it were
     * really simple, you'd just use an initializer)
     */
    static {
        try {
            instance = new Settings();
        }
        catch( AppException e ) {
        	logger.error("", e);
            if( e.isFatal() )
            {
                System.exit( 1 );
            }
        }
    }

    /** Static 'instance' method 
     * @return singleton instance of the object*/
    public static XMLConfiguration getInstance() {
        return instance.config;
    }
    
    /**
     * Saves the properties to disk
     * @throws AppException 
     */
    public void Save() throws AppException {
    	try {
			config.save( fileName );
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AppException( e );
		}
    }
}
