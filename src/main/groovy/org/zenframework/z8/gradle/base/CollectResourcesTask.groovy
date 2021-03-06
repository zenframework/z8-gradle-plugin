package org.zenframework.z8.gradle.base

import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

class CollectResourcesTask extends ArtifactDependentTask {

	@OutputDirectory final DirectoryProperty output = project.objects.directoryProperty()
	@Input final replaceMatching = []

	void replaceMatching(Object... matchings) {
		replaceMatching.addAll(Arrays.asList(matchings))
	}

	@Override
	Task configure(Closure closure) {
		doLast {
			project.copy {
				includeEmptyDirs = false

				from(extractRequires()) {
					include requiresInclude
					exclude requiresExclude
					if (replaceMatching != null && !replaceMatching.empty)
						filesMatching(replaceMatching) {
							expand project: project
						}
				}

				into output

				renames.each { k, v ->
					rename k, v
				}
			}
		}
		return super.configure(closure);
	}

}
