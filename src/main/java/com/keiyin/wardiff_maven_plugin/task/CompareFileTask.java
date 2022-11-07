package com.keiyin.wardiff_maven_plugin.task;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.keiyin.wardiff_maven_plugin.generator.BaseDiffGenerator;
import com.keiyin.wardiff_maven_plugin.generator.DiffGenerator;
import com.keiyin.wardiff_maven_plugin.generator.DiffGeneratorUtils;
import com.keiyin.wardiff_maven_plugin.generator.GenerateHtmlFile;
import com.keiyin.wardiff_maven_plugin.generator.GenerateHtmlFileSimple;
import com.keiyin.wardiff_maven_plugin.generator.GenerateUnifiedFile;
import com.keiyin.wardiff_maven_plugin.model.CompareFile;
import com.keiyin.wardiff_maven_plugin.model.PathSet;

public class CompareFileTask implements Task {

	private File revisedDirectory;
	private File originalDirectory;
	private File targetDirectory;
	private CompareFile compareFile;
	private Log log;

	public CompareFileTask(File originalDirectory, File revisedDirectory, File targetDirectory,
			CompareFile compareFiles, Log log) {
		super();
		this.revisedDirectory = revisedDirectory;
		this.originalDirectory = originalDirectory;
		this.compareFile = compareFiles;
		this.targetDirectory = targetDirectory;
		this.log = log;
	}

	public void performTask() throws MojoExecutionException {

		// Get the file path (with the file name) that you want to compare
		PathSet checkFile = DiffGeneratorUtils.getFilesToIncludes(revisedDirectory, compareFile.getIncludes(),
				compareFile.getExcludes(), false);

		// Where is the extracted war file?
		// Normally the war plugin will extract it to the
		// target/war/work/{groupId}/{artifactId}
		log.info("The following files are " + compareFile.getId()
				+ "war dependency that has been overwritten by the local");
		log.info("-------------------------------------------------------------------------------------------");
		int index = 1;

		// For loop the PathSet
		for (String fileToCheckName : checkFile.paths()) {
			// The local project file
			final File fileLocal = new File(originalDirectory.getPath(), fileToCheckName);
			// The extracted war dependency file
			final File fileDepd = new File(revisedDirectory, fileToCheckName);

			// Check if both files exist, if exists then do comparison
			if (!fileLocal.isDirectory() && !fileDepd.isDirectory()) {
				try {
					// If both files are not same, then do comparison and output to the HTML file
					if (!DiffGeneratorUtils.isTwoFileSame(fileLocal, fileDepd)) {
						// Check if the files is binary
						if (DiffGeneratorUtils.isBinaryFile(fileToCheckName)) {
							log.info(index + ". Binary file " + fileToCheckName + " differ.");
						} else {

							log.info(index + ". File " + fileToCheckName + " differ.");
							String fileName = fileToCheckName.replace("/", "_");

							// Decorator pattern
							DiffGenerator diffGenerator = new GenerateUnifiedFile(
									new GenerateHtmlFile(new GenerateHtmlFileSimple(new BaseDiffGenerator())));

							diffGenerator.generate(getClass().getClassLoader(), fileLocal.getPath(), fileDepd.getPath(),
									targetDirectory.getPath(), fileName);
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
		log.info("Generating diff file to [" + targetDirectory.getPath() + "\\differ\\" + compareFile.getId() + "]");
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

}
