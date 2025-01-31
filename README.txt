********************************************
*        INSTALLATION INSTRUCTIONS         *
********************************************

* On windows systems,
	- Download and install Java JRE 1.5+ on http://java.sun.com
	- run the following script : jperf.bat

* On Linux / OS X systems, run the following script : 
	- The 'java' (JRE 1.5+) executable have to be into the system path
	- Don't forget to set execution permissions on the jperf.sh script (execute 'chmod u+x jperf.sh')
	- run the following script : jperf.sh

* To use the OS X app, simply open the DMG file and copy jperf.app to Applications.
  If when run, jperf complains about not having iperf installed and you're sure
  that it is installed, you may need to add the location of iperf to your global
  PATH variable. Execute this in the Terminal and reboot:

  > echo "setenv PATH /usr/bin:/bin:/usr/sbin:/sbin:$(dirname `which iperf`)" | sudo tee /etc/launchd.conf

********************************************
*        COMPILATION INSTRUCTIONS          *
********************************************

* run the following command (Apache ANT has to be installed on the system) :  

	> ant release -f utilities/build.xml

This script will create a JPerf distribution into the 'release' directory.

* To create zip package

	> ant zipped -f utilities/build.xml

This script will create a zip distribution into the 'release' directory.

* To clean package

	> ant  clean -f utilities/build.xml

* To build the OS X distributable disk image, execute:

  > ant macdist -f utilities/build.xml

The resulting .dmg file will be in the 'release/jperf-<version>-mac' directory.
