package com.sangupta.wmdownload;

public class WaybackConfiguration {

	private String configName;
	
	private String waybackUrl;
	
	private String site;
	
	private String folderToDumpDownloadIn;
	
	private int maxDepth;
	
	// Usual accessors follow

	/**
	 * @return the configName
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @param configName the configName to set
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	/**
	 * @return the waybackUrl
	 */
	public String getWaybackUrl() {
		return waybackUrl;
	}

	/**
	 * @param waybackUrl the waybackUrl to set
	 */
	public void setWaybackUrl(String waybackUrl) {
		this.waybackUrl = waybackUrl;
	}

	/**
	 * @return the folderToDumpDownloadIn
	 */
	public String getFolderToDumpDownloadIn() {
		return folderToDumpDownloadIn;
	}

	/**
	 * @param folderToDumpDownloadIn the folderToDumpDownloadIn to set
	 */
	public void setFolderToDumpDownloadIn(String folderToDumpDownloadIn) {
		this.folderToDumpDownloadIn = folderToDumpDownloadIn;
	}

	/**
	 * @return the maxDepth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * @param maxDepth the maxDepth to set
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}
	
}
