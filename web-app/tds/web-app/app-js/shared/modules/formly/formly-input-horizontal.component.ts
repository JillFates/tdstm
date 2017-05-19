/**
 * Created by Jorge Morayta on 3/20/2017.
 * Creates a Wrapper to manage horizontal Form Group Controls for Formly
 */

import {Component, ViewChild, ViewContainerRef} from '@angular/core';
import {FieldWrapper} from 'ng-formly';

@Component({
    selector: 'horizontal-wrapper',
    template: `<div class="form-group" [ngClass]="{'has-danger': !formControl.valid}">
      <label attr.for="{{key}}" class="col-sm-2 control-label">{{to.label}}: {{to.required ? "*" : ""}}</label>
      <div class="col-sm-10" [style.max-width.px]="to.options[0].maxWidth">
        <ng-template #fieldComponent></ng-template>
      </div>
    </div>`
})

export class FormlyInputHorizontal extends FieldWrapper {
    @ViewChild('fieldComponent', {read: ViewContainerRef}) fieldComponent: ViewContainerRef;
}