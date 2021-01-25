package org.zenframework.z8.gradle.bl

import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
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

abstract class CompileBlForkedTask extends JavaExec {

	@Incremental @InputFiles abstract ConfigurableFileCollection getSources()
	@OutputDirectory abstract DirectoryProperty getOutput()
	@Input abstract ListProperty<String> getSourcePaths()

	@Optional @InputDirectory abstract DirectoryProperty getDocsTemplates()
	@Optional @OutputDirectory abstract DirectoryProperty getDocsOutput()

	@Optional @InputFiles abstract ConfigurableFileCollection getRequires()

	void sourcePaths(Object... sourcePaths) {
		this.sourcePaths.addAll(sourcePaths.collect { it.toString() })
	}

	@Override
	public Task configure(Closure closure) {
		super.configure(closure);

		requires.setFrom(project.configurations.blcompile)
		sources.setFrom(sources.plus(project.files(sourcePaths.get().collect { project.file("${project.projectDir}/${it}") }.toArray())))

		classpath = project.configurations.compiler
		main = 'org.zenframework.z8.compiler.cmd.Main'
	}

	@TaskAction
	public void exec(InputChanges inputChanges) {
		def sourcePaths = sourcePaths.get()
		def output = Z8GradleUtil.getPath(output)
		def requires = requires.asFileTree.collect() { it.path }
		def docsTemplates = Z8GradleUtil.getPath(docsTemplates)
		def docsOutput = Z8GradleUtil.getPath(docsOutput)

		args = [ project.projectDir, "-projectName:${project.name}", "-sources:${sourcePaths.join(';')}", "-output:${output}", "-requires:${requires.join(';')}" ] + args
		exec();
	}

}
