package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements

class Z8JsBasePlugin implements Plugin<Project> {

	void apply(Project project) {
		if (!project.hasProperty('z8Version'))
			project.ext.z8Version = Z8Constants.Z8_DEFAULT_VERSION

		project.configurations {
			webcompile {
				canBeResolved = true
				canBeConsumed = false
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
			webartifact {
				canBeResolved = false
				canBeConsumed = true
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
		}

		project.tasks.register('concatCss', ConcatTask) {
			group 'build'
			description 'Concat CSS files'
			requires project.configurations.webcompile
			source = "${project.projectDir}/src/css"
			output = "${project.buildDir}/web/debug/css/${project.rootProject.name}.css"
		}

		project.tasks.register('concatJs', ConcatTask) {
			group 'build'
			description 'Concat JS files'
			requires project.configurations.webcompile
			source = "${project.projectDir}/src/js"
			output = "${project.buildDir}/web/debug/${project.rootProject.name}.js"
		}

	}

}
