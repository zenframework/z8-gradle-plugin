package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project;
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import org.zenframework.z8.gradle.bl.CompileBlForkedTask

class Z8BlPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8JavaPlugin.class)
		project.pluginManager.apply(Z8BasePlugin.class)

		project.configurations {
			compiler
			blcompile {
				canBeResolved = true
				canBeConsumed = false
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							project.objects.named(LibraryElements, 'bl'))
				}
			}
			blartifact {
				canBeResolved = false
				canBeConsumed = true
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							project.objects.named(LibraryElements, 'bl'))
				}
			}
		}

		project.dependencies {
			compiler "org.zenframework.z8:org.zenframework.z8.compiler:${project.z8Version}"

			compile "org.zenframework.z8:org.zenframework.z8.server:${project.z8Version}"
			compile "org.zenframework.z8:org.zenframework.z8.lang:${project.z8Version}"

			blcompile "org.zenframework.z8:org.zenframework.z8.lang:${project.z8Version}@zip"
		}

		project.tasks.register('compileBl', CompileBlForkedTask) {
			group 'build'
			description 'Compile BL sources'
		}

		project.tasks.z8zip {
			dependsOn project.tasks.compileBl
		}

		project.tasks.withType(JavaCompile) {
			dependsOn project.tasks.compileBl
		}

		project.tasks.withType(ProcessResources) {
			dependsOn project.tasks.compileBl
		}

		project.tasks.assemble {
			dependsOn project.tasks.z8zip
		}

		project.tasks.clean.doLast {
			project.tasks.compileBl.output.deleteDir()
		}

		project.sourceSets.main {
			java.srcDir project.tasks.compileBl.output
			resources.srcDir project.tasks.compileBl.output
		}

		project.pluginManager.withPlugin('eclipse') {
			project.eclipse.project.natures 'org.zenframework.z8.pde.ProjectNature'
			project.eclipse.classpath.file.whenMerged {
				if (!project.hasProperty('z8Home'))
					entries += new org.gradle.plugins.ide.eclipse.model.ProjectDependency('/org.zenframework.z8.lang')
			}
		}

		project.afterEvaluate {
			project.components.findByName('java').addVariantsFromConfiguration(project.configurations.blartifact) {
				it.mapToMavenScope("compile")
			}
		}

		project.artifacts.add('blartifact', project.tasks.z8zip.archivePath) {
			type 'zip'
			builtBy project.tasks.z8zip
		}
	}

}