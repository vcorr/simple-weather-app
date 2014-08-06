'use strict';

/**
 * @ngdoc function
 * @name weatherAppApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the weatherApp
 */
angular.module('weatherAppApp')
    .controller('MainCtrl', function ($scope, CityForecasts) {

        $scope.map = {
            center: {
                latitude: 63.6,
                longitude: 26
            },
            zoom: 6,

            options: {
                zoomControl: false,
                streetViewControl: false,
                panControl: false,
                mapTypeControl: false,
                styles: [
                    {featureType: "administrative.country",
                        elementType: "labels",
                        stylers: [
                            { visibility: "off" }
                        ]
                    },

                    {featureType: "road",
                        stylers: [
                            { visibility: "off" }
                        ]
                    },

                    {featureType: "landscape",
                        stylers: [
                            { color: "#4444a2" }
                        ]
                    },

                    {featureType: "poi",
                        stylers: [
                            { "visibility": "off" }
                        ]
                    },

                    {featureType: "water",
                        stylers: [
                            { color: "#212180" }
                        ]
                    }
                ]
            }
        };

        $scope.iconindex = 0;

        // switch icons for markers
        $scope.$watch("iconindex", function(newValue, oldValue) {
            $scope.parseIcons();
        });

        $scope.markers = CityForecasts.query(null, function () {
            console.log("success");
            console.log($scope.markers.length);
            prepareMarkers();
        });

        var prepareMarkers = function () {
            _.each($scope.markers, function (marker) {
                marker.id = marker.cityName;
                marker.latitude = marker.cityCoords.lat;
                marker.longitude = marker.cityCoords.lon;
                marker.showWindow = false;
                marker.icon = parseIcon(marker, 0);

                marker.closeClick = function () {
                    marker.showWindow = false;
                    $scope.$apply();
                };
                marker.markerClicked = function () {
                    onMarkerClicked(marker);
                }
            });
        }

        $scope.parseIcons = function () {
            _.each($scope.markers, function (marker) {
                marker.icon = parseIcon(marker, $scope.iconindex);
            });
        }


        var parseIcon = function (marker, index) {
            var weatherValue = marker.forecasts[index].weatherSymbol;

            var weatherValueTime = new Date(marker.forecasts[index].forecastDate);
            var iconFileName = "";
            iconFileName =  Math.round(weatherValue);
            // handle time of day
            if (weatherValueTime.getHours() > 20 || weatherValueTime.getHours() < 6) {
                iconFileName += "n.png";
            } else {
                iconFileName += "d.png";
            }

            return "../app/images/icons_60x50/" + iconFileName;
        }

        var onMarkerClicked = function (marker) {

            var icons= [];
        	_.each(marker.forecasts, function(dto, index) {
              icons.push(dto.weatherSymbol);
              dto.iconFile = parseIcon(marker,index);
            });

            $scope.selectedMarker = marker;
            $scope.$apply();
        }
        
        $scope.selectedMarker = $scope.markers[0];
    })
;

