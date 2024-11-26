package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.Copy
import org.zenframework.z8.gradle.base.BuildPropertiesTask
import org.zenframework.z8.gradle.base.CollectResourcesTask
import org.zenframework.z8.gradle.base.ServerPropertiesTask
import org.zenframework.z8.gradle.js.MinifyCssTask
import org.zenframework.z8.gradle.js.MinifyJsTask

class Z8AppPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(ApplicationPlugin.class)
		project.pluginManager.apply(Z8BlBasePlugin.class)
		project.pluginManager.apply(Z8JsBasePlugin.class)

		project.configurations {
			boot
			resources {
				canBeResolved = true
				canBeConsumed = false
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
			jst {
				canBeResolved = true
				canBeConsumed = false
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'bl'))
			}
		}

		project.dependencies {
			z8 "org.zenframework.z8:org.zenframework.z8.compiler"
			z8 "org.zenframework.z8:org.zenframework.z8.server"
			z8 "org.zenframework.z8:org.zenframework.z8.js"
			boot "org.zenframework.z8:org.zenframework.z8.boot"
			resources "org.zenframework.z8:org.zenframework.z8.resources"
		}

		project.ext.z8BootLib = project.configurations.boot.singleFile.name

		project.sourceSets.main.resources.srcDirs "${project.srcMainDir}/resources"

		project.tasks.concatCss {
			output = project.file("${project.buildDir}/web/debug/css/${project.name}.css")
		}

		project.tasks.concatJs {
			output = project.file("${project.buildDir}/web/debug/${project.name}.js")
		}

		project.tasks.collectDependantJsResources {
			replaceMatching 'web/**/*.html'
		}

		/* TODO: Return normal minification, as soon as we find a modern CSS minifier/compressor */
		project.tasks.register('minifyCss', /*MinifyCssTask*/Copy) {
			/*group 'build'
			description 'Minify CSS files'
			source = project.tasks.concatCss.output
			output = project.file("${project.buildDir}/web/css/${project.name}.css")
			doLast {
				project.ant.replaceregexp(file: output.get(), match: '(calc\\([\\d|\\.]+[^+]*)(\\+)', replace: '\\1 \\2 ', flags: 'g')
			}*/
			from project.tasks.concatCss.output
			into project.file("${project.buildDir}/web/css")
			rename { fileName ->
				"${project.name}.css"
			}
		}

		project.tasks.register('minifyJs', MinifyJsTask) {
			group 'build'
			description 'Minify JS files'
			languageOut = 'ECMASCRIPT_2017'
			source = project.tasks.concatJs.output
			output = project.file("${project.buildDir}/web/${project.name}.js")
		}

		project.tasks.register('collectDependantWebinfResources', CollectResourcesTask) {
			description 'Collect dependant WEB-INF resources'

			requires project.configurations.blcompile
			requiresInclude 'WEB-INF/**/*'
			// TODO Remove later
			requiresExclude 'WEB-INF/project.xml', 'WEB-INF/server.properties', 'WEB-INF/build.properties'

			into "${project.buildDir}/web"
		}

		project.tasks.register('collectProjectResources', Copy) {
			description 'Collect project own resources'

			into project.buildDir
		}

		project.tasks.register('serverProperties', ServerPropertiesTask) {
			description 'Generate server.properties'

			output = project.file("${project.buildDir}/web/WEB-INF/server.properties")
			template = 'WEB-INF/server.properties'
			customTemplate = 'WEB-INF/server.properties.custom'
		}

		project.tasks.register('buildProperties', BuildPropertiesTask) {
			description 'Generate build.properties'
			additionalConfiguration project.configurations.z8
			output = project.file("${project.buildDir}/web/WEB-INF/build.properties")
		}

		project.tasks.register('collectProjectDebugResources', Copy) {
			description 'Collect WEB debug resources'
			dependsOn project.tasks.collectJsResources

			from("${project.buildDir}/web") {
				include 'css/**/*'
				exclude '**/*.css'
			}
			from("${project.srcMainDir}/web") {
				include 'index.html'
			}
			into "${project.buildDir}/web/debug"
		}

		project.tasks.register('collectDistributionResources', CollectResourcesTask) {
			description 'Collect application resources'

			requires project.configurations.resources
			requiresInclude 'bin/**/*', 'conf/**/*'
			replaceMatching 'bin/*.sh', 'bin/service', 'conf/wrapper.conf'

			into project.buildDir
		}

		project.tasks.register('jstDependencies', Copy) {
			from project.configurations.jst
			into "${project.buildDir}/web/WEB-INF/just-in-time/dependencies"
		}

		project.tasks.register('assembleWeb') {
			group 'Build'
			description 'Assemble WEB resources'
			dependsOn project.tasks.minifyCss, project.tasks.minifyJs,
					project.tasks.serverProperties, project.tasks.buildProperties,
					project.tasks.collectProjectDebugResources, project.tasks.collectProjectResources,
					project.tasks.collectDependantWebinfResources, project.tasks.collectDistributionResources,
					project.tasks.jstDependencies
		}

		project.tasks.run.dependsOn project.tasks.assembleWeb
		project.tasks.distZip.dependsOn project.tasks.assembleWeb
		project.tasks.distTar.dependsOn project.tasks.assembleWeb
		project.tasks.installDist.dependsOn project.tasks.assembleWeb

		project.pluginManager.withPlugin('eclipse') {
			project.eclipse {
				autoBuildTasks project.tasks.assembleWeb
			}
		}
	}

}
