package org.zenframework.z8.gradle

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input

class NpmInstallTask extends Exec {
	@Input String module

	void module(String module) {
		this.module = module
	}

	@Override
	protected void exec() {
		workingDir project.nodejsPath
		commandLine project.npmCmd, 'install', module

		super.exec();
	}
}

class Z8NodeJsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.ext.npmCmd = Os.isFamily(Os.FAMILY_WINDOWS) ? 'npm.cmd' : 'npm'
		project.ext.nodejsPath = new File("${System.getProperty('user.home')}/.z8/nodejs")

		project.nodejsPath.mkdirs()
	}

}
