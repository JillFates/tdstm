/**
 * Created by aaferreira on 15/02/2017.
 */
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'addEmotion' })
export class AddEmotionPipe implements PipeTransform {
    transform(value: string, emphasis = false): string {
        value += emphasis ? '!!!!' : '';
        return value.toUpperCase();
    }
}