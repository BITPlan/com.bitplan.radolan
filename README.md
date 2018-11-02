### com.bitplan.radolan
[parses the DWD RADOLAN / RADVOR radar composite format - Java port of https://gitlab.cs.fau.de/since/radolan](http://www.bitplan.com/Radolan) 

[![Travis (.org)](https://img.shields.io/travis/BITPlan/com.bitplan.radolan.svg)](https://travis-ci.org/BITPlan/com.bitplan.radolan)
[![Maven Central](https://img.shields.io/maven-central/v/com.bitplan.radolan/com.bitplan.radolan.svg)](https://search.maven.org/artifact/com.bitplan.radolan/com.bitplan.radolan/0.0.2/jar)
[![GitHub issues](https://img.shields.io/github/issues/BITPlan/com.bitplan.radolan.svg)](https://github.com/BITPlan/com.bitplan.radolan/issues)
[![GitHub issues](https://img.shields.io/github/issues-closed/BITPlan/com.bitplan.radolan.svg)](https://github.com/BITPlan/com.bitplan.radolan/issues/?q=is%3Aissue+is%3Aclosed)
[![GitHub](https://img.shields.io/github/license/BITPlan/com.bitplan.radolan.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![BITPlan](http://wiki.bitplan.com/images/wiki/thumb/3/38/BITPlanLogoFontLessTransparent.png/198px-BITPlanLogoFontLessTransparent.png)](http://www.bitplan.com)

### Documentation
* [Wiki](http://www.bitplan.com/Radolan)
* [com.bitplan.radolan Project pages](https://BITPlan.github.io/com.bitplan.radolan)
* [Javadoc](https://BITPlan.github.io/com.bitplan.radolan/apidocs/index.html)
* [Test-Report](https://BITPlan.github.io/com.bitplan.radolan/surefire-report.html)
### Maven dependency

Maven dependency
```xml
<dependency>
  <groupId>com.bitplan.radolan</groupId>
  <artifactId>com.bitplan.radolan</artifactId>
  <version>0.0.2</version>
</dependency>
```

[Current release at repo1.maven.org](http://repo1.maven.org/maven2/com/bitplan/radolan/com.bitplan.radolan/0.0.2/)

### How to build
```
git clone https://github.com/BITPlan/com.bitplan.radolan
cd com.bitplan.radolan
mvn install
```
### Dual License
Some parts of this project are dual Licensed: MIT/Apache since the original golang Library is MIT licensed

### DWD OpenData
The Precipitation Information of Deutscher Wetterdienst DWD is based on RADOLAN data which is publicly available via the OpenData Services of DWD

https://www.dwd.de/DE/leistungen/radolan/radolan_info/home_freie_radolan_kartendaten.html

![DWD Regenradar](https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg)
![DWD RADOLAN RW](https://www.dwd.de/DE/leistungen/radolan/radolan_info/rw_karte.png?view=nasImage&nn=16102)
![DWD RADOLAN SF](https://www.dwd.de/DE/leistungen/radolan/radolan_info/sf_karte.png?view=nasImage&nn=16102)

This is a library to help analyze and visualize the RADOLAN files
# Version History
| Version | date       |  changes 
| ------: | ---------- | ------------------------
|   0.0.1 | 2018-08-14 | Migration of classes and tests (not working yet)
|   0.0.1 | 2018-08-16 | working display and input/output handling
|   0.0.1 | 2018-08-18 | fixed data issues and implemented zoom 
|   0.0.1 | 2018-08-20 | first release to maven central and last 0.0.1 commit
|   0.0.2 | 2018-08-26 | adds -p option and command line selection 
|   0.0.2 | 2018-11-02 | release 0.0.2 to maven central 
