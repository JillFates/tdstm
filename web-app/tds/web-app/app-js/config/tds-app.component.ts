/**
 * A Component is a Controller, that can permute into a directive or as a Service.
 */

import { Component } from '@angular/core';

@Component({
  selector: 'tds-app',
  template: `<h1>Hello {{name}}</h1>`,
})

export class TDSAppComponent  {
  name = 'Angular';
}
