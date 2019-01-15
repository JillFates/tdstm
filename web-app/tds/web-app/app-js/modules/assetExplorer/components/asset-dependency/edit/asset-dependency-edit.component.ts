import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import {clone, equals} from 'ramda';

import {DependencyType, DependencyChange} from '../model/asset-dependency.model';

@Component({
	selector: 'asset-dependency-edit',
	template: `
		<div>
			<div class="dependency-row dependency-edit">
				<div class="dependency-row-legend">Frequency</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						(valueChange)="notifyChanges(DependencyType.dependencyA)"
						name="frequencyListA"
						[valuePrimitive]="true"
						[data]="frequencyList"
						[(ngModel)]="dependencies.a.dataFlowFreq">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						(valueChange)="notifyChanges(DependencyType.dependencyB)"
						name="frequencyListB"
						[valuePrimitive]="true"
						[data]="frequencyList"
						[(ngModel)]="dependencies.b.dataFlowFreq">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit ">
				<div class="dependency-row-legend">Type</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						name="typeListA"
						(valueChange)="notifyChanges(DependencyType.dependencyA)"
						[valuePrimitive]="true"
						[data]="typeList"
						[(ngModel)]="dependencies.a.type">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						name="typeListB"
						(valueChange)="notifyChanges(DependencyType.dependencyB)"
						[valuePrimitive]="true"
						[data]="typeList"
						[(ngModel)]="dependencies.b.type">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">Status</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						(valueChange)="notifyChanges(DependencyType.dependencyA)"
						name="statusListA"
						[valuePrimitive]="true"
						[data]="statusList"
						[(ngModel)]="dependencies.a.status">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						(valueChange)="notifyChanges(DependencyType.dependencyB)"
						name="statusListB"
						[valuePrimitive]="true"
						[data]="statusList"
						[(ngModel)]="dependencies.b.status">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">Direction</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						(valueChange)="notifyChanges(DependencyType.dependencyA)"
						name="directionListA"
						[valuePrimitive]="true"
						[data]="directionList"
						[(ngModel)]="dependencies.a.dataFlowDirection">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						(valueChange)="notifyChanges(DependencyType.dependencyB)"
						name="directionListB"
						[valuePrimitive]="true"
						[data]="directionList"
						[(ngModel)]="dependencies.b.dataFlowDirection">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c1</div>
				<div class="dependency-a">
					<input type="text" name="c1A" [(ngModel)]="dependencies.a.c1"
					(change)="notifyChanges(DependencyType.dependencyA)">
				</div>
				<div class="dependency-b">
					<input type="text" name="c1B" [(ngModel)]="dependencies.b.c1"
					(change)="notifyChanges(DependencyType.dependencyB)">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c2</div>
				<div class="dependency-a">
					<input type="text" name="c2A" [(ngModel)]="dependencies.a.c2"
					(change)="notifyChanges(DependencyType.dependencyA)">
				</div>
				<div class="dependency-b">
					<input type="text" name="c2B" [(ngModel)]="dependencies.b.c2"
					(change)="notifyChanges(DependencyType.dependencyB)">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c3</div>
				<div class="dependency-a">
					<input type="text" name="c3A" [(ngModel)]="dependencies.a.c3"
					(change)="notifyChanges(DependencyType.dependencyA)">
				</div>
				<div class="dependency-b">
					<input type="text" name="c3B" [(ngModel)]="dependencies.b.c3"
					(change)="notifyChanges(DependencyType.dependencyB)">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c4</div>
				<div class="dependency-a">
					<input type="text" name="c4A" [(ngModel)]="dependencies.a.c4"
					(change)="notifyChanges(DependencyType.dependencyA)">
				</div>
				<div class="dependency-b">
					<input type="text" name="c4B" [(ngModel)]="dependencies.b.c4"
					(change)="notifyChanges(DependencyType.dependencyB)">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">comment</div>
				<div class="dependency-a">
					<textarea name="commentA" [(ngModel)]="dependencies.a.comment"
					(change)="notifyChanges(DependencyType.dependencyA)"></textarea>
				</div>
				<div class="dependency-b">
					<textarea name="commentB" [(ngModel)]="dependencies.b.comment"
					(change)="notifyChanges(DependencyType.dependencyB)"></textarea>
				</div>
			</div>
		</div>
	`
})
export class AssetDependencyEditComponent implements OnInit {
	@Output() change: EventEmitter<any> = new EventEmitter<any>();
	@Input() dependencyA: any;
	@Input() dependencyB: any;
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

	private getDependencyElements(dependency: any): any {
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

	protected notifyChanges(type: DependencyType): void {
		console.log(JSON.stringify(this.dependencies, null, 2));
		this.change.emit({type, dependencies: this.dependencies});
	}
}