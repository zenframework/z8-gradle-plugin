package org.zenframework.z8.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip

class BlzipTask extends Zip {

	@Override
	public Task configure(Closure closure) {
		dependsOn project.tasks.withType(CompileBlTask.class)
		archiveName "${project.name}-${project.version}.zip"
		destinationDir project.file("${project.buildDir}/libs/")
		from(project.projectDir) {
			include '**/*.bl'
			includeEmptyDirs = false
		}
		return super.configure(closure);
	}

}
