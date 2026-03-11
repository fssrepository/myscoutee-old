import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { NavigationService } from '../navigation.service';

const physiques = { s: 'Slim', a: 'Average', sp: 'Sum plus', m: 'Muscular' };
@Injectable({
  providedIn: 'any',
})
export class TransformService {
  constructor(private datePipe: DatePipe, private navService: NavigationService) { }

  transform(value, url?, inList = false, refresh = false, table = false): any {
    // for mixed type of list, we need to identify profile
    /*if (value['_links']) {
      const url = value['_links'].self.href;
      value['key'] = url.substr(url.lastIndexOf('/') + 1);
    }

    const id = value['key'] !== undefined ? value['key'] : value['id'];

    if (id === undefined) {
      return;
    }*/

    let data;

    if (value !== undefined) {
      if (value['message'] != undefined) {
        this.alignItemZone(value['message']);

        data = this.transformMessage(value['message'].key,
          value['message'],
          url,
          inList,
          value);
      } else if (value['profile'] !== undefined) {
        this.alignItemZone(value['profile']);

        value['profile'].role = value['role'];
        data = this.transformProfile(
          value['profile'].key,
          value['profile'],
          url,
          inList,
          refresh,
          value
        );
      } else if (value['car'] !== undefined) {
        this.alignItemZone(value['car']);

        data = this.transformCar(value['car'].key, value['car'], url, inList);
      } else if (value['school'] !== undefined) {
        this.alignItemZone(value['school']);

        data = this.transformSchool(
          value['school'].key,
          value['school'],
          url,
          inList
        );
      } else if (value['event'] !== undefined) {
        this.alignItemZone(value['event'].info);

        data = this.transformEvent(
          value['event'].key,
          value['event'],
          url,
          inList,
          table,
          value
        );
      } else if (value['item'] !== undefined) {
        this.alignItemZone(value['item']);

        data = this.transformEventItem(
          value['item'].key,
          value['item'],
          url,
          inList,
          value
        );
      } else if (value['feedback'] !== undefined) {
        this.alignItemZone(value['feedback']);

        data = this.transformFeedback(
          value['feedback'].key,
          value['feedback'],
          url,
          inList
        );
      } else if (value['member'] !== undefined) {
        this.alignItemZone(value['member']);

        data = this.transformMember(
          value['member'].key,
          value['member'],
          url,
          inList
        );
      } else if (value['job'] !== undefined) {
        this.alignItemZone(value['job']);

        data = this.transformJob(value['job'].key, value['job'], url, inList);
      } else if (value['idea'] !== undefined) {
        this.alignItemZone(value['idea']);

        data = this.transformIdea(
          value['idea'].key,
          value['idea'],
          url,
          inList
        );
      } else if (value['group'] !== undefined) {
        this.alignItemZone(value['group']);

        data = this.transformGroup(
          value['group'].key,
          value['group'],
          url,
          inList
        );
      } else if (value['promotion'] !== undefined) {
        this.alignItemZone(value['promotion']);

        data = this.transformPromotion(
          value['promotion'].key,
          value['promotion'],
          url,
          inList
        );
      }

      this.alignZone(value);

      data.sortKey = value.sortKey;
      data.groupKey = value.groupKey;
      data.rate = value.rate;
      data.role = value.role;

      return data;
    }
  }

  alignZone(value) {
    if (value !== undefined) {
      value['sortKey'] = this.shiftWithTimeZone(value['sortKey']);

      if (
        value['groupKey'] !== undefined &&
        !Number.isInteger(value['groupKey'])
      ) {
        const oldLength = value['groupKey'].length;
        value['groupKey'] = this.shiftWithTimeZone(value['groupKey']).substring(
          0,
          oldLength
        );
      }
    }
  }

