package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.zenframework.z8.gradle.js.ConcatTask
import org.zenframework.z8.gradle.js.MinifyCssTask
import org.zenframework.z8.gradle.js.MinifyJsTask

class Z8JsBasePlugin implements Plugin<Project> {

	void apply(Project project) {
		if (!project.hasProperty('z8Version'))
			project.ext.z8Version = Z8Constants.Z8_DEFAULT_VERSION

		project.configurations {
			webtools
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
			webtools 'org.zenframework.z8.dependencies.minimizers:closure:3.0'
			webtools 'org.zenframework.z8.dependencies.minimizers:yuicompressor:3.0'
		}

		project.tasks.register('concatCss', ConcatTask) {
			group 'build'
			description 'Concat CSS files'
			requires project.configurations.webcompile
			source = project.file("${project.projectDir}/src/main/css")
			output = project.file("${project.buildDir}/web/debug/css/${project.rootProject.name}.css")
		}

		project.tasks.register('minifyCss', MinifyCssTask) {
			group 'build'
			description 'Minify CSS files'
			source = project.tasks.concatCss.output
			output = project.file("${project.buildDir}/web/css/${project.rootProject.name}.css")
			doLast {
				project.ant.replaceregexp(file: output, match: '(calc\\([\\d|\\.]+[^+]*)(\\+)', replace: '\\1 \\2 ', flags: 'g')
			}
		}

		project.tasks.register('concatJs', ConcatTask) {
			group 'build'
			description 'Concat JS files'
			requires project.configurations.webcompile
			source = project.file("${project.projectDir}/src/main/js")
			output = project.file("${project.buildDir}/web/debug/${project.rootProject.name}.js")
		}

		project.tasks.register('minifyJs', MinifyJsTask) {
			group 'build'
			description 'Minify JS files'
			source = project.tasks.concatJs.output
			output = project.file("${project.buildDir}/web/${project.rootProject.name}.js")
		}

		project.tasks.register('assembleJs') {
			group 'Build'
			description 'Assemble JS & CSS resources'
			dependsOn project.tasks.minifyCss, project.tasks.minifyJs
		}

	}

}
