import { Injectable, ComponentRef } from '@angular/core';
import { NotifierService } from './notifier.service';

@Injectable()
export class UIDialogService {
    constructor(private notifier: NotifierService) {

    }

    open(component: any, params: Array<any>): Promise<any> {
        return new Promise((resolve, reject) => {
            this.notifier.broadcast({
                name: 'dialog.open',
                component: component,
                params: params,
                resolve: resolve,
                reject: reject
            });
        });
    }
}

@Injectable()
export class UIActiveDialogService {
    componentInstance: ComponentRef<{}>;

    constructor(private notifier: NotifierService) {

    }

    isDialogOpen(): boolean {
        return this.componentInstance ? true : false;
    }

    close(value: any): void {
        if (this.isDialogOpen()) {
            this.notifier.broadcast({
                name: 'dialog.close',
                result: value
            });
        }
    }

    dismiss(value: any): void {
        if (this.isDialogOpen()) {
            this.notifier.broadcast({
                name: 'dialog.dismiss',
                result: value
            });
        }
    }
}