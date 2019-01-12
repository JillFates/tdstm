import { Component, Input } from '@angular/core';
import {TDSActionsButton} from '../../../../../shared/components/button/model/action-button.model';

@Component({
	selector: 'asset-dependency-show',
	template: `
		<div class="dependency-row">
			<div class="dependency-row-legend">Frequency</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.dataFlowFreq}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.dataFlowFreq}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">Type</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.type}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.type}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">Status</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.status}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.status}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">Direction</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.dataFlowDirection}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.dataFlowDirection}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">C1</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.c1}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.c1}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">C2</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.c2}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.c2}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">C3</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.c3}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.c3}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">C4</div>
			<div class="dependency-a"><label class="dependency-label">{{dependencyA?.c4}}</label></div>
			<div class="dependency-b"><label class="dependency-label">{{dependencyB?.c4}}</label></div>
		</div>

		<div class="dependency-row">
			<div class="dependency-row-legend">Comment</div>
			<div class="dependency-a">
				<label class="dependency-label dependency-comment">{{dependencyA?.comment}}</label>
				<div *ngIf="dependencyA">
					<tds-button
						[action]="ButtonActions.AssetDelete"
						class="btn-danger"
						tooltip="Delete dependency">
					</tds-button>
				</div>
			</div>
			<div class="dependency-b">
				<label class="dependency-label dependency-comment">{{dependencyB?.comment}}</label>
				<div *ngIf="dependencyB">
					<tds-button
						[action]="ButtonActions.AssetDelete"
						class="btn-danger"
						tooltip="Delete dependency">
					</tds-button>
				</div>
			</div>
		</div>
			`
})
export class AssetDependencyShowComponent {
	@Input() dependencyA: any;
	@Input() dependencyB: any;

	protected ButtonActions = TDSActionsButton;
}