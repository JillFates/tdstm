import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

import {DependencyType, Dependency} from '../model/asset-dependency.model';

@Component({
	selector: 'asset-dependency-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/asset-dependency/edit/asset-dependency-edit.component.html'
})
export class AssetDependencyEditComponent implements OnInit {
	@Output() change: EventEmitter<any> = new EventEmitter<any>();
	@Input() dependencyA: Dependency;
	@Input() dependencyB: Dependency;
	@Input() frequencyList: string[] = [];
	@Input() statusList: string[] = [];
	@Input() typeList: string[] = [];
	@Input() directionList: string[] = [];
	protected DependencyType = DependencyType;

	protected dependencies = {
		a: {},
		b: {}
	};

	ngOnInit() {
		this.dependencies.a = this.getDependencyElements(this.dependencyA);
		this.dependencies.b = this.getDependencyElements(this.dependencyB);
	}

	/**
	 * Extract the dependency elements used by the form
	 * @param {any} dependency Dependency containing all the informacion
	 * @return {DependencyResults)
	 */
	private getDependencyElements(dependency: any): Dependency {
		return {
			dataFlowFreq: dependency && dependency.dataFlowFreq || '',
			dataFlowDirection: dependency && dependency.dataFlowDirection || '',
			status: dependency && dependency.status || '',
			type: dependency && dependency.type || '',
			c1: dependency && dependency.c1 || '',
			c2: dependency && dependency.c2 || '',
			c3: dependency && dependency.c3 || '',
			c4: dependency && dependency.c4 || '',
			comment: dependency && dependency.comment || ''
		}
	}

	/**
	 * Notify to the host component changes on some dependency
	 * @param {DependencyType} type Type of dependency thas was changed
	 * @return {void)
	 */
	protected notifyChanges(type: DependencyType): void {
		this.change.emit({type, dependencies: this.dependencies});
	}
}