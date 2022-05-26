package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import groovy.text.SimpleTemplateEngine

class ServerPropertiesTask extends DefaultTask {

	@Optional @InputFile final RegularFileProperty source = project.objects.fileProperty()
	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	@Optional @Input final Map<String, String> defaults = [
		'application.database.schema' : 'public',
		'application.database.user' : 'z8',
		'application.database.password' : 'z8',
		'application.database.driver' : 'org.postgresql.Driver',
		'application.database.connection' : 'jdbc:postgresql://localhost/z8',
		'application.database.charset' : 'UTF-8',

		'application.email.login' : 'noreply@yourdomain',
		'application.email.password' : '',

		'application.language' : 'ru',

		'web.server.start.application.server' : true,
		'web.server.start.authority.center' : true,
		'web.server.start.interconnection.center' : false,
		'web.server.useContainerSession' : true,

		'web.server.cache.enable' : false,

		'fts.configuration' : 'russian',

		'web.server.upload.max' : 15,
		'web.client.download.max' : 1,

		'application.server.port' : '',
		'application.server.host' : '',

		'authority.center.port' : '',
		'authority.center.host' : '',
		'authority.center.session.timeout' : '',

		'interconnection.center.port' : '',
		'interconnection.center.host' : '',

		'trace.sql' : false,
		'trace.sql.connections' : false,

		'scheduler.enabled' : true,

		'transport.job.cron' : '',
		'maintenance.job.cron' : '',

		'office.home' : '/path/to/libreoffice'
	]

	@Optional @Input final Map<String, String> custom = [:]

	void defaults(def defaults) {
		this.defaults.putAll(defaults)
	}

	void custom(def custom) {
		this.custom.putAll(custom)
	}

	@Override
	public Task configure(Closure closure) {
		defaults.each {
			if (!project.hasProperty(it.key))
				project.ext[it.key] = it.value
		}
		return super.configure(closure);
	}

	@TaskAction
	def run() {
		def source = this.source.asFile.getOrElse(getClass().getResource('/template/server.properties'))
		def output = this.output.asFile.get()

		def text = new SimpleTemplateEngine().createTemplate(source.text).make([project:project]).toString()
		if (!custom.isEmpty())
			text += "\n\n### Custom properties\n\n${custom.entrySet().join('\n')}"

		output.text = text
	}

}
