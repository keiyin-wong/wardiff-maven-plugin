package com.keiyin.wardiff_maven_plugin.task;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.keiyin.wardiff_maven_plugin.generator.BaseDiffGenerator;
import com.keiyin.wardiff_maven_plugin.generator.DiffGenerator;
import com.keiyin.wardiff_maven_plugin.generator.DiffGeneratorUtils;
import com.keiyin.wardiff_maven_plugin.generator.GenerateHtmlFileWithPreviousVersion;
import com.keiyin.wardiff_maven_plugin.model.CompareFile;
import com.keiyin.wardiff_maven_plugin.model.PathSet;

public class CompareFileWithPreviousVersion implements Task {
	private File originalDirectory;
	private File revisedDirectory;
	private File revisedPreviosDirectory;
	private File targetDirectory;
	private CompareFile compareFile;
	private String previousVersion;
	private Log log;

	public CompareFileWithPreviousVersion(File originalDirectory, File revisedDirectory, File revisedPreviosDirectory,
			File targetDirectory, CompareFile compareFiles, String previousVersion, Log log) {
		super();
		this.revisedDirectory = revisedDirectory;
		this.originalDirectory = originalDirectory;
		this.revisedPreviosDirectory = revisedPreviosDirectory;
		this.compareFile = compareFiles;
		this.targetDirectory = targetDirectory;
		this.previousVersion = previousVersion;
		this.log = log;
	}

	@Override
	public void performTask() throws MojoExecutionException {
		PathSet checkFile = DiffGeneratorUtils.getFilesToIncludes(revisedDirectory, compareFile.getIncludes(),
				compareFile.getExcludes(), false);

		log.info("Comparing files with " + compareFile.getId() + " that differ from the previous version.");

		log.info("The following files are " + compareFile.getId()
				+ " war dependency (and also differ from previous version " + previousVersion
				+ " ) that has been overwritten by the local");
		log.info("-------------------------------------------------------------------------------------------");

		int index = 1;
		for (String fileName : checkFile.paths()) {
			// The local project file
			final File fileLocal = new File(originalDirectory.getPath(), fileName);
			// The extracted war dependency file
			final File fileRevised = new File(revisedDirectory, fileName);

			final File fileRevisedPrevious = new File(revisedPreviosDirectory, fileName);

			// Check the revised and revisedPrevious file that is different
			// And the local and revised file that is different
			try {
				if (!fileRevised.isDirectory() && !fileRevisedPrevious.isDirectory()
						&& !DiffGeneratorUtils.isTwoFileSame(fileRevised, fileRevisedPrevious)
						&& !DiffGeneratorUtils.isTwoFileSame(fileLocal, fileRevised)) {
					if (DiffGeneratorUtils.isBinaryFile(fileName)) {
						log.info(index + ". Binary file " + fileName + " differ.");
					} else {
						log.info(index + ". File " + fileName + " differ.");
						String targetFileName = fileName.replace("/", "_");

						DiffGenerator diffGenerator = new BaseDiffGenerator();

						// Add a fileRevisedPreviousaPath to the Diff generator
						diffGenerator = new GenerateHtmlFileWithPreviousVersion(diffGenerator,
								fileRevisedPrevious.getPath());
						diffGenerator.generate(getClass().getClassLoader(), fileLocal.getPath(), fileRevised.getPath(),
								targetDirectory.getPath(), targetFileName);

					}
				}
			} catch (IOException e) {
				log.error(e);
			}

		}

	}

	public File getRevisedDirectory() {
		return revisedDirectory;
	}

	public File getRevisedPreviosDirectory() {
		return revisedPreviosDirectory;
	}

	public void setRevisedPreviosDirectory(File revisedPreviosDirectory) {
		this.revisedPreviosDirectory = revisedPreviosDirectory;
	}

	public File getOriginalDirectory() {
		return originalDirectory;
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
		this.revisedDirectory = revisedDirectory;
	}

	public void setOriginalDirectory(File originalDirectory) {
		this.originalDirectory = originalDirectory;
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
