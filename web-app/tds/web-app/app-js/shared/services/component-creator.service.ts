/**
 * Component Creator allow you to create component dynamically and/or add to a specificy view.
 */
import {Injectable, Injector, ComponentRef, ComponentFactoryResolver, ReflectiveInjector, ViewContainerRef} from '@angular/core';

@Injectable()
export class ComponentCreatorService {

	constructor(private resolver: ComponentFactoryResolver, private parentInjector: Injector) {

	}

	/**
	 * Create component and already add it to an existing view
	 * @param component ComponentType
	 * @param params properties to be inject in the component creation
	 * @param view view to have component inserted into
	 */
	insert(component: any, params: Array<any>, view: ViewContainerRef): ComponentRef<{}> {
		let resolvedInputs = ReflectiveInjector.resolve(params);
		let injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, view.parentInjector);
		let factory = this.resolver.resolveComponentFactory(component);
		return view.createComponent(factory, null, injector);
	}

	/**
	 * Create the component letting you access its methods and variables
	 * @param component ComponentType
	 * @param params properties to be inject in the component creation
	 */
	create(component: any, params: Array<any>): ComponentRef<{}> {
		let resolvedInputs = ReflectiveInjector.resolve(params);
		let injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, this.parentInjector);
		return this.resolver.resolveComponentFactory(component).create(injector);
	}
}