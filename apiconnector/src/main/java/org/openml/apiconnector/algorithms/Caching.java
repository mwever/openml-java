package org.openml.apiconnector.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.openml.apiconnector.io.HttpConnector;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.settings.Settings;

public class Caching {

	public static void cache(String s, String type, int identifier, String extension) throws IOException {
		String directoryPath = Settings.CACHE_DIRECTORY + "/" + type;
		File directory = new File(directoryPath);
		directory.mkdirs();
		String name = type + "_" + identifier + "." + extension;
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(directory.getAbsolutePath() + "/" + name)));
		bw.append(s);
		bw.close();
		Conversion.log("OK", "Cache", "Stored to cache: " + type + "/" + name);
	}
	

	public static File cache(URL url, String type, int identifier, String extension) throws IOException {
		String directoryPath = Settings.CACHE_DIRECTORY + "/" + type;
		File directory = new File(directoryPath);
		directory.mkdirs();
		String name = type + "_" + identifier + "." + extension;
		File current = OpenmlConnector.getFileFromUrl(url,directory.getAbsolutePath() + "/" + name);
		Conversion.log("OK", "Cache", "Stored to cache: " + type + "/" + name);
		return current;
	}

	public static void cache(Object o, String type, int identifier, String extension) throws IOException {
		String directoryPath = Settings.CACHE_DIRECTORY + "/" + type;
		File directory = new File(directoryPath);
		directory.mkdirs();
		String name = type + "_" + identifier + "." + extension;
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(directory.getAbsolutePath() + "/" + name)));
		bw.append(HttpConnector.xstreamClient.toXML(o));
		bw.close();
		Conversion.log("OK", "Cache", "Stored to cache: " + type + "/" + name);
	}

	public static boolean in_cache(String type, int identifier, String extension) {
		File check = new File(Settings.CACHE_DIRECTORY + "/" + type + "/" + type + "_" + identifier + "." + extension);
		return check.exists();
	}

	public static File cached(String type, int identifier, String extension) throws IOException {
		String name = type + "_" + identifier + "." + extension;
		File cached = new File(Settings.CACHE_DIRECTORY + "/" + type + "/" + name);

		if (cached.exists() == false) {
			throw new IOException("Cache file of " + type + " #" + identifier + " not available.");
		} else {
			Conversion.log("OK", "Cache", "Obtained from cache: " + type + "/" + name);
			return cached;
		}
	}

}
