package org.zenframework.z8.gradle.js

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.zenframework.z8.gradle.base.ArtifactDependentTask
import org.zenframework.z8.gradle.util.Z8GradleUtil

class ConcatTask extends ArtifactDependentTask {

	private static final String ENCODING = 'UTF-8'

	@InputDirectory final DirectoryProperty source = project.objects.directoryProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	@TaskAction
	def run() {
		def src = extractRequires().matching {
			include requiresInclude
		}.plus(project.file("${Z8GradleUtil.getPath(source)}/.buildorder").readLines().findAll { !it.trim().isEmpty() }
				.collect { project.file("${Z8GradleUtil.getPath(source)}/${it}") })
		src.each { println "Concat: ${it}" }
		def dest = output.asFile.get()
		dest.parentFile.mkdirs()
		dest.newWriter(ENCODING).withWriter { w ->
			src.each { f -> w << f.getText(ConcatTask.ENCODING) << '\n' }
		}
	}

}
