import { EventEmitter, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ListService {
  private _edit: EventEmitter<any> = new EventEmitter();
  private _remove: EventEmitter<any> = new EventEmitter();
  private _add: EventEmitter<any> = new EventEmitter();
  private _move: EventEmitter<any> = new EventEmitter();
  private _code: EventEmitter<any> = new EventEmitter();
  private _share: EventEmitter<any> = new EventEmitter();
  private _overlay: EventEmitter<any> = new EventEmitter();
  private _status: EventEmitter<any> = new EventEmitter();

  private _clone: EventEmitter<any> = new EventEmitter();

  private _join: EventEmitter<any> = new EventEmitter();

  private _dialog: EventEmitter<any> = new EventEmitter();

  private _refresh: EventEmitter<any> = new EventEmitter();

  private _recommend: EventEmitter<any> = new EventEmitter();

  private _delete: EventEmitter<any> = new EventEmitter();

  constructor() {}

  get edit(): EventEmitter<any> {
    return this._edit;
  }

  get share(): EventEmitter<any> {
    return this._share;
  }

  get clone(): EventEmitter<any> {
    return this._clone;
  }

  get join(): EventEmitter<any> {
    return this._join;
  }

  get status(): EventEmitter<any> {
    return this._status;
  }

  get remove(): EventEmitter<any> {
    return this._remove;
  }

  get move(): EventEmitter<any> {
    return this._move;
  }

  get add(): EventEmitter<any> {
    return this._add;
  }

  get code(): EventEmitter<any> {
    return this._code;
  }

  get overlay(): EventEmitter<any> {
    return this._overlay;
  }

  get dialog(): EventEmitter<any> {
    return this._dialog;
  }

  get refresh(): EventEmitter<any> {
    return this._refresh;
  }

  get recommend(): EventEmitter<any> {
    return this._recommend;
  }

  get delete(): EventEmitter<any> {
    return this._delete;
  }
}
