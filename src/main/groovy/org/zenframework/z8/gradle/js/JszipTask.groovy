package org.zenframework.z8.gradle.js

import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip

class JszipTask extends Zip {

	@Override
	public Task configure(Closure closure) {
		dependsOn project.tasks.withType(MinifyCssTask.class)
		dependsOn project.tasks.withType(MinifyJsTask.class)
		archiveName "${project.name}-${project.version}.zip"
		destinationDir project.file("${project.buildDir}/libs/")
		from("${project.buildDir}") {
			include 'web/debug/css/*.css'
			include 'web/debug/*.js'
			includeEmptyDirs = false
		}
		return super.configure(closure);
	}

}
