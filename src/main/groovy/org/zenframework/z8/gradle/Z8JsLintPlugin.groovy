package org.zenframework.z8.gradle

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input

class RunJsLintTask extends Exec {
	@Input String mask = '**/*.js'

	void mask(String mask) {
		this.mask = mask
	}

	@Override
	protected void exec() {
		File path = project.file("${project.srcMainDir}/js")
		if (!path.exists())
			return;

		commandLine "${project.nodejsPath}/node_modules/.bin/${project.eslintCmd}", path.getAbsolutePath() + '/' + mask,
				'--resolve-plugins-relative-to', project.nodejsPath

		super.exec();
	}
}

class Z8JsLintPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8NodeJsPlugin.class)

		project.ext.eslintCmd = Os.isFamily(Os.FAMILY_WINDOWS) ? 'eslint.cmd' : 'eslint'
		project.ext.jslintModule = 'git+https://git.crazydoctor.org/dz/eslint-plugin-z8.git'

		project.tasks.register('installJsLint', NpmInstallTask) {
			group 'z8 build setup'
			description 'Install JS lint tools'
			module project.jslintModule
		}

		project.tasks.register('runJsLint', RunJsLintTask) {
			group 'z8 js'
			description 'Run JS lint'
			dependsOn project.tasks.installJsLint
		}

	}

}
