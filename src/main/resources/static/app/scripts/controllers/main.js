'use strict';

/**
 * @ngdoc function
 * @name weatherAppApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the weatherAppApp
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
                    //{stylers: [{ lightness: -34 }]},
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

        $scope.$watch("iconindex", function(newValue, oldValue) {
            console.log("INDEX: "+$scope.iconindex);
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
            console.log("using index" + index);
            var weatherValue = marker.measurementDTO[index];
            var weatherValueTime = new Date(weatherValue.date);
            console.log(new Date(weatherValue.date).getHours());
            console.log(weatherValue.type);
            console.log(weatherValue.value);
            var iconFileName = "";

            var intvalue = Math.round(weatherValue.value);
            iconFileName = intvalue;


            // handle time of day
            if (weatherValueTime.getHours() > 20) {
                iconFileName += "n.png";
            } else {
                iconFileName += "d.png";
            }

            return "../app/images/icons_60x50/" + iconFileName;
        }


        var onMarkerClicked = function (marker) {
            marker.showWindow = true;
            $scope.$apply();
            console.log("MARKER CLICKED :" + marker.measurementDTO[0].value);
        }
    })
;

/*        $scope.markers = [
 {
 id:1,
 latitude: 62.42,
 longitude: 23.62,
 title: "Tampere Pirkkala",
 showWindow: false
 },

 {
 id:2,
 latitude: 61.47,
 longitude: 23.75,
 title: "Tampere Härmälä",
 showWindow: false,
 icon: '../app/images/icons_60x50/08d.png'
 }
 ];*/
