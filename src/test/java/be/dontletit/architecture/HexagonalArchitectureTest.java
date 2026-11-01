package be.dontletit.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Dependencies only point inwards. The REST adapters reach the core through the API ports only,
 * the data adapters implement the SPI ports, and the core never knows about either.
 */
class HexagonalArchitectureTest {

    private static final Path SOURCES = Path.of("src/main/java/be/dontletit");
    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+)", Pattern.MULTILINE);
    private static final String BASE = "be.dontletit.";
    private static final List<String> JDK = List.of("java.");
    private static final List<String> CDI = List.of("jakarta.inject.", "jakarta.enterprise.", "jakarta.annotation.");

    @Test
    void domain_dependsOnNothingButItself() {
        assertOnly("domain", List.of(BASE + "domain."));
    }

    @Test
    void api_dependsOnNothingButItself() {
        assertOnly("api", List.of(BASE + "api."));
    }

    @Test
    void spi_dependsOnlyOnTheDomain() {
        assertOnly("spi", List.of(BASE + "spi.", BASE + "domain."));
    }

    @Test
    void application_dependsOnThePortsAndTheDomain_neverOnInfrastructure() {
        assertOnly("application", concat(List.of(BASE + "application.", BASE + "api.", BASE + "spi.", BASE + "domain."), CDI));
    }

    @Test
    void restAdapters_reachTheCoreOnlyThroughTheApiPorts() {
        assertNone("infrastructure/rest", List.of(BASE + "application.", BASE + "spi.", BASE + "domain.", BASE + "infrastructure.data."));
    }

    @Test
    void dataAdapters_implementTheSpiPorts_andNeverTouchTheApiOrTheApplication() {
        assertNone("infrastructure/data", List.of(BASE + "api.", BASE + "application.", BASE + "infrastructure.rest."));
    }

    /** Fails listing every import of {@code layer} outside the JDK and the allowed prefixes. */
    private static void assertOnly(String layer, List<String> allowed) {
        List<String> bad = imports(layer).stream()
                .filter(i -> Stream.concat(JDK.stream(), allowed.stream()).noneMatch(i.imported()::startsWith))
                .map(Import::toString)
                .toList();
        assertEquals(List.of(), bad, layer + " imports what it must not");
    }

    /** Fails listing every import of {@code layer} that starts with a forbidden prefix. */
    private static void assertNone(String layer, List<String> forbidden) {
        List<String> bad = imports(layer).stream()
                .filter(i -> forbidden.stream().anyMatch(i.imported()::startsWith))
                .map(Import::toString)
                .toList();
        assertEquals(List.of(), bad, layer + " imports what it must not");
    }

    record Import(String file, String imported) {
        @Override
        public String toString() {
            return file + " -> " + imported;
        }
    }

    private static List<Import> imports(String layer) {
        Path root = SOURCES.resolve(layer);
        List<Import> found = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher matcher = IMPORT.matcher(Files.readString(file));
                while (matcher.find()) found.add(new Import(SOURCES.relativize(file).toString(), matcher.group(1)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (found.isEmpty() && !Files.isDirectory(root)) throw new IllegalStateException("No sources under " + root);
        return found;
    }

    private static List<String> concat(List<String> a, List<String> b) {
        return Stream.concat(a.stream(), b.stream()).toList();
    }
}
