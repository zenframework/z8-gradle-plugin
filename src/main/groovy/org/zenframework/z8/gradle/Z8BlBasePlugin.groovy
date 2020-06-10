package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements

class Z8BlBasePlugin implements Plugin<Project> {

	void apply(Project project) {
		if (!project.hasProperty('z8Version'))
			project.ext.z8Version = Z8Constants.Z8_DEFAULT_VERSION

		project.configurations {
			compiler
			blcompile {
				canBeResolved = true
				canBeConsumed = false
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							project.objects.named(LibraryElements, 'bl'))
				}
			}
			blartifact {
				canBeResolved = false
				canBeConsumed = true
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							project.objects.named(LibraryElements, 'bl'))
				}
			}
		}

		project.dependencies {
			compiler "org.zenframework.z8:org.zenframework.z8.compiler:${project.z8Version}"
		}

		project.tasks.register('compileBl', CompileBlForkedTask) {
			group 'build'
			description 'Compile BL sources'
		}

		project.tasks.register('blzip', BlzipTask) {
			group = 'build'
			description = "Assemble BL archive ${archiveName} into ${project.relativePath(destinationDir)}"
		}

		project.afterEvaluate {
			project.components.findByName('java').addVariantsFromConfiguration(project.configurations.blartifact) {
				it.mapToMavenScope("compile")
			}
		}

		project.pluginManager.withPlugin('eclipse') {
			project.eclipse.project.natures 'org.zenframework.z8.pde.ProjectNature'
		}

		project.artifacts.add('blartifact', project.tasks.blzip.archivePath) {
			type 'zip'
			builtBy project.tasks.blzip
		}
	}

}
