package org.zenframework.z8.gradle.js

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.zenframework.z8.gradle.util.Z8GradleUtil

class MinifyJsTask extends JavaExec {

	@InputFile final RegularFileProperty source = project.objects.fileProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()
	@Optional @Input String warningLevel = null
	@Optional @Input String languageIn = null
	@Optional @Input String languageOut = null

	@Override
	public Task configure(Closure closure) {
		classpath = project.configurations.jstools
		main = 'com.google.javascript.jscomp.CommandLineRunner'
		super.configure(closure);
	}

	@Override
	public void exec() {
		def source = Z8GradleUtil.getPath(source)
		def output = Z8GradleUtil.getPath(output)
		def argsList = [ '--rewrite_polyfills', 'false' ]
		if (warningLevel != null)
			argsList.addAll([ '--warning_level', warningLevel ])
		if (languageIn != null)
			argsList.addAll([ '--language_in', languageIn ])
		if (languageOut != null)
			argsList.addAll([ '--language_out', languageOut ])
		argsList.addAll([ '--js_output_file', output, source ])
		args = argsList
		super.exec();
	}

}
