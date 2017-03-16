/**
 * Identify Modules that requires a specific parameter or security to acces to it.
 * Created by Jorge Morayta on 3/13/2017.
 */

import { Injectable } from '@angular/core';

@Injectable()
export class AuthService {

    /**
     * Returns true if the user is currently authenticated, else false
     */
    isAuthenticated() {
        return true;
    }

}