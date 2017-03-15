import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

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

@NgModule({
    imports: [CommonModule],
    declarations: [
        UILoaderDirective,
        UIToastDirective,
        UIBooleanPipe,
        UIDialogDirective
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
        UIBooleanPipe]
})
export class SharedModule {
}