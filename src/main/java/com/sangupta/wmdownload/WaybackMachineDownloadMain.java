package com.sangupta.wmdownload;

import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Help;

public class WaybackMachineDownloadMain {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		CliBuilder<Runnable> builder = Cli.<Runnable>builder("wmd")
                .withDescription("Work with Archive.org WayBack Machine")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, WaybackMachineDownload.class, WaybackMachineConfigure.class);

        Cli<Runnable> gitParser = builder.build();

        gitParser.parse(args).run();
	}

}
