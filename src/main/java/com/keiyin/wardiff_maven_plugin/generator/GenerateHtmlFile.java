package com.keiyin.wardiff_maven_plugin.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import difflib.DiffUtils;
import difflib.Patch;

public class GenerateHtmlFile extends DiffGeneratorDecorator {

	public GenerateHtmlFile(DiffGenerator diffGenerator) {
		super(diffGenerator);
	}

	@Override
	public void generate(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {
		super.generate(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
		generateDiffHtmlFile(classLoader, originalFilePath, revisedFilePath, targetFilePath, fileName);
	}
	
	
	// =======================================================================================
	// Logic
	// =======================================================================================

	public void generateDiffHtmlFile(ClassLoader classLoader, String originalFilePath, String revisedFilePath,
			String targetFilePath, String fileName) throws IOException {

		File htmlTargetFilePathFile = new File(new File(targetFilePath), "html");
		File htmlTargetFilePathFileWithFileName = new File(htmlTargetFilePathFile, fileName);

		List<String> difString = diffString(originalFilePath, revisedFilePath);
		
		// Generate diff HTML file and write to disk.
		generateDiffHtml(difString, htmlTargetFilePathFileWithFileName.getPath(), classLoader);
	}

	public static List<String> diffString(List<String> original, List<String> revised) {
		return diffString(original, revised, null, null);
	}

	public static List<String> diffString(List<String> original, List<String> revised, String originalFileName,
			String revisedFileName) {
		originalFileName = originalFileName == null ? "original" : originalFileName;
		revisedFileName = revisedFileName == null ? "revised" : revisedFileName;
		Patch<String> patch = DiffUtils.diff(original, revised);
		List<String> unifiedDiff = DiffUtils.generateUnifiedDiff(originalFileName, revisedFileName, original, patch, 0);
		int diffCount = unifiedDiff.size();
		if (unifiedDiff.size() == 0) {
			unifiedDiff.add("--- " + originalFileName);
			unifiedDiff.add("+++ " + revisedFileName);
			unifiedDiff.add("@@ -0,0 +0,0 @@");
		} else if (unifiedDiff.size() >= 3 && !unifiedDiff.get(2).contains("@@ -1,")) {
			unifiedDiff.set(1, unifiedDiff.get(1));
			unifiedDiff.add(2, "@@ -0,0 +0,0 @@");
		}

		List<String> original1 = new ArrayList<>();
		for (String s : original) {
			StringBuilder tempBuilder = new StringBuilder();
			tempBuilder.append(" ");
			tempBuilder.append(s);
			original1.add(tempBuilder.toString());
		}

		return insertOrig(original1, unifiedDiff);
	}

	public static List<String> diffString(String filePathOriginal, String filePathRevised) {
		List<String> original = null;
		List<String> revised = null;
		File originalFile = new File(filePathOriginal);
		File revisedFile = new File(filePathRevised);
		try {
			original = Files.readAllLines(originalFile.toPath(), StandardCharsets.ISO_8859_1);
			revised = Files.readAllLines(revisedFile.toPath(), StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diffString(original, revised, originalFile.getName(), revisedFile.getName());
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
		FileUtils.write(new File(htmlPath), template, "utf-8");
	}

	public static List<String> insertOrig(List<String> original, List<String> unifiedDiff) {
		List<String> result = new ArrayList<>();
		List<List<String>> diffList = new ArrayList<>();
		List<String> d = new ArrayList<>();
		for (int i = 0; i < unifiedDiff.size(); i++) {
			String u = unifiedDiff.get(i);
			if (u.startsWith("@@") && !"@@ -0,0 +0,0 @@".equals(u) && !u.contains("@@ -1,")) {
				List<String> twoList = new ArrayList<>();
				twoList.addAll(d);
				diffList.add(twoList);
				d.clear();
				d.add(u);
				continue;
			}
			if (i == unifiedDiff.size() - 1) {
				d.add(u);
				List<String> twoList = new ArrayList<>();
				twoList.addAll(d);
				diffList.add(twoList);
				d.clear();
				break;
			}
			d.add(u);
		}

		for (int i = 0; i < diffList.size(); i++) {
			List<String> diff = diffList.get(i);
			List<String> nexDiff = i == diffList.size() - 1 ? null : diffList.get(i + 1);
			String simb = i == 0 ? diff.get(2) : diff.get(0);
			String nexSimb = nexDiff == null ? null : nexDiff.get(0);
			insert(result, diff);
			Map<String, Integer> map = getRowMap(simb);
			if (null != nexSimb) {
				Map<String, Integer> nexMap = getRowMap(nexSimb);
				int start = 0;
				if (map.get("orgRow") != 0) {
					start = map.get("orgRow") + map.get("orgDel") - 1;
				}
				int end = nexMap.get("revRow") - 2;
				insert(result, getOrigList(original, start, end));
			}

			if (simb.contains("@@ -1,") && null == nexSimb && map.get("orgDel") != original.size()) {
				insert(result, getOrigList(original, 0, original.size() - 1));
			} else if (null == nexSimb && (map.get("orgRow") + map.get("orgDel") - 1) < original.size()) {
				int start = (map.get("orgRow") + map.get("orgDel") - 1);
				start = start == -1 ? 0 : start;
				insert(result, getOrigList(original, start, original.size() - 1));
			}
		}
		return result;
	}

	public static void insert(List<String> result, List<String> noChangeContent) {
		for (String ins : noChangeContent) {
			result.add(ins);
		}
	}

	public static Map<String, Integer> getRowMap(String str) {
		Map<String, Integer> map = new HashMap<>();
		if (str.startsWith("@@")) {
			String[] sp = str.split(" ");
			String org = sp[1];
			String[] orgSp = org.split(",");
			map.put("orgRow", Integer.valueOf(orgSp[0].substring(1)));
			map.put("orgDel", Integer.valueOf(orgSp[1]));

			String[] revSp = org.split(",");
			map.put("revRow", Integer.valueOf(revSp[0].substring(1)));
			map.put("revAdd", Integer.valueOf(revSp[1]));
		}
		return map;
	}

	public static List<String> getOrigList(List<String> original1, int start, int end) {
		List<String> list = new ArrayList<>();
		if (!original1.isEmpty() && start <= end && end < original1.size()) {
			for (; start <= end; start++) {
				list.add(original1.get(start));
			}
		}
		return list;
	}

}
