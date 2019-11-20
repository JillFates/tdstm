import {
	Binding,
	Diagram,
	Layout,
	Link,
	Margin,
	Node,
	Panel,
	Picture,
	Shape,
	Size,
	Spot,
	TextBlock,
	TreeLayout
} from 'gojs';
import {
	ITdsContextMenuOption
} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {IconModel, IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {ILinkPath} from '../../../taskManager/components/neighborhood/neighborhood.component';
import {IArchitectureGraphAsset, IAssetLink, IAssetNode} from '../../model/architecture-graph-asset.model';
import {ASSET_ICONS} from '../../model/asset-icon.constant';

export class ArchitectureGraphDiagramHelper {

	/**
	 * Diagram data object
	 */
	static diagramData(rootAsset: number | string, currentUserId?: any, data?: any, scaleMode?: any, iconOnly?: boolean): IDiagramData {
		const d = this.data(data);
		return {
			nodeDataArray: d.nodeDataArray,
			linkDataArray: d.linkDataArray,
			currentUserId: currentUserId,
			ctxMenuOptions: this.contextMenuOptions(),
			nodeTemplate: iconOnly ? this.iconOnlyNodeTemplate() : this.nodeTemplate(),
			linkTemplate: this.linkTemplate(),
			lowScaleTemplate: this.lowScaleNodeTemplate(),
			mediumScaleTemplate: this.mediumScaleNodeTemplate(),
			layout: this.layout(),
			autoScaleMode: !!scaleMode && scaleMode || null,
			rootAsset: rootAsset
		};
	}

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	static data(data?: any): any {
		const nodeDataArr = [];
		const linksPath = [];

		const dataCopy: IArchitectureGraphAsset = Object.assign({}, data);

		dataCopy.nodes.map((t: IAssetNode) => {
			t.key = t.id;
			t.iconPath = t.assetClass && ASSET_ICONS[t.assetClass.toLowerCase()].icon;
			nodeDataArr.push(t);
		});
		dataCopy.links
			.forEach((link: IAssetLink) => linksPath.push(this.getLinksPath(link)));
		// console.log('tasks found', this.tasks.length, 'tasks dependencies', linksPath);
		return {
			nodeDataArray: nodeDataArr,
			linkDataArray: linksPath,
		};
	}

	/**
	 * Load events to fill events dropdown
	 **/
	static getLinksPath(link: any): ILinkPath {
		const t = Object.assign({}, link);
		if (t) {
			return {
				from: t.parentId,
				to: t.childId
			};
		}
		return null;
	}

	static currentUser(): Node {
		return null;
	}

	static nodeTemplate(): Node {
		const node = new Node(Panel.Horizontal);
		node.margin = new Margin(1, 1, 1, 1);

		const panel = new Panel(Panel.Auto);
		panel.background = '#fff';
		panel.padding = new Margin(0, 0, 0, 0);

		const nodeShape = new Shape();
		nodeShape.figure = 'RoundedRectangle';
		nodeShape.strokeWidth = 2;
		nodeShape.stroke = '#3c8dbc';
		nodeShape.fill = '#3c8dbc';

		const panelBody = new Panel(Panel.Vertical);
		panel.padding = new Margin(0, 0, 0, 0);
		panel.margin = new Margin(0, 0, 0, 0);

		// Picture Icon
		const iconPicture = new Picture();
		iconPicture.desiredSize = new Size(50, 50);
		iconPicture.bind(new Binding('source', 'assetClass',
			(val: string) => this.getIconPath(val)));
		iconPicture.imageAlignment = Spot.Center;

		// TextBlock
		const textBlock = new TextBlock();
		textBlock.stroke = '#fff';
		textBlock.bind(new Binding('text', 'name'));

		panelBody.add(iconPicture);
		panelBody.add(textBlock);

		panel.add(nodeShape);
		panel.add(panelBody);

		node.add(panel);
		node.click = (i, o) => console.log('click');

		return node;
	}

	static iconOnlyNodeTemplate(): Node {
		const node = new Node(Panel.Viewbox);
		node.margin = new Margin(1, 1, 1, 1);
		node.desiredSize = new Size(72, 62);

		// Picture Icon
		const iconPicture = new Picture();
		iconPicture.desiredSize = new Size(70, 60);
		iconPicture.bind(new Binding('source', 'assetClass',
			(val: string) => this.getIconPath(val)));
		iconPicture.imageStretch = Diagram.UniformToFill;

		node.add(iconPicture);
		node.click = (i, o) => console.log('click');

		return node;
	}

	private static getIconPath(name: string): string {
		const icon = ASSET_ICONS[name && name.toLowerCase()];
		return !!icon ? icon.icon : ASSET_ICONS.application.icon;
	}

	static linkTemplate(): Link {
		const linkTemplate = new Link();
		linkTemplate.routing = Link.AvoidsNodes;
		linkTemplate.corner = 5;

		const linkShape = new Shape();
		linkShape.strokeWidth = 5;
		linkShape.stroke = '#ddd';
		const arrowHead = new Shape();
		arrowHead.toArrow = 'Standard';
		arrowHead.stroke = '#3c8dbc';
		arrowHead.fill = '#3c8dbc';

		linkTemplate.add(linkShape);
		linkTemplate.add(arrowHead);

		return linkTemplate;
	}

	static layout(): Layout {
		const treeLayout = new TreeLayout();
		treeLayout.angle = 90;
		treeLayout.layerSpacing = 35;
		return treeLayout;
	}

	static lowScaleNodeTemplate(): Node {
		const node = new Node(Panel.Horizontal);

		const  shape = new Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new Size(25, 35);
		shape.fill = '#ddd';
		node.add(shape);

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => console.log('click');
		return node;
	}

	static mediumScaleNodeTemplate(): Node {

		const node = new Node(Panel.Horizontal);

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => console.log('click');
		return node;
	}

	static contextMenuOptions(): ITdsContextMenuOption {
		return null;
	}

	static icons(): IconModel {
		return null;
	}

	static ctxMenuActionDispatched(action: string): void {
		// TODO
	}

	static nodeClicked(): void {
		// TODO
	}

	static animationFinished(): void {
		// TODO
	}

}
