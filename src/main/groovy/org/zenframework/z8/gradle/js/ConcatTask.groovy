package org.zenframework.z8.gradle.js

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.zenframework.z8.gradle.base.ArtifactDependentTask

class ConcatTask extends ArtifactDependentTask {

	private static final String ENCODING = 'UTF-8'

	@Optional @InputDirectory final DirectoryProperty source = project.objects.directoryProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()
	@Input def buildorder = '.buildorder'

	@TaskAction
	def run() {
		def extractedRequires = extractRequires();
		def source = this.source.asFile.getOrNull()

		def src = requiresInclude.collect { requirement ->
			extractedRequires.matching { include requirement }.singleFile
		}
		if (source != null && source.exists()) {
			def buildorder = project.file("${source.path}/${this.buildorder}")
			if (buildorder.exists())
				src = src.plus(buildorder.readLines().findAll {
					def path = it.trim()
					!path.isEmpty() && !path.startsWith('#')
				}.collect {
					project.file("${source.path}/${it}")
				})
			else
				project.logger.info "Source buildorder ${buildorder} doesn't exist. Skipping"
		} else {
			project.logger.info "Source ${this.source.asFile} doesn't exist. Skipping"
		}

		if (src.empty)
			return

		src.each { project.logger.info "Concat: ${it}" }

		def dest = output.asFile.get()
		dest.parentFile.mkdirs()
		dest.newWriter(ENCODING).withWriter { w ->
			src.each { f -> w << f.getText(ConcatTask.ENCODING) << '\n' }
		}
	}

}
