package com.keiyin.wardiff_maven_plugin.generator;

import java.io.IOException;

public class BaseDiffGenerator implements DiffGenerator{
	
	@Override
	public void generate(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
		// Do nothing
	}

}
