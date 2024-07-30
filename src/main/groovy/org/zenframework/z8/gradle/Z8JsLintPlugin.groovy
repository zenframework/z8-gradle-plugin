package org.zenframework.z8.gradle

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.zenframework.z8.gradle.node.NpmInstallTask
import org.zenframework.z8.gradle.node.RunJsLintTask

class Z8JsLintPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8NodeJsPlugin.class)

		project.ext.eslintCmd = Os.isFamily(Os.FAMILY_WINDOWS) ? 'eslint.cmd' : 'eslint'
		project.ext.jslintModule = 'git+https://git.doczilla.pro/CrazyDoctor/eslint-plugin-z8.git'

		project.tasks.register('installJsLint', NpmInstallTask) {
			group 'z8 build setup'
			description 'Install JS lint tools'
			module project.jslintModule
		}

		project.tasks.register('runJsLint', RunJsLintTask) {
			group 'z8 js'
			description 'Run JS lint'
			dependsOn project.tasks.installJsLint
			if(!project.hasProperty('warn')) {
				options '--quiet'
			}
		}

		project.tasks.register('fixJsLint', RunJsLintTask) {
			group 'z8 js'
			description 'Run JS lint and fix errors'
			dependsOn project.tasks.installJsLint
			options '--fix'
		}
	}

}
