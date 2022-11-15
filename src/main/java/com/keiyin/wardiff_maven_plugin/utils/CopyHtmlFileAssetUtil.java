package com.keiyin.wardiff_maven_plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

public class CopyHtmlFileAssetUtil {
	
	private final File targetAssestDirectory;
	
	public CopyHtmlFileAssetUtil(File targetAssestDirectory) {
		super();
		this.targetAssestDirectory = targetAssestDirectory;
	}

	public void copyHtmlDiffAssets() throws IOException {
		File diffHtmlCssFile = new File(targetAssestDirectory, "assets/diff2html.min.css");
		File diffHtmlJsFile = new File(targetAssestDirectory, "assets/diff2html-ui.min.js");
		File gitHubCss = new File(targetAssestDirectory, "assets/github.min.css");

		if(!diffHtmlCssFile.exists())
			copyFilePlainJava("assets/diff2html.min.css", diffHtmlCssFile.getPath());
		if(!diffHtmlJsFile.exists())
			copyFilePlainJava("assets/diff2html-ui.min.js", diffHtmlJsFile.getPath());
		if(!gitHubCss.exists())
			copyFilePlainJava("assets/github.min.css", gitHubCss.getPath());
	}

	public void copyFilePlainJava(String from, String to) throws IOException {
		// Get the HTML template to the String
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(from);
		InputStreamReader isReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(isReader);
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = reader.readLine()) != null) {
			sb.append(str);
		}
		FileUtils.write(new File(to), sb.toString(), "utf-8");
	}
}
