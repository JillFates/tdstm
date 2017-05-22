/**
 * Created by Jorge Morayta on 12/14/2015.
 * Prints out all Toast message when detected from server or custom msg using the directive itself
 *
 * Probably values are:
 *
 * success, danger, info, warning
 *
 */
'use strict';

var TDSTM = require('../../config/App.js');

TDSTM.createDirective('toastHandler', ['$log', '$timeout', 'HTTPRequestHandlerInterceptor', 'HTTPRequestErrorHandlerInterceptor',
    'HTTPResponseHandlerInterceptor', 'HTTPResponseErrorHandlerInterceptor',
    function ($log, $timeout, HTTPRequestHandlerInterceptor, HTTPRequestErrorHandlerInterceptor,
              HTTPResponseHandlerInterceptor, HTTPResponseErrorHandlerInterceptor) {

    $log.debug('ToastHandler loaded');
    return {
        scope: {
            msg: '=',
            type: '=',
            status: '='
        },
        priority: 5,
        templateUrl: '../app-js/directives/tools/ToastHandler.html',
        restrict: 'E',
        controller: ['$scope', '$rootScope', function ($scope, $rootScope) {
            $scope.alert = {
                success: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 2000
                },
                danger: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 4000
                },
                info: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 2000
                },
                warning: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 4000
                },
            };

            $scope.progress = {
                show: false
            };

            function turnOffNotifications(){
                $scope.alert.success.show = false;
                $scope.alert.danger.show = false;
                $scope.alert.info.show = false;
                $scope.alert.warning.show = false;
                $scope.progress.show = false;
            }

            /**
             * Listen to any request, we can register listener if we want to add extra code.
             */
            HTTPRequestHandlerInterceptor.listenRequest().then(null, null, function(config){
                $log.debug('Request to: ',  config);
                var time = config.requestTimestamp;
                $log.debug(time);
                $scope.progress.show = true;
            });

            HTTPRequestErrorHandlerInterceptor.listenError().then(null, null, function(rejection){
                $log.debug('Request error: ',  rejection);
                $scope.progress.show = false;
            });

            HTTPResponseHandlerInterceptor.listenResponse().then(null, null, function(response){
                var time = response.config.responseTimestamp - response.config.requestTimestamp;
                $log.debug('The request took ' + (time / 1000) + ' seconds');
                $log.debug('Response result: ', response);
                $scope.progress.show = false;

                if(response && response.headers && response.headers('x-login-url')) {
                    window.location.href = response.headers('x-login-url');
                }
            });

            HTTPResponseErrorHandlerInterceptor.listenError().then(null, null, function(rejection){
                $log.debug('Response error: ', rejection);
                $scope.progress.show = false;
                $scope.alert.danger.show = true;
                $scope.alert.danger.status = rejection.status;
                $scope.alert.danger.statusText = rejection.statusText;
                $scope.alert.danger.errors = rejection.data.errors;
                $timeout(turnOffNotifications, 3000);
            });

            /**
             * Hide the Pop up notification manually
             */
            $scope.onCancelPopUp = function() {
                turnOffNotifications();
            };

            /**
             * It watch the value to show the msg if necessary
             */
            $rootScope.$on('broadcast-msg', function(event, args) {
                $log.debug('broadcast-msg executed');
                $scope.alert[args.type].show = true;
                $scope.alert[args.type].statusText = args.text;
                $scope.alert[args.type].status = null;
                $timeout(turnOffNotifications,  $scope.alert[args.type].time);
                $scope.$apply(); // rootScope and watch exclude the apply and needs the next cycle to run
            });

            /**
             * It watch the value to show the msg if necessary
             */
            $scope.$watch('msg', function(newValue, oldValue) {
                if (newValue && newValue !== '') {
                    $scope.alert[$scope.type].show = true;
                    $scope.alert[$scope.type].statusText = newValue;
                    $scope.alert[$scope.type].status = $scope.status;
                    $timeout(turnOffNotifications, 2500);
                }
            });

        }]
    };
}]);
