package com.sangupta.wmdownload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;
import com.sangupta.jerry.util.GsonUtils;
import com.sangupta.jerry.util.StringUtils;

import io.airlift.airline.Command;

@Command(name = "configure", description = "Generate a configuration for the site")
public class WaybackMachineConfigure implements Runnable {

	@Override
	public void run() {
		String configName = ConsoleUtils.readLine("Configuration filename: ", true);
		checkEmpty(configName);
		
		String waybackUrl = ConsoleUtils.readLine("Wayback URL to start with: ", true);
		checkEmpty(waybackUrl);
		String siteName = extractSiteName(waybackUrl);
		
		String folderToDumpDownloadIn = ConsoleUtils.readLine("Folder path where to dump the site to: ", true);
		checkEmpty(folderToDumpDownloadIn);
		File file = com.sangupta.jerry.util.FileUtils.resolveToFile(folderToDumpDownloadIn).getAbsoluteFile();
		if(!file.exists()) {
			file.mkdirs();
		} else {
			if(!file.isDirectory()) {
				System.out.println("Given path does not represent a valid folder... exiting!");
				return;
			}
		}
		
		String maxDepth = ConsoleUtils.readLine("Max crawling depth: ", true);
		checkEmpty(maxDepth);
		int depth = StringUtils.getIntValue(maxDepth, -1);
		if(depth <= 0) {
			System.out.println("Max depth cannot be less than or equal to zero.");
			return;
		}
		
		WaybackConfiguration configuration = new WaybackConfiguration();
		configuration.setConfigName(configName);
		configuration.setWaybackUrl(waybackUrl);
		configuration.setFolderToDumpDownloadIn(file.getAbsolutePath());
		configuration.setMaxDepth(depth);
		configuration.setSite(siteName);
		
		try {
			FileUtils.write(new File(configName + ".wmd"), GsonUtils.getGson().toJson(configuration));
		} catch (IOException e) {
			System.out.println("Unable to write configuration file to disk");
			e.printStackTrace();
		}
	}

	private String extractSiteName(String waybackUrl) {
		int index = waybackUrl.lastIndexOf("://");
		if(index < 0) {
			System.out.println("Invalid wayback URL: " + waybackUrl);
			System.exit(0);
			return null;
		}
		
		String site = waybackUrl.substring(index + 3);
		if(!site.endsWith("/")) {
			return site;
		}
		
		return site.substring(0, site.length() - 1);
	}

	private void checkEmpty(String param) {
		if(AssertUtils.isEmpty(param)) {
			System.out.println("Field cannot be empty... exiting!");
			System.exit(0);
		}
	}
}
