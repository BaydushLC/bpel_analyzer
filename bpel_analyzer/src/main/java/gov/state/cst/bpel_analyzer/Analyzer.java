package gov.state.cst.bpel_analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Hello world!
 *
 */
public class Analyzer {
    private static final Logger logger = LoggerFactory.getLogger( Analyzer.class );

    @SuppressWarnings( "javadoc" )
    public static void main( String[] args ) throws AppException {
        logger.trace("Starting application");
        // Generate the selective list, with a one-use File object.
        
        CommandLine cli = new Cli( args ).parse();
        final ArrayList<Path> projectFiles = new ArrayList<Path>();
        if( cli.hasOption( "r" ) )
        {
	        try {
				Files.walkFileTree( Paths.get( cli.getArgs()[0] ), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile( Path file, java.nio.file.attribute.BasicFileAttributes attrs )
					{
						if( file.toString().endsWith( ".jpr" ) )
						{
							projectFiles.add( file );
						}
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult visitFileFailed( Path file, java.io.IOException e )
					{
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				logger.error( "Error recursing through target directory.", e );
			}
        }
        else
        {
        	for( String bpelPath : new java.io.File( args[0] ).list( new OnlyJPRFilter() ) )
        	{
        		projectFiles.add( Paths.get( bpelPath ) );
        	}
        	projectFiles.sort( null );
        }

        for( Path projectFile : projectFiles ) {
            processProjectFile( projectFile.getParent().toString(), projectFile.getFileName().toString() );
        }
        logger.trace("Ending application");
    }

	private static void processProjectFile(String basePath, String projectFile)
			throws AppException {
    	logger.info("Processing project file {}", projectFile );
    	Utilities.println( 0, "Project: " + projectFile );
		JDeveloperProject project = new JDeveloperProject( basePath, projectFile );
		try {
		    for( String bpelFile : project.getBPELFiles() ) {
		        processBpelFile(basePath, bpelFile);
		    }
		}
		catch( Exception e ) {
			logger.error( "Exception from processBpelFile()", e );
		    e.printStackTrace();
		    if( e instanceof AppException )
		    {
		    	throw (AppException)e;
		    }
		    throw new AppException( e, true );
		}
	}

	private static void processBpelFile(String basePath, String bpelFile) throws Exception {
		Utilities.println( 0, "BPEL: " + bpelFile );
		
		String bpelPath = Paths.get( basePath, bpelFile ).toString();
		File f = new File( bpelPath );
		if( !f.exists() || f.isDirectory()) {
			AppException e = new AppException( bpelPath + " (The system cannot find the file specified)" );
			Utilities.println( 1, Utilities.StringifyAppException( e ) );
			return;
		}
		
    	logger.info("Processing BPEL file {}", bpelFile );
		List<HierarchicalConfiguration> rules = Settings.getInstance().configurationsAt( "rules.rule" );
		for( HierarchicalConfiguration rule : rules ) {
			try {
				RuleViolations evaluator = GetEvaluator( rule );
				String description = rule.getString( "description" );
				if( evaluator != null ) {
					// invoke the evaluation
					evaluator.evaluate( bpelPath );
					evaluator.printOutput( 1, description );
				} else {
					throw new ClassNotFoundException( "Class '" + rule.getString( "analyzer" ) + "' does not implement the 'IEvaluateBpel' interface." );
				}
			}
			catch( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	private static RuleViolations GetEvaluator(HierarchicalConfiguration rule) throws Exception {
		String analyzer = rule.getString( "analyzer" );
		String jarPath = rule.getString( "analyzer[@jar]" );
		Class<RuleViolations> evaluatorClass = null;
		try {
			Class<?> clazz = null;
			if( jarPath != null ) {
				File theJarFile = new File( jarPath );
				if( !theJarFile.isFile() ) {
					throw new FileNotFoundException( "Invlaid 'jar' attribute value: " + theJarFile.toString() );
				}
				URL theJarUrl = theJarFile.toURI().toURL();
				URLClassLoader cl = URLClassLoader.newInstance( new URL[]{ theJarUrl } );
				clazz = cl.loadClass( analyzer );
			} else {
				clazz = Class.forName( analyzer );
			}
			if( RuleViolations.class.isAssignableFrom( clazz ) ) {
				@SuppressWarnings("unchecked")
				Class<RuleViolations> tmp = (Class<RuleViolations>)clazz;
				evaluatorClass = tmp;
			}
		}
		catch( ClassNotFoundException e ) {
			throw new AppException( "Unable to locate class " + analyzer + " from " + jarPath, e );
		}
		RuleViolations evaluator = evaluatorClass.newInstance();
		if( !(evaluator instanceof RuleViolations) ) {
			return null;
		}
		// set the parameters
		List<HierarchicalConfiguration> properties = rule.configurationsAt( "properties.property" );
		for( HierarchicalConfiguration property : properties ) {
			String propName = property.getString( "[@name]" );
			String propValue = property.getString( "[@value]" );
			
			Utilities.setProperty( evaluatorClass, evaluator, propName, propValue );
		}
		return evaluator;
	}

	@SuppressWarnings( "javadoc" )
    private static class OnlyJPRFilter implements FilenameFilter {

        /* (non-Javadoc)
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept( File dir, String s ) {
            if( s.endsWith( ".jpr" ) ) {
                return true;
            }
            // others: projects, ... ?
            return false;
        }

    }
}
