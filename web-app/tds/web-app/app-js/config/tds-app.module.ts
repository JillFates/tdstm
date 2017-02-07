/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TDSAppComponent }  from './tds-app.component';

// Decorator that tells to Angular is a module.
@NgModule({
  imports:      [ BrowserModule], // Means browser runnable, only add Angular Module here
  declarations: [ TDSAppComponent], // components, directives and pipes ONLY and only ONCE
  bootstrap:    [ TDSAppComponent ] // Contains the root component that is being injected on the index.html
})

export class TDSAppModule { }
