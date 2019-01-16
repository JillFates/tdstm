import { Component, Input, Output, EventEmitter } from '@angular/core';
import {DependencyChange, DependencyType} from '../model/asset-dependency.model';

@Component({
	selector: 'asset-dependency-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/asset-dependency/show/asset-dependency-show.component.html'
})
export class AssetDependencyShowComponent {
	@Output() deleteDependency: EventEmitter<DependencyType> = new EventEmitter<DependencyType>();
	@Input() dependencyA: any;
	@Input() dependencyB: any;
	protected DependencyType = DependencyType;

	/**
	 * Notify to the host component a delete action
	 * @param {DependencyType} dependencyType Type of dependency to be deleted
	 * @return {void)
	 */
	protected onDeleteDependency(dependencyType: DependencyType): void {
		this.deleteDependency.emit(dependencyType);

	}
}