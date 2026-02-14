import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class MockApiService {
  private readonly pageSize = 20;
  private readonly settingsByKey: { [key: string]: any } = {};

  private readonly firstNames = [
    'Alex',
    'Jamie',
    'Taylor',
    'Jordan',
    'Morgan',
    'Avery',
    'Casey',
    'Riley',
    'Skyler',
    'Quinn',
  ];

  private readonly vehicleMakes = ['Toyota', 'Honda', 'Ford', 'BMW', 'Audi'];

  get(urlPart: string, params: HttpParams = new HttpParams()): Observable<any> {
    const data = this.createGetResponse(urlPart, params);
    return of(data).pipe(delay(120));
  }

  post(urlPart: string, body?: any, params: HttpParams = new HttpParams()): Observable<any> {
    if (urlPart.indexOf('/verify') !== -1) {
      return of({ ok: true, verified: true, code: body ? body.code : undefined }).pipe(delay(120));
    }

    return of(this.wrapWrite(urlPart, body, params)).pipe(delay(120));
  }

  patch(urlPart: string, body?: any, params: HttpParams = new HttpParams()): Observable<any> {
    return of(this.wrapWrite(urlPart, body, params)).pipe(delay(120));
  }

  save(urlPart: string, body?: any, params: HttpParams = new HttpParams()): Observable<any> {
    return of(this.wrapWrite(urlPart, body, params)).pipe(delay(120));
  }

  delete(urlPart: string): Observable<any> {
    return of({ ok: true, deleted: true, url: urlPart }).pipe(delay(120));
  }

  upload(urlPart: string): Observable<any> {
    return of({ ok: true, uploaded: true, url: urlPart }).pipe(delay(120));
  }

  private createGetResponse(urlPart: string, params: HttpParams): any {
    if (urlPart === '/i18n_messages') {
      return {
        msg: {
          'expl.videos': [],
          server_down: 'Server is unavailable',
        },
      };
    }

    if (urlPart === '/user') {
      return this.userPayload();
    }

    if (urlPart === '/user/settings') {
      const key = params.get('key') || 'default';
      if (this.settingsByKey[key] === undefined) {
        this.settingsByKey[key] = this.defaultSetting();
      }

      return {
        setting: JSON.parse(JSON.stringify(this.settingsByKey[key])),
      };
    }

    if (urlPart.indexOf('/share') !== -1) {
      return {
        info: {
          title: 'Myscoutee mock share',
          msg: 'This is a mock share payload for UI preview',
        },
        link: { key: this.id('share') },
      };
    }

    if (urlPart.indexOf('/code') !== -1) {
      return { code: 'MOCK-' + this.num(1000, 9999) };
    }

    if (urlPart.indexOf('/rewards') !== -1) {
      return {
        score: 1280,
        badges: ['Starter', 'Reliable', 'Connector'],
        streak: 6,
      };
    }

    if (urlPart.indexOf('/user/profile') !== -1) {
      const profile = this.profile(0);
      return {
        profile,
      };
    }

    const direction = parseInt(params.get('direction') || '1', 10);
    const offset = params.getAll('offset') || [];

    return this.listPayload(urlPart, direction, offset);
  }

  private listPayload(url: string, direction: number, offset: string[]): any {
    let values = new Array(this.pageSize)
      .fill(0)
      .map((_, idx) => this.itemForUrl(url, idx));

    if (url.indexOf('/games/') !== -1) {
      const setting = this.settingsByKey[url] || this.defaultSetting();

      const distItem = setting.items.find((item) => item.name === 'dist');
      const groupItem = setting.items.find((item) => item.name === 'group');

      const distMin = distItem && distItem.data && distItem.data.length > 0 ? distItem.data[0] : 0;
      const distMax = distItem && distItem.data && distItem.data.length > 1 ? distItem.data[1] : 100;

      values = values
        .map((value, idx) => {
          const dist = (idx + 1) * 4;
          if (value.profile) {
            value.profile.firstName = value.profile.firstName + ' (' + dist + 'km)';
            const dt = new Date(Date.now() - idx * 86_400_000);
            value.groupKey = this.groupKeyByStep(
              groupItem && groupItem.options && groupItem.data
                ? groupItem.options[groupItem.data[0]].value
                : 'm',
              dt
            );
            value.sortKey = dt.toISOString();
            value.dist = dist;
          }
          return value;
        })
        .filter((value) => value.dist === undefined || (value.dist >= distMin && value.dist <= distMax));
    }

    const now = Date.now();
    const offsetSeed = offset.length > 0 ? decodeURIComponent(offset[0]) : new Date(now).toISOString();

    return {
      role: 'A',
      scroll: 0,
      offset: [offsetSeed],
      values: direction === -1 ? values.reverse() : values,
    };
  }

  private itemForUrl(url: string, idx: number): any {
    const base = {
      sortKey: new Date(Date.now() - idx * 3600_000).toISOString(),
      groupKey: new Date().toISOString(),
      rate: this.num(0, 10),
      role: 'M',
    } as any;

    if (url.indexOf('/channels/') !== -1 && url.indexOf('/items') !== -1) {
      return {
        ...base,
        name: this.firstNames[idx % this.firstNames.length],
        from: { name: 'avatar-member-' + idx + '.jpg' },
        reads: idx % 3 === 0 ? [{ name: 'avatar-member-read-' + idx + '.jpg' }] : [],
        message: {
          key: this.id('msg-detail', idx),
          eventId: this.id('evt', 1),
          ref: this.id('ref', idx),
          from: this.id('profile', idx % this.firstNames.length),
          type: 'p',
          value: 'Member message #' + (idx + 1) + ' in this channel',
          createdDate: new Date(Date.now() - idx * 60_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/channels') !== -1 && url.indexOf('/items') === -1) {
      return {
        ...base,
        name: 'Channel ' + (idx + 1),
        from: { name: 'avatar-' + idx + '.jpg' },
        reads: [],
        message: {
          key: this.id('msg', idx),
          eventId: this.id('evt', Math.floor(idx / 2)),
          ref: this.id('ref', idx),
          from: idx % 2 === 0 ? this.id('profile', 1) : this.id('profile', 2),
          type: 'p',
          value: 'Mock chat message #' + (idx + 1),
          createdDate: new Date(Date.now() - idx * 60_000).toISOString(),
        },
      };
    }

    if (
      url.indexOf('/invitations') !== -1 &&
      url.indexOf('/items') === -1 &&
      url.indexOf('/members') === -1 &&
      url.indexOf('/feedbacks') === -1
    ) {
      return {
        ...base,
        status: ['A', 'P', 'C'][idx % 3],
        event: {
          key: this.id('inv-event', idx),
          createdBy: this.id('profile', idx % 5),
          info: this.eventInfo('Invitation Event ' + (idx + 1), idx),
        },
      };
    }

    if (url.indexOf('/members') !== -1) {
      return {
        ...base,
        member: {
          key: this.id('member', idx),
          status: ['A', 'F', 'I'][idx % 3],
          profile: this.profile(idx),
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/feedbacks') !== -1) {
      return {
        ...base,
        feedback: {
          key: this.id('fb', idx),
          desc: 'Mock feedback entry #' + (idx + 1),
          rate: this.num(1, 10),
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/schools') !== -1) {
      return {
        ...base,
        school: {
          key: this.id('school', idx),
          name: 'University ' + (idx + 1),
          role: idx % 2 === 0 ? 'Student' : 'Alumni',
          type: idx % 2 === 0 ? 'w' : 'c',
          range: {
            start: new Date(2014 + (idx % 6), 0, 1).toISOString(),
            end: new Date(2018 + (idx % 6), 0, 1).toISOString(),
          },
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/cars') !== -1) {
      return {
        ...base,
        car: {
          key: this.id('car', idx),
          make: this.vehicleMakes[idx % this.vehicleMakes.length],
          model: 'Model ' + (idx + 1),
          capacity: 4 + (idx % 3),
          regNum: 'MOCK-' + (1000 + idx),
          images: [],
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/groups') !== -1 && url.indexOf('/profiles') === -1) {
      return {
        ...base,
        group: {
          key: this.id('group', idx),
          name: 'Mock Group ' + (idx + 1),
          desc: 'Group description #' + (idx + 1),
          images: [],
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/events') !== -1 && url.indexOf('/items') === -1) {
      return {
        ...base,
        status: ['A', 'P', 'C'][idx % 3],
        event: {
          key: this.id('event', idx),
          createdBy: this.id('profile', idx % 5),
          info: this.eventInfo('Mock Event ' + (idx + 1), idx),
        },
      };
    }

    if (url.indexOf('/items') !== -1) {
      return {
        ...base,
        item: this.eventItemInfo(idx),
        main: idx === 0,
        category: idx % 2 === 0 ? 'g' : 'c',
      };
    }

    if (url.indexOf('/ideas') !== -1) {
      return {
        ...base,
        idea: {
          key: this.id('idea', idx),
          name: 'Idea ' + (idx + 1),
          desc: 'Idea description #' + (idx + 1),
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    if (url.indexOf('/jobs') !== -1) {
      return {
        ...base,
        job: {
          key: this.id('job', idx),
          name: 'Job post ' + (idx + 1),
          desc: 'Job description #' + (idx + 1),
          createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
        },
      };
    }

    return {
      ...base,
      profile: this.profile(idx),
      role: 'U',
    };
  }

  private userPayload(): any {
    const profile = this.profile(0);

    return {
      user: {
        profile,
        group: 'group-1',
      },
      groups: [
        {
          group: {
            key: 'group-1',
            type: 'd',
            name: 'Mock Dating',
          },
        },
      ],
      likes: [],
    };
  }

  private profile(idx: number): any {
    return {
      key: this.id('profile', idx),
      firstName: this.firstNames[idx % this.firstNames.length],
      birthday: new Date(1990 + (idx % 12), idx % 12, 10).toISOString(),
      height: 158 + (idx % 30),
      physique: ['a', 's', 'm', 'sp'][idx % 4],
      smoker: idx % 5 === 0,
      hasChild: idx % 6 === 0,
      marital: idx % 7 === 0 ? 't' : 's',
      status: ['A', 'F', 'I'][idx % 3],
      images: [],
      createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
    };
  }

  private wrapWrite(urlPart: string, body: any, params: HttpParams = new HttpParams()): any {
    if (urlPart === '/user' || urlPart.indexOf('/user/groups/') !== -1) {
      return this.userPayload();
    }

    if (urlPart === '/user/settings') {
      const key = params.get('key') || 'default';
      this.settingsByKey[key] = body || this.defaultSetting();

      return {
        setting: JSON.parse(JSON.stringify(this.settingsByKey[key])),
      };
    }

    return {
      ok: true,
      url: urlPart,
      data: body,
    };
  }

  private id(prefix: string, idx: number = this.num(1, 99999)): string {
    return prefix + '-' + idx;
  }

  private eventInfo(name: string, idx: number): any {
    return {
      key: this.id('event-info', idx),
      type: idx % 2 === 0 ? 'g' : 'e',
      optional: false,
      ticket: idx % 3 === 0,
      discreet: idx % 4 === 0,
      category: idx % 2 === 0 ? 'g' : 'c',
      access: idx % 3 === 0 ? 'F' : 'P',
      name,
      urlRef: 'https://example.com/event/' + idx,
      range: {
        start: new Date(Date.now() + idx * 3_600_000).toISOString(),
        end: new Date(Date.now() + (idx + 2) * 3_600_000).toISOString(),
      },
      capacity: { min: 2, max: 10 },
      desc: 'Mock event description for UI preview #' + (idx + 1),
      autoInvite: idx % 2 === 0,
      rule: {
        type: 'j',
        autoApprove: true,
        eventGrace: 15,
        memberGrace: 10,
        balanced: false,
        mutual: false,
        rate: 0,
        rankType: 'none',
      },
      amount: {
        value: 0,
        currency: 'USD',
      },
      num: this.num(1, 8),
      chatKey: this.id('chat', idx),
      createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
    };
  }

  private eventItemInfo(idx: number): any {
    return {
      key: this.id('item', idx),
      type: idx % 2 === 0 ? 'g' : 'e',
      optional: idx % 4 === 0,
      ticket: false,
      discreet: false,
      category: idx % 2 === 0 ? 'g' : 'c',
      access: 'P',
      name: 'Activity Item ' + (idx + 1),
      urlRef: 'https://example.com/item/' + idx,
      range: {
        start: new Date(Date.now() + idx * 3_600_000).toISOString(),
        end: new Date(Date.now() + (idx + 1) * 3_600_000).toISOString(),
      },
      capacity: { min: 1, max: 8 },
      desc: 'Mock activity item description for UI preview #' + (idx + 1),
      autoInvite: false,
      rule: {
        type: 'j',
        autoApprove: true,
        eventGrace: 0,
        memberGrace: 0,
        balanced: false,
        mutual: false,
        rate: 0,
        rankType: 'none',
      },
      amount: {
        value: 0,
        currency: 'USD',
      },
      num: this.num(0, 7),
      createdDate: new Date(Date.now() - idx * 86_400_000).toISOString(),
    };
  }

  private num(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }

  private defaultSetting(): any {
    return {
      items: [
        {
          name: 'group',
          type: 'os',
          options: [
            { value: 'd', viewValue: 'Day' },
            { value: 'w', viewValue: 'Week' },
            { value: 'm', viewValue: 'Month' },
          ],
          data: [2],
        },
        {
          name: 'dist',
          type: 'ms',
          options: [],
          range: {
            min: 0,
            max: 100,
          },
          data: [5, 30],
        },
      ],
    };
  }

  private groupKeyByStep(step: string, date: Date): string {
    const year = date.getUTCFullYear();
    const month = (date.getUTCMonth() + 1).toString().padStart(2, '0');
    const day = date.getUTCDate().toString().padStart(2, '0');

    if (step === 'd') {
      return year + '-' + month + '-' + day;
    }

    if (step === 'w') {
      const onejan = new Date(Date.UTC(year, 0, 1));
      const week = Math.ceil((((date.getTime() - onejan.getTime()) / 86400000) + onejan.getUTCDay() + 1) / 7);
      return year + ' W' + week.toString().padStart(2, '0');
    }

    return year + '-' + month;
  }
}