  alignItemZone(value) {
    if (value !== undefined) {
      value['createdDate'] = this.shiftWithTimeZone(value['createdDate']);

      if (value['range'] !== undefined) {
        value['range'].start = this.shiftWithTimeZone(value['range'].start);
        value['range'].end = this.shiftWithTimeZone(value['range'].end);
      }

      if (value['slots'] !== undefined) {
        (value['slots'] as Array<Object>).map((slot) => {
          if (slot['range'] !== undefined) {
            slot['range'].start = this.shiftWithTimeZone(slot['range'].start);
            slot['range'].end = this.shiftWithTimeZone(slot['range'].end);
          }
          return slot;
        });
      }
    }
  }

  shiftWithTimeZone(date): string {
    if (date === undefined) {
      return date;
    }

    const start = new Date(date);
    if (isNaN(start.getTime())) {
      return date;
    } else {
      const offset = start.getTimezoneOffset() * 60000;
      return new Date(start.getTime() - offset).toISOString();
    }
  }

  transformMessage(id, value, url, inList, item): any {
    let isChild = url.indexOf('items') !== -1
    let lId = isChild ? id : value["eventId"];

    let image = item['from']
      ? this.resolveMessageImage(item['from'])
      : undefined;

    let reads = (item['reads'] !== undefined
      ? (item['reads'] as Array<string>)
      : []
    ).map(
      (readImage) => this.resolveMessageImage(readImage));

    const profile = this.navService.user ? this.navService.user['profile'] : undefined;
    const data = {
      id: lId,
      type: 'msg',
      header: { main: item.name, image: image },
      desc: value['value'],
      url: url + '/' + lId,
      children: !isChild,
      value,
      isOwn: profile ? value.from === profile.key : false,
      reads
    };

    return data;
  }

  private resolveMessageImage(image): string {
    if (environment.mockUiData === true) {
      return '../assets/img/man.svg';
    }

    const imageName = image && image['name'] ? image['name'] : image;
    return location.origin + '/backend/user/profile/images/' + imageName;
  }

  transformGroup(id, value, url, inList): any {
    let actions = ['E', 'S']; // edit

    if (url.indexOf('recommendations') !== -1) {
      actions = ['J'];
    }

    let itemUrl = url.substring(1);
    itemUrl = itemUrl.substring(itemUrl.indexOf('/'));

    const data = {
      id,
      type: 'evt',
      state: 'E',
      header: { main: value['name'] },
      desc: value['desc'],
      actions,
      imgs: (value['images'] !== undefined
        ? (value['images'] as Array<string>)
        : []
      ).map(
        (image) =>
          location.origin +
          '/backend' +
          itemUrl +
          (inList ? '/' + id : '') +
          '/images/' +
          image['name']
      ),
      children: url.indexOf('recommendations') === -1,
      value,
      url: url + '/' + id,
    };
    return data;
  }

  transformIdea(id, value, url, inList): any {
    const actions = ['E']; // edit

    const data = {
      id,
      type: 'evt',
      state: 'E',
      header: { main: value['name'] },
      desc: value['desc'],
      actions,
      value,
      url: url + '/' + id,
    };
    return data;
  }

  transformJob(id, value, url, inList): any {
    const actions = ['E']; // edit

    const data = {
      id,
      type: 'evt',
      state: 'E',
      header: { main: value['name'] },
      desc: value['desc'],
      actions,
      value,
      url: url + '/' + id,
    };
    return data;
  }

  transformFeedback(id, value, url, inList): any {
    const actions = ['E']; // edit

    const data = {
      id,
      type: 'evt',
      state: 'E',
      color:
        value['rate'] < 3
          ? 'red'
          : value['rate'] > 7
            ? 'yellowgreen'
            : 'bisque',
      desc: value['desc'],
      value,
      url: url + '/' + id,
      actions,
    };
    return data;
  }

