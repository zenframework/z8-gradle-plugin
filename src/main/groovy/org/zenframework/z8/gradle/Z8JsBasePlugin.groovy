package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.bundling.Zip
import org.zenframework.z8.gradle.base.ArtifactDependentTask
import org.zenframework.z8.gradle.js.ConcatTask
import org.zenframework.z8.gradle.js.MinifyCssTask
import org.zenframework.z8.gradle.js.MinifyJsTask

class Z8JsBasePlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(BasePlugin.class)

		project.configurations {
			jstools
			webcompile {
				canBeResolved = true
				canBeConsumed = false
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
			webartifact {
				canBeResolved = false
				canBeConsumed = true
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
		}

		project.dependencies {
			jstools 'org.zenframework.z8.dependencies.minimizers:closure:3.0'
			jstools 'org.zenframework.z8.dependencies.minimizers:yuicompressor:3.0'
		}

		project.tasks.register('concatCss', ConcatTask) {
			group 'build'
			description 'Concat CSS files'
			requires project.configurations.webcompile
			source = project.file("${project.srcMainDir}/css")
			output = project.file("${project.buildDir}/web/debug/css/${project.name}.css")
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

		project.tasks.register('concatJs', ConcatTask) {
			group 'build'
			description 'Concat JS files'
			requires project.configurations.webcompile
			source = project.file("${project.srcMainDir}/js")
			output = project.file("${project.buildDir}/web/debug/${project.name}.js")
		}

		project.tasks.register('minifyJs', MinifyJsTask) {
			group 'build'
			description 'Minify JS files'
			source = project.tasks.concatJs.output
			output = project.file("${project.buildDir}/web/${project.name}.js")
		}

		project.tasks.register('prepareResources', ArtifactDependentTask) {
			description 'Prepare WEB resources'

			requires project.configurations.webcompile

			doLast {
				project.copy {
					into project.buildDir

					from ("${project.srcMainDir}/css") {
						exclude '.*', '**/*.css'
					}

					if (!requiresInclude.isEmpty()) {
						from (extractRequires()) {
							include requiresInclude
						}
					}
				}
			}
		}

		project.tasks.register('assembleJs') {
			group 'Build'
			description 'Assemble JS & CSS resources'
			dependsOn project.tasks.minifyCss, project.tasks.minifyJs, project.tasks.prepareResources
		}

		project.tasks.register('jszip', Zip) {
			group 'Build'
			description "Assemble JS/CSS archive ${archiveName} into ${project.relativePath(destinationDir)}"
			dependsOn project.tasks.assembleJs

			archiveName "${project.name}-${project.version}.zip"
			destinationDir project.file("${project.buildDir}/libs/")
			from("${project.buildDir}") {
				include 'web/**/*'
				includeEmptyDirs = false
			}
		}

		project.artifacts.add('webartifact', project.tasks.jszip) {
			builtBy project.tasks.jszip
		}

		// TODO eclipse autoBuildTasks
		//eclipse {
		//	autoBuildTasks assembleWeb
		//}
	}

}
