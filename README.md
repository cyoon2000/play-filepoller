# File Poller using Play 2.0 and Anorm DB  #

## Install play 2.0.4 and run file poller app ##
1. Install play 2.0.4 (get the app working first, then later on, you can look into migrating to 2.1)
  http://www.playframework.com/download

2. Verify your installation and 'play' is in your PATH.  
	$ play -version
		
3. download source (READ-ONLY)
	$ git clone https://github.com/cyoon2000/play-filepoller.git 

4. Start dev mode (dynamic)
	$ play ~run

5. Browse 'File Inventory Status' in browser http://localhost:9000/

##  Application specific properties: conf/application.conf ##

Configurable properties are : list of directories to be polled, interval, max # of workers
<blockquote><p>
	filepoller.roots="/www/a/data/test/filepoller, /www/a/data/test/filepoller2"  
	filepoller.interval=2000  
	filepoller.max_num_workers=5
</p></blockquote>	
	
1. Make sure you point the polling root to existing directory (or create one) before you start the app, otherwise poller won't start.
2. To test, drop some image file in one or all of the directories and see if shows up in the inventory ( http://localhost:9000/ ) after N (milli)seconds.