  transformEventItem(id, value, url, inList, param?): any {
    let state = 'E';
    let actions = []; // edit

    const main = param && param['main'] === true;

    let isPromotion = param && param['isPromotion'] === true;
    let isMember = param && param['isMember'] === true;
    let role = param && param['role'];

    if (main) {
      actions = ['E']; // edit
    } else {
      actions = ['E', 'D']; // edit
    }

    let children = false;

    if (url) {
      if (url.indexOf('invitations') !== -1) {
        state = 'I';
        actions = [];
      } else if (url.indexOf('recommendations') !== -1) {
        actions = [];
      } else if (url.indexOf('promotions') !== -1) {
        if (url.indexOf('activity') !== -1) {
          actions = [];
        }
      } else if (url.indexOf('history') !== -1) {
        if (url.indexOf('events') !== -1) {
          if (value['optional'] === true) {
            if (
              (isPromotion && role === 'P') ||
              (!isPromotion && role === 'M')
            ) {
              actions = ['E', 'D']; // edit
            }

            if (isMember && (role === 'U' || role === 'M')) {
              actions.push('L');
            } else {
              actions.push('J');
            }
            children = true;
          }
          children = true; //TODO: temporary solution, transformEventItem and transformEvent needs to be merged into one
        }
      }
    }

    let color = 'cornsilk';
    if (value['type'] === 'g') {
      color = 'yellowgreen';
    } else if (value['type'] === 'c') {
      color = 'aliceblue';
    }

    if (value['optional'] === true) {
      color = 'pink';

      if (value['num'] === value['capacity'].max) {
        color = 'gray';
      } else if (value['num'] < value['capacity'].min) {
        color = 'red';
      }
    }

    if (main) {
      color = 'coral';
    }

    const category = param && param['category'];

    const positions =
      value['positions'] && value['positions']
        ? value['positions']
        : param && param['positions']
          ? param['positions']
          : value['position']
            ? [value['position']]
            : undefined;

    if (positions !== undefined) {
      actions.push('M');
    }

    const data = {
      id,
      type: 'evt',
      state,
      header: { main: value.name },
      actions,
      value,
      desc: value.desc,
      url: url + '/' + id,
      color,
      children,
      extra: {
        main,
        category,
      },
      positions,
      num: value.num,
    };

    return data;
  }

  transformEvent(id, value, url, inList, table, param?): any {
    const info = value['info'];

    let isPromotion = param && param['isPromotion'] === true;
    let role = param && param['role'];

    let state = 'E';
    let actions = []; // leave, edit, qr code
    if (url) {
      if (url.indexOf('invitations') !== -1) {
        state = 'I';
        if (info['num'] === info['capacity'].max) {
          actions = ['W', 'R']; // waiting, reject
        } else {
          actions = ['A', 'R']; // accept, reject
        }
      } else if (url.indexOf('recommendations') !== -1) {
        if (url.indexOf('events') !== -1) {
          if (
            param &&
            param['groupType'] === 'd' &&
            param['promoType'] === 'e'
          ) {
            actions = ['C'];
            // nasty hack
            param['rate'] = undefined;
          }
        } else {
          actions = [];
        }
      } else if (url.indexOf('promotions') !== -1) {
        if (url.indexOf('activity') !== -1) {
          if (url.indexOf('business') !== -1) {
            if (info['num'] === info['capacity'].max) {
              actions = ['W', 'R']; // waiting, reject
            } else {
              actions = ['A', 'R']; // accept, reject
            }
          } else {
            actions = [];
          }
        } else {
          actions = ['E']; // edit
        }
      } else if (url.indexOf('history') !== -1) {
        if (url.indexOf('business') === -1) {
          if ((isPromotion && role === 'P') || (!isPromotion && role === 'M')) {
            actions = ['E']; // edit
          }
          actions.push('L', 'Q', 'CH', 'U');
        } else {
          actions = ['CA', 'Q', 'CH'];
        }
      } else if (url.indexOf('groups') !== -1) {
        actions = ['P', 'R'];
      }
    }

    let color = 'aliceblue';
    if (value['status'] === 'P') {
      color = 'orange';
    } else if (value['status'] === 'C') {
      color = 'darkgrey';
    } else if (value['status'] === 'A') {
      if (isPromotion) {
        color = 'pink';
      }
    }

    if (info['num'] === info['capacity'].max) {
      color = 'gray';
    } else if (info['num'] < info['capacity'].min) {
      color = 'red';
    }

    const positions =
      value['positions'] && value['positions']
        ? value['positions']
        : param && param['positions']
          ? param['positions']
          : value['position']
            ? [value['position']]
            : undefined;

    if (positions !== undefined) {
      actions.push('M');
    }

    let data;

    if (table !== true) {
      data = {
        id,
        type: 'evt',
        createdBy: value.createdBy,
        state,
        header: { main: info ? info.name : '' },
        actions,
        value: info,
        desc: info ? info.desc : '',
        url: url ? url + '/' + id : undefined,
        children: true,
        color,
        chat: info ? info.chatKey : undefined,
        name: info ? info.name : undefined,
        isPromotion,
        positions,
        num: info ? info.num : 0,
      };
    } else {
      data = {
        id,
        title: info.name,
        range: info.range,
        color,
        name: info ? info.name : undefined,
        isPromotion,
        num: info ? info.num : 0,
      };
    }

    return data;
  }

