package org.zenframework.z8.gradle.bl

import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.FileChange
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.zenframework.z8.gradle.util.Z8GradleUtil

class CompileBlForkedTask extends JavaExec {

	@Incremental @InputDirectory final DirectoryProperty source = project.objects.directoryProperty().fileValue(project.file("${project.projectDir}"))
	@OutputDirectory final DirectoryProperty output = project.objects.directoryProperty().fileValue(project.file("${project.projectDir}/.java"))

	@Optional @InputDirectory final DirectoryProperty docsTemplates = project.objects.directoryProperty()
	@Optional @OutputDirectory final DirectoryProperty docsOutput = project.objects.directoryProperty()

	@Optional @InputFiles final ConfigurableFileCollection requires = project.objects.fileCollection()

	File getOutput() {
		return output.asFile.getOrNull()
	}

	@Override
	public Task configure(Closure closure) {
		requires.setFrom(project.configurations.blcompile)

		classpath = project.configurations.compiler
		main = 'org.zenframework.z8.compiler.cmd.Main'

		super.configure(closure);
	}

	@TaskAction
	public void exec(InputChanges inputChanges) {
		if (inputChanges.getFileChanges(source).find { FileChange change ->
			change.file.name.endsWith('.bl')
		} == null) {
			project.logger.info 'No BL source changed. Task is UP-TO-DATE'
			return
		}

		def source = Z8GradleUtil.getPath(source)
		def output = Z8GradleUtil.getPath(output)
		def requires = requires.asFileTree.collect() { it.path }
		def docsTemplates = Z8GradleUtil.getPath(docsTemplates)
		def docsOutput = Z8GradleUtil.getPath(docsOutput)

		project.logger.info "BL Source:   ${source}" +
				"\nBL Output:   ${output}" +
				"\nBL Requires: ${requires.join('\n             ')}" +
				(docsTemplates != null ? "\nBL Docs Templates: ${docsTemplates}" : '') +
				(docsOutput != null ? "BL Docs Templates: ${docsOutput}" : '')

		args = [ source, "-projectName:${project.name}", "-output:${output}", "-requires:${requires.join(';')}" ] + args
		exec();
	}

}
