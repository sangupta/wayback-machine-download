package com.sangupta.wmdownload;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sangupta.jerry.http.WebInvoker;
import com.sangupta.jerry.http.WebResponse;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ReadableUtils;
import com.sangupta.jerry.util.UriUtils;

public class WaybackMachineDownloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WaybackMachineDownloader.class);
	
	private final String baseUrl;
	
	private final String site;
	
	private final int maxDepth;
	
	private final File baseDir;
	
	private final Set<String> allFiles = new HashSet<>();
	
	private final Queue<CrawlableUrl> crawlQueue = new LinkedBlockingQueue<>();

	/**
	 * Create an instance of {@link WaybackMachineDownloader}.
	 * 
	 * @param configuration
	 */
	public WaybackMachineDownloader(WaybackConfiguration configuration) {
		if(configuration == null) {
			throw new IllegalArgumentException("Wayback configuration cannot be null");
		}
		
		this.baseUrl = configuration.getWaybackUrl();
		this.site = configuration.getSite();
		this.baseDir = new File(configuration.getFolderToDumpDownloadIn());
		this.maxDepth = configuration.getMaxDepth();
		
		this.crawlQueue.offer(new CrawlableUrl(this.baseUrl, 1));
	}
	
	/**
	 * Start the download process now.
	 * 
	 */
	public void downloadNow() {
		final long start = System.currentTimeMillis();
		try {
			this.startCrawling();
		} finally {
			final long end = System.currentTimeMillis();
			System.out.println("Finished downloading " + this.allFiles.size() + " files in " + ReadableUtils.getReadableTimeDuration(end - start));
		}
	}
	
	/**
	 * Crawl one URL at a time.
	 * 
	 */
	private void startCrawling() {
		do {
			CrawlableUrl cu = this.crawlQueue.poll();
			if(cu == null) {
				LOGGER.debug("Nothing more to crawl... exit!");
				return;
			}
			
			downloadPage(cu);
		} while(true);
	}

	/**
	 * Download one page/file/asset at a time.
	 * 
	 * @param crawlableUrl
	 */
	private void downloadPage(CrawlableUrl crawlableUrl) {
		String url = crawlableUrl.url;
		if(AssertUtils.isEmpty(url)) {
			return;
		}
		
		if(url.startsWith("/")) {
			url = "http://web.archive.org" + url;
		}
		
		LOGGER.debug("Crawling url: {}", url);
		
		// fetch from internet
		WebResponse response = WebInvoker.getResponse(url);
		if(response == null) {
			LOGGER.debug("Webresponse is null for url: {}", url);
			return;
		}
		
		if(!response.isSuccess()) {
			LOGGER.debug("Webresponse is non-success as: {} for url: {}", response, url);
			return;
		}
		
		// check if asset
		if(crawlableUrl.depth == 0) {
			// file is asset
			// write directly to disk
			writeFileToDisk(url, response.asBytes());
			return;
		}
		
		// parse html file
		HtmlParser parser = new HtmlParser(response.getContent(), this.site);
		
		// write page to disk
		writeFileToDisk(url, parser.getPageBytes());
		
		// get all assets
		Set<String> assets = parser.getAssets();
		if(AssertUtils.isNotEmpty(assets)) {
			for(String asset : assets) {
				this.addToQueueIfNeedBe(asset, 0);
			}
		}

		// check if need to crawl more
		if(crawlableUrl.depth >= this.maxDepth) {
			LOGGER.debug("Reached maxdepth on url {}", url);
			return;
		}
		
		// get all links from file
		Set<String> childPages = parser.getLinks();
		if(AssertUtils.isNotEmpty(childPages)) {
			for(String childPage : childPages) {
				this.addToQueueIfNeedBe(childPage, crawlableUrl.depth + 1);
			}
		}
		
	}
	
	private void addToQueueIfNeedBe(String url, int depth) {
		String actualUrl = HtmlParser.changeLink(url, this.site);
		
		String host = UriUtils.extractHost(actualUrl);
		if(AssertUtils.isNotEmpty(host)) {
			if(!this.site.equals(host)) {
				LOGGER.debug("Skipping url to add to crawl queue as outside of domain {}: {}", host, url);
				return;
			}
		}
		
		boolean added = allFiles.add(actualUrl);
		if(!added) {
			return;
		}
		
		LOGGER.debug("Adding url to crawling queue: {}", actualUrl);
		this.crawlQueue.add(new CrawlableUrl(url, depth));
	}

	private void writeFileToDisk(String urlOriginal, byte[] bytes) {
		String url = HtmlParser.changeLink(urlOriginal, this.site);
		
		// check if its a relative url
		if(UriUtils.appearsValidUrl(url)) {
			return;
		}
		
		// go ahead - write to disk
		if(AssertUtils.isEmpty(url)) {
			url = "index.html";
		}
		
		LOGGER.debug("Writing file to disk for url {} to file {}", urlOriginal, url);
		File file = new File(this.baseDir, url);
		try {
			// create parent dir
			file.getAbsoluteFile().getParentFile().mkdirs();
			
			// write data
			FileUtils.writeByteArrayToFile(file, bytes);
		} catch (IOException e) {
			LOGGER.warn("Unable to write file {}", url);
		}
	}

	public static class CrawlableUrl {
		
		String url;
		
		int depth;
		
		public CrawlableUrl(String url, int depth) {
			this.url = url;
			this.depth = depth;
		}
		
	}
}
