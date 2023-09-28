package org.zenframework.z8.gradle.base

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import groovy.text.SimpleTemplateEngine

class ServerPropertiesTask extends DefaultTask {

	static final String DEFAULT_TEMPLATE = '/template/server.properties'

	@OutputFile final RegularFileProperty output = project.objects.fileProperty()

	@Optional @Input final Map<String, String> defaults = [
		'application.database.schema' : 'public',
		'application.database.user' : 'z8',
		'application.database.password' : 'z8',
		'application.database.driver' : 'org.postgresql.Driver',
		'application.database.connection' : 'jdbc:postgresql://localhost/z8',
		'application.database.charset' : 'UTF-8',

		'application.name' : 'Z8 App',

		'application.language' : 'ru',

		'security.log.enabled': false,

		'web.server.scheme': 'https',

		'web.server.startApplicationServer' : true,
		'web.server.startAuthorityCenter' : true,
		'web.server.startInterconnectionCenter' : false,
		'web.server.useContainerSession' : true,

		'web.server.cache.enable' : false,

		'web.server.sso.authenticator': '',

		'oauth.config': '../../conf/oauth/config.json',
		'saml.config': '../../conf/saml/onelogin.saml.properties',

		'trusted.users.create': false,

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

		'office.home' : '/path/to/libreoffice',
		'converter.url' : '',

		'mail.smtp.config': '../conf/smtp.properties',
	]

	@Input def template
	@Input def customTemplate
	@Input def customProperties = [:]

	void defaults(def defaults) {
		this.defaults.putAll(defaults)
	}

	void customProperties(def customProperties) {
		this.customProperties.putAll(custom)
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
		def template = project.file(this.template)
		if (template == null || !template.exists())
			template = getClass().getResource(DEFAULT_TEMPLATE)
		def customTemplate = project.file(this.customTemplate)
		def output = this.output.asFile.get()

		def text = new SimpleTemplateEngine().createTemplate(template.text).make([project:project]).toString()

		if (customTemplate.exists() || !customProperties.isEmpty()) {
			text += '\n\n### Custom properties'
			if (customTemplate.exists())
				text += '\n\n' + new SimpleTemplateEngine().createTemplate(customTemplate.text).make([project:project]).toString()
			if (!customProperties.isEmpty())
				text += '\n\n' + customProperties.entrySet().join('\n')
		}

		output.text = text
	}

}
