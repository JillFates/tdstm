import {Component, Inject, Input, OnInit} from '@angular/core';
import { UIExtraDialog } from '../../../shared/services/ui-dialog.service';

@Component({
	selector: 'tds-modal-html-wrapper',
	template: `
	<div class="modal fade in model-html-wrapper-component"
		tds-handle-escape (escPressed)="cancelCloseDialog()"
		id="modal-html-wrapper" data-backdrop="static" tabindex="-1" role="dialog">
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
						(ngSubmit)="onSubmit()"
						target="target-frame">
						<iframe name="model-edit-view"></iframe>
					</form>
					<!--
						<div class="box-body">
							<div [innerHtml]="html"></div>
						</div>
					-->
				</div>
				</div>
				<div class="modal-footer">
				</div>
			</div>
		</div>
	</div>
`
})
export class TDSModalHtmlWrapperComponent extends UIExtraDialog implements OnInit {
	constructor(
		@Inject('title') public title: string,
		@Inject('html') public html: string) {
		super('#modal-html-wrapper');
		console.log('constructor');
	}

	ngOnInit() {
		console.log('onInit');
	}

	public cancelCloseDialog() {
		this.close(null);
	}
}