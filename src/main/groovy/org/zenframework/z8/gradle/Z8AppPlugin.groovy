package org.zenframework.z8.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.Copy
import org.zenframework.z8.gradle.base.ArtifactDependentTask

class Z8AppPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.pluginManager.apply(ApplicationPlugin.class)
		project.pluginManager.apply(Z8BasePlugin.class)
		project.pluginManager.apply(Z8JavaPlugin.class)

		project.configurations {
			boot
			webresources {
				canBeResolved = true
				canBeConsumed = false
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
						project.objects.named(LibraryElements, 'web'))
			}
		}

		project.dependencies {
			boot "org.zenframework.z8:org.zenframework.z8.boot:${project.z8Version}"
			webresources "org.zenframework.z8:org.zenframework.z8.resources:${project.z8Version}@zip"
		}

		project.sourceSets.main.resources.srcDirs "${project.srcMainDir}/resources"

		project.tasks.register('prepareWeb', ArtifactDependentTask) {
			description 'Prepare WEB resources'

			requires project.configurations.webresources
			requiresInclude 'bin/**/*', 'conf/**/*', 'web/css/**'
			requiresInclude 'web/WEB-INF/fonts/**'
			requiresInclude 'web/WEB-INF/reports/**'
			requiresInclude 'web/WEB-INF/resources/**'

			doLast {
				project.copy {
					into project.buildDir

					from (project.srcMainDir) {
						include 'web/**/*'
						filesMatching(['web/**/*.html', 'web/WEB-INF/project.xml']) {
							expand project: project.project
						}
					}

					if (!requiresInclude.isEmpty()) {
						from (extractRequires()) {
							include requiresInclude
							filesMatching(['bin/*.sh', 'conf/wrapper.conf']) {
								expand project: project.rootProject
							}
						}
					}
				}
			}
		}

		project.tasks.register('prepareDebug', Copy) {
			description 'Prepare WEB debug resources'
			dependsOn project.tasks.prepareWeb
		
			from ("${project.buildDir}/web") {
				include 'css/**/*'
				exclude '**/*.css'
			}
			from("${project.srcMainDir}/web") {
				include 'index.html'
			}
			into "${project.buildDir}/web/debug"
		}

		project.tasks.register('assembleWeb') {
			group 'Build'
			description 'Assemble WEB resources'
			dependsOn project.tasks.prepareWeb, project.tasks.prepareDebug
		}

		project.pluginManager.withPlugin('z8-js') {
			project.tasks.assembleWeb.dependsOn project.tasks.assembleJs
		}

		project.tasks.assemble.dependsOn project.tasks.assembleWeb
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
