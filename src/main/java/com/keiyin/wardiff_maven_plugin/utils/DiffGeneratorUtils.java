package com.keiyin.wardiff_maven_plugin.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import com.keiyin.wardiff_maven_plugin.model.InvalidCompareFileConfigurationException;
import com.keiyin.wardiff_maven_plugin.model.PathSet;

import difflib.DiffUtils;
import difflib.Patch;

public class DiffGeneratorUtils {

	private DiffGeneratorUtils() {
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
		if (unifiedDiff.isEmpty()) {
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
		List<String> original = new ArrayList<>();
		List<String> revised = new ArrayList<>();
		;
		File originalFile = new File(filePathOriginal);
		File revisedFile = new File(filePathRevised);
		try {
			if (originalFile.exists())
				original = Files.readAllLines(originalFile.toPath(), StandardCharsets.ISO_8859_1);
			if (revisedFile.exists())
				revised = Files.readAllLines(revisedFile.toPath(), StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diffString(original, revised, originalFile.getName(), revisedFile.getName());
	}

	public static List<String> diffString(String filePathOriginal, String filePathRevised, String originalFileName,
			String revisedFileName) {
		List<String> original = new ArrayList<>();
		List<String> revised = new ArrayList<>();
		;
		File originalFile = new File(filePathOriginal);
		File revisedFile = new File(filePathRevised);
		try {
			if (originalFile.exists())
				original = Files.readAllLines(originalFile.toPath(), StandardCharsets.ISO_8859_1);
			if (revisedFile.exists())
				revised = Files.readAllLines(revisedFile.toPath(), StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diffString(original, revised, originalFileName, revisedFileName);
	}

	public static List<String> diffStringWithoutOrigin(List<String> original, List<String> revised,
			String originalFileName, String revisedFileName) {
		originalFileName = originalFileName == null ? "original" : originalFileName;
		revisedFileName = revisedFileName == null ? "revised" : revisedFileName;
		Patch<String> patch = DiffUtils.diff(original, revised);
		List<String> unifiedDiff = DiffUtils.generateUnifiedDiff(originalFileName, revisedFileName, original, patch, 0);
		if (unifiedDiff.isEmpty()) {
			unifiedDiff.add("--- " + originalFileName);
			unifiedDiff.add("+++ " + revisedFileName);
			unifiedDiff.add("@@ -0,0 +0,0 @@");
		} else if (unifiedDiff.size() >= 3 && !unifiedDiff.get(2).contains("@@ -1,")) {
			unifiedDiff.set(1, unifiedDiff.get(1));
			unifiedDiff.add(2, "@@ -0,0 +0,0 @@");
		}

		return unifiedDiff;
	}

	public static List<String> diffStringWithoutOrigin(String filePathOriginal, String filePathRevised) {
		List<String> original = new ArrayList<>();
		List<String> revised = new ArrayList<>();
		File originalFile = new File(filePathOriginal);
		File revisedFile = new File(filePathRevised);
		try {
			if (originalFile.exists())
				original = Files.readAllLines(originalFile.toPath(), StandardCharsets.ISO_8859_1);
			if (revisedFile.exists())
				revised = Files.readAllLines(revisedFile.toPath(), StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diffStringWithoutOrigin(original, revised, originalFile.getName(), revisedFile.getName());
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

	/**
	 * Returns the file to copy. If the includes are {@code null} or empty, the
	 * default includes are used.
	 *
	 * @param baseDir            the base directory to start from
	 * @param includes           the includes
	 * @param excludes           the excludes
	 * @param includeDirectories include directories yes or not.
	 * @return the files to copy
	 * @throws InvalidCompareFileConfigurationException 
	 */
	// CHECKSTYLE_OFF: LineLength
	public static PathSet getFilesToIncludes(File baseDir, String[] includes, String[] excludes,
			boolean includeDirectories) throws InvalidCompareFileConfigurationException
	// CHECKSTYLE_ON: LineLength
	{
		final DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(baseDir);

		if (excludes != null) {
			scanner.setExcludes(excludes);
		}
		scanner.addDefaultExcludes();

		if (includes != null && includes.length > 0) {
			scanner.setIncludes(includes);
		} else {
			throw new InvalidCompareFileConfigurationException("Please provides includes");
		}

		scanner.scan();

		PathSet pathSet = new PathSet(scanner.getIncludedFiles());

		if (includeDirectories) {
			pathSet.addAll(scanner.getIncludedDirectories());
		}

		return pathSet;
	}

	public static boolean isTwoFileSame(File file1, File file2) throws IOException {
		return FileUtils.contentEquals(file1, file2);
	}

	public static boolean isBinaryFile(String fileName) {
		return (fileName.endsWith(".pdf") || fileName.endsWith(".png") || fileName.endsWith(".mp4")
				|| fileName.endsWith(".gif") || fileName.endsWith(".zip") || fileName.endsWith(".jpg"));
	}
}
