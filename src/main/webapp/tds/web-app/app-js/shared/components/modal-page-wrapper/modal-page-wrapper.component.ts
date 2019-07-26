import {Component, Inject, Input, OnInit, AfterViewInit} from '@angular/core';
import { UIExtraDialog } from '../../../shared/services/ui-dialog.service';

@Component({
	selector: 'tds-modal-page-wrapper',
	template: `
	<div class="modal fade in modal-page-wrapper-component"
		tds-handle-escape (escPressed)="cancelCloseDialog()"
		id="modal-page-wrapper" data-backdrop="static" tabindex="-1" role="dialog">
		<div class="modal-dialog modal-lg" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
						<span aria-hidden="true">×</span>
					</button>
					<h4 class="modal-title">{{title}}</h4>
				</div>
				<div class="modal-body">
					<div class="modal-body-container">
					<form id="wrapperForm"
						#myForm="ngForm"
						action="/tdstm/model/edit?id=3520"
						method="post"
						target="target-frame">
						<iframe name="target-frame" class="wrapper-frame"></iframe>
					</form>
				</div>
				</div>
				<div class="modal-footer">
				</div>
			</div>
		</div>
	</div>
`
})
export class TDSModalPageWrapperComponent extends UIExtraDialog implements AfterViewInit {
	constructor(
		@Inject('title') public title: string,
		@Inject('html') public html: string) {
		super('#modal-html-wrapper');
		console.log('constructor');
	}

	ngAfterViewInit() {
		setTimeout(() => {
			console.log('onInit');
			document.getElementById('wrapperForm')['submit']();
		}, 3000);
	}

	public cancelCloseDialog() {
		this.close(null);
	}
}