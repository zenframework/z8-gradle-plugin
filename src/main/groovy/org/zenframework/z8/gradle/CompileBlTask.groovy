package org.zenframework.z8.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.zenframework.z8.compiler.cmd.Main

class CompileBlTask extends DefaultTask {

	@InputDirectory DirectoryProperty source = project.objects.directoryProperty().fileValue(project.file("${project.projectDir}"))
	@OutputDirectory DirectoryProperty output = project.objects.directoryProperty().fileValue(project.file("${project.projectDir}/.java"))

	@Optional @InputDirectory DirectoryProperty docsTemplates = project.objects.directoryProperty()
	@Optional @OutputDirectory DirectoryProperty docsOutput = project.objects.directoryProperty()

	@Optional @Input ConfigurableFileCollection requires = project.objects.fileCollection()

	File getSource() {
		source.asFile.getOrNull()
	}

	File getDocsTemplates() {
		docsTemplates.asFile.getOrNull()
	}

	File getOutput() {
		output.asFile.getOrNull()
	}

	File getDocsOutput() {
		docsOutput.asFile.getOrNull()
	}

	FileCollection getRequires() {
		requires.asFileTree
	}

	@Override
	public Task configure(Closure closure) {
		requires.setFrom(project.configurations.bl)
		return super.configure(closure);
	}

	@TaskAction
	def run() {
		def source = getPath(getSource())
		def output = getPath(getOutput())
		def requires = getRequires().collect() { it.path }
		def docsTemplates = getPath(getDocsTemplates())
		def docsOutput = getPath(getDocsOutput())

		println "BL Source:   ${source}" +
				"\nBL Output:   ${output}" +
				"\nBL Requires: ${requires.join('\n             ')}" +
				(docsTemplates != null ? "\nBL Docs Templates: ${docsTemplates}" : '') +
				(docsOutput != null ? "BL Docs Templates: ${docsOutput}" : '')

		Main.compile(project.name, source, requires.toArray(new String[0]), output, docsOutput, docsTemplates);
	}

	static String getPath(File file) {
		return file != null ? file.path : null
	}

}
