import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

/**
 * A script to only enable subprojects that you are working on.
 *
 * Usage:
 * - Run "java gradle/Focus.java notebook-networking-api-v1" to focus on notebook-networking-api-v1
 * - Run "java gradle/Focus.java notebook-networking-api-v1 notebook-sound-api-v1" to focus on notebook-networking-api-v1 and notebook-sound-api-v1
 * - Run "java gradle/Focus.java" to reset focus
 *
 * After running the script, refresh the Gradle project in your IDE.
 */
class Focus {
	// Matches the content of moduleDependencies and testDependencies
	private static final Pattern OUTER_PATTERN = Pattern.compile("(?:moduleDependencies|testDependencies)\\s*\\(.*?\\[\\s*([\\s\\S]*?)\\s*\\]\\s*\\)\n");
	// Matches the dependency string
	private static final Pattern INNER_PATTERN = Pattern.compile("['\\\"]([^'\\\"]+)['\\\"]");

	public static void main(String[] args) throws IOException {
		Path path = Paths.get("focus.txt");

		if (args.length == 0) {
			Files.deleteIfExists(path);
			System.out.println("Reset focus");
			return;
		}

		Set<String> dependencies = new HashSet<>();

		for (String arg : args) {
			readDependencies(arg, dependencies);
		}

		readDependencies("notebook-registry-sync-v0", dependencies);

		System.out.println("Focusing on:\n" + String.join("\n", dependencies));

		Files.writeString(path, String.join("\n", dependencies));
	}

	private static void readDependencies(String project, Set<String> dependencies) throws IOException {
		if (dependencies.contains(project)) {
			return;
		}

		dependencies.add(project);

		Path buildGradlePath = Paths.get(project, "build.gradle");

		if (Files.notExists(buildGradlePath)) {
			throw new RuntimeException("Project not found: " + project);
		}

		String content = Files.readString(buildGradlePath);
		Matcher outerMatcher = OUTER_PATTERN.matcher(content);

		while (outerMatcher.find()) {
			String outerMatch = outerMatcher.group(1);
			Matcher innerMatcher = INNER_PATTERN.matcher(outerMatch);
			while (innerMatcher.find()) {
				String dependency = innerMatcher.group(1).replace(":", "");
				readDependencies(dependency, dependencies);
			}
		}
	}
}
