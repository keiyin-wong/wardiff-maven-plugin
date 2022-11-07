package com.keiyin.wardiff_maven_plugin.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class GenerateHtmlFileWithPreviousVersion extends DiffGeneratorDecorator {
	private final String revisedPreviousVersionFilePath;

	public GenerateHtmlFileWithPreviousVersion(DiffGenerator diffGenerator, String revisedPreviousVersionFilePath) {
		super(diffGenerator);
		this.revisedPreviousVersionFilePath = revisedPreviousVersionFilePath;
	}

	@Override
	public void generate(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
		super.generate(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
		generateDiffHtmlFile(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
	}

	public void generateDiffHtmlFile(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {

		File htmlTargetFilePathFile = new File(new File(targetFilePath), "html");
		File htmlTargetFilePathFileWithFileName = new File(htmlTargetFilePathFile, fileName);

		File originalFile = new File(originalFilePath);
		File revisedFile = new File(revisedFilePath);
		File revisedPreviousVersionFile = new File(revisedPreviousVersionFilePath);

		List<String> difString = DiffGeneratorUtils.diffString(revisedFilePath, revisedPreviousVersionFilePath,
				revisedFile.getName() + "_dependency", revisedPreviousVersionFile.getName() + "_previous_version");
		difString.addAll(DiffGeneratorUtils.diffString(originalFilePath, revisedFilePath));

		// Generate diff HTML file and write to disk.
		generateDiffHtml(difString, htmlTargetFilePathFileWithFileName.getPath(), classLoader);
	}

	public static void generateDiffHtml(List<String> diffString, String htmlPath, ClassLoader classLoader)
			throws IOException {
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
		FileUtils.write(new File(htmlPath + ".html"), template, "utf-8");
	}
}
