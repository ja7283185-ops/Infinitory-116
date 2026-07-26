package furgl.infinitory.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Very small, dependency-free config. Kept intentionally simple (a .properties file)
 * instead of pulling in Cloth Config / ModMenu, so the mod has the fewest possible
 * moving parts to get building on 1.16.5.
 *
 * Edit config/infinitory.properties after the first run to change these values.
 */
public class Config {

	/** Max size any item stack can reach. Kept well under Integer.MAX_VALUE to avoid overflow when adding to a stack. */
	public static int maxStackSize = 1_000_000_000;

	/** Max number of "extra" inventory slots that can be unlocked (must end up a multiple of 9). Default = 81 rows. */
	public static int maxExtraSlots = 729;

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("infinitory.properties");

	public static void load() {
		Properties props = new Properties();

		if (Files.exists(CONFIG_PATH)) {
			try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
				props.load(in);
				maxStackSize = Integer.parseInt(props.getProperty("maxStackSize", String.valueOf(maxStackSize)));
				maxExtraSlots = Integer.parseInt(props.getProperty("maxExtraSlots", String.valueOf(maxExtraSlots)));
			} catch (IOException | NumberFormatException e) {
				System.err.println("[Infinitory] Failed to read config, using defaults: " + e.getMessage());
			}
		}

		// round maxExtraSlots up to a multiple of 9
		if (maxExtraSlots % 9 != 0) {
			maxExtraSlots += (9 - maxExtraSlots % 9);
		}

		save();
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("maxStackSize", String.valueOf(maxStackSize));
		props.setProperty("maxExtraSlots", String.valueOf(maxExtraSlots));

		try {
			if (!Files.exists(CONFIG_PATH.getParent())) {
				Files.createDirectories(CONFIG_PATH.getParent());
			}
			try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
				props.store(out, "Infinitory config - restart Minecraft after editing");
			}
		} catch (IOException e) {
			System.err.println("[Infinitory] Failed to save config: " + e.getMessage());
		}
	}
}
