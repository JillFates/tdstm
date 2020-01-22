import {Layout, Link, Node} from 'gojs';
import {IconModel, IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';

export interface IDiagramLayoutHelper {

	/**
	 * Diagram data object
	 */
	diagramData(params?: any): IDiagramData;

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	data(data?: any): any;

	/**
	 * LinksPath object
	 **/
	getLinksPath(link: any): any;

	/**
	 * Node template
	 **/
	nodeTemplate(): Node;

	/**
	 * Links Template
	 **/
	linkTemplate(): Link;

	/**
	 * Layout template
	 **/
	layout(): Layout;

	/**
	 * Low scale node template
	 **/
	lowScaleNodeTemplate(): Node;
	/**
	 * medium scale node template
	 **/
	mediumScaleNodeTemplate(): Node;

	/**
	 * context menu options
	 **/
	contextMenuOptions(): ITdsContextMenuOption;

	/**
	 * Diagram events
	 */
	diagramEvents(): any[];

	/**
	 * icons for the diagram
	 **/
	icons(): IconModel;
}
