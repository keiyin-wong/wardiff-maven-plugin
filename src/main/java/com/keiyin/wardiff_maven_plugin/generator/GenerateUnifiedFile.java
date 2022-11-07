package com.keiyin.wardiff_maven_plugin.generator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class GenerateUnifiedFile extends DiffGeneratorDecorator{
	public GenerateUnifiedFile(DiffGenerator diffGenerator) {
		super(diffGenerator);
	}

	@Override
	public void generate(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
		super.generate(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
		File unfiedTargetFilePath = new File(new File(targetFilePath), "unifiedFormat");
		generateUnifiedDiff(originalFilePath, revisedFilePath, unfiedTargetFilePath.getPath(), fileName);
	}
	
	private void generateUnifiedDiff(
			String originalFilePath, 
			String revisedFilePath,
			String targetFilePath, 
			String fileName) throws IOException {
		
		List<String> diffString = DiffGeneratorUtils.diffStringWithoutOrigin(originalFilePath, revisedFilePath);
		
		StringBuilder builder = new StringBuilder();
		for (String line : diffString) {
			builder.append(line);
			builder.append("\n");
		}
		
		FileUtils.write(new File(targetFilePath, fileName + ".txt"), builder.toString(), "utf-8");
	}
}
