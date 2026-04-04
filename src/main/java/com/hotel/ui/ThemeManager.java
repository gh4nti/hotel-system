package com.hotel.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javafx.scene.Scene;
import javafx.scene.Parent;

public final class ThemeManager {

	private static final String BASE_CSS = "/style.css";
	private static final String LIGHT_CSS = "/theme-light.css";
	private static final String DARK_CSS = "/theme-dark.css";
	private static final boolean SYSTEM_DARK_MODE = detectSystemDarkMode();

	private ThemeManager() {
	}

	public static void applyTheme(Scene scene) {
		if (scene == null) {
			return;
		}

		boolean darkMode = SYSTEM_DARK_MODE;
		String selectedTheme = darkMode ? DARK_CSS : LIGHT_CSS;

		scene.getStylesheets().clear();
		scene.getStylesheets().add(ThemeManager.class.getResource(BASE_CSS).toExternalForm());
		scene.getStylesheets().add(ThemeManager.class.getResource(selectedTheme).toExternalForm());

		Parent root = scene.getRoot();
		if (root != null) {
			root.getStyleClass().removeAll("theme-light", "theme-dark");
			root.getStyleClass().add(darkMode ? "theme-dark" : "theme-light");
		}
	}

	private static boolean detectSystemDarkMode() {
		String osName = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);

		try {
			if (osName.contains("win")) {
				return isWindowsDarkMode();
			}
			if (osName.contains("mac")) {
				return isMacDarkMode();
			}
			if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
				return isLinuxDarkMode();
			}
		} catch (Exception ignored) {
		}

		return false;
	}

	private static boolean isWindowsDarkMode() {
		String output = runCommand("reg", "query",
				"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
				"/v", "AppsUseLightTheme");

		if (output == null) {
			return false;
		}

		String normalized = output.toLowerCase(Locale.ENGLISH);
		return normalized.contains("0x0");
	}

	private static boolean isMacDarkMode() {
		String output = runCommand("defaults", "read", "-g", "AppleInterfaceStyle");
		return output != null && output.toLowerCase(Locale.ENGLISH).contains("dark");
	}

	private static boolean isLinuxDarkMode() {
		String colorScheme = runCommand("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
		if (colorScheme != null && colorScheme.toLowerCase(Locale.ENGLISH).contains("dark")) {
			return true;
		}

		String gtkTheme = runCommand("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme");
		return gtkTheme != null && gtkTheme.toLowerCase(Locale.ENGLISH).contains("dark");
	}

	private static String runCommand(String... command) {
		try {
			Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				StringBuilder output = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append('\n');
				}
				process.waitFor();
				return output.toString();
			}
		} catch (Exception ignored) {
			return null;
		}
	}
}
