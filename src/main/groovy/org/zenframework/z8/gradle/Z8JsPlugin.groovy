package org.zenframework.z8.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class Z8JsPlugin extends Z8JsBasePlugin{

	void apply(Project project) {
		super.apply(project)

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
