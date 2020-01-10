// Angular
import {Component} from '@angular/core';
// Service
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
// Model
import {NoticeModel} from '../../model/notice.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
// Other
import 'rxjs/add/operator/finally';

@Component({
	selector: 'tds-notice-view-html',
	templateUrl: 'view-html.component.html'
})
export class ViewHtmlComponent extends UIExtraDialog {

	protected modalOptions: DecoratorOptions;

	constructor(
		private noticeModel: NoticeModel) {
		super('#noticeViewHtml');
		this.modalOptions = {isFullScreen: false, isResizable: false};
	}

	protected cancelCloseDialog(): void {
		this.close();
	}

}
