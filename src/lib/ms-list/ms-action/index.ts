import { Component, Inject } from '@angular/core';
import {
  MatBottomSheetRef,
  MAT_BOTTOM_SHEET_DATA
} from '@angular/material/bottom-sheet';
import { Router } from '@angular/router';
import { NavigationService } from 'src/app/navigation.service';
import { ListService } from 'src/app/services/list.service';

// campaign button - is the big boom
@Component({
  selector: 'ms-action',
  templateUrl: 'index.html',
})
export class MsAction {
  actions: Array<string>;
  chat: any;
  map: any;

  constructor(
    private menu: MatBottomSheetRef<MsAction>,
    @Inject(MAT_BOTTOM_SHEET_DATA) public data: any,
    private listService: ListService,
    private navService: NavigationService,
    private router: Router
  ) {
    this.actions = data.actions;
    this.chat = data.chat;
    this.map = this.buildMapLink(this.data.positions);
  }

  do(action: string): void {
    switch (action) {
      case 'E':
        this.listService.edit.emit({ alias: this.data.alias });
        break;
      case 'L':
      case 'A':
      case 'R':
      case 'P':
      case 'CA':
      case 'W':
        this.listService.status.emit({ alias: this.data.alias, action });
        break;
      case 'D':
        this.listService.delete.emit({ alias: this.data.alias });
        break;
      case 'S': // share
        this.listService.share.emit({ alias: this.data.alias });
        break;
      case 'Q':
        this.listService.code.emit({ alias: this.data.alias });
        break;
      case 'C':
        this.listService.clone.emit({ alias: this.data.alias });
        break;
      case 'J':
        this.listService.join.emit({ alias: this.data.alias });
        break;
      case 'U':
        this.listService.recommend.emit({ alias: this.data.alias });
        break;
      case 'M':
        if (this.map !== undefined) {
          window.open(this.map, '_blank');
        }
        break;
      case 'CH':
        if (this.chat !== undefined) {
          window.open(this.chat, '_blank');
        } else {
          this.listService.edit.emit({ alias: this.data.alias });
        }
        break;
    }
    this.menu.dismiss({ action });
  }

  private buildMapLink(positions) {
    if (positions === undefined || positions.length === 0) {
      return undefined;
    }

    let link = '';
    if (positions.length === 1) {
      link =
        'https://www.google.com/maps/search/?api=1&query=' +
        positions[0].x +
        '%2C' +
        positions[0].y;
    } else {
      link =
        'https://www.google.com/maps/dir/?api=1&origin=' +
        positions[0].x +
        '%2C' +
        positions[0].y +
        '&destination=' +
        positions[positions.length - 1].x +
        '%2C' +
        positions[positions.length - 1].y;

      if (positions.length > 2) {
        link += '&waypoints=';

        for (let i = 1; i < positions.length - 1; i++) {
          link += positions[i].x + '%2C' + positions[i].y;

          if (i < positions.length - 2) {
            link += '%7C';
          }
        }
      }
    }
    return link;
  }

}
