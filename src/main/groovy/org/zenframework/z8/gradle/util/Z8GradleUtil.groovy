package org.zenframework.z8.gradle.util

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocationProperty
import org.gradle.api.file.FileTree

class Z8GradleUtil {

	private Z8GradleUtil() {}

	public static String getPath(FileSystemLocationProperty path) {
		File file = path.asFile.getOrNull()
		return file != null ? file.path : null
	}

	public static File ifExistsOrNull(File file) {
		return file != null && file.exists() ? file : null
	}

	public static FileTree subTree(Project project, FileTree tree, String root) {
		if (root == null || root.empty)
			return tree
		root = normalize(root, false)
		def subtree = project.files().asFileTree
		tree.visit { f ->
			def path = normalize(f.file.toString())
			def rel = normalize(f.relativePath.toString())
			if (!f.relativePath.file && rel == root /*rel.startsWith(root)*/) {
				subtree = subtree.plus(project.fileTree(path))
				project.logger.info "Subtree: +++ ${rel} (${path})"
			} else {
				project.logger.info "Subtree: --- ${rel}"
			}
		}
		subtree
	}

	public static String normalize(String path) {
		return path.replace('\\', '/')
	}

	public static String normalize(String path, boolean trailingSlash) {
		path = normalize(path)
		if (trailingSlash && !path.endsWith('/'))
			return path + '/'
		if (!trailingSlash && path.endsWith('/'))
			return path.substring(0, path.length() - 1)
		path
	}

}
