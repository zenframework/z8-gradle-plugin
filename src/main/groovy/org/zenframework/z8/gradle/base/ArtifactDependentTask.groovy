package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class ArtifactDependentTask extends DefaultTask {

	@Optional @Input final ConfigurableFileCollection requires = project.objects.fileCollection()
	@Optional @Input protected final List<String> requiresInclude = []
	@Optional @Input protected final Map<String, String> renames = [:]
	
	FileTree extractRequires() {
		requires.inject(project.files()) { tree, zip ->
			project.logger.info "Requires: ${zip}"
			tree.plus(project.zipTree(zip))
		}.asFileTree
	}

	void requires(FileCollection requires) {
		this.requires.setFrom(this.requires.plus(requires))
	}

	void requiresInclude(Object... include) {
		include.each { requiresInclude << it.toString() }
	}

	void rename(String source, String target) {
		renames[source] = target
	}

}
