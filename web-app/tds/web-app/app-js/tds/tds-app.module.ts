import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TDSAppComponent }  from './tds-app.component';

@NgModule({
  imports:      [ BrowserModule ],
  declarations: [ TDSAppComponent ],
  bootstrap:    [ TDSAppComponent ]
})

export class TDSAppModule { }
