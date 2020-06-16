package org.zenframework.z8.gradle.bl

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
import org.zenframework.z8.gradle.base.ArtifactDependentTask

class CompileBlTask extends ArtifactDependentTask {

	@InputDirectory final DirectoryProperty source = project.objects.directoryProperty()
	@OutputDirectory final DirectoryProperty output = project.objects.directoryProperty()

	@Optional @InputDirectory final DirectoryProperty docsTemplates = project.objects.directoryProperty()
	@Optional @OutputDirectory final DirectoryProperty docsOutput = project.objects.directoryProperty()

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
		def source = Z8GradleUtil.getPath(source)
		def output = Z8GradleUtil.getPath(output)
		def requires = requires.asFileTree.collect() { it.path }
		def docsTemplates = Z8GradleUtil.getPath(docsTemplates)
		def docsOutput = Z8GradleUtil.getPath(docsOutput)

		println "BL Source:   ${source}" +
				"\nBL Output:   ${output}" +
				"\nBL Requires: ${requires.join('\n             ')}" +
				(docsTemplates != null ? "\nBL Docs Templates: ${docsTemplates}" : '') +
				(docsOutput != null ? "BL Docs Templates: ${docsOutput}" : '')

		Main.compile(project.name, source, requires.toArray(new String[0]), output, docsOutput, docsTemplates);
	}

}
