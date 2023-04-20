package org.zenframework.z8.gradle.node

import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input

class RunJsLintTask extends Exec {
	@Input String mask = '**/*.js'
	@Input ListProperty<String> options = project.objects.listProperty(String.class) 

	void mask(String mask) {
		this.mask = mask
	}

	void options(Object... options) {
		this.options.addAll(options.collect { it.toString() })
	}

	@Override
	public Task configure(Closure closure) {
		options '--resolve-plugins-relative-to', project.nodejsPath
		return super.configure(closure);
	}

	@Override
	protected void exec() {
		File path = project.file("${project.srcMainDir}/js")

		if (!path.exists())
			return;

		commandLine(["${project.nodejsPath}/node_modules/.bin/${project.eslintCmd}", path.absolutePath + '/' + mask]
				+ options.get())

		super.exec();
	}
}
