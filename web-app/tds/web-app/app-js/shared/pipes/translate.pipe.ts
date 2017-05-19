import { Pipe, PipeTransform } from '@angular/core';
import { en_DICTIONARY } from '../i18n/en.dictionary';

@Pipe({ name: 'translate' })
export class TranslatePipe implements PipeTransform {

    private dictionary: Object = {};

    constructor() {

        /*
         TODO: Will look for dictionary file here based on user locale code (i.e. 'es.json'), by the moment will always load default en.json
         */
        let found = false;
        let localizaedDictionary: Object;

        // If not found grab default english dictionmary
        if (!found) {
            localizaedDictionary = en_DICTIONARY;
        }

        /*
         For performance reasons we will load properties from dictionary at first so we avoid iterate through it
         everytime a translation is required.
         */
        for (let property of Object.keys(localizaedDictionary)) {
            this.traverse(property, localizaedDictionary[property]);
        }
    }

    /**
     * Traverse to the entire dictionary tree storing it's definitions as a unique key - value pair.
     * @param key
     * @param value
     */
    traverse(key, value): void {

        if ( typeof value === 'object' ) {
            for (let property of Object.keys(value) ) {
                this.traverse(key + '.' + property, value[property]);
            }
        }else {
            this.dictionary[key] = value;
        }

    }

    transform(value: string): string {

        let translatedValue: string;

        translatedValue = this.dictionary[value] ? this.dictionary[value] : value;

        return translatedValue;
    }
}