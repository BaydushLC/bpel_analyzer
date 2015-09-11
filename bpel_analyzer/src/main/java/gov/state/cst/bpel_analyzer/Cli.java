package gov.state.cst.bpel_analyzer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cli {
    private static final Logger logger = LoggerFactory.getLogger( Cli.class );
    private String[] args = null;
    private Options options = new Options();
    private CommandLine cmd = null;
    
    @SuppressWarnings("unused")
	private Cli()
    {
    }
    
    public Cli( String[] args )
    {
    	this.args = args;
    	
    	options.addOption( "h", "help", false, "show help." );
    	options.addOption( "r", "recurse", false, "recurse into sub-directories for jDeveloper project files." );
    	
    }
    
    public CommandLine parse()
    {
    	CommandLineParser parser = new DefaultParser();
    	
    	cmd = null;
    	try
    	{
    		cmd = parser.parse( options, args );
    		
    		if( cmd.hasOption( "h" ) )
    		{
    			help();
    		}
    		
    		if( cmd.getArgs().length != 1 ) {
    			System.err.println( "Required path argument missing!" );
    			help();
    		}
    		
    		return cmd;
    	}
    	catch( ParseException e )
    	{
    		logger.error( "Failed to parse command line properties.", e );
    	}
    	return null;
    }
    
    public CommandLine getCommandLine()
    {
    	return cmd;
    }
    
    private void help()
    {
    	HelpFormatter formatter = new HelpFormatter();
    	
    	formatter.printHelp( "bpel_analyzer [OPTION]... {DIRECTORY}",
    			"Scan directories for jpr files and analyze BPELs referenced therein." + System.lineSeparator() + System.lineSeparator(),
    			options,
    			"{DIRECTORY}     Directory path to look for jDeveloper projects in." );
    	System.exit( 0 );
    }
}
