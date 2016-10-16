/**
 * Created by Jorge Morayta on 12/21/2015.
 */

'use strict';

import angular  from 'angular';
import HeaderController from './HeaderController.js';
import DialogAction from '../dialogAction/DialogAction.js';

var HeaderModule = angular.module('TDSTM.HeaderModule', []);

HeaderModule.controller('HeaderController', ['$log', '$state', HeaderController]);

// Modal - Controllers
HeaderModule.controller('DialogAction', ['$log','$uibModal', '$uibModalInstance', 'params', DialogAction]);

export default HeaderModule;