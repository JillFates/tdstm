import {
		Component,
		OnInit,
		Input,
		Output,
		AfterViewInit,
		OnChanges,
		SimpleChanges,
		EventEmitter,
		Renderer2
} from '@angular/core';
import * as go from 'gojs';
import {ICONS_PATH} from '../gojs-diagrams/icons-path';
import {
	Adornment,
	Binding,
	Diagram,
	InputEvent,
	Overview,
	Panel,
	Placeholder,
	Shape,
	Spot,
	TextBlock
} from 'gojs';
import {TaskService} from '../../../modules/taskManager/service/task.service';

const enum NodeTemplateEnum {
	HIGH_SCALE,
	MEDIUM_SCALE,
	LOW_SCALE
}

const categoryColors = {
	physical: 'brown',
	shutdown: 'red',
	general: '#ddd',
	moveday: 'skyblue'
};

@Component({
	selector: 'tds-layered-digraph-layout',
	template: `
		<div class="layered-digraph-layout-container">
				<div
						id="digraph-layout-container"
						[style.width]="containerWidth"
						[style.height]="containerHeight"></div>
				<div id="overview-container" class="overview-container"></div>
		</div>
	`,
	// styleUrls: ['../../../../../css/shared/components/layered-digraph-layout.component.scss']
})
export class LayeredDigraphLayoutComponent implements OnInit, AfterViewInit, OnChanges {
	@Input() name: string;
	@Input() model: any;
	@Input() nodeDataArray = [];
	@Input() linksPath = [];
	@Input() nodeTemplateOpts: go.Node;
	@Input() linkTemplateOpts: go.Link;
	@Input() layoutOpts: go.Layout;
	@Input() containerWidth: string;
	@Input() containerHeight: string;
	@Output() nodeClicked: EventEmitter<number> = new EventEmitter<number>();
	@Output() editClicked: EventEmitter<number> = new EventEmitter<number>();
	@Output() showTaskDetailsClicked: EventEmitter<number> = new EventEmitter<number>();
	stateIcons = ICONS_PATH;
	diagram: go.Diagram;
	myModel: go.Model;
	layouts: string[] = ['TreeLayout', 'ForceDirectedLayout', 'GridLayout', 'LayeredDigraphLayout'];
	selectedLayout = 'TreeLayout';
	direction = 0;
	diagramAvailable = false;
	tasks: any[];
	actualNodeTemplate: number;
	diagramOverview: Overview;

	constructor(private taskService: TaskService, renderer: Renderer2) {}

	ngOnInit() {
		console.log('model');
	}

	ngOnChanges(simpleChanges: SimpleChanges): void {
		if (simpleChanges && ( simpleChanges.nodeDataArray && simpleChanges.linksPath)
				&& !(simpleChanges.nodeDataArray.firstChange && simpleChanges.linksPath.firstChange)
		) {
			// console.log('simpleChange: ', simpleChanges);
			this.loadModel();
			this.initialiseDiagramContainer();
			this.generateDiagram();
		}
	}

	ngAfterViewInit() {
		this.loadModel();
		// console.log('diagram', this.nodeDataArray, this.linksPath);
		this.initialiseDiagramContainer();
		this.generateDiagram();
	}

	initialiseDiagramContainer(): void {
		if (!this.diagram) {
			this.diagram = new Diagram('digraph-layout-container');
		}
	}

	onNodeClick(inputEvent: InputEvent, obj: any): void {
		console.log('node clicked', obj.selectionAdornmentTemplate);
		// if (obj && obj.part && obj.part.data) { this.nodeClicked.emit(obj.part.data.id); }
		if (obj && obj.part && obj.part.data) {
			obj.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
		}
	}

	loadModel(): void {
		// console.log('loadModel');
		if (this.nodeDataArray && this.linksPath) {
			// console.log('load model: ', this.nodeDataArray, this.linksPath);
			this.myModel = new go.GraphLinksModel(this.nodeDataArray, this.linksPath);
		}
	}

	generateDiagram(): void {
		// console.log('generate');
		this.diagram.startTransaction('generateDiagram');
		this.diagram.initialDocumentSpot = Spot.TopCenter;
		this.diagram.initialViewportSpot = Spot.TopCenter;
		this.diagram.undoManager.isEnabled = true;
		this.diagram.allowZoom = true;
		this.diagram.initialAutoScale = go.Diagram.UniformToFill;
		this.setDiagramNodeTemplate();
		this.setDiagramLinksTemplate();
		this.diagram.layout = this.layeredDigraphLayout();
		this.diagram.allowSelect = true;
		this.diagram.commitTransaction('generateDiagram');
		this.diagram.model = this.myModel;
		this.diagramAvailable = true;
		this.overrideMouseWheel();
		this.overviewTemplate();
	}

	overviewTemplate() {
		this.diagramOverview = new Overview('overview-container');
		this.diagramOverview.observed = this.diagram;
		this.diagramOverview.contentAlignment = go.Spot.Center;
	}

