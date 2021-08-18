package org.zenframework.z8.gradle.js
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.OutputFile
import org.zenframework.z8.gradle.util.Z8GradleUtil

class CompileSassTask extends Exec {
    @InputFile final RegularFileProperty source = project.objects.fileProperty()
    @OutputFile final RegularFileProperty output = project.objects.fileProperty()

    @Override
    protected void exec() {
        def source = Z8GradleUtil.getPath(source)
        def output = Z8GradleUtil.getPath(output)

        commandLine('sass', '--indented', '--style=compressed', '--update', '--stop-on-error', '--no-source-map', source, output)

        super.exec()
    }
}
