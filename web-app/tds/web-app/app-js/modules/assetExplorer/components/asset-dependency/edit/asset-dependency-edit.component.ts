import { Component, Input, OnInit } from '@angular/core';

@Component({
	selector: 'asset-dependency-edit',
	template: `
		<div>
			<div class="dependency-row dependency-edit">
				<div class="dependency-row-legend">Frequency</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						name="frequencyListA"
						[valuePrimitive]="true"
						[data]="frequencyList"
						[(ngModel)]="dependencies.a.frequency">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						name="frequencyListB"
						[valuePrimitive]="true"
						[data]="frequencyList"
						[(ngModel)]="dependencies.a.frequency">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit ">
				<div class="dependency-row-legend">Type</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						name="typeListA"
						[valuePrimitive]="true"
						[data]="typeList"
						[(ngModel)]="dependencies.a.type">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						name="typeListB"
						[valuePrimitive]="true"
						[data]="typeList"
						[(ngModel)]="dependencies.a.type">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">Status</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						name="statusListA"
						[valuePrimitive]="true"
						[data]="statusList"
						[(ngModel)]="dependencies.a.status">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						name="statusListB"
						[valuePrimitive]="true"
						[data]="statusList"
						[(ngModel)]="dependencies.a.status">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">Direction</div>
				<div class="dependency-a">
					<kendo-dropdownlist
						name="directionListA"
						[valuePrimitive]="true"
						[data]="directionList"
						[(ngModel)]="dependencies.a.direction">
					</kendo-dropdownlist>
				</div>
				<div class="dependency-b">
					<kendo-dropdownlist
						name="directionListB"
						[valuePrimitive]="true"
						[data]="directionList"
						[(ngModel)]="dependencies.b.direction">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c1</div>
				<div class="dependency-a">
					<input type="text" name="c1A" [(ngModel)]="dependencies.a.c1">
				</div>
				<div class="dependency-b">
					<input type="text" name="c1B" [(ngModel)]="dependencies.b.c1">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c2</div>
				<div class="dependency-a">
					<input type="text" name="c2A" [(ngModel)]="dependencies.a.c2">
				</div>
				<div class="dependency-b">
					<input type="text" name="c2B" [(ngModel)]="dependencies.b.c2">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c3</div>
				<div class="dependency-a">
					<input type="text" name="c3A" [(ngModel)]="dependencies.a.c3">
				</div>
				<div class="dependency-b">
					<input type="text" name="c3B" [(ngModel)]="dependencies.b.c3">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">c4</div>
				<div class="dependency-a">
					<input type="text" name="c4A" [(ngModel)]="dependencies.a.c4">
				</div>
				<div class="dependency-b">
					<input type="text" name="c4B" [(ngModel)]="dependencies.b.c4">
				</div>
			</div>
			<div class="dependency-row  dependency-edit">
				<div class="dependency-row-legend">comment</div>
				<div class="dependency-a">
					<textarea name="commentA" [(ngModel)]="dependencies.a.comment"></textarea>
				</div>
				<div class="dependency-b">
					<textarea name="commentB" [(ngModel)]="dependencies.b.comment"></textarea>
				</div>
			</div>
		</div>
	`
})
export class AssetDependencyEditComponent implements OnInit {
	@Input() dependencyA: any;
	@Input() dependencyB: any;
	@Input() frequencyList: string[] = [];
	@Input() statusList: string[] = [];
	@Input() typeList: string[] = [];
	@Input() directionList: string[] = [];

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
			frequency: dependency && dependency.dataFlowFreq || '',
			direction: dependency && dependency.dataFlowDirection || '',
			status: dependency && dependency.status || '',
			type: dependency && dependency.type || '',
			c1: dependency && dependency.c1 || '',
			c2: dependency && dependency.c2 || '',
			c3: dependency && dependency.c3 || '',
			c4: dependency && dependency.c4 || '',
			comment: dependency && dependency.comment || ''
		}
	}
}