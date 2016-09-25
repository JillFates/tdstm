/**
 * Created by Jorge Morayta on 12/21/2015.
 */

'use strict';

import angular  from 'angular';
import HeaderController from './HeaderController.js';

var HeaderModule = angular.module('TDSTM.HeaderModule', []);

HeaderModule.controller('HeaderController', ['$log', '$state', HeaderController]);

export default HeaderModule;