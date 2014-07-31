'use strict';

/**
 * Created by vascoc on 7/28/14.
 */
angular.module('weatherAppApp')
    .factory('CityForecasts', ['$resource', function($resource) {
        return $resource('/forecast/cities');
    }]);




