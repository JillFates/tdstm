/**
 * The Vendor files works as the last entry points of the lib we use
 * instead of reflecting the node_modules directly we define each lib here
 * if the Import match an import in the app.js it will create a bundle here
 * if the import in app does not match any here it will be on app.js
 */

// Angular Guide
// import '@angular/platform-browser';
// import '@angular/platform-browser-dynamic';
// import '@angular/core';
// import '@angular/common';
// import '@angular/http';
// import '@angular/forms';

// Angular
// Forms
import {AbstractControl} from '@angular/forms'; // Maybe remove, only in NoticeForm
import {FormControl} from '@angular/forms'; // Maybe remove, only in NoticeForm
import {FormGroup} from '@angular/forms'; // Maybe remove, only in TaskManager
import {FormsModule} from '@angular/forms';
import {NgForm} from '@angular/forms';
import {NG_VALIDATORS} from '@angular/forms';
import {ReactiveFormsModule} from '@angular/forms'; // Maybe remove, only in TaskManager
import {Validator} from '@angular/forms';
// HTTP
import {ConnectionBackend} from '@angular/http';
import {Headers} from '@angular/http';
import {HttpModule} from '@angular/http';
import {Http} from '@angular/http';
import {Request} from '@angular/http';
import {RequestOptions} from '@angular/http';
import {RequestOptionsArgs} from '@angular/http';
import {Response} from '@angular/http';
import {XHRBackend} from '@angular/http';
// Common HTTP
import {CommonModule} from '@angular/common';
import {HttpClientModule} from '@angular/common/http';
import {HttpEvent} from '@angular/common/http';
import {HttpEventType} from '@angular/common/http';
import {HttpHandler} from '@angular/common/http';
import {HttpInterceptor} from '@angular/common/http';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {HttpProgressEvent} from '@angular/common/http';
import {HttpRequest} from '@angular/common/http';
import {HttpResponse} from '@angular/common/http';
// Platform-browser
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DomSanitizer} from '@angular/platform-browser';
import {SafeHtml} from '@angular/platform-browser';
// Core
import {AfterViewInit} from '@angular/core';
import {Attribute} from '@angular/core';
import {Compiler} from '@angular/core';
import {Component} from '@angular/core';
import {ComponentRef} from '@angular/core';
import {ComponentFactoryResolver} from '@angular/core';
import {Directive} from '@angular/core';
import {DoCheck} from '@angular/core';
import {ElementRef} from '@angular/core';
import {enableProdMode} from '@angular/core';
import {EventEmitter} from '@angular/core';
import {forwardRef} from '@angular/core';
import {HostListener} from '@angular/core';
import {Inject} from '@angular/core';
import {Injectable} from '@angular/core';
import {Injector} from '@angular/core';
import {Input} from '@angular/core';
import {KeyValueDiffers} from '@angular/core';
import {NgModule} from '@angular/core';
import {NgModuleFactoryLoader} from '@angular/core';
import {NgModuleRef} from '@angular/core';
import {OnChanges} from '@angular/core';
import {OnDestroy} from '@angular/core';
import {OnInit} from '@angular/core';
import {Output} from '@angular/core';
import {Pipe} from '@angular/core';
import {PipeTransform} from '@angular/core';
import {QueryList} from '@angular/core';
import {ReflectiveInjector} from '@angular/core'; // from v5 - slow and brings in a lot of code, Use `Injector.create` instead.
import {Renderer2} from '@angular/core'; // Look for deprecated "Render"
import {SimpleChanges} from '@angular/core'; // Do we really use this?
import {SystemJsNgModuleLoader} from '@angular/core'; // Do we really use this?
import {ViewChild} from '@angular/core';
import {ViewChildren} from '@angular/core';
import {ViewContainerRef} from '@angular/core';
import {ViewEncapsulation} from '@angular/core';

// --------------------------------------------------

// Angular Guide
// import 'rxjs';

// RXJS
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {Scheduler} from 'rxjs/Scheduler';
import {ReplaySubject} from 'rxjs/ReplaySubject';
import {Observer} from 'rxjs/Observer';
import {Subscription} from 'rxjs/Subscription';
// Observable functions
import {zip} from 'rxjs/observable/zip';
// Observable extensions
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/operator/take';
import 'rxjs/add/observable/empty';
// Observable operators
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/finally';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/bufferCount';
import 'rxjs/add/operator/take';
import 'rxjs/add/operator/delay';
import 'rxjs/add/observable/concat';