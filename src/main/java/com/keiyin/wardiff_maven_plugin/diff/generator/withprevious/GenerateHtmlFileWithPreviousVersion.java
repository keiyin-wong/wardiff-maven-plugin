package com.keiyin.wardiff_maven_plugin.diff.generator.withprevious;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.keiyin.wardiff_maven_plugin.utils.CopyHtmlFileAssetUtil;
import com.keiyin.wardiff_maven_plugin.utils.DiffGeneratorUtils;

public class GenerateHtmlFileWithPreviousVersion {
	private final String localFilePath;
	private final String dependencyFilePath;
	private final String dependencyPreviousVersionFilePath;
	private final String targetFilePath;
	private final String fileName;
	private final String diffHtmlLocalFileName;
	private final String diffHtmlDependencyFileName;
	private final String diffHtmlDependencyPreviousVersionFileName;

	public GenerateHtmlFileWithPreviousVersion(String localFilePath, String dependencyFilePath,
			String dependencyPreviousVersionFilePath, String targetFilePath, String fileName,
			String diffHtmlLocalFileName, String diffHtmlDependencyFileName,
			String diffHtmlDependencyPreviousVersionFileName) {
		super();
		this.localFilePath = localFilePath;
		this.dependencyFilePath = dependencyFilePath;
		this.dependencyPreviousVersionFilePath = dependencyPreviousVersionFilePath;
		this.targetFilePath = targetFilePath;
		this.fileName = fileName;
		this.diffHtmlLocalFileName = diffHtmlLocalFileName;
		this.diffHtmlDependencyFileName = diffHtmlDependencyFileName;
		this.diffHtmlDependencyPreviousVersionFileName = diffHtmlDependencyPreviousVersionFileName;
	}

	public void generateDiffHtmlFileWithPreviousVersion() throws IOException {
		File htmlTargetFilePathFile = new File(new File(targetFilePath), "html");
		File htmlTargetFilePathFileWithFileName = new File(htmlTargetFilePathFile, fileName);

		List<String> difString = DiffGeneratorUtils.diffString(
				dependencyPreviousVersionFilePath, 
				dependencyFilePath,
				diffHtmlDependencyPreviousVersionFileName, 
				diffHtmlDependencyFileName 
				);
		
		difString.addAll(DiffGeneratorUtils.diffString(
				dependencyFilePath, 
				localFilePath,
				diffHtmlDependencyFileName, 
				diffHtmlLocalFileName));

		// Generate diff HTML file and write to disk.
		generateDiffHtml(difString, htmlTargetFilePathFileWithFileName.getPath(), getClass().getClassLoader());
		new CopyHtmlFileAssetUtil(htmlTargetFilePathFile).copyHtmlDiffAssets();
	}

	public static void generateDiffHtml(List<String> diffString, String htmlPath, ClassLoader classLoader)
			throws IOException {
		File targetFilePath = new File(htmlPath + ".html");
		StringBuilder builder = new StringBuilder();
		for (String line : diffString) {
			builder.append(line);
			builder.append("\n");
		}

		InputStream inputStream = classLoader.getResourceAsStream("htmlTemplates/diff2-html.html");

		// the stream holding the file content

		InputStreamReader isReader = new InputStreamReader(inputStream);
		// Creating a BufferedReader object
		BufferedReader reader = new BufferedReader(isReader);
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = reader.readLine()) != null) {
			sb.append(str + "\n");
		}
		String template = sb.toString();

		template = template.replace("${title}", "diff").replace("${content}",
				builder.toString().replace("${", "\\${").replace("</script>", "<\\/script>"));
		FileUtils.write(targetFilePath, template, "utf-8");
	}
}