  transformPromotion(id, value, url, inList): any {
    const state = 'E';
    let actions = ['E']; // edit

    if (url.indexOf('recommendations') !== -1) {
      actions = [];
    }

    const data = {
      id,
      type: 'evt',
      state,
      header: { main: value.name },
      actions,
      value,
      desc: value.desc,
      url: url + '/' + id,
      footer:
        this.datePipe.transform(value['range'].start, 'dd/MMM/y') +
        ' - ' +
        this.datePipe.transform(value['range'].end, 'dd/MMM/y'),
      children: true,
      color: 'lightpink',
    };
    return data;
  }

  transformSchool(id, value, url, inList): any {
    const actions = ['E', 'D']; // edit

    const data = {
      id,
      type: 'evt',
      state: 'E',
      header: { main: value['name'] },
      color: value['type'] === 'w' ? 'aliceblue' : 'beige',
      desc: value['role'],
      actions,
      value,
      url: url + '/' + id,
      footer:
        this.datePipe.transform(value['range'].start, 'MMM/y') +
        ' - ' +
        this.datePipe.transform(value['range'].end, 'MMM/y'),
    };
    return data;
  }

  transformCar(id, value, url, inList): any {
    const actions = ['E', 'D']; // edit

    let itemUrl = url.substring(1);
    itemUrl = itemUrl.substring(itemUrl.indexOf('/'));

    const data = {
      id,
      type: 'img',
      state: 'E',
      header: {
        main:
          value['make'] +
          ' ' +
          value['model'] +
          ' (' +
          value['capacity'] +
          ') ',
        sub: value['regNum'],
      },
      imgs: (value['images'] !== undefined
        ? (value['images'] as Array<string>)
        : []
      ).map(
        (image) =>
          location.origin +
          '/backend' +
          itemUrl +
          (inList ? '/' + id : '') +
          '/images/' +
          image['name']
      ),
      value,
      url: url + '/' + id,
      actions,
    };
    return data;
  }

  transformMember(id, member, url, inList): any {
    const value = member['profile'];

    let itemUrl = url.substring(1);
    itemUrl = itemUrl.substring(itemUrl.indexOf('/'));

    const age = Math.floor(
      (new Date().getTime() - new Date(value['birthday']).getTime()) /
      (1000 * 3600 * 24 * 365)
    );

    let color;
    switch (member['status']) {
      case 'F':
        color = 'orange';
        break;
      case 'I':
        color = 'gray';
        break;
      case 'P':
        color = 'black';
        break;
      case 'S':
        color = 'red';
        break;
      default:
        color = 'green';
    }

    // header main
    let main;
    if (value['firstName'] !== undefined) {
      main = value['firstName'] + ' (' + age + ' / ' + value['height'] + 'cm)';
    }

    // header sub
    let sub = '';

    if (value['physique'] !== undefined) {
      if (value['physique'] !== 'a') {
        sub += physiques[value['physique']];
      }
    }

    if (value['smoker'] === true) {
      sub += ' \uD83D\uDEAD';
    }

    if (value['hasChild'] === true) {
      sub += ' \uD83D\uDC76';
    }

    const data = {
      id,
      type: 'img',
      state: 'E',
      header: {
        main,
        sub,
      },
      imgs: (value['images'] !== undefined
        ? (value['images'] as Array<string>)
        : []
      ).map(
        (image) =>
          location.origin +
          '/backend' +
          itemUrl +
          (inList ? '/' + value['key'] : '') +
          '/images/' +
          image['name']
      ),
      value,
      url: url + '/' + id,
      rate: 0,
      color,
    };
    return data;
  }

