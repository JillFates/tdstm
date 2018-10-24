/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
// Services
import {UserService} from './service/user.service';
import {UserListComponent} from "./components/list/user-list.component";
import {UserPreferencesComponent} from "./components/preferences/user-preferences.component";

@NgModule({
	providers: [UserService],

    declarations: [
        UserListComponent,
        UserPreferencesComponent
    ],
    exports: [
        UserListComponent,
        UserPreferencesComponent
    ]
})

export class UserModule {

}