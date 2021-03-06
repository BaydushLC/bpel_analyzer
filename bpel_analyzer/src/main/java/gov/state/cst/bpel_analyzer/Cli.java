package gov.state.cst.bpel_analyzer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cli {
    private static final Logger logger = LoggerFactory.getLogger( Cli.class );
    private static Cli instance;
    private Options options = null;
    private CommandLine cmd = null;
    
    /**
     * Exists to prevent instantiation of singleton object
     */
    private Cli()
    {
    }
    
	/**
	 * Gets the single instance of Cli.
	 *
	 * @return single instance of Cli
	 */
	public synchronized static Cli getInstance() {
		if (instance == null) {
			instance = new Cli();
			logger.info("created singleton: {}", instance);
		}
		return instance;
	}
    
    /**
     * Initialize.
     *
     * @param args the args
     * @return true, if successful
     */
    public boolean initialize( String[] args )
    {
    	options = new Options();
    	options.addOption( "h", "help", false, "Show help." );
    	options.addOption( "r", "recurse", false, "Recurse into sub-directories for jDeveloper project files." );
    	options.addOption( Option.builder("s")
    			.longOpt("summary")
    			.required(false)
    			.optionalArg(true)
    			.argName("summaryFilename")
    			.type(String.class)
                .desc( "Output summary data to STDOUT or filename if given.  Defaults to no summary output." )
                .build() );
    	options.addOption( "nd", "noDetail", false, "Suppress detail output result.  Mutually exclusive with 'd' option.");
    	options.addOption( Option.builder("d")
    			.longOpt("detail")
    			.required(false)
    			.optionalArg(true)
    			.argName("detailFilename")
    			.type(String.class)
                .desc( "Output detail data to STDOUT or filename if given.  Defaults to STDOUT." )
                .build() );
    	
    	CommandLineParser parser = new DefaultParser();
    	
    	cmd = null;
    	try
    	{
    		cmd = parser.parse( options, args );
    		
    		if( cmd.hasOption( "h" ) )
    		{
    			help();
    			return false;
    		}
    		
    		if( cmd.hasOption("d") && cmd.hasOption("nd") ) {
    			logger.error("Mutually exclusive switches specified.");
    			help();
    			return false;
    		}
    		
    		if( cmd.getArgs().length != 1 ) {
    			logger.error("Required path argument missing!");
    			help();
    			return false;
    		}
    	}
    	catch( ParseException e )
    	{
    		logger.error( "Failed to parse command line properties.", e );
    		return false;
    	}
    	return true;
    }
    
    public CommandLine getCommandLine()
    {
    	if( cmd == null) {
    		logger.error( "Attempt to retrieve CommandLine object before intialization.");
    	}
    	return cmd;
    }
    
    private void help()
    {
    	HelpFormatter formatter = new HelpFormatter();
    	
    	formatter.printHelp( "bpel_analyzer [OPTION]... {DIRECTORY}",
    			"Scan directories for jpr files and analyze BPELs referenced therein." + System.lineSeparator() + System.lineSeparator(),
    			options,
    			"{DIRECTORY}     Directory path to look for jDeveloper projects in." );
    }
}
