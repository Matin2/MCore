package com.github.matin2.mcore;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class MCoreLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://maven.myket.ir/").build());
        Arrays.stream(new String[]{
                "org.jetbrains.kotlin:kotlin-stdlib:2.4.0",
                "org.jetbrains.kotlin:kotlin-reflect:2.4.0",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.11.0",
                "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0",
                "io.insert-koin:koin-core-jvm:4.2.2"
        }).map(dependency -> new Dependency(new DefaultArtifact(dependency), null)).forEach(resolver::addDependency);
        classpathBuilder.addLibrary(resolver);
    }
}
