import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HttpServiceProvider } from '../shared/providers/http-interceptor.provider';
// Shared Services
import { UserService } from '../shared/services/user.service';
import { NotifierService } from '../shared/services/notifier.service';
// Shared Directives
import { UILoaderDirective } from '../shared/directives/ui-loader.directive';
import { UIToastDirective } from '../shared/directives/ui-toast.directive';
import { UIDialogDirective } from '../shared/directives/ui-dialog.directive';

@NgModule({
    imports: [CommonModule],
    declarations: [
        UILoaderDirective,
        UIToastDirective,
        UIDialogDirective
    ],
    providers: [
        UserService,
        NotifierService,
        HttpServiceProvider
    ],
    exports: [UILoaderDirective,
        UIToastDirective,
        UIDialogDirective]
})
export class SharedModule {
}