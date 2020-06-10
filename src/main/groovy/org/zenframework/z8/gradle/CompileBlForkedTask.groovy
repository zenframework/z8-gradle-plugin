package org.zenframework.z8.gradle

import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory

class CompileBlForkedTask extends JavaExec {

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

	FileTree getRequires() {
		requires.asFileTree
	}

	@Override
	public Task configure(Closure closure) {
		requires.setFrom(project.configurations.blcompile)

		classpath = project.configurations.compiler
		main = 'org.zenframework.z8.compiler.cmd.Main'

		super.configure(closure);
	}

	@Override
	public void exec() {
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

		args = [ source, "-projectName:${project.name}", "-output:${output}", "-requires:${requires.join(';')}" ] + args
		super.exec();
	}

	private static String getPath(File file) {
		return file != null ? file.path : null
	}

}
