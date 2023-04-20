package org.zenframework.z8.gradle

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project

class Z8NodeJsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.ext.npmCmd = Os.isFamily(Os.FAMILY_WINDOWS) ? 'npm.cmd' : 'npm'
		project.ext.nodejsPath = new File("${System.getProperty('user.home')}/.z8/nodejs")

		project.nodejsPath.mkdirs()
	}

}
