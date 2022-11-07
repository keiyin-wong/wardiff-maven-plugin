package com.keiyin.wardiff_maven_plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;

public class CompareFile {

	public static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };

	public static final String[] DEFAULT_EXCLUDES = new String[] { "META-INF/MANIFEST.MF" };

	private String id;

	private String groupId;

	private String artifactId;
	
	private Artifact artifact;

	private String[] includes = DEFAULT_INCLUDES;

	private String[] excludes = DEFAULT_EXCLUDES;

	private String type = "war";

	public String getId() {
		if (id == null) {
			final StringBuilder sb = new StringBuilder();
			sb.append(getGroupId()).append(".").append(getArtifactId());
			id = sb.toString();
		}
		return id;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getType() {
		return type;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String[] getIncludes() {
		return includes;
	}

	public void setIncludes(String includes) {
		this.includes = parse(includes);
	}

	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	public String[] getExcludes() {
		return excludes;
	}

	public void setExcludes(String excludes) {
		this.excludes = parse(excludes);
	}

	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	public CompareFile() {
	}

	public CompareFile(String groupId, String artifactId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	public String getFilePath() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getGroupId()).append("/").append(getArtifactId());
		return sb.toString();
	}

	@Override
	public String toString() {
		return " id " + getId();
	}

	private String[] parse(String s) {
		final List<String> result = new ArrayList<>();
		if (s == null) {
			return result.toArray(new String[result.size()]);
		} else {
			String[] tokens = s.split(",");
			for (String token : tokens) {
				result.add(token.trim());
			}
			return result.toArray(new String[result.size()]);
		}
	}

	public Artifact getArtifact() {
		return artifact;
	}

	public void setArtifact(Artifact artifact) {
		this.artifact = artifact;
	}
}
