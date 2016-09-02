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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public class Analyzer {
	private static final Logger logger = LoggerFactory
			.getLogger(Analyzer.class);

	@SuppressWarnings("javadoc")
	public static void main(String[] args) throws AppException {
		logger.trace("Starting application");
		// Generate the selective list, with a one-use File object.
		if (!Cli.getInstance().initialize(args)) {
			return;
		}

		for (JDeveloperProject project : getProjectFiles(Cli.getInstance()
				.getCommandLine().getArgs()[0])) {
			Utilities.println("Processing project " + project.getName());
			Utilities.indent();

			if (project.hasComposite()) {
				BPELComposite composite;
				try {
					composite = new BPELComposite(project.getCompositePath());
					try {
						for (BPELFile bpel : composite.getBPELFiles()) {
							processBpelFile(bpel);
						}
					} catch (Exception e) {
						logger.error("Error calling processBpelFile()", e);
					}
				} catch (ParserConfigurationException | SAXException
						| IOException e) {
					logger.error("Error constructing BPELComposite object", e);
				}
			} else {
				Utilities.println(project.getName()
						+ " does not have a BPEL composite.");
			}

			Utilities.outdent();
		}
		logger.trace("Ending application");
	}

	private static ArrayList<JDeveloperProject> getProjectFiles(String basePath) {
		final ArrayList<JDeveloperProject> projectFiles = new ArrayList<JDeveloperProject>();
		CommandLine cli = Cli.getInstance().getCommandLine();
		if (cli.hasOption("r")) {
			try {
				Files.walkFileTree(Paths.get(cli.getArgs()[0]),
						new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(
									Path file,
									java.nio.file.attribute.BasicFileAttributes attrs) {
								if (file.toString().endsWith(".jpr")) {
									try {
										projectFiles.add(new JDeveloperProject(
												file));
									} catch (ParserConfigurationException
											| SAXException | IOException e) {
										logger.error(
												"Error creating JDeveloperProject object",
												e);
									}
								}
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFileFailed(Path file,
									java.io.IOException e) {
								return FileVisitResult.CONTINUE;
							}
						});
			} catch (IOException e) {
				logger.error("Error recursing through target directory.", e);
			}
		} else {
			for (String jprPath : new java.io.File(basePath)
					.list(new OnlyJPRFilter())) {
				try {
					projectFiles.add(new JDeveloperProject(Paths.get(jprPath)));
				} catch (ParserConfigurationException | SAXException
						| IOException e) {
					logger.error("Error creating JDeveloperProject object", e);
				}
			}
			// can we sort Path objects? projectFiles.sort(null);
		}
		return projectFiles;
	}

	private static void processBpelFile(BPELFile bpel) throws Exception {
		Path bpelPath = bpel.getPath();
		Utilities.println("BPEL: " + bpelPath.getFileName());
		Utilities.indent();

		File f = new File(bpelPath.toString());
		if (!f.exists() || f.isDirectory()) {
			AppException e = new AppException(bpelPath
					+ " (The system cannot find the file specified)");
			Utilities.println(Utilities.StringifyAppException(e));
			Utilities.outdent();
			return;
		}

		logger.info("Processing BPEL file {}", bpelPath.getFileName());
		List<HierarchicalConfiguration> rules = Settings.getInstance()
				.configurationsAt("rules.rule");
		for (HierarchicalConfiguration rule : rules) {
			try {
				RuleViolations evaluator = GetEvaluator(rule);
				String description = rule.getString("description");
				if (evaluator != null) {
					// invoke the evaluation
					evaluator.evaluate(bpel);
					evaluator.printOutput(description);
				} else {
					throw new ClassNotFoundException(
							"Class '"
									+ rule.getString("analyzer")
									+ "' does not implement the 'IEvaluateBpel' interface.");
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				e.printStackTrace();
				throw e;
			}
		}
		Utilities.outdent();
	}

	private static RuleViolations GetEvaluator(HierarchicalConfiguration rule)
			throws Exception {
		String analyzer = rule.getString("analyzer");
		String jarPath = rule.getString("analyzer[@jar]");
		Class<RuleViolations> evaluatorClass = null;
		try {
			Class<?> clazz = null;
			if (jarPath != null) {
				File theJarFile = new File(jarPath);
				if (!theJarFile.isFile()) {
					throw new FileNotFoundException(
							"Invlaid 'jar' attribute value: "
									+ theJarFile.toString());
				}
				URL theJarUrl = theJarFile.toURI().toURL();
				try (URLClassLoader cl = URLClassLoader
						.newInstance(new URL[] { theJarUrl })) {
					clazz = cl.loadClass(analyzer);
				}
			} else {
				clazz = Class.forName(analyzer);
			}
			if (RuleViolations.class.isAssignableFrom(clazz)) {
				@SuppressWarnings("unchecked")
				Class<RuleViolations> tmp = (Class<RuleViolations>) clazz;
				evaluatorClass = tmp;
			}
		} catch (ClassNotFoundException e) {
			throw new AppException("Unable to locate class " + analyzer
					+ " from " + jarPath, e);
		}
		RuleViolations evaluator = evaluatorClass.newInstance();
		if (!(evaluator instanceof RuleViolations)) {
			return null;
		}
		// set the parameters
		List<HierarchicalConfiguration> properties = rule
				.configurationsAt("properties.property");
		for (HierarchicalConfiguration property : properties) {
			String propName = property.getString("[@name]");
			String propValue = property.getString("[@value]");

			Utilities.setProperty(evaluatorClass, evaluator, propName,
					propValue);
		}
		return evaluator;
	}

	@SuppressWarnings("javadoc")
	private static class OnlyJPRFilter implements FilenameFilter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File dir, String s) {
			if (s.endsWith(".jpr")) {
				return true;
			}
			// others: projects, ... ?
			return false;
		}

	}
}
