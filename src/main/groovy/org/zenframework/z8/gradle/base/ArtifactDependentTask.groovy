package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

class ArtifactDependentTask extends DefaultTask {

	@Optional @Input final ConfigurableFileCollection requires = project.objects.fileCollection()
	@Optional @Input protected final List<String> requiresInclude = []

	FileTree extractRequires() {
		requires.inject(project.files()) { tree, zip ->
			tree.plus(project.zipTree(zip))
		}.asFileTree
	}

	void requires(FileCollection requires) {
		this.requires.setFrom(requires)
	}

	void requiresInclude(Object include) {
		requiresInclude << include.toString()
	}

}
