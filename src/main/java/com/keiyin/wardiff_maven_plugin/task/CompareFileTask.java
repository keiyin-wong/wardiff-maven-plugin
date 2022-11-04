package com.keiyin.wardiff_maven_plugin.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;

import com.keiyin.wardiff_maven_plugin.generator.BaseDiffGenerator;
import com.keiyin.wardiff_maven_plugin.generator.DiffGenerator;
import com.keiyin.wardiff_maven_plugin.generator.GenerateHtmlFile;
import com.keiyin.wardiff_maven_plugin.generator.GenerateHtmlFileSimple;
import com.keiyin.wardiff_maven_plugin.model.CompareFile;
import com.keiyin.wardiff_maven_plugin.model.PathSet;

public class CompareFileTask implements Task {

	private File workDirectory;
	private File webappDirectory;
	private File targetDirectory;
	private CompareFile compareFiles;
	private Log log;

	public CompareFileTask(File workDirectory, File webappDirectory, File targetDirectory, CompareFile compareFiles,
			Log log) {
		super();
		this.workDirectory = workDirectory;
		this.webappDirectory = webappDirectory;
		this.compareFiles = compareFiles;
		this.targetDirectory = targetDirectory;
		this.log = log;
	}

	public void performTask() throws MojoExecutionException {

		// Get the file path (with the file name) that you want to compare
		PathSet checkFile = getFilesToIncludes(new File(workDirectory.getPath(), compareFiles.getFilePath()),
				compareFiles.getIncludes(), compareFiles.getExcludes(), false);
		
		// Where is the extracted war file? 
		// Normally the war plugin will extract it to the target/war/work/{groupId}/{artifactId}
		File workDirectoryPathWithDepd = new File(workDirectory.getPath(), compareFiles.getFilePath());
		log.info("The following files are not identical with the war dependOency " + compareFiles.getId());
		log.info("-------------------------------------------------------------------------------------------");
		int index = 1;
		
		// For loop the PathSet
		for (String fileToCheckName : checkFile.paths()) {
			// The local project file
			final File fileLocal = new File(webappDirectory.getPath(), fileToCheckName);
			// The extracted war dependency file
			final File fileDepd = new File(workDirectoryPathWithDepd, fileToCheckName);

			// Check if both files exist, if exists then do comparison
			if (fileLocal.exists() && !fileLocal.isDirectory() && fileDepd.exists() && !fileDepd.isDirectory()) {
				try {
					// If both files are not same, then do comparison and output to the HTML file
					if (!isTwoFileSame(fileLocal, fileDepd)) {
						// Check if the files is binary
						if (fileToCheckName.endsWith(".pdf") || fileToCheckName.endsWith(".png")
								|| fileToCheckName.endsWith(".mp4") || fileToCheckName.endsWith(".gif")
								|| fileToCheckName.endsWith(".zip") || fileToCheckName.endsWith(".jpg")) {
							log.info(index + ". Binary file " + fileToCheckName + " differ.");
						} else {

							log.info(index + ". File " + fileToCheckName + " differ.");
							String fileName = fileToCheckName.replace("/", "_") + ".html";
							File targetFilePath =new File(new File(targetDirectory, "differ"), compareFiles.getId());
							
							// Decorator pattern
							DiffGenerator diffGenerator = new GenerateHtmlFile(new GenerateHtmlFileSimple(new BaseDiffGenerator()));
							
							diffGenerator.generate(
									getClass().getClassLoader(), 
									fileLocal.getPath(), 
									fileDepd.getPath(), 
									targetFilePath.getPath(), 
									fileName
							);
						}
						index++;
					}
				} catch (IOException e) {
					log.error(e);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		log.info("");
		log.info("Generated diff file in the " + targetDirectory.getPath() + "\\differ\\" + compareFiles.getId());
	}

	
	/**
	 * Returns the file to copy. If the includes are {@code null} or empty, the
	 * default includes are used.
	 *
	 * @param baseDir            the base directory to start from
	 * @param includes           the includes
	 * @param excludes           the excludes
	 * @param includeDirectories include directories yes or not.
	 * @return the files to copy
	 */
	// CHECKSTYLE_OFF: LineLength
	public PathSet getFilesToIncludes(File baseDir, String[] includes, String[] excludes, boolean includeDirectories)
	// CHECKSTYLE_ON: LineLength
	{
		final DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(baseDir);

		if (excludes != null) {
			scanner.setExcludes(excludes);
		}
		scanner.addDefaultExcludes();

		if (includes != null && includes.length > 0) {
			scanner.setIncludes(includes);
		} else {
			// String[] defaultIncludes = { "**/**" };
			// scanner.setIncludes(defaultIncludes);
		}

		scanner.scan();

		PathSet pathSet = new PathSet(scanner.getIncludedFiles());

		if (includeDirectories) {
			pathSet.addAll(scanner.getIncludedDirectories());
		}

		return pathSet;
	}

	public boolean isTwoFileSame(File file1, File file2) throws IOException {
		return FileUtils.contentEquals(file1, file2);
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

}
