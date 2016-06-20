# Strava Data Miner #

## Setup instructions ##
- install scrava (Strava API library)
 - git clone https://github.com/kiambogo/scrava.git
 - cd scrava
 - sbt publish-local
- run strava-data-miner
 - git clone https://github.com/skennedy/strava-data-miner.git
 - cd strava-data-miner
 - sbt run

## Explanation ##

I am a keen cyclist and upload most of my rides to [Strava](http://www.strava.com). 

This application downloads all my ride data from Strava using it's API and collects my best average speed over different time periods.

## Architecture ##

It is implemented in Scala and Akka. The main actors are:

- StravaDataMiner
    - main class
    - instantiates the other main actors and sends the statistic requests
- ActivityLoader
    - loads the data streams of all my ride activities by sending messages to StravaApi actor
- StravaApi
    - actor that wraps Strava API library
    - multiple instances via a router for faster loading
- BestAverageSpeedPerTimePeriodMiner
    - main data miner actor
    - is given all the activities by the loader
    - creates child actors for each activity
    - takes the per-activity results and determines the best overall activity
- BestAverageSpeedPerTimePeriodPerActivity
    - child of above
    - finds the section of its activity with the best average speed
    - receives many average speed values for slices of its data streams, returns the best to its parent
    - the real work is done by its children:
        - StreamSlicer
            - given the Time data stream it finds slices that cover the given period
            - resulting Slice objects are sent to...
        - AverageCalculator
            - given the Speed data stream and a Slice object it calculates the average of that data
            - resulting SliceValue objects are sent back to parent
            - constructed using a Router for parallelism
