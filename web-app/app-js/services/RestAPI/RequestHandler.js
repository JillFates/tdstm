/**
 * Created by Jorge Morayta on 12/23/2015.
 * Implements RX Observable to dispose and track better each call to the server
 * The Observer subscribe a promise.
 */


export default class RequestHandler {
    constructor(rx) {
        this.rx = rx;
        this.promise = [];
    }

    /**
     * Called from RestServiceHandler.subscribeRequest
     * it verify that the call is being done to the server and return a promise
     * @param request
     * @returns {*}
     */
    subscribeRequest(request, onSuccess, onError) {
        var rxObservable = this.rx.Observable.fromPromise(request);
        // Verify is not a duplicate call
        if (this.isSubscribed(rxObservable)) {
            this.cancelRequest(rxObservable);
        }

        // Subscribe the request
        var resultSubscribe = this.addSubscribe(rxObservable, onSuccess, onError);
        if (resultSubscribe && resultSubscribe.isStopped) {
            // An error happens, tracked by HttpInterceptorInterface
            delete this.promise[rxObservable._p];
        }
    }

    addSubscribe(rxObservable, onSuccess, onError) {
        this.promise[rxObservable._p] = rxObservable.subscribe(
            (response) => {
                return this.onSubscribedSuccess(response, rxObservable, onSuccess);
            },
            (error) => {
                return this.onSubscribedError(error, rxObservable, onError);
            }, () => {
                // NO-OP Subscribe completed
            });

        return this.promise[rxObservable._p];
    }

    cancelRequest(rxObservable) {
        if (this.isSubscribed(rxObservable)) {
            delete this.promise[rxObservable._p];
            rxObservable.dispose();
        }
    }

    isSubscribed(rxObservable) {
        return (rxObservable && rxObservable._p && this.promise[rxObservable._p]);
    }

    onSubscribedSuccess(response, rxObservable, onSuccess) {
        if (this.isSubscribed(rxObservable)) {
            delete this.promise[rxObservable._p];
        }
        if(onSuccess){
            return onSuccess(response.data);
        }
    }

    /**
     * Throws immediately error when the petition call is wrong
     * or with a delay if the call is valid
     * @param error
     * @returns {*}
     */
    onSubscribedError(error, rxObservable, onError) {
        if (this.isSubscribed(rxObservable)) {
            delete this.promise[rxObservable._p];
        }
        if(onError){
            return onError({});
        }
    }

}