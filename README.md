# The Electric Delorean Project
![Picture](https://raw.githubusercontent.com/yichengsun/Electric-DeLorean-Project/master/ElectricDeLoreanTracker/app/src/main/res/drawable-mdpi/rear.png) 
## Table of Contents

* [Team Members](#team-members)
* [Introduction](#introduction)
* [User Manual and Pictures](#user-manual-and-pictures)
* [Installation](#installation)
* [Third Party Libraries](#third-party-libraries)
* [Credits](#credits)

## Team Members
* "Yicheng Sun" <yichengs@princeton.edu>
* "Henry Shangguan" <hys@princeton.edu>

## Introduction 
>No, no, no, no, no, this sucker's electrical!
>
>--Dr. Emmett Brown

The Electric Delorean Project is a partnership between Queen's University Belfast and Northern Ireland Electric to build a fully functional electric-powered Delorean DMC-12. The car has a 270 horsepower AC motor and can do 0-60 in 8 seconds. The completed vehicle will debut at the 30th Anniversary celebration of the *Back to the Future* franchise on October 21, 2015 in Belfast, Northern Ireland. 

This repository contains all the software for an in-car dashboard system and route tracker.

The Raspberry Pi interfaces with the Orion battery management system and Soliton motor controller in order to track battery levels (charge state, current) and engine performance (RPM, Power). All data is transmitted to the dashboard application via bluetooth and uploaded to the cloud via Parse.

The Android-based dashboard serves the dual purpose of displaying driving data (velocity, distance to empty, efficiency, and more) and a multiple route tracker with nearest electric vehicle charging stations.

## User Manual and Pictures
Detailed user manual with pictures can be found in this **[Google document](https://docs.google.com/document/d/1CpdqsstL3xIvifqvO7MZdxyz_jfw7N-QEhuCiqaaNv4/edit?usp=sharing)**. 

## Installation
#### Android Dashboard and Tracker
Development was done using Android Studio on a Lenovo Tablet S8-50f running KitKat 4.4.2. Eclipse and other Android IDEs should work but may require creating a new package structure and copying the source files.

1. Clone 'ElectricDeLoreanTracker'
2. Open Android Studio and import project, selecting the build.gradle file in 'ElectricDeLoreanTracker'
2. Build project
3. Run application, making sure USB debugging is enabled on tablet

#### Raspberry Pi Data Sampler
Development was done on a Raspberry Pi 2 (Model B 1 GB) with a Cambridge Silicon Radio bluetooth dongle and a 8 channel 17 bit ADC expansion board from [ABElectronics](https://www.abelectronics.co.uk/products/3/Raspberry-Pi-Model-A-and-B/17/ADC-Pi-V2---Raspberry-Pi-Analogue-to-Digital-converter)

1. Install bluetooth packages, [instructions](http://www.modmypi.com/blog/installing-the-raspberry-pi-nano-bluetooth-dongle)
2. Clone 'Raspberry Pi ADC Sampler' onto Pi board
3. Set btminder to execute on boot, [instructions](http://raspberrypi.stackexchange.com/questions/8734/execute-script-on-start-up)

## Third Party Libraries

1. [Parse Android SDK](https://parse.com/docs/downloads) for backend support
2. [ADC Pi Python LIbrary](https://github.com/abelectronicsuk/ABElectronics_Python_Libraries/tree/master/ADCPi) from ABElectronics 

## Credits
Special thanks to Dr. David Laverty and the rest of the QUB Energy, Power, and Intelligent Control research cluster for guidance and to Queen's University Belfast, Princeton University, and Northern Ireland Electricity for funding.
