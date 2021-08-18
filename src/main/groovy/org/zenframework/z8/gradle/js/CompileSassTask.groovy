package org.zenframework.z8.gradle.js
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.zenframework.z8.gradle.util.Z8GradleUtil

class CompileSassTask extends Exec {
	@Optional @SkipWhenEmpty @InputDirectory final DirectoryProperty source = project.objects.directoryProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()
	@Input String index = 'index.sass'

	@Override
	protected void exec() {
		def source = this.source.asFile.getOrNull()
		def output = this.output.asFile.get()

		commandLine('sass', '--indented', '--style=compressed', '--update', '--stop-on-error', '--no-source-map', new File(source, index), output)

		super.exec()
	}
}
