package org.zenframework.z8.gradle.node;

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
