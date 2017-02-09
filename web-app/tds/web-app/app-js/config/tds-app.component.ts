/**
 * A Component is a Controller, that can permute into a directive or as a Service.
 */

import { Component } from '@angular/core';
import { UserService } from '../shared/services/user.service'

@Component({
  selector: 'tds-app',
  template: '<h1>Compiled {{name}} for {{userName}}</h1> <p tds-highlight [elementHighlight]="color">Highlight me for a {{color}} color</p><br><games-list></games-list>',
})

export class TDSAppComponent  {

  name = 'Angular';
  color = 'blue';
  userName = '';

  constructor(userService: UserService){
    this.userName = userService.userName;
  }

}
