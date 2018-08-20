# com.bitplan.radolan
parses the DWD RADOLAN / RADVOR radar composite format - Java port of https://gitlab.cs.fau.de/since/radolan

Dual Licensed: MIT/Apache

The Precipitation Information of Deutscher Wetterdienst DWD is based on RADOLAN data which is publicly available via the OpenData Services of DWD

https://www.dwd.de/DE/leistungen/radolan/radolan_info/home_freie_radolan_kartendaten.html

![DWD Regenradar](https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg)
![DWD RADOLAN RW](https://www.dwd.de/DE/leistungen/radolan/radolan_info/rw_karte.png?view=nasImage&nn=16102)
![DWD RADOLAN SF](https://www.dwd.de/DE/leistungen/radolan/radolan_info/sf_karte.png?view=nasImage&nn=16102)

This is a library to help analyze and visualize the RADOLAN files 

# Creator 
[![BITPlan](http://wiki.bitplan.com/images/wiki/thumb/3/38/BITPlanLogoFontLessTransparent.png/198px-BITPlanLogoFontLessTransparent.png)](http://www.bitplan.com)

[![Build Status](https://travis-ci.org/BITPlan/com.bitplan.radolan.svg?branch=master)](https://travis-ci.org/BITPlan/com.bitplan.radolan)
### Distribution
[Available via maven repository](https://search.maven.org/artifact/com.bitplan.radolan/com.bitplan.radolan/0.0.1/jar)

Maven dependency
```xml
<dependency>
  <groupId>com.bitplan.radolan</groupId>
  <artifactId>com.bitplan.radolan</artifactId>
  <version>0.0.1</version>
</dependency>
```
### How to build
```
git clone https://github.com/BITPlan/com.bitplan.radolan
cd com.bitplan.radolan
mvn install
```

# Documentation
* http://www.bitplan.com/index.php/Radolan

# Links
* https://www.dwd.de/DE/leistungen/radolan/radolan.html
# History
* 0.0.1 2018-08-14 Migration of classes and tests (not working yet)
* 0.0.1 2018-08-16 working display and input/output handling
* 0.0.1 2018-08-18 fixed data issues and implemented zoom 
                   first release to maven central and last 0.0.1 commit
