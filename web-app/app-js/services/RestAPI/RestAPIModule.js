/**
 * Created by Jorge Morayta on 12/22/2015.
 */

'use strict';

import angular  from 'angular';
import RestServiceHandler from './RestServiceHandler.js';

var RestAPIModule = angular.module('TDSTM.RestAPIModule',[]);

RestAPIModule.service('RestServiceHandler', ['$log', '$http', '$resource', 'rx', RestServiceHandler]);

export default RestAPIModule;
