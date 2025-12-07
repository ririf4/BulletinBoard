package net.ririfa.bulletinboard.loader;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class BulletinBoardPluginLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        PluginLibraries pluginLibraries = load();

        for (Dependency dependency : pluginLibraries.asDependencies()) {
            resolver.addDependency(dependency);
        }

        for (RemoteRepository repo : pluginLibraries.asRepositories()) {
            if ("https://repo.maven.apache.org/maven2/".equals(repo.getUrl())) {
                resolver.addRepository(
                        new RemoteRepository.Builder(
                                "central",
                                "default",
                                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
                        ).build()
                );
            } else {
                resolver.addRepository(repo);
            }
        }

        classpathBuilder.addLibrary(resolver);
    }

    private PluginLibraries load() {
        InputStream input = getClass().getResourceAsStream("/paper-libraries.json");
        if (input == null) {
            throw new IllegalStateException("Missing paper-libraries.json in resources");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            return new Gson().fromJson(reader, PluginLibraries.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class PluginLibraries {
        @SerializedName("repositories")
        private Map<String, String> repositories = new HashMap<>();

        @SerializedName("dependencies")
        private List<String> dependencies = new ArrayList<>();

        public List<Dependency> asDependencies() {
            List<Dependency> result = new ArrayList<>();
            for (String dep : dependencies) {
                result.add(new Dependency(new DefaultArtifact(dep), null));
            }
            return result;
        }

        public List<RemoteRepository> asRepositories() {
            List<RemoteRepository> result = new ArrayList<>();
            for (Map.Entry<String, String> entry : repositories.entrySet()) {
                result.add(new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build());
            }
            return result;
        }

        public Map<String, String> getRepositories() {
            return repositories;
        }

        public void setRepositories(Map<String, String> repositories) {
            this.repositories = repositories;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }
    }
}