  transformProfile(id, value, url, inList, refresh, param?): any {
    value['type'] = "P";

    const age =
      value['birthday'] !== undefined
        ? Math.floor(
          (new Date().getTime() - new Date(value['birthday']).getTime()) /
          (1000 * 3600 * 24 * 365)
        )
        : undefined;

    let actions;
    if (url.indexOf('groups') !== -1) {
      actions = ['E']; // edit
    }

    let itemUrl = url.substring(1);
    itemUrl = itemUrl.substring(itemUrl.indexOf('/'));

    let icon = 'person';
    if (value['role'] === 'ROLE_ADMIN') {
      icon = 'engineering';
    }

    let color;
    switch (value['status']) {
      case 'F':
        color = 'orange';
        break;
      case 'I':
        color = 'gray';
        break;
      case 'P':
        color = 'black';
        break;
      case 'S':
        color = 'red';
        break;
      default:
        color = 'green';
    }

    // header main
    let main;
    if (value['firstName'] !== undefined) {
      main =
        value['firstName'] +
        (age !== undefined ? ' (' + age + ' / ' + value['height'] + 'cm)' : '');
    }

    // header sub
    let sub = '';

    if (value['physique'] !== undefined) {
      if (value['physique'] !== 'a') {
        sub += physiques[value['physique']];
      }
    }

    if (value['marital'] !== undefined) {
      if (value['marital'] === 't') {
        sub += ' \uD83D\uDC91';
      }
    }

    if (value['smoker'] === true) {
      sub += ' \uD83D\uDEAD';
    }

    if (value['hasChild'] === true) {
      sub += ' \uD83D\uDC76';
    }

    const schools = param && param['schools'] ? (param['schools'] as Array<any>) : [];
    if (schools.length > 0) {
      const schoolNames = schools
        .map((school) => school['name'])
        .filter((name) => name !== undefined && name !== null && name !== '');

      if (schoolNames.length > 0) {
        const preview = schoolNames.slice(0, 2).join(', ');
        const moreCount = schoolNames.length - 2;
        sub +=
          ' • \uD83C\uDF93 ' +
          preview +
          (moreCount > 0 ? ' +' + moreCount.toString() : '');
      }
    }

    const imgs = (
      value['images'] !== undefined ? (value['images'] as Array<string>) : []
    ).map(
      (image) =>
        location.origin +
        '/backend' +
        itemUrl +
        (inList ? '/' + value['key'] : '') +
        '/images/' +
        image['name'] +
        (refresh ? '?' + Date.now() : '')
    );

    if (imgs.length === 0) {
      imgs.push('../assets/img/man.svg');
    }

    let children = false;
    if (url && (url.indexOf('job') !== -1 || url.indexOf('business') !== -1)) {
      if (url.indexOf('activity') !== -1 && url.indexOf('events') !== -1) {
        children = true;
      }

      if (url.indexOf('games') !== -1) {
        children = true;
      }

      if (
        url.indexOf('recommendations') === -1 &&
        url.indexOf('promotions') !== -1
      ) {
        children = true;
      }
    }

    const data = {
      id,
      name: value['firstName'],
      type: 'img',
      state: 'E',
      ref: param ? param['ref'] : undefined,
      role: { icon, color },
      header: {
        main,
        sub,
      },
      imgs,
      actions,
      value,
      url: url ? url + '/' + id : undefined,
      children,
    };
    return data;
  }

  uuidv4(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }
}
