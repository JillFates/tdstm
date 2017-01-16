/**
 * Created by Jorge Morayta on 1/10/2017.
 */

import { Component } from '@angular/core';

@Component({
    selector: 'my-app',
    template: `<h1>Hello {{name}}</h1>`
})

export class AppComponent {
    name = 'Angular';
}
