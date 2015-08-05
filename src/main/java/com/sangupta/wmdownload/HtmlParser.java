package com.sangupta.wmdownload;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.UriUtils;

public class HtmlParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlParser.class);
	
	public static final String SCHEME_SEPARATOR = "://";

	private static final String WAY_BACK_TOOLBAR_START = "<!-- BEGIN WAYBACK TOOLBAR INSERT -->";

	private static final String WAY_BACK_TOOLBAR_END = "<!-- END WAYBACK TOOLBAR INSERT -->";

	private final String site;
	
	private final Document doc;
	
	private final Set<String> assets;
	
	private final Set<String> links;
	
	public HtmlParser(String html, String site) {
		String cleanedUpHtml = removeWayBackToolBar(html);
		
		this.doc = Jsoup.parse(cleanedUpHtml);
		this.site = site;
		
		// remove way back noise
		this.removeWayBackNoise();
		
		this.links = this.getChildPageLinks();
		this.assets = this.getAssetLinks();
		
		this.changeToRelativeLinks();
	}

	/**
	 * Remove wayback toolbar from html code by string operations
	 * 
	 * @param html
	 * @return
	 */
	private String removeWayBackToolBar(String html) {
		int start = html.indexOf(WAY_BACK_TOOLBAR_START);
		if(start < 0) {
			return html;
		}
		
		int end = html.indexOf(WAY_BACK_TOOLBAR_END, start);
		if(end < 0) {
			return html;
		}
		
		return html.substring(0, start) +  html.substring(end + WAY_BACK_TOOLBAR_END.length());
	}

	/**
	 * Remove other superflous tags
	 * 
	 */
	public void removeWayBackNoise() {
		// remove all script tags that do not http:// or http://
		Elements elements = doc.getElementsByTag("script");
		if(elements != null && !elements.isEmpty()) {
			for(int index = 0; index < elements.size(); index++) {
				Element element = elements.get(index);
				if(element.attr("src").equals("/static/js/analytics.js")) {
					element.remove();
				}
				
				String text = element.html();
				if(text.trim().startsWith("archive_analytics.values.server_name")) {
					element.remove();
				}
			}
		}
		
		// remove all link tags
		elements = doc.getElementsByTag("link");
		if(elements != null && !elements.isEmpty()) {
			for(int index = 0; index < elements.size(); index++) {
				Element element = elements.get(index);
				if(element.attr("href").equals("/static/css/banner-styles.css")) {
					element.remove();
				}
			}
		}
		
		// remove comment around file archiving
		elements = doc.getAllElements();
		for(int index = 0; index < elements.size(); index++) {
			Element element = elements.get(index);
			for(Node node : element.childNodes()) {
				if(node instanceof Comment) {
					Comment comment = (Comment) node;
					String text = comment.getData().trim();
					if(text.startsWith("FILE ARCHIVED ON") && text.contains("JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE")) {
						node.remove();
					}
				}
			}
		}
	}

	private Set<String> getChildPageLinks() {
		return getElementAttributes("a", "href");
	}
	
	private Set<String> getElementAttributes(String tag, String attribute) {
		Elements elements = this.doc.select(tag);
		if(elements == null || elements.isEmpty()) {
			return null;
		}
		
		Set<String> links = new HashSet<>();
		for(int index = 0; index < elements.size(); index++) {
			org.jsoup.nodes.Element element = elements.get(index);
			
			String url = element.attr(attribute);
			if(AssertUtils.isNotEmpty(url)) {
//				url = element.absUrl(url);
				links.add(url);
			}
		}
		
		return links;
	}

	private Set<String> getAssetLinks() {
		Set<String> assets = new HashSet<>();
		
		Set<String> some = getElementAttributes("img", "src");
		if(AssertUtils.isNotEmpty(some)) {
			assets.addAll(some);
		}
		
		some = getElementAttributes("link", "href");
		if(AssertUtils.isNotEmpty(some)) {
			assets.addAll(some);
		}
		
		some = getElementAttributes("script", "src");
		if(AssertUtils.isNotEmpty(some)) {
			assets.addAll(some);
		}
		
		return assets;
	}

	public byte[] getPageBytes() {
		return this.doc.toString().getBytes();
	}

	public void changeToRelativeLinks() {
		changeLinks("a", "href");
		changeLinks("img", "src");
		changeLinks("link", "href");
		changeLinks("script", "src");
		changeLinks("form", "action");
		
		// TODO: remove them
		changeLinks("input", "action");
		changeLinks("td", "background");
	}
	
	private void changeLinks(String tag, String attribute) {
		Elements elements = this.doc.select(tag);
		if(elements == null || elements.isEmpty()) {
			return;
		}
		
		for(int index = 0; index < elements.size(); index++) {
			org.jsoup.nodes.Element element = elements.get(index);
			
			String url = element.attr(attribute);
			if(AssertUtils.isNotEmpty(url)) {
//				url = element.absUrl(url);
				String modified = changeLink(url, this.site);
				if(modified != null) {
					element.attr(attribute, modified);
				}
			}
		}
	}

	public static String changeLink(String url, String site) {
		if(url.startsWith("#")) {
			return null;
		}
		
		if(url.startsWith("javascript:")) {
			return null;
		}
		
		if(url.startsWith("mailto:")) {
			return null;
		}
		
		int index = url.lastIndexOf(SCHEME_SEPARATOR);
		if(index < 0) {
			LOGGER.warn("Unable to compute filename from url {}", url);
			return url;
		}
		
		String prefix = url.substring(0,  index);
		url = url.substring(index + SCHEME_SEPARATOR.length());
		if(!url.startsWith(site)) {
			int last = prefix.lastIndexOf('/');
			return UriUtils.addWebPaths(prefix.substring(last + 1) + SCHEME_SEPARATOR, url);
		}
		
		index = url.indexOf('/');
		return url.substring(index + 1);
	}

	public static void main(String[] args) {
		String url = "http://web.archive.org/web/20140929053608js_/http://www.google.com/cse/brand?form=cse-search-box&lang=en";
		String site = "www.matrika-india.org";
		System.out.println(changeLink(url, site));
	}
	
	/**
	 * @return the assets
	 */
	public Set<String> getAssets() {
		return assets;
	}

	/**
	 * @return the links
	 */
	public Set<String> getLinks() {
		return links;
	}
	
}
