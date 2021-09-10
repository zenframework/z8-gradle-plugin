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

		project.tasks.register('assembleJs', Zip) {
			group 'Build'
			description "Assemble JS/CSS archive ${archiveName} into ${project.relativePath(destinationDir)}"
			dependsOn project.tasks.concatCss, project.tasks.concatJs, project.tasks.collectJsResources

			archiveName "${project.name}-${project.version}.zip"
			destinationDir project.file("${project.buildDir}/libs")

			from(project.buildDir) {
				include 'web/**/*'
				exclude 'web/WEB-INF/**/*'
				includeEmptyDirs = false
			}
		}

		project.tasks.assemble.dependsOn project.tasks.assembleJs

		project.artifacts.add('webartifact', project.tasks.assembleJs.archivePath) {
			type 'zip'
			builtBy project.tasks.assembleJs
		}

		project.pluginManager.withPlugin('maven-publish') {
			project.publishing {
				repositories { mavenLocal() }
				publications {
					mavenJs(MavenPublication) { artifact source: project.tasks.assembleJs, extension: 'zip' }
				}
			}
		}
	}

}
