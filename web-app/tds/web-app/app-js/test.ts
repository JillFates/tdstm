import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting
} from '@angular/platform-browser-dynamic/testing';

import './specs/highlight.directive.spec';
import './specs/notifier.service.spec';
import './specs/tds-app.component.spec';
import './specs/ui-boolean.pipe.spec';
import './specs/ui-loader.directive.spec';
import './specs/ui-toast.directive.spec';
import './specs/component-creator.service.spec';
import './modules/noticeManager/specs/notice-list.component.spec';
// Unfortunately there's no typing for the `__karma__` variable. Just declare it as any.
declare var __karma__: any;

// Prevent Karma from running prematurely.
__karma__.loaded = () => {
  let a = 1;
};

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting()
);

// Finally, start Karma to run the tests.
__karma__.start();
