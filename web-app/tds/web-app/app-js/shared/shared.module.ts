import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TranslateModule} from 'ng2-translate';

import { HttpServiceProvider } from '../shared/providers/http-interceptor.provider';
// Shared Services
import { AuthService } from '../shared/services/auth.service';
import { UserService } from '../shared/services/user.service';
import { NotifierService } from '../shared/services/notifier.service';
import { ComponentCreatorService } from '../shared/services/component-creator.service';
import { UIDialogService, UIActiveDialogService } from '../shared/services/ui-dialog.service';

// Shared Directives
import { UILoaderDirective } from '../shared/directives/ui-loader.directive';
import { UIToastDirective } from '../shared/directives/ui-toast.directive';
import { UIDialogDirective } from '../shared/directives/ui-dialog.directive';
// Shared Pipes
import { UIBooleanPipe } from './pipes/types/ui-boolean.pipe';
// Shared Components
import { HeaderComponent } from './modules/header/header.component';
import { FormlyInputHorizontal } from './modules/formly/formly-input-horizontal.component';
import { RichTextEditorComponent } from './modules/rich-text-editor/rich-text-editor.component';

@NgModule({
    imports: [
        CommonModule,
        TranslateModule
    ],
    declarations: [
        UILoaderDirective,
        UIToastDirective,
        UIBooleanPipe,
        UIDialogDirective,
        HeaderComponent,
        FormlyInputHorizontal,
        RichTextEditorComponent
    ],
    providers: [
        AuthService,
        UserService,
        NotifierService,
        HttpServiceProvider,
        ComponentCreatorService,
        UIDialogService,
        UIActiveDialogService
    ],
    exports: [UILoaderDirective,
        UIToastDirective,
        UIDialogDirective,
        UIBooleanPipe,
        HeaderComponent,
        FormlyInputHorizontal,
        RichTextEditorComponent,
        TranslateModule
    ]
})
export class SharedModule {
}