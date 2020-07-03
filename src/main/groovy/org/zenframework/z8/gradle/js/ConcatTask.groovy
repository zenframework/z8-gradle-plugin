package org.zenframework.z8.gradle.js

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.zenframework.z8.gradle.base.ArtifactDependentTask

class ConcatTask extends ArtifactDependentTask {

	private static final String ENCODING = 'UTF-8'

	@InputDirectory final DirectoryProperty source = project.objects.directoryProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	@TaskAction
	def run() {
		File source = this.source.asFile.getOrNull()
		if (source == null || !source.exists()) {
			project.logger.info "Source ${this.source.asFile} doesn't exist. Skipping"
			return
		}
		project.logger.info "Concat from ${source.path}..."
		def src = extractRequires().matching {
			include requiresInclude
		}.plus(project.file("${source.path}/.buildorder").readLines().findAll {
			def path = it.trim()
			!path.isEmpty() && !path.startsWith('#')
		}.collect {
			project.file("${source.path}/${it}")
		})
		src.each { project.logger.info "Concat: ${it}" }
		def dest = output.asFile.get()
		dest.parentFile.mkdirs()
		dest.newWriter(ENCODING).withWriter { w ->
			src.each { f -> w << f.getText(ConcatTask.ENCODING) << '\n' }
		}
	}

}
