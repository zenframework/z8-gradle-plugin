package org.zenframework.z8.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project;
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip

class Z8BlPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8BlBasePlugin.class)

		project.configurations {
			blartifact {
				canBeResolved = false
				canBeConsumed = true
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							project.objects.named(LibraryElements, 'bl'))
				}
			}
		}

		project.tasks.register('collectNls', DefaultTask) {
			doLast {
				project.copy {
					for (File sourcePath : project.tasks.compileBl.sources) {
						from(sourcePath) {
							includeEmptyDirs = false
							include '**/*.nls'
						}
					}

					into "${project.buildDir}/WEB-INF/resources"
				}
			}
		}

		project.tasks.register('assembleBl', Zip) {
			group 'Build'
			dependsOn project.tasks.compileBl, project.tasks.collectNls
			description "Assemble BL archive ${archiveName} into ${project.relativePath(destinationDir)}"

			archiveName "${project.name}-${project.version}.zip"
			destinationDir project.file("${project.buildDir}/libs")

			for (File sourcePath : project.tasks.compileBl.sources) {
				from(sourcePath) {
					includeEmptyDirs = false
					include '**/*.bl'
				}
			}

			from("${project.buildDir}") {
				includeEmptyDirs = false
				include 'WEB-INF/**/*'
			}
		}

		project.tasks.assemble.dependsOn project.tasks.assembleBl

		project.afterEvaluate {
			project.components.findByName('java').addVariantsFromConfiguration(project.configurations.blartifact) {
				it.mapToMavenScope("compile")
			}
		}

		project.artifacts.add('blartifact', project.tasks.assembleBl.archivePath) {
			type 'zip'
			builtBy project.tasks.assembleBl
		}

		project.pluginManager.withPlugin('maven-publish') {
			project.publishing {
				repositories { mavenLocal() }
				publications {
					mavenBl(MavenPublication) { artifact source: project.tasks.assembleBl, extension: 'zip' }
				}
			}
		}
	}

}