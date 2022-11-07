package com.keiyin.wardiff_maven_plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import com.keiyin.wardiff_maven_plugin.model.CompareFile;
import com.keiyin.wardiff_maven_plugin.model.InvalidCompareFileConfigurationException;
import com.keiyin.wardiff_maven_plugin.task.CompareFileWithPreviousVersion;
import com.keiyin.wardiff_maven_plugin.task.Task;

@Mojo(name = "withPreviousVersion", defaultPhase = LifecyclePhase.COMPILE)
public class CompareFileWithPreviousMojo extends AbstractMojo {
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
			if (compareWithPreviousVersion != null) {
				test(compareFile);
				Task task = new CompareFileWithPreviousVersion(webappDirectory,
						new File(workDirectory, compareFile.getFilePath()),
						new File(workDirectory, compareFile.getFilePath() + "-" + compareWithPreviousVersion),
						new File(new File(targetDirectory, "differ"),
								"local_vs_" + compareFile.getId() + "_vs_" + compareWithPreviousVersion),
						compareFile, compareWithPreviousVersion, getLog());
				task.performTask();
			} else {
				throw new InvalidCompareFileConfigurationException(
						"Please provide <compareWithPreviousVersion> in the <configuration>");
			}
		}
	}

	public void test(CompareFile compareFile) throws MojoExecutionException {
		try {
			final Artifact artifact = getAssociatedArtifact(compareFile);
			compareFile.setArtifact(artifact);

			File previousVersionWarFile = new File(localMavenRepo,
					compareFile.getGroupId().replace(".", "/") + "/" + compareFile.getArtifactId() + "/"
							+ compareWithPreviousVersion + "/" + compareFile.getArtifactId() + "-"
							+ compareWithPreviousVersion + ".war");

			File unpackDirectory = new File(workDirectory,
					compareFile.getGroupId() + "/" + compareFile.getArtifactId() + "-" + compareWithPreviousVersion);

			createDirectoryIfNotExists(unpackDirectory);
			try {
				UnArchiver unArchiver = archiverManager.getUnArchiver("war");
				unArchiver.setSourceFile(previousVersionWarFile);
				unArchiver.setDestDirectory(unpackDirectory);
				unArchiver.setOverwrite(true);
				unArchiver.extract();
			} catch (ArchiverException e) {
				throw new MojoExecutionException("Error unpacking file [" + previousVersionWarFile.getAbsolutePath()
						+ "]" + " to [" + unpackDirectory.getAbsolutePath() + "]", e);
			} catch (NoSuchArchiverException e) {
				getLog().warn("Skip unpacking dependency file [" + previousVersionWarFile.getAbsolutePath()
						+ " with unknown extension [" + "war" + "]");
			}

		} catch (InvalidCompareFileConfigurationException e) {
			e.printStackTrace();
		}
	}

	protected void createDirectoryIfNotExists(File file) {
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	Artifact getAssociatedArtifact(final CompareFile overlay) throws InvalidCompareFileConfigurationException {

		ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
		@SuppressWarnings("unchecked")
		final Set<Artifact> artifacts = project.getArtifacts();

		final List<Artifact> artifactsOverlays = new ArrayList<>();
		for (Artifact artifact : artifacts) {
			if (!artifact.isOptional() && filter.include(artifact) && ("war".equals(artifact.getType()))) {
				artifactsOverlays.add(artifact);
			}
		}

		for (Artifact artifact : artifactsOverlays) {
			// Handle classifier dependencies properly (clash management)
			if (compareFileWithArtifact(overlay, artifact)) {
				return artifact;
			}
		}

		// maybe its a project dependencies zip or an other type
		@SuppressWarnings("unchecked")
		Set<Artifact> projectArtifacts = this.project.getDependencyArtifacts();
		if (projectArtifacts != null) {
			for (Artifact artifact : projectArtifacts) {
				if (compareFileWithArtifact(overlay, artifact)) {
					return artifact;
				}
			}
		}

		throw new InvalidCompareFileConfigurationException(
				"CompareFiles [" + overlay + "] is not a dependency of the project.");
	}

	private boolean compareFileWithArtifact(CompareFile compareFile, Artifact artifact) {
		return (Objects.equals(compareFile.getGroupId(), artifact.getGroupId())
				&& Objects.equals(compareFile.getArtifactId(), artifact.getArtifactId())
				&& Objects.equals(compareFile.getType(), artifact.getType()));
	}
}
