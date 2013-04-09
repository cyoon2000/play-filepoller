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


	filepoller.roots="/www/a/data/test/filepoller, /www/a/data/test/filepoller2"    
	
	filepoller.interval=2000    

	filepoller.max_num_workers=5   
	
	
1. Make sure you point the polling root to existing directory (or create one) before you start the app, otherwise poller won't start.
2. To test, drop some image file in one or all of the directories and see if shows up in the inventory ( http://localhost:9000/ ) after N (milli)seconds.

## H2 Database Web Console ##

  	1) cd to the app directory, then open play console 
		$ play  
	
  	2) Launch web console in browser 
		$ h2-browser
	
	3) Run file poller application. Run Evolution script if in-memory. 
		$ run 
	
	4) In web console Enter JDBC URL, User Name/Password as you specified in applciation.conf
		In-memory JDBC URL : 'jdbc:h2:mem:<db-name>'  or 'jdbc:h2:tcp://localhost/mem:<db-name>' (if web console is launched from separate console as app)
  		File-base JDBC URL : 'jdbc:h2:file:/data/<db-name>'
  		
    	5) Test the connection and connect, then you should be able to see the table and data.

	H2 Reference : http://www.andyroot.com/2012/11/open-play-frameworks-h2-console.html


## IDEs ##
	Intellij IDEA community edition or Eclipse
	
	1. Intellij IDEA community edition (free)
		1) play new your-project-name
		2) open IDEA, create new project, give same name as project-name.
	
	2. Eclipse : I have not tried but here is reference
	http://stackoverflow.com/questions/10038673/using-eclipse-with-play-framework-2-0
		

## Git - one time setup ##

	* If creating your own repo:
	(Create a new repository on the command line)

	$ touch README.md
	
	$ git init
	
	$ git add README.md
	
	$ git commit -m "first commit"
	
	$ git remote add origin https://github.com/<your repo name>
	
	$ git push -u origin master


	* If pushing to existing repo:
	( Push an existing repository from the command line)

	$ git remote add origin https://github.com/<existing repo name>
	
	$ git push -u origin master


## Git - daily basis commands ##

	$ git pull
	
	... then modify files ...

	$ git add <directory or filename>  (or git rm <filename>)
	
	$ git status

	$ git commit -m "init checkin for source"

	$ git push -u origin master


## Future reference - Possible Migration to Maven ##

	(how to setup a Maven Scala project)
	http://theyougen.blogspot.com/2010/01/how-to-setup-maven-scala-project-with.html


