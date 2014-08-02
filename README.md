Simple Weather App
==================

This is a super simple demonstration on how to utilize FMI's (Finnish Meteorological Institute) open data interface.

##Stack and tools used

###Frontend

The frontend is an AngularJS app based on Yeoman generated skeleton. It has currently only one view which displays weather forecasts on a map. The map in turn is provided using the awesome Google Maps for AngularJS directives.
http://nlaplante.github.io/angular-google-maps/

A set of buttons are provided along the map to switch between forecast for next hour and six hours ahead.

The icons: http://www.dotvoid.com/2009/12/free-weather-icons/

###Backend

The backend is a RESTful web app implemented using Spring Boot. It provides the forecasts for the frontend via a REST api and fetches forecasts from FMI's open data interface. New forecasts are fetched hourly and stored in a database to minimize FMI's interface use.

The app makes use of Ehcache to limit database queries. Although current functionality could have been implemented using Ehcache alone, a database is used to facilitate more sophisticated features in the future.




