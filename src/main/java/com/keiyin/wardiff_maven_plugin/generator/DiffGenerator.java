package com.keiyin.wardiff_maven_plugin.generator;

import java.io.IOException;

public interface DiffGenerator {
	public  void generate(
    		ClassLoader classLoader,
    		String originalFilePath, 
    		String revisedFilePath,
    		String targetFilePath,
    		String fileName) throws IOException;

}
