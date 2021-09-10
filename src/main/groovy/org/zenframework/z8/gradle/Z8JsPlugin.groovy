package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip

class Z8JsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8JsBasePlugin.class)

		project.configurations {
			webartifact {
				canBeResolved = false
				canBeConsumed = true
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
		}

		project.tasks.register('assembleWebartifact', Zip) {
			group 'Build'
			description "Assemble JS/CSS archive ${archiveName} into ${project.relativePath(destinationDir)}"
			dependsOn project.tasks.assembleJs

			archiveName "${project.name}-${project.version}.zip"
			destinationDir project.file("${project.buildDir}/libs")

			from(project.buildDir) {
				include 'web/**/*'
				exclude 'web/WEB-INF/**/*'
				includeEmptyDirs = false
			}
		}

		project.tasks.assemble.dependsOn project.tasks.assembleWebartifact

		project.artifacts.add('webartifact', project.tasks.assembleWebartifact.archivePath) {
			type 'zip'
			builtBy project.tasks.assembleWebartifact
		}

		project.pluginManager.withPlugin('maven-publish') {
			project.publishing {
				repositories { mavenLocal() }
				publications {
					mavenJs(MavenPublication) { artifact source: project.tasks.assembleWebartifact, extension: 'zip' }
				}
			}
		}
	}

}
