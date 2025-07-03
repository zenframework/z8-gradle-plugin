package org.zenframework.z8.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySubstitution
import org.gradle.api.artifacts.component.ModuleComponentSelector

class Z8BasePlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		if (!project.hasProperty('srcMainDir'))
			project.ext.srcMainDir = project.file("${project.projectDir}/src/main")
		if (!project.hasProperty('srcTestDir'))
			project.ext.srcTestDir = project.file("${project.projectDir}/src/test")
		if (!project.hasProperty('resolveGroups'))
			project.ext.resolveGroups = [ project.group ]
		if (!project.hasProperty('z8DependenciesVersion'))
			project.ext.z8DependenciesVersion = '4.1'
	
		project.configurations {
			z8
		}

		project.configurations.all {
			resolutionStrategy.dependencySubstitution.all { DependencySubstitution dependency ->
				if (dependency.requested instanceof ModuleComponentSelector) {
					def resolve = project.resolveGroups.find { dependency.requested.group.startsWith it } != null
					project.logger.debug "Resolve ${dependency.requested.group} by ${project.resolveGroups} ... ${resolve ? 'Ok' : 'Skip'}"
					if (resolve) {
						def targetProject = project.findProject(":${dependency.requested.module}")
						if (targetProject != null) {
							project.logger.info "Z8 [${project.name}]: substitute ${dependency.requested.displayName} by ${targetProject}"
							dependency.useTarget targetProject
						}
					}
				}
			}
		}

		project.tasks.register('z8Info', DefaultTask) {
			doLast {
				println "Z8 Project [${project.name}] sources main dir: ${project.srcMainDir}"
			}
		}
	}

}
