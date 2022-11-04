package com.keiyin.wardiff_maven_plugin.task;

import org.apache.maven.plugin.MojoExecutionException;

public interface Task {
	public void performTask() throws MojoExecutionException;
}
