import {Component, Inject, AfterViewInit} from '@angular/core';
import {StateService} from '@uirouter/angular';
import {NotifierService} from '../../services/notifier.service';
import {AlertType} from '../../model/alert.model';
// import {TranslateService} from 'ng2-translate';

declare var jQuery: any;
@Component({
	moduleId: module.id,
	selector: 'header',
	templateUrl: '../tds/web-app/app-js/shared/modules/header/header.component.html',
})

export class HeaderComponent implements AfterViewInit {

	private state: StateService;
	private pageMetaData: {
		title: string,
		instruction: string,
		menu: Array<string>
	};
	taskCount: Number;

	constructor(@Inject('taskCount') tasks, state: StateService, notifierService: NotifierService /* translate: TranslateService*/) {
		tasks.subscribe(
			(result) => {
				this.taskCount = result.count;
				// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #3
				jQuery('#todoCountProjectId').html(this.taskCount);
			},
			(err) => {
				notifierService.broadcast({
					name: AlertType.WARNING,
					message: err
				});

				console.log(err);
			});
		this.state = state;
		// this language will be used as a fallback when a translation isn't found in the current language
		// translate.setDefaultLang('en');

		if (this.state && this.state.$current && this.state.$current.data) {
			this.pageMetaData = this.state.$current.data.page;
			// translate.get(this.pageMetaData.title).subscribe((translateWord: string) => {
			//     document.title = translateWord;
			// });

		}
	}

	ngAfterViewInit(): void {
		// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #3
		jQuery('.menu-parent-tasks > a')[0].onclick = null;
	}
}