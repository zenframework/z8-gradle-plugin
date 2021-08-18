package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.zenframework.z8.gradle.util.Z8GradleUtil

class ArtifactDependentTask extends DefaultTask {

	@Optional @InputFiles final ConfigurableFileCollection requires = project.objects.fileCollection()
	@Optional @Input final List<String> requiresInclude = []
	@Optional @Input final List<String> requiresExclude = []
	@Optional @Input String requiresRoot = null

	@Optional @Input final Map<String, String> renames = [:]

	FileTree extractRequires() {
		requires.inject(project.files().asFileTree) { tree, req ->
			project.logger.info "Requires: ${req}"
			File file = project.file(req)
			tree = tree.plus(Z8GradleUtil.subTree(project, file.isFile() && file.name.endsWith('.zip')
					? project.zipTree(req) : project.fileTree(req), requiresRoot))
		}.asFileTree
	}

	void requires(FileCollection requires) {
		this.requires.setFrom(this.requires.plus(requires))
	}

	void requiresInclude(Object... include) {
		include.each { requiresInclude << it.toString() }
	}

	void requiresExclude(Object... exclude) {
		exclude.each { requiresExclude << it.toString() }
	}

	void rename(String source, String target) {
		renames[source] = target
	}

}
