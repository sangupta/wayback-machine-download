package com.sangupta.wmdownload;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.GsonUtils;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "download", description = "Download the entire site")
public class WaybackMachineDownloadCommand implements Runnable {

	@Arguments(description = "Configuration file to use")
	private List<String> configurations;
	
	@Override
	public void run() {
		if(AssertUtils.isEmpty(this.configurations)) {
			System.out.println("You must specify a configuration to download.");
			return;
		}
		
		if(this.configurations.size() > 1) {
			System.out.println("Only one configuration file is required.");
			return;
		}
		
		File file = new File(this.configurations.get(0));
		if(!file.exists()) {
			System.out.println("Configuration file does not exists!");
			return;
		}
		
		if(!file.isFile()) {
			System.out.println("Configuration file does not represent a valid file.");
			return;
		}
		
		if(!file.canRead()) {
			System.out.println("Unable to read file.");
			return;
		}
		
		String json = null;
		try {
			json = FileUtils.readFileToString(file);
		} catch (IOException e) {
			System.out.println("Unable to read configuration file from disk.");
			return;
		}
		
		if(AssertUtils.isEmpty(json)) {
			System.out.println("Configuration file is empty!");
			return;
		}
		
		WaybackConfiguration configuration = null;
		try {
			configuration = GsonUtils.getGson().fromJson(json, WaybackConfiguration.class);
		} catch(Exception e) {
			System.out.println("Unable to parse configuration file.");
			return;
		}
		
		WaybackMachineDownloader downloader = new WaybackMachineDownloader(configuration);
		
		// start downloading
		downloader.downloadNow();
	}

}
