package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class BuildPropertiesTask extends DefaultTask {

	@OutputFile final RegularFileProperty output = project.objects.fileProperty()
	final additionalArtifacts = []

	public additionalConfiguration(Configuration... additionalConfigurations) {
		for (Configuration conf : additionalConfigurations)
			additionalArtifact(conf.resolvedConfiguration.resolvedArtifacts)
	}

	public additionalConfiguration(Collection<Configuration> additionalConfigurations) {
		for (Configuration conf : additionalConfigurations)
			additionalArtifact(conf.resolvedConfiguration.resolvedArtifacts)
	}

	public additionalArtifact(ResolvedArtifact... additionalArtifacts) {
		additionalArtifact(Arrays.asList(additionalArtifacts))
	}

	public additionalArtifact(Collection<ResolvedArtifact> additionalArtifacts) {
		this.additionalArtifacts.addAll(additionalArtifacts.collect {
			"${it.name}.version=${it.moduleVersion.id.version}"
		})
	}

	@Override
	public Task configure(Closure closure) {
		return super.configure(closure);
	}

	@TaskAction
	def run() {
		def modules = project.subprojects.collect { "${it.name}.version=${it.version}" }.sort()
		def additional = additionalArtifacts.sort()
		output.asFile.get().text = '# Application\n' + "${project.name}.version=${project.version}" + '\n\n# Modules\n' + modules.join('\n') + '\n\n# Framework\n' + additional.join('\n')
	}
}
