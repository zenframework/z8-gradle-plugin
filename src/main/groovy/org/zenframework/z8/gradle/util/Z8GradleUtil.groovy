package org.zenframework.z8.gradle.util

import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.file.FileTree

class Z8GradleUtil {

	private Z8GradleUtil() {}

	public static String getPath(FileSystemLocationProperty dir) {
		File file = dir.asFile.getOrNull()
		return file != null ? file.path : null
	}

	public static File ifExistsOrNull(File file) {
		return file != null && file.exists() ? file : null
	}

	public static FileTree subTree(FileTree tree, String subfolder) {
		if (subfolder.isEmpty())
			return tree
		subfolder = normalize(subfolder)
		if (!subfolder.endsWith('/'))
			subfolder += '/'
		int pos = subfolder.length()
		extractRequires().filter {
			normalize(it.path).startsWith(subfolder)
		}.each {
			it.path = it.path.substring(pos)
		}
	}

	public static String normalize(String path) {
		return path.replace('\\', '/')
	}

}
