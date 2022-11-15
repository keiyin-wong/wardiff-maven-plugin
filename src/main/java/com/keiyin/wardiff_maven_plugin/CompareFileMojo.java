package com.keiyin.wardiff_maven_plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

import com.keiyin.wardiff_maven_plugin.model.CompareFile;
import com.keiyin.wardiff_maven_plugin.task.CompareFileTask;
import com.keiyin.wardiff_maven_plugin.task.Task;

@Mojo(name = "compareFile", defaultPhase = LifecyclePhase.COMPILE)
public class CompareFileMojo extends AbstractMojo {
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter
	private List<CompareFile> compareFiles = new ArrayList<>();

	@Parameter(defaultValue = "${project.build.directory}/war/work", required = true)
	private File workDirectory;

	@Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
	private File webappDirectory;

	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File targetDirectory;

	@Parameter(defaultValue = "${project.basedir}", required = true)
	private File homeDirectory;

	@Parameter(defaultValue = "${settings.localRepository}")
	private File localMavenRepo;

	@Parameter
	private String compareWithPreviousVersion;

	@Component(role = ArchiverManager.class)
	private ArchiverManager archiverManager;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		for (CompareFile compareFile : compareFiles) {
			Task task = new CompareFileTask(new File(workDirectory, compareFile.getFilePath()), webappDirectory, 
					new File(new File(targetDirectory, "differ"), "local_overwrites_" + compareFile.getId()),
					compareFile, getLog());
			task.performTask();
		}
	}


}
