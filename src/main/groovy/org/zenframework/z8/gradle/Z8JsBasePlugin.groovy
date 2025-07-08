package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.zenframework.z8.gradle.base.CollectResourcesTask
import org.zenframework.z8.gradle.base.ConcatTask
import org.zenframework.z8.gradle.js.CompileSassTask
import org.zenframework.z8.gradle.js.EmbedSvgTask

class Z8JsBasePlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(BasePlugin.class)
		project.pluginManager.apply(Z8BasePlugin.class)

		project.configurations {
			jstools
			webcompile {
				canBeResolved = true
				canBeConsumed = false
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
		}

		project.dependencies {
			jstools 'org.zenframework.z8.dependencies.tools:closure-compiler-v20180805:4.0'
			jstools 'org.zenframework.z8.dependencies.tools:yuicompressor:4.0'
		}

		project.tasks.register('compileSass', CompileSassTask) {
			group 'Build'
			description 'Compile SASS files'
			// Closure allows setting non-existing file
			source = { project.file("${project.srcMainDir}/sass").with { it.exists() ? it : null } }.call()
			output = project.file("${project.buildDir}/tmp/${project.name}.sass.css")
		}

		project.tasks.register('embedSvg', EmbedSvgTask) {
			group 'build'
			description 'Embed SVG into CSS file'
			source.from project.file("${project.srcMainDir}/css/img").with { it.exists() ? project.fileTree(it) : null }
			output = project.file("${project.buildDir}/tmp/${project.name}.svg.css")
		}

		project.tasks.register('concatCss', ConcatTask) {
			group 'Build'
			description 'Concat CSS files'
			dependsOn project.tasks.compileSass
			dependsOn project.tasks.embedSvg
			requires project.configurations.webcompile
			beforeSource "${project.buildDir}/tmp/${project.name}.sass.css"
			beforeSource "${project.buildDir}/tmp/${project.name}.svg.css"
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

		project.tasks.register('concatTestJs', ConcatTask) {
			group 'Build'
			description 'Concat JS test files'
			requires project.configurations.webcompile
			// Closure allows setting non-existing file
			source = { project.file("${project.srcTestDir}/js").with { it.exists() ? it : null } }.call()
			output = project.file("${project.buildDir}/web/${project.name}.test.js")
		}

		project.tasks.register('collectDependantJsResources', CollectResourcesTask) {
			requires project.configurations.webcompile
			requiresExclude '**/*.css', '**/*.js'
			output = project.file(project.buildDir)
		}

		project.tasks.register('collectProjectJsResources', Copy) {
			from(project.srcMainDir) {
				include 'css/**/*'
				exclude 'css/**/*.css', 'css/**/*.buildorder'
			}
			from("${project.srcMainDir}/html")
			into "${project.buildDir}/web"
		}

		project.tasks.register('collectJsResources') {
			description 'Collect JS/CSS resources'
			dependsOn project.tasks.collectDependantJsResources, project.tasks.collectProjectJsResources
		}

		project.tasks.register('assembleJs') {
			group 'Build'
			description 'Assemble JS/CSS & web resources'
			dependsOn project.tasks.concatCss, project.tasks.concatJs, project.tasks.concatTestJs, project.tasks.collectJsResources
		}
	}

}
