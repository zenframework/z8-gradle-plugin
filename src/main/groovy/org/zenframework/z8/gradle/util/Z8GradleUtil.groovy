package org.zenframework.z8.gradle.util

import org.gradle.api.file.FileSystemLocationProperty

class Z8GradleUtil {

	private Z8GradleUtil() {}

	public static String getPath(FileSystemLocationProperty dir) {
		File file = dir.asFile.getOrNull()
		return file != null ? file.path : null
	}

	public static File ifExistsOrNull(File file) {
		return file != null && file.exists() ? file : null
	}

}
