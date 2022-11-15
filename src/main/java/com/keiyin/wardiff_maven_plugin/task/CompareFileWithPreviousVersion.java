package com.keiyin.wardiff_maven_plugin.task;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.keiyin.wardiff_maven_plugin.diff.generator.withprevious.GenerateHtmlFileWithPreviousVersion;
import com.keiyin.wardiff_maven_plugin.model.CompareFile;
import com.keiyin.wardiff_maven_plugin.model.PathSet;
import com.keiyin.wardiff_maven_plugin.utils.DiffGeneratorUtils;

public class CompareFileWithPreviousVersion implements Task {
	private File localDirectory;
	private File dependencyDirectory;
	private File dependencyPreviosDirectory;
	private File targetDirectory;
	private CompareFile compareFile;
	private String previousVersion;
	private Log log;

	public CompareFileWithPreviousVersion(File localDirectory, File dependencyDirectory,
			File dependencyPreviosDirectory, File targetDirectory, CompareFile compareFiles, String previousVersion,
			Log log) {
		super();
		this.localDirectory = localDirectory;
		this.dependencyDirectory = dependencyDirectory;
		this.dependencyPreviosDirectory = dependencyPreviosDirectory;
		this.compareFile = compareFiles;
		this.targetDirectory = targetDirectory;
		this.previousVersion = previousVersion;
		this.log = log;
	}

	@Override
	public void performTask() throws MojoExecutionException {
		PathSet checkFile = DiffGeneratorUtils.getFilesToIncludes(dependencyDirectory, compareFile.getIncludes(),
				compareFile.getExcludes(), false);

		log.info("Comparing files with " + compareFile.getId() + " that differ from the previous version.");

		log.info("The following files are " + compareFile.getId()
				+ " war dependency (and also differ from previous version " + previousVersion
				+ ") that has been overwritten by the local");
		log.info("-------------------------------------------------------------------------------------------");

		// For the file path that need to be compared
		int index = 1;
		for (String fileName : checkFile.paths()) {
			final File fileDependency = new File(dependencyDirectory.getPath(), fileName);
			final File fileLocal = new File(localDirectory, fileName);
			final File fileDependencyPrevious = new File(dependencyPreviosDirectory, fileName);

			try {
				// Check whether the previous dependency version and current dependency is same
				if (!fileLocal.isDirectory() && !fileDependencyPrevious.isDirectory()
						&& !DiffGeneratorUtils.isTwoFileSame(fileDependency, fileDependencyPrevious)
						&& !DiffGeneratorUtils.isTwoFileSame(fileDependency, fileLocal)) {
					// Check whether the file is binary
					if (DiffGeneratorUtils.isBinaryFile(fileName)) {
						log.info(index + ". Binary file " + fileName + " differ.");
					} else {
						// If the is not binary file, then do comparison
						log.info(index + ". File " + fileName + " differ.");
						String targetFileName = fileName.replace("/", "_");

						GenerateHtmlFileWithPreviousVersion generateHtmlFileWithPreviousVersion = new GenerateHtmlFileWithPreviousVersion(
								fileLocal.getPath(), 
								fileDependency.getPath(), 
								fileDependencyPrevious.getPath(), 
								targetDirectory.getPath(), 
								targetFileName,
								fileLocal.getName() + "_local", 
								fileLocal.getName() + "_dependency", 
								fileLocal.getName() + "_dependency_previous_version");
						generateHtmlFileWithPreviousVersion.generateDiffHtmlFileWithPreviousVersion();
					}
				}
			} catch (IOException e) {
				log.error(e);
			}

		}

	}

	public File getRevisedDirectory() {
		return localDirectory;
	}

	public File getRevisedPreviosDirectory() {
		return dependencyPreviosDirectory;
	}

	public void setRevisedPreviosDirectory(File revisedPreviosDirectory) {
		this.dependencyPreviosDirectory = revisedPreviosDirectory;
	}

	public File getOriginalDirectory() {
		return dependencyDirectory;
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public CompareFile getCompareFile() {
		return compareFile;
	}

	public Log getLog() {
		return log;
	}

	public void setRevisedDirectory(File revisedDirectory) {
		this.localDirectory = revisedDirectory;
	}

	public void setOriginalDirectory(File originalDirectory) {
		this.dependencyDirectory = originalDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public void setCompareFile(CompareFile compareFile) {
		this.compareFile = compareFile;
	}

	public void setLog(Log log) {
		this.log = log;
	}

}
