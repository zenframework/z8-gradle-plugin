package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.zenframework.z8.gradle.js.CollectResourcesTask
import org.zenframework.z8.gradle.js.ConcatTask

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
			group 'Build'
			description 'Concat CSS files'
			requires project.configurations.webcompile
			// Closure allows setting non-existing file
			source = { project.file("${project.srcMainDir}/css").with { it.exists() ? it : null } }.call()
			output = project.file("${project.buildDir}/web/css/${project.name}.css")
		}

		project.tasks.register('concatJs', ConcatTask) {
			group 'Build'
			description 'Concat JS files'
			requires project.configurations.webcompile
			// Closure allows setting non-existing file
			source = { project.file("${project.srcMainDir}/js").with { it.exists() ? it : null } }.call()
			output = project.file("${project.buildDir}/web/${project.name}.js")
		}

		project.tasks.register('collectDependantJsResources', CollectResourcesTask) {
			requires project.configurations.webcompile
			requiresExclude '**/*.css', '**/*.js'
			output = project.file("${project.buildDir}")
		}

		project.tasks.register('collectOwnJsResources', Copy) {
			from("${project.srcMainDir}/css") {
				exclude '**/*.css', '**/*.buildorder'
			}
			into "${project.buildDir}/web/css"
		}

		project.tasks.register('collectJsResources') {
			group 'Build'
			description 'Collect JS/CSS resources'
			dependsOn project.tasks.collectDependantJsResources, project.tasks.collectOwnJsResources
		}

		project.tasks.register('assembleJs') {
			group 'Build'
			description 'Assemble JS & CSS resources'
			dependsOn project.tasks.concatCss, project.tasks.concatJs, project.tasks.collectJsResources
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
	}

}
