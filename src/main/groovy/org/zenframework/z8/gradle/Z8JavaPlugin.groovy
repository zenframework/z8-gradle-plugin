package org.zenframework.z8.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile

class Z8JavaPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(Z8BasePlugin.class)
		project.pluginManager.apply(JavaPlugin.class)

		// Set Java version and sources encoding
		project.tasks.withType(JavaCompile) {
			sourceCompatibility = JavaVersion.VERSION_1_8
			targetCompatibility = JavaVersion.VERSION_1_8
			options.encoding = 'UTF-8'
		}

		project.sourceSets {
			main {
				java.outputDir = project.file("${project.buildDir}/classes/main")
				resources.outputDir = project.file("${project.buildDir}/classes/main")
				output.resourcesDir = project.file("${project.buildDir}/classes/main")
			}
		}

		project.tasks.jar.duplicatesStrategy = 'exclude'

		project.pluginManager.withPlugin('eclipse') {
			project.eclipse {
				// Eclipse: default java output -> $buildDir/classes/default
				it.classpath.defaultOutputDir = new File(project.buildDir, 'classes/main')
				// Eclipse: java source folders output -> ${buildDir}/classes/...
				it.classpath.file.whenMerged {
					entries.findAll { entry ->
						entry instanceof org.gradle.plugins.ide.eclipse.model.SourceFolder
					}.each { entry ->
						entry.output = entry.output.replace('bin/', project.relativePath("${project.buildDir}/classes/") + '/')
					}
				}
			}
		}

		project.pluginManager.withPlugin('maven-publish') {
			project.publishing {
				repositories { mavenLocal() }
				publications {
					maven(MavenPublication) { from project.components.java }
				}
			}
		}
	}

}