	layeredDigraphLayout(opts?: any): go.LayeredDigraphLayout {
		// console.log('direction: ', this.direction);
		const ldl = new go.LayeredDigraphLayout();
		ldl.direction = 0;
		ldl.layerSpacing = 25;
		ldl.columnSpacing = 25;
		ldl.cycleRemoveOption = go.LayeredDigraphLayout.CycleDepthFirst;
		return ldl;
	}

	setDiagramNodeTemplate(): void {
		this.diagram.nodeTemplate = this.horizontalNodeTemplate();
	}

	setDiagramLinksTemplate(): void {
		this.diagram.linkTemplate = this.linkTemplate();
	}

	linkTemplate(templateOpts?: go.Link, linkShapeOpts?: go.Shape): go.Link {

		if (templateOpts) {
			if (linkShapeOpts) { templateOpts.add(linkShapeOpts); }
			return templateOpts;
		}

		const linkTemplate = new go.Link();
		linkTemplate.routing = go.Link.AvoidsNodes;
		linkTemplate.corner = 5;

		const linkShape = new go.Shape();
		linkShape.strokeWidth = 5;
		linkShape.stroke = '#ddd';

		linkTemplate.add(linkShape);

		return linkTemplate;
	}

	setDirection(dir: any): void {
		// console.log('Direction: ', dir.target.value);
		this.direction = dir.target.value;
	}

	horizontalNodeTemplate(): go.Node {
		console.log('default template');
		this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;
		const node = new go.Node(Panel.Horizontal);
		node.background = '#3c8dbc';
		// node.background = 'lightblue';
		node.selectionAdorned = true;
		node.add(this.iconShape());
		node.add(this.categoryIconShape());
		node.add(this.textBlockShape());
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		// node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		return node;
	}

	selectionAdornmentTemplate(node?: go.Node): Adornment {
		const selAdornmentTemplate = new Adornment(Panel.Auto);
		selAdornmentTemplate.selectionAdorned = true;
		if (node) { selAdornmentTemplate.adornedObject = node; }

		const selAdornmentShape = new Shape();
		selAdornmentShape.figure = 'RoundedRectangle';
		selAdornmentShape.fill = null;
		selAdornmentShape.stroke = 'red';
		selAdornmentShape.strokeWidth = 4;

		const placeholder = new Placeholder();

		// placeholder.background = 'transparent';
		// placeholder.visible = true;
		selAdornmentTemplate.add(selAdornmentShape);
		selAdornmentTemplate.add(placeholder);

		return selAdornmentTemplate;
	}

	categoryIconShape(options?: any): Shape {
		if (options) { return options; }

		const  categoryIconShape = new Shape();
		categoryIconShape.figure = 'RoundedRectangle';
		categoryIconShape.margin = 8;
		categoryIconShape.strokeWidth = 2;
		categoryIconShape.alignment = Spot.LeftCenter;
		categoryIconShape.desiredSize = new go.Size(25, 25);
		categoryIconShape.stroke = 'yellow';
		categoryIconShape.bind(new Binding('geometry', 'category',
			(val: any) => this.getIcon(val)));
		return categoryIconShape;
	}

	iconShape(options?: any): Shape {
		if (options) { return options; }

		const  iconShape = new Shape();
		iconShape.figure = 'RoundedRectangle';
		iconShape.margin = 8;
		iconShape.strokeWidth = 2;
		iconShape.alignment = Spot.LeftCenter;
		iconShape.stroke = 'white';
		iconShape.desiredSize = new go.Size(25, 25);
		iconShape.bind(new Binding('background', 'color'));
		iconShape.bind(new Binding('geometry', 'icon',
			(val: any) => this.getIcon(val)));

		return iconShape;
	}

	textBlockShape(options?: any): TextBlock {
		if (options) { return options; }

		const textBlock = new TextBlock();
		textBlock.margin = 8;
		textBlock.stroke = 'white';
		textBlock.font = 'bold 16px sans-serif';
		textBlock.bind(new Binding('text', 'label'));
		return textBlock;
	}

	getIcon(name: string): any {
		if (!this.stateIcons[name]) {
			console.log('notFound: ', name);
			return go.Geometry.parse(this.stateIcons.unknown);
			// return this.stateIcons.unknown;
		}
		return go.Geometry.parse(this.stateIcons[name]);
		// return this.stateIcons[name];
	}

