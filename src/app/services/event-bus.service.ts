import { EventEmitter, Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class EventBusService {
  private _select: EventEmitter<any> = new EventEmitter();
  private _rate: EventEmitter<any> = new EventEmitter();
  private _rotate: EventEmitter<any> = new EventEmitter();
  private _grow: EventEmitter<any> = new EventEmitter();

  private _list: BehaviorSubject<any> = new BehaviorSubject({});

  constructor() {}

  get grow(): EventEmitter<any> {
    return this._grow;
  }

  get select(): EventEmitter<any> {
    return this._select;
  }

  get rate(): EventEmitter<any> {
    return this._rate;
  }

  get rotate(): EventEmitter<any> {
    return this._rotate;
  }
}