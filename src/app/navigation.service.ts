import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NavigationService {
  private _reset = new Subject<void>();
  private _online = new BehaviorSubject<any>({ online: true });
  private _server = new Subject<any>();
  private _token: string;
  private _profileId: string; // = 'b33ec186-aea8-4636-b635-4a2f620a0c54';
  private _pos: any;
  private _prevUrl: any;
  private _selected: any;
  private _groupName: any;
  private _groupType: any;
  private _user: any;

  private _notif: any;

  private _locale: any;

  private _xlink: any;

  private _msg = new Array();

  constructor() {}

  get xlink(): any {
    return this._xlink;
  }

  set xlink(xlink: any) {
    this._xlink = xlink;
  }

  get reset(): Subject<void> {
    return this._reset;
  }

  get online(): Subject<any> {
    return this._online;
  }

  get server(): Subject<any> {
    return this._server;
  }

  get token(): string {
    return this._token;
  }

  set token(token) {
    this._token = token;
  }

  get user(): string {
    return this._user;
  }

  set user(user) {
    this._user = user;
  }

  get prevUrl(): string {
    return this._prevUrl;
  }

  set prevUrl(prevUrl) {
    this._prevUrl = prevUrl;
  }

  get selected(): string {
    return this._selected;
  }

  set selected(selected) {
    this._selected = selected;
  }

  get pos(): any {
    return this._pos;
  }

  set pos(pos) {
    this._pos = pos;
  }

  get groupName(): any {
    return this._groupName;
  }

  set groupName(groupName) {
    this._groupName = groupName;
  }

  get groupType(): any {
    return this._groupType;
  }

  set groupType(groupType) {
    this._groupType = groupType;
  }


  get notif(): any {
    return this._notif;
  }

  set notif(notif) {
    this._notif = notif;
  }

  get profileId(): string {
    return this._profileId;
  }

  set profileId(profileId) {
    this._profileId = profileId;
  }

  addMsg(route: string, msg: string) {
    if (this._msg[route] === undefined) {
      this._msg[route] = new Array();
    }

    (this._msg[route] as Array<string>).push(msg);
  }

  getMsg(route: string): Array<string> {
    return this._msg[route];
  }

  get locale(): any {
    return this._locale;
  }

  set locale(msg: any) {
    this._locale = msg;
  }
}
