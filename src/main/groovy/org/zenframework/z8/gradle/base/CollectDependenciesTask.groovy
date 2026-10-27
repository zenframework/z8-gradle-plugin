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
		File mavenLocalRepo = output.asFile.getOrNull()

		// Collect dependencies IDs
		for (Configuration config : project.getConfigurations()) {
			if (!config.isCanBeResolved())
				continue;

			try {
				config.getIncoming().getResolutionResult().getAllComponents().forEach { component ->
					if (component.getId() instanceof ModuleComponentIdentifier)
						componentIds.add((ModuleComponentIdentifier) component.getId());
				}
			} catch (Exception e) {
				logger.info("Skipping metadata extraction for configuration: " + config.getName());
			}
		}

		if (componentIds.isEmpty())
			return;

		// Query POMs
		ArtifactResolutionResult pomQueryResult = project.getDependencies()
				.createArtifactResolutionQuery()
				.forComponents(componentIds)
				.withArtifacts(MavenModule.class, MavenPomArtifact.class)
				.execute();

		for (ComponentArtifactsResult component : pomQueryResult.getResolvedComponents()) {
			if (!(component.getId() instanceof ModuleComponentIdentifier))
				continue;

			ModuleComponentIdentifier id = (ModuleComponentIdentifier) component.getId();
			File targetDir = getTargetDir(mavenLocalRepo, id.getGroup(), id.getModule(), id.getVersion());

			component.getArtifacts(MavenPomArtifact.class).forEach { artifactResult ->
				if (artifactResult instanceof ResolvedArtifactResult)
					project.copy { spec ->
						spec.from(((ResolvedArtifactResult) artifactResult).getFile())
						spec.into(targetDir)
					}
			}
		}

		// Copy JARs
		for (Configuration config : project.getConfigurations()) {
			if (!config.isCanBeResolved())
				continue;

			try {
				config.getResolvedConfiguration().getResolvedArtifacts().forEach { artifact ->
					var id = artifact.getModuleVersion().getId();
					File targetDir = getTargetDir(mavenLocalRepo, id.getGroup(), id.getName(), id.getVersion());
					project.copy { spec ->
						spec.from(artifact.getFile())
						spec.into(targetDir)
					}
				}
			} catch (Exception e) {
				logger.info("Skipping file copy for configuration: " + config.getName());
			}
		}
	}

	protected File getTargetDir(File repoRoot, String group, String artifact, String version) {
		String groupPath = group.replace('.', '/');
		return new File(repoRoot, groupPath + "/" + artifact + "/" + version);
	}
}
