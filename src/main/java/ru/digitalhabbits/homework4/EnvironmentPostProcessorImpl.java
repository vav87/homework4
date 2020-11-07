package ru.digitalhabbits.homework4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnvironmentPostProcessorImpl implements EnvironmentPostProcessor {

    private static final String CONFIG_PATH = "./src/main/resources/config";
    private static final String CONFIG_ROOT = "config/";
    private final PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<Resource> resourceList = getPropertyFiles();
        resourceList.stream()
                .map(resource -> loadProperty(resource))
                .forEach(propertySource -> environment.getPropertySources().addLast(propertySource));
    }

    private static List<Resource> getPropertyFiles() {

        BiPredicate<Path, BasicFileAttributes> predicate = (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith("properties");

        try(Stream<Path> entries = Files.find(Paths.get(CONFIG_PATH), 1, predicate)) {
            return entries
                    .map(e -> new ClassPathResource(CONFIG_ROOT+e.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot find configuration file in " + CONFIG_PATH);
        }
    }

    private PropertySource<?> loadProperty(Resource resource) {
        if (!resource.exists()) {
            throw new IllegalArgumentException("Resource " + resource + " does not exist");
        }
        try {
            return this.loader.load(resource.getFilename(), resource).get(0);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to load property configuration from " + resource, ex);
        }
    }
}
