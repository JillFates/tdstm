// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {UserService} from '../service/user.service';

@Injectable()
export class UserResolveService {
	constructor(private userService: UserService, private router: Router) {
	}
}