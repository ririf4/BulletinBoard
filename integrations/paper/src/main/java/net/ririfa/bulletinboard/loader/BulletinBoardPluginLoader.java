package net.ririfa.bulletinboard.loader;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class BulletinBoardPluginLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        PluginLibraries pluginLibraries = load();

        for (Dependency dep : pluginLibraries.asDependencies()) {
            resolver.addDependency(dep);
        }
        for (RemoteRepository repo : pluginLibraries.asRepositories()) {
            if (repo.getUrl().equals("https://repo.maven.apache.org/maven2/")) {
                resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
                continue;
            }
            resolver.addRepository(repo);
        }

        classpathBuilder.addLibrary(resolver);
    }

    private PluginLibraries load() {
        InputStream input = getClass().getResourceAsStream("/paper-libraries.json");
        if (input == null) {
            throw new IllegalStateException("Missing paper-libraries.json in resources");
        }

        return new Gson().fromJson(
                new InputStreamReader(input, StandardCharsets.UTF_8),
                PluginLibraries.class
        );
    }

    private static class PluginLibraries {

        @SerializedName("repositories")
        private Map<String, String> repositories;

        @SerializedName("dependencies")
        private List<String> dependencies;

        public List<Dependency> asDependencies() {
            List<Dependency> list = new ArrayList<>();
            for (String dep : dependencies) {
                list.add(new Dependency(new DefaultArtifact(dep), null));
            }
            return list;
        }

        public List<RemoteRepository> asRepositories() {
            List<RemoteRepository> list = new ArrayList<>();
            for (Map.Entry<String, String> entry : repositories.entrySet()) {
                list.add(new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build());
            }
            return list;
        }
    }
}
