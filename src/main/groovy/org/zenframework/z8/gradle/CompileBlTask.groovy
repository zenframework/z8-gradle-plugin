package org.zenframework.z8.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.zenframework.z8.compiler.cmd.Main

class CompileBlTask extends ArtifactDependentTask {

	@OutputDirectory private DirectoryProperty output = project.objects.directoryProperty()

	@Optional @InputDirectory private DirectoryProperty docsTemplates = project.objects.directoryProperty()
	@Optional @OutputDirectory private DirectoryProperty docsOutput = project.objects.directoryProperty()

	File getOutput() {
		output.asFile.getOrNull()
	}

	void setOutput(Object output) {
		this.output.set(project.file(output))
	}

	File getDocsTemplates() {
		docsTemplates.asFile.getOrNull()
	}

	File getDocsOutput() {
		docsOutput.asFile.getOrNull()
	}

	@Override
	public Task configure(Closure closure) {
		if (getSource() == null)
			setSource(project.projectDir)
		if (getOutput() == null)
			setOutput("${project.projectDir}/.java")
		requires project.configurations.blcompile
		super.configure(closure);
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

}
