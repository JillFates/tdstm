// Angular
import {AfterViewInit, Component} from '@angular/core';
// Model
import {UserContextModel} from '../../../../modules/auth/model/user-context.model';
import {PageMetadataModel} from '../../header/model/page-metadata.model';
// Service
import {UserContextService} from '../../../../modules/auth/service/user-context.service';
import {NotifierService} from '../../../services/notifier.service';

declare var jQuery: any;

@Component({
	selector: 'tds-footer',
	templateUrl: 'footer.component.html',
})
export class FooterComponent implements AfterViewInit {
	public userContext: UserContextModel;
	public pageMetaData: PageMetadataModel = new PageMetadataModel();
	public today = new Date();

	constructor(
		private appSettingsService: UserContextService,
		private notifierService: NotifierService) {
		this.getUserContext();
		this.footerListeners();
	}

	/**
	 * AdminLTE is main js that handles the layout
	 */
	ngAfterViewInit(): void {
		if (jQuery.AdminLTE && jQuery.AdminLTE.layout) {
			jQuery.AdminLTE.layout.fix();
		}
		jQuery('.main-footer').show();
	}

	private footerListeners(): void {
		this.notifierService.on('notificationRouteChange', event => {
			if (event.event.url.indexOf('/auth/') >= 0) {
				this.pageMetaData.hideTopNav = true;
			}
		});
	}

	protected getUserContext(): void {
		this.appSettingsService.getUserContext().subscribe((userContext: UserContextModel) => {
			if (!userContext.user) {
				this.pageMetaData.hideTopNav = true;
			}
			this.userContext = userContext;
		});
	}
}