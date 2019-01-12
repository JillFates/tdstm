import { Component, Input } from '@angular/core';

@Component({
	selector: 'asset-dependency-edit',
	template: `
		<div>Editing the information</div>
	`
})
export class AssetDependencyEditComponent {
	@Input() dependencyA: any;
	@Input() dependencyB: any;
}