package com.keiyin.wardiff_maven_plugin.generator;

import java.io.IOException;

public abstract class DiffGeneratorDecorator implements DiffGenerator{
	
	protected DiffGenerator diffGenerator; 
	
	protected DiffGeneratorDecorator(DiffGenerator diffGenerator) {
		this.diffGenerator = diffGenerator;
	}

	@Override
	public void generate(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
		this.diffGenerator.generate(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
	}

}
