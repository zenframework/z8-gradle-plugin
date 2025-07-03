package org.zenframework.z8.gradle.base

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class ConcatTask extends ArtifactDependentTask {

	private static final String ENCODING = 'UTF-8'

	@Optional @InputDirectory final DirectoryProperty source = project.objects.directoryProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	@Optional @Input final List<String> beforeSource = []
	@Optional @Input final List<String> afterSource = []

	@Input def buildorder = '.buildorder'
	@Input def append = false

	@TaskAction
	def run() {
		def extractedRequires = extractRequires()
		def source = this.source.asFile.getOrNull()

		def src = requiresInclude.collect { requirement ->
			def found = extractedRequires.matching { include requirement }
			def count = found.size()

			if (count == 0)
				project.logger.warn "Requirement '${requirement}' not found. Skipping"
			if (count > 1)
				project.logger.warn "Multiple requirements '${requirement}' found. Skipping"

			found.singleFile
		}

		beforeSource.findAll { it.exists() }.each { file ->
			project.logger.info "Before source: ${file}"
			src.add(file)
		}

		if (source != null && source.exists()) {
			def buildorder = project.file("${source.path}/${this.buildorder}")
			if (buildorder.exists())
				src.addAll(buildorder.readLines().findAll {
					def path = it.trim()
					!path.isEmpty() && !path.startsWith('#')
				}.collect {
					project.file("${source.path}/${it}")
				})
			else
				project.logger.info "Source buildorder ${buildorder.absolutePath} doesn't exist. Skipping"
		} else {
			project.logger.info "Source ${source != null ? source.absolutePath : null} doesn't exist. Skipping"
		}

		afterSource.findAll { it.exists() }.each { file ->
			project.logger.info "After source: ${file}"
			src.add(file)
		}

		if (src.empty)
			return

		src.each { project.logger.info "Concat: ${it}" }

		def dest = output.asFile.get()
		dest.parentFile.mkdirs()
		dest.newWriter(ENCODING, append).withWriter { w ->
			src.each { f -> w << f.getText(ConcatTask.ENCODING) << '\n' }
		}
	}

	void beforeSource(Object beforeSource) {
		this.beforeSource.add project.file(beforeSource)
	}

	void afterSource(Object afterSource) {
		this.afterSource.add project.file(afterSource)
	}

}
