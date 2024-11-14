package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import groovy.text.SimpleTemplateEngine

class BuildPropertiesTask extends DefaultTask {

	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	@Override
	public Task configure(Closure closure) {
		return super.configure(closure);
	}

	@TaskAction
	def run() {
		def data = ["${project.name}.version=${project.version}"]

		// Zenframework Z8 version
		data += project.configurations.boot.resolvedConfiguration.resolvedArtifacts.collect {
			"${it.moduleVersion.id.group}.version=${it.moduleVersion.id.version}"
		}

		data += project.subprojects.collect { "${it.name}.version=${it.version}" }.sort()

		output.asFile.get().text = data.join('\n')
	}
}
