/**
 * Created by Jorge Morayta on 1/10/2017.
 */

import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent }  from './app.component.ts';

@NgModule({
    imports:      [ BrowserModule ],
    declarations: [ AppComponent ], // Only components, directives and pipes belong here
    bootstrap:    [ AppComponent ]
})

export class AppModule {

}