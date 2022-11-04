package com.keiyin.wardiff_maven_plugin.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;

import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;

public class GenerateHtmlFileSimple extends DiffGeneratorDecorator{

	public GenerateHtmlFileSimple(DiffGenerator diffGenerator) {
		super(diffGenerator);
	}
	
	@Override
	public void generate(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
		super.generate(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
		File htmlTargetFilePath = new File(new File(targetFilePath), "htmlSimple");
		generateDiffHtmlFile(classLoader, originalFilePath, revisedFilePath, htmlTargetFilePath.getPath(), fileName);
	}
	
	public void generateDiffHtmlFile(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
			
			List<String> original = Files.readAllLines(new File(originalFilePath).toPath(), StandardCharsets.ISO_8859_1);
			List<String> revised = Files.readAllLines(new File(revisedFilePath).toPath(), StandardCharsets.ISO_8859_1);
			
			String deletion = "<span style=\"background-color: #FB504B\">${text}</span>";
			String insertion = "<span style=\"background-color: #45EA85\">${text}</span>";
			String change = "<span style=\"background-color: skyblue\">${text}</span>";

			String left = "</br>";
			String right = "</br>";

			Patch<String> patch = DiffUtils.diff(original, revised);

//	        for (Delta<String> delta : patch.getDeltas()) {
//	        	log.info(delta.toString());
//	        }

			DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
			builder.showInlineDiffs(false);
			DiffRowGenerator generator = builder.build();
			for (Delta<String> delta : patch.getDeltas()) {
				List<DiffRow> generateDiffRows = generator.generateDiffRows(delta.getOriginal().getLines(),
						delta.getRevised().getLines());
				int leftPos = delta.getOriginal().getPosition();
				int rightPos = delta.getRevised().getPosition();
				left = left + "Position:" + leftPos + "</br>";
				right = right + "Position:" + rightPos + "</br>";
				for (DiffRow row : generateDiffRows) {
					DiffRow.Tag tag = row.getTag();
					if (tag == DiffRow.Tag.INSERT) {
						left = left + "</br>";
						right = right + insertion.replace("${text}", "" + row.getNewLine() + "</br>");
					} else if (tag == DiffRow.Tag.CHANGE) {
						left = left + change.replace("${text}", "" + row.getOldLine() + "</br>");
						right = right + change.replace("${text}", "" + row.getNewLine() + "</br>");
					} else if (tag == DiffRow.Tag.DELETE) {
						left = left + deletion.replace("${text}", "" + row.getOldLine() + "</br>");
						right = right + "</br>";
					} else if (tag == DiffRow.Tag.EQUAL) {
						left = left + row.getOldLine() + "</br>";
						right = right + row.getNewLine() + "</br>";
					} else {
						throw new IllegalStateException("Unknown pattern tag: " + tag);
					}
				}
				left = left + "</br>";
				right = right + "</br>";
			}

			
			// Get the HTML template to the String
			InputStream inputStream = classLoader.getResourceAsStream("htmlTemplates/diff-html.html");

			// the stream holding the file content

			InputStreamReader isReader = new InputStreamReader(inputStream);
			// Creating a BufferedReader object
			BufferedReader reader = new BufferedReader(isReader);
			StringBuilder sb = new StringBuilder();
			String str;
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
			String template = sb.toString();
			
			// Replace the particular string in the HTML file
			String out1 = template.replace("${left}", left);
			String output = out1.replace("${right}", right);
			
			// Write the HTML file to the disk
			FileUtils.write(new File(targetFilePath, fileName), output, "utf-8");
		
	}
}
