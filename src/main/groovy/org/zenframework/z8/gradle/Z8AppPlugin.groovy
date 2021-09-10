package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.Copy
import org.zenframework.z8.gradle.base.CollectResourcesTask
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
			boot "org.zenframework.z8:org.zenframework.z8.boot:${project.z8Version}"
			resources "org.zenframework.z8:org.zenframework.z8.resources:${project.z8Version}@zip"
		}

		project.sourceSets.main.resources.srcDirs "${project.srcMainDir}/resources"

		project.tasks.concatCss {
			output = project.file("${project.buildDir}/web/debug/css/${project.name}.css")
		}

		project.tasks.concatJs {
			output = project.file("${project.buildDir}/web/debug/${project.name}.js")
		}

		project.tasks.register('minifyCss', MinifyCssTask) {
			group 'build'
			description 'Minify CSS files'
			source = project.tasks.concatCss.output
			output = project.file("${project.buildDir}/web/css/${project.name}.css")
			doLast {
				project.ant.replaceregexp(file: output.get(), match: '(calc\\([\\d|\\.]+[^+]*)(\\+)', replace: '\\1 \\2 ', flags: 'g')
			}
		}

		project.tasks.register('minifyJs', MinifyJsTask) {
			group 'build'
			description 'Minify JS files'
			source = project.tasks.concatJs.output
			output = project.file("${project.buildDir}/web/${project.name}.js")
		}

		project.tasks.register('collectDependantWebinfResources', CollectResourcesTask) {
			description 'Collect dependant WEB-INF resources'

			requires project.configurations.blcompile
			requiresInclude 'WEB-INF/**/*'
			// TODO Remove later
			requiresExclude 'WEB-INF/project.xml', 'WEB-INF/server.properties'

			into "${project.buildDir}/web"
		}

		project.tasks.register('collectProjectWebResources', Copy) {
			dependsOn project.tasks.collectDependantWebinfResources
			from(project.srcMainDir) {
				include 'web/**/*'
				filesMatching(['web/**/*.html', 'web/WEB-INF/project.xml', 'web/WEB-INF/server.properties']) {
					expand project: project.project
				}
			}
			into project.buildDir
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
					project.tasks.collectProjectDebugResources, project.tasks.collectProjectWebResources,
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
