package org.zenframework.z8.gradle.js

import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.zenframework.z8.gradle.base.ArtifactDependentTask

class PrepareResourcesTask extends ArtifactDependentTask {

	@InputDirectory final DirectoryProperty source = project.objects.directoryProperty().fileValue(project.file("${project.projectDir}"))
	@OutputDirectory final DirectoryProperty output = project.objects.directoryProperty().fileValue(project.file("${project.projectDir}/.java"))

	@Override
	Task configure(Closure closure) {
		doLast {
			project.copy {
				into output

				from (source) {
					exclude '.buildorder', '**/*.css'
				}

				if (!requiresInclude.isEmpty()) {
					from (Z8GradleUtil.subTree(extractRequires(), 'web/css/').matching {
						include requiresInclude
					})
				}
			}
		}
		return super.configure(closure);
	}

}
