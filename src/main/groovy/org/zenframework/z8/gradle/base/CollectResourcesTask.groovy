package org.zenframework.z8.gradle.base

import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

class CollectResourcesTask extends ArtifactDependentTask {

	@OutputDirectory final DirectoryProperty output = project.objects.directoryProperty()
	@Input final replaceMatching = []
	@Input boolean stripFolders = false

	void replaceMatching(Object... matchings) {
		replaceMatching.addAll(Arrays.asList(matchings))
	}

	void into(Object into) {
		output.set(project.file(into))
	}

	@Override
	Task configure(Closure closure) {
		doLast {
			project.copy {
				includeEmptyDirs = false

				def extracted = extractRequires()
				if (stripFolders)
					extracted = extracted.files

				from(extracted) {
					include requiresInclude
					exclude requiresExclude
					if (replaceMatching != null && !replaceMatching.empty) {
						filesMatching(replaceMatching) {
							expand project: project
						}
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