	zoomIn(): void {
		this.diagram.commandHandler.increaseZoom(1.2);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	zoomOut(): void {
		this.diagram.commandHandler.decreaseZoom(0.8);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	highlightAllNodes(): void {
		this.diagram.commit(d => {
			d.selectCollection(d.nodes);
		});
	}

	highlightSelectedNode(node: go.GraphObject): void {
		// TODO
	}

	highlightNodesByCategory(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.category));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	highlightNodesByStatus(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.status));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	highlightNodesByText(match: string): void {
		this.diagram.commit(d => {
			if (match.length <= 0) { return d.clearSelection(); }
			const highlightCollection = d.nodes.filter(f => f.data.name.toLowerCase().startsWith(match.toLowerCase()));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	findNode(): void {
		console.log(this.diagram.findPartForData({ key: '1', name: 'Ruben', source: '' }));
	}

	overrideMouseWheel(): void {
		// console.log('overriding mouse wheel event');
		const tool = this.diagram.currentTool;
		tool.standardMouseWheel = () => {
			go.Tool.prototype.standardMouseWheel.call(tool);
			this.setNodeTemplateByScale(this.diagram.scale, this.diagram.lastInput);
		};
	}

	setNodeTemplateByScale(scale?: number, inputEvent?: go.InputEvent): void {
		if (inputEvent.control) {
			if (scale >= 0.6446089162177968
					&& this.actualNodeTemplate !== NodeTemplateEnum.HIGH_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;
				console.log('scale >= 0.6446089162177968');
				this.highScaleNodeTemplate();
			}
			if (scale < 0.6446089162177968 && scale > 0.4581115219913999
					&& this.actualNodeTemplate !== NodeTemplateEnum.MEDIUM_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.MEDIUM_SCALE;
				console.log('scale < 0.6446089162177968');
				this.mediumScaleNodeTemplate();
			}
			if (scale <= 0.4581115219913999
					&& this.actualNodeTemplate !== NodeTemplateEnum.LOW_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.LOW_SCALE;
				console.log('scale <= 0.4581115219913999');
				this.lowScaleNodeTemplate();
			}
		}
	}

	highScaleNodeTemplate(): void {
		this.diagram.commit(() => this.diagram.nodeTemplate = this.horizontalNodeTemplate());
	}

	mediumScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		const  iconShape = new go.Shape();
		iconShape.figure = 'RoundedRectangle';
		iconShape.margin = 3;
		iconShape.strokeWidth = 2;
		iconShape.alignment = go.Spot.LeftCenter;
		iconShape.stroke = 'white';
		iconShape.desiredSize = new go.Size(25, 25);
		iconShape.bind(new go.Binding('background', 'color'));
		iconShape.bind(new go.Binding('geometry', 'icon',
			(val: any) => this.getIcon(val)));
		node.add(iconShape);

		const  categoryIconShape = new go.Shape();
		categoryIconShape.figure = 'RoundedRectangle';
		categoryIconShape.margin = 3;
		categoryIconShape.strokeWidth = 2;
		categoryIconShape.alignment = go.Spot.LeftCenter;
		categoryIconShape.desiredSize = new go.Size(25, 25);
		categoryIconShape.stroke = '#7a7a7a';
		categoryIconShape.bind(new go.Binding('geometry', 'category',
			(val: any) => this.getIcon(val)));
		node.add(categoryIconShape);

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	lowScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		const  shape = new go.Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new go.Size(25, 35);
		shape.bind(new go.Binding('fill', 'category', this.getCategoryColor));
		node.add(shape);
		node.contextMenu = this.contextMenu();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	getCategoryColor(name: string): string {
		const color = categoryColors[name];
		return color ? color : '#3c8dbc';
	}

	contextMenu(): any {
		const $ = go.GraphObject.make;
		return $('ContextMenu',  // that has one button
							$('ContextMenuButton',
									$(go.TextBlock, { text: 'Change Color', stroke: '#3c8dbc'}),
									{ click: (e: go.InputEvent, obj: go.GraphObject) => this.changeColor(e, obj, this.diagram) }),
							$('ContextMenuButton',
									$(go.TextBlock, { text: 'Show Detail', stroke: '#3c8dbc'}),
									{ click: (e: go.InputEvent, obj: go.GraphObject) => this.showTaskDetails(obj, this.diagram) }),
							$('ContextMenuButton',
									$(go.TextBlock, { text: 'Edit', stroke: '#3c8dbc'}),
									{ click: (e: go.InputEvent, obj: go.GraphObject) => this.editTask(obj, this.diagram) })
							// more ContextMenuButtons would go here
					); // end Adornment
	}

	showTaskDetails(obj: go.GraphObject, diagram: go.Diagram): void {
		const nodeData = obj.part.data;
		this.showTaskDetailsClicked.emit(nodeData.id);
	}

	editTask(obj: go.GraphObject, diagram: go.Diagram): void {
		const nodeData = obj.part.data;
		this.editClicked.emit(nodeData.id);
	}

	changeColor(e: go.InputEvent, obj: go.GraphObject, diagram) {
		diagram.commit(d => {
			// get the context menu that holds the button that was clicked
			const contextmenu = obj.part;
			console.log('menu: ', contextmenu);
			// get the node data to which the Node is data bound
			const nodedata = contextmenu.data;
			// compute the next color for the node
			let newcolor = 'lightblue';
			switch (nodedata.color) {
				case 'lightblue': newcolor = 'lightgreen'; break;
				case 'lightgreen': newcolor = 'lightyellow'; break;
				case 'lightyellow': newcolor = 'orange'; break;
				case 'orange': newcolor = 'lightblue'; break;
			}
			// modify the node data
			// this evaluates data Bindings and records changes in the UndoManager
			d.model.set(nodedata, 'color', newcolor);
		}, 'changed color');

	}

}
