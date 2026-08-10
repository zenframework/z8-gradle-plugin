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

	@Input long buildTimestamp = 0L
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	private final additionalArtifacts = []

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
		def gitCommit = getGitCommit()
		def gitBranch = getGitBranch()

		output.asFile.get().text = '# Application\n' +
				"application.name=${project.name}\n" +
				"application.version=${project.version}\n" +
				"build.timestamp=${project.buildTimestamp}\n" +
				"git.commit=${gitCommit}\n" +
				"git.branch=${gitBranch}" +
				'\n\n# Modules\n' + "${project.name}.version=${project.version}\n" + modules.join('\n') +
				'\n\n# Framework\n' + additional.join('\n')
	}

	public static String getGitCommit() {
		return exec('git rev-parse --short HEAD', 'unknown')
	}

	public static String getGitBranch() {
		return exec('git rev-parse --abbrev-ref HEAD', 'unknown')
	}

	public static String exec(String cmd, String defaultValue) {
		try {
			def process = cmd.execute()
			return process.text.trim()
		} catch (Throwable e) {
			return defaultValue
		}
	}
}
