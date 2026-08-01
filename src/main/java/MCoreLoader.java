package com.github.matin2.mcore;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jspecify.annotations.NonNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class MCoreLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://maven.myket.ir/").build());
        var kotlin = "2.4.20-Beta2";
        var kotlinxCoroutines = "1.11.0";
        var kotlinxSerialization = "1.11.0";
        var koin = "4.2.2";
        for (String library : new String[]{
                "org.jetbrains.kotlin:kotlin-stdlib:%s".formatted(kotlin),
                "org.jetbrains.kotlin:kotlin-reflect:%s".formatted(kotlin),
                "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:%s".formatted(kotlinxCoroutines),
                "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:%s".formatted(kotlinxSerialization),
                "io.insert-koin:koin-core-jvm:%s".formatted(koin)
        }) {
            resolver.addDependency(new Dependency(new DefaultArtifact(library), null));
        }
        classpathBuilder.addLibrary(resolver);
    }
}
