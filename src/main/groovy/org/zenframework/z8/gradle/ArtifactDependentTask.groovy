package org.zenframework.z8.gradle

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

	@InputDirectory private DirectoryProperty source = project.objects.directoryProperty()
	@Optional @Input private ConfigurableFileCollection requires = project.objects.fileCollection()

	File getSource() {
		source.asFile.getOrNull()
	}

	void setSource(Object source) {
		this.source.set(project.file(source))
	}

	FileTree getRequires() {
		requires.asFileTree
	}

	ConfigurableFileTree extractRequires() {
		requires.inject(project.files()) { tree, zip ->
			tree.plus(zipTree(zip))
		}
	}

	void requires(FileCollection requires) {
		this.requires.setFrom(requires)
	}

	static protected String getPath(File file) {
		return file != null ? file.path : null
	}

}
