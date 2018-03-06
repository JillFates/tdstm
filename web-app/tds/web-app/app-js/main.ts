/**
 * Compile the Application Dynamically Just-in-Time (JIT)
 * Application is using the JSDoc 3
 */

// Polyfills
import 'core-js/es6';
import 'core-js/es7/reflect';
import 'zone.js/dist/long-stack-trace-zone';

import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {TDSAppModule} from './config/tds-app.module';

enableProdMode();

// Compile and launch the module
platformBrowserDynamic().bootstrapModule(TDSAppModule);