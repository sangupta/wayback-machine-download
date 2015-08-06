Wayback Machine Download
========================

A simple command-line interface to interact with <a href="http://archive.org">archive.org</a>
**WayBack Machine** including tool to download a snapshot of an entire site. This is particularly
useful for making up for lost backups and disaster recovery.

Dependencies
------------

The tool needs to the following to run:

* Oracle JDK 7 (should work with OpenJDK, but haven't tested)
* Internet Connection

Usage
-----

```
usage: wmd <command> [<args>]

The most commonly used wmd commands are:
    configure   Generate a configuration for the site
    download    Download the entire site
    help        Display help information

See 'wmd help <command>' for more information on a specific command.
```

To begin the download process, first generate a configuration answering the command prompts:

```
$ java -jar wayback-machine-download.jar configure

Configuration filename: somesite
Wayback URL to start with: http://web.archive.org/web/20140929053608/http://www.somesite.com/
Folder path where to dump the site to: ~/wayback
Max crawling depth: 5
```

This will generate a configuration file called `somesite.wmd` in the folder to be used.

To start the download process, execute:

```
$ java -jar wayback-machine-download.jar download somesite.wmd
```

This will crawl and dump the entire site in `~/wayback` folder.

Why this tool?
--------------

I lost code to a static website that I maintained for a friend of mine, and the server it was hosted
on crashed beyond recovery. I was left with two options:

* either to recreate the site manually, which would have been months if not days
* or, to try and download everything from archive.org

As I found there were two tools that could help me retrieve dump from wayback machine:

* warrick - https://code.google.com/p/warrick/
* http://waybackdownloader.com/

I tried using `warrick` but for some reason it did not work. My bad - am sure, I would not have 
configured it properly. Again am not a `Perl` guy so as to debug it. The second option requested 
me money, and thus was ruled out.

This led to the birth of this tool: to help me recover the site from `wayback machine`.

Am sure someone would need it too, one day!

License
-------
	
```
wmdownload - Wayback Machine Download
Copyright (c) 2015, Sandeep Gupta

http://sangupta.com/projects/wayback-machine-download

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
