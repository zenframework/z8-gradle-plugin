package org.zenframework.z8.gradle

import org.gradle.api.Project;
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources

class Z8BlPlugin extends Z8BlBasePlugin {

	void apply(Project project) {
		super.apply(project);

		project.pluginManager.apply(Z8JavaPlugin.class);

		project.dependencies {
			compile "org.zenframework.z8:org.zenframework.z8.server:${project.z8Version}"
			compile "org.zenframework.z8:org.zenframework.z8.lang:${project.z8Version}"

			blcompile "org.zenframework.z8:org.zenframework.z8.lang:${project.z8Version}@zip"
		}

		project.tasks.withType(JavaCompile) {
			dependsOn project.tasks.compileBl
		}

		project.tasks.withType(ProcessResources) {
			dependsOn project.tasks.compileBl
		}

		project.tasks.assemble {
			dependsOn project.tasks.blzip
		}

		project.tasks.clean.doLast {
			project.tasks.compileBl.output.deleteDir()
		}

		project.sourceSets.main {
			java.srcDir project.tasks.compileBl.output
			resources.srcDir project.tasks.compileBl.output
		}

		project.pluginManager.withPlugin('eclipse') {
			project.eclipse.classpath.file.whenMerged {
				if (!project.hasProperty('z8Home'))
					entries += new org.gradle.plugins.ide.eclipse.model.ProjectDependency('/org.zenframework.z8.lang')
			}
		}

		project.pluginManager.withPlugin('maven-publish') {
			project.publishing {
				repositories { mavenLocal() }
				publications {
					mavenBl(MavenPublication) { artifact source: project.tasks.blzip, extension: 'zip' }
				}
			}
		}
	}

}