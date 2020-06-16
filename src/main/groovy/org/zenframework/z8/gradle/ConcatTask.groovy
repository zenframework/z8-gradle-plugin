package org.zenframework.z8.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class ConcatTask extends ArtifactDependentTask {

	private static final String ENCODING = 'UTF-8'

	@OutputFile RegularFileProperty output = project.objects.fileProperty()

	File getOutput() {
		output.asFile.getOrNull()
	}

	void setOutput(Object output) {
		this.output.set(project.file(output))
	}

	@TaskAction
	def run() {
		def src = extractRequires().matching {
			include requiresInclude
		}.plus(project.file("${source}/.buildorder").readLines().findAll { !it.trim().isEmpty() }
				.collect { project.file("${source}/${it}") })
		src.each { println "Concat: ${it}" }
		def dest = output.asFile.get()
		dest.parentFile.mkdirs()
		dest.newWriter(ENCODING).withWriter { w ->
			src.each { f -> w << f.getText(ConcatTask.ENCODING) << '\n' }
		}
	}

}
