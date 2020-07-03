package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySubstitution
import org.gradle.api.artifacts.component.ModuleComponentSelector

class Z8BasePlugin implements Plugin<Project> {

	static final String Z8_DEFAULT_VERSION = '1.3.0'

	@Override
	void apply(Project project) {
		if (!project.hasProperty('z8Version'))
			project.ext.z8Version = Z8_DEFAULT_VERSION
		if (!project.hasProperty('srcMainDir'))
			project.ext.srcMainDir = project.file("${project.projectDir}/src/main")

		project.configurations.all {
			resolutionStrategy.dependencySubstitution.all { DependencySubstitution dependency ->
				if (dependency.requested instanceof ModuleComponentSelector && dependency.requested.group == project.group) {
					def targetProject = project.findProject(":${dependency.requested.module}")
					if (targetProject != null) {
						project.logger.info "Z8 [${project.name}]: substitute ${dependency.requested.displayName} by ${targetProject}"
						dependency.useTarget targetProject
					}
				}
			}
		}
	}

}
