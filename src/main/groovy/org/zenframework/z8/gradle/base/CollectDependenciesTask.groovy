package org.zenframework.z8.gradle.base

import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ArtifactResolutionResult
import org.gradle.api.artifacts.result.ComponentArtifactsResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges
import org.zenframework.z8.gradle.util.Z8GradleUtil
import org.gradle.api.file.FileSystemOperations;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

class CollectDependenciesTask extends DefaultTask {

	@OutputDirectory DirectoryProperty output = project.objects.directoryProperty()

	void output(Object path) {
		output.set(project.file(path))
	}

	@TaskAction
	public void exec(InputChanges inputChanges) {
		Logger logger = getLogger();
		Set<ModuleComponentIdentifier> componentIds = new HashSet<>();
		File mavenLocalRepo = output.asFile.orNull

		// Collect dependencies IDs
		for (Configuration config : project.configurations) {
			if (!config.canBeResolved)
				continue;

			try {
				config.getIncoming().getResolutionResult().getAllComponents().forEach { component ->
					if (component.id instanceof ModuleComponentIdentifier)
						componentIds.add((ModuleComponentIdentifier) component.id);
				}
			} catch (Exception e) {
				logger.error("Skipping metadata extraction for configuration: ${config.name}", e);
			}
		}

		if (componentIds.empty)
			return;

		// Query POMs
		ArtifactResolutionResult pomQueryResult = project.dependencies
				.createArtifactResolutionQuery()
				.forComponents(componentIds)
				.withArtifacts(MavenModule.class, MavenPomArtifact.class)
				.execute();

		for (ComponentArtifactsResult component : pomQueryResult.resolvedComponents) {
			if (!(component.id instanceof ModuleComponentIdentifier))
				continue;

			ModuleComponentIdentifier id = (ModuleComponentIdentifier) component.id;
			File targetDir = getTargetDir(mavenLocalRepo, id.group, id.module, id.version);

			component.getArtifacts(MavenPomArtifact.class).forEach { artifactResult ->
				if (artifactResult instanceof ResolvedArtifactResult)
					project.copy { spec ->
						spec.from(((ResolvedArtifactResult) artifactResult).file)
						spec.into(targetDir)
					}
			}
		}

		// Copy JARs
		for (Configuration config : project.getConfigurations()) {
			logger.info("Config: ${config} ${config.canBeResolved ? 'can be resolved' : 'CAN NOT BE RESOLVED'}")

			if (!config.canBeResolved)
				continue;

			try {
				config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
					def id = artifact.moduleVersion.id;
					File targetDir = getTargetDir(mavenLocalRepo, id.group, id.name, id.version);
					logger.info("Copy ${artifact.file} to ${targetDir}")
					project.copy { spec ->
						spec.from(artifact.file)
						spec.into(targetDir)
					}
				}
			} catch (Exception e) {
				logger.error("Skipping file copy for configuration: ${config.name}", e);
			}
		}
	}

	protected File getTargetDir(File repoRoot, String group, String artifact, String version) {
		String groupPath = group.replace('.', '/');
		return new File(repoRoot, groupPath + "/" + artifact + "/" + version);
	}
}
