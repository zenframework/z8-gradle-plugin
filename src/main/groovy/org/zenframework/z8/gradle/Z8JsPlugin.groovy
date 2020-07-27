package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.publish.maven.MavenPublication

class Z8JsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8BasePlugin.class)
		project.pluginManager.apply(Z8JsBasePlugin.class)

		project.artifacts.add('webartifact', project.tasks.jszip) {
			builtBy project.tasks.jszip
		}

		project.pluginManager.withPlugin('maven-publish') {
			project.publishing {
				repositories { mavenLocal() }
				publications {
					mavenJs(MavenPublication) { artifact source: project.tasks.jszip, extension: 'zip' }
				}
			}
		}
	}

}
