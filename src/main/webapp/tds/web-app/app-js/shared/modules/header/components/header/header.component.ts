// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UserContextService} from '../../../../../modules/security/services/user-context.service';
import {UserContextModel} from '../../../../../modules/security/model/user-context.model';

declare var jQuery: any;

@Component({
	selector: 'tds-header',
	templateUrl: '../tds/web-app/app-js/shared/modules/header/components/header/header.component.html',
})

export class HeaderComponent implements OnInit {

	protected userContext: UserContextModel;

	constructor(
		private userContextService: UserContextService) {
		this.getUserContext();
	}

	ngOnInit(): void {
		/**
		 * AdminLTE is main js that handles the layout, this could me removed later when implementing the footer
		 */
		if (jQuery.AdminLTE) {
			jQuery.AdminLTE.layout.fix();
		}
		jQuery('.main-footer').show();
	}

	protected getUserContext(): void {
		this.userContextService.getUserContext().subscribe( (userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}
}