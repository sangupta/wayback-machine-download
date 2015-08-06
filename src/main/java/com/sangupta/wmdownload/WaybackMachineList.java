package com.sangupta.wmdownload;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sangupta.jerry.http.WebInvoker;
import com.sangupta.jerry.http.WebResponse;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;
import com.sangupta.jerry.util.StringUtils;
import com.sangupta.jerry.util.UriUtils;

public class WaybackMachineList implements Runnable {
	
	public static void main(String[] args) {
		WaybackMachineList wml = new WaybackMachineList();
		wml.run();
	}
	
	@Override
	public void run() {
		String site = ConsoleUtils.readLine("Site: ", true);
		if(AssertUtils.isEmpty(site)) {
			System.out.println("Site to dump cannot be empty.");
			return;
		}
		
		System.out.println("Finding number of snapshots at archive.org...");
		
		String url = "http://web.archive.org/web/*/http://" + site;
		WebResponse response = WebInvoker.getResponse(url);
		if(response == null) {
			System.out.println("Unable to connect to the internet.");
			return;
		}
		
		if(!response.isSuccess()) {
			System.out.println("Non-success response from archive.org.");
			return;
		}
		
		String html = response.getContent();
		
		// jsoup parse
		Document doc = Jsoup.parse(html);
		
		// find number of times it has been read
		Elements pops = doc.select("#wbMeta");
		if(pops == null || pops.isEmpty()) {
			System.out.println("Unable to detect number of snapshots for the website.");
			return;
		}
		
		// find number
		String text = pops.get(0).text();
		int saved = text.indexOf("Saved ");
		int times = text.indexOf(" times");
		
		String number = text.substring(saved + 6, times);
		System.out.println("Found " + number + " number of snapshots.");
		
		// continue to list
		String cont = ConsoleUtils.readLine("\nContinue to list (yes/no)? ", true);
		if(!("yes".equalsIgnoreCase(cont))) {
			return;
		}
		
		// find all pages to hit from the timeline
		pops = doc.select("#sparklineImgId");
		if(pops == null || pops.isEmpty()) {
			System.out.println("Cannot read spark line for exact details of snapshots.");
			return;
		}
		
		// store the years in a list
		final List<Integer> yearList = new ArrayList<>();
		
		// read the spark line
		String sparkUrl = pops.get(0).attr("src");
		
		// parse spark line and read times
		int index = StringUtils.nthIndexOf(sparkUrl, "_", 2);
		sparkUrl = sparkUrl.substring(index + 1);
		
		// break on year
		String[] tokens = sparkUrl.split("_");
		for(String token : tokens) {
			String[] subTokens = token.split(":");
			if(subTokens.length != 3) {
				System.out.println("Unable to parse year subtokens from sparkline: " + token);
				return;
			}
			
			int year = StringUtils.getIntValue(subTokens[0], -1);
			boolean hasSnapshots = subTokens[2].contains("1");
			
			if(hasSnapshots) {
				yearList.add(year);
			}
		}
		
		// check emptyness
		if(AssertUtils.isEmpty(yearList)) {
			System.out.println("Unable to read year's where snapshots are available");
			return;
		}
		
		// find all pop elements
		final List<Snapshot> snapshots = new ArrayList<>();
		
		for(Integer year : yearList) {
			// hit the web page
			url = "http://web.archive.org/web/" + year + "0601000000*/http://" + site;
			System.out.println("Reading snapshots for year: " + year + " via url: " + url);
			
			// get the response page
			response = WebInvoker.getResponse(url);
			if(response == null || !response.isSuccess()) {
				System.out.println("Unable to fetch snapshot list for year: " + year);
				continue;
			}
			
			// construct a jsoup doc
			doc = Jsoup.parse(response.getContent());
			
			// parse
			pops = doc.select(".pop");
			if(pops == null || pops.isEmpty()) {
				System.out.println("Unable to find any snapshot for the website.");
				return;
			}
			
			for(Element pop : pops) {
				Elements links = pop.select("li a");
				if(links == null || links.isEmpty()) {
					continue;
				}
				
				for(Element link : links) {
					Snapshot snapshot = new Snapshot();
					snapshot.time = link.text();
					snapshot.url = UriUtils.addWebPaths("http://web.archive.org", link.attr("href"));
					
					snapshots.add(snapshot);
				}
			}
		}
		
		// output the entire list
		System.out.println("\n\nFound " + snapshots.size() + " snapshots:");
		for(index = 0; index < snapshots.size(); index++) {
			Snapshot snapshot = snapshots.get(index);
			System.out.println("Snapshot " + (index + 1) + ":: " + snapshot);
		}
	}
	
	public static class Snapshot {
		public String time;
		public String url;
		
		@Override
		public String toString() {
			return this.time + ": " + this.url;
		}
	}

}
