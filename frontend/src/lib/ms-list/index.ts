export * from './ms-action';
export * from './ms-calendar';
export * from './ms-panel';

const months = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

import { DatePipe } from '@angular/common';
import { HttpParams } from '@angular/common/http';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ComponentFactoryResolver,
  EmbeddedViewRef,
  EventEmitter,
  Injector,
  Input,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  TemplateRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { GroupFilterComponent } from 'src/app/dating/components/forms/group-filter/group-filter.component';
import { NavigationService } from 'src/app/navigation.service';
import { HttpService } from 'src/app/services/http.service';
import { ListService } from 'src/app/services/list.service';
import { TransformService } from 'src/app/services/transform.service';
import { MsDialog } from '../ms-dialog';
import { getUrl } from '../../app/app-routing.strategy';
import { ILazy } from './interface';
import { MsPanel } from './ms-panel';

//import("./ms-panel").then(({MsPanel}) => {}

//1) copy to canvas at upload - resize img android native
//2) latch only map - and for one panel only
//3) draggable / dragzone polyfill for touch screen

//list_panel_30 - conf.json - 30 view buffer
//https://developers.google.com/web/updates/2017/11/overscroll-behavior
//paging - scroll page up / down

const MAX_INT = 2147483647;

const injector = Injector.create({
  providers: [{ provide: MsPanel, deps: [] }, { provide: DatePipe }],
});

const pairRate = {
  type: 'route',
  icon: 'people_outline',
  url: '/games/rate_double',
};

const actionMap = {
  L: 'leave',
  R: 'reject',
  A: 'accept',
  P: 'publish',
  CA: 'leave',
  W: 'wait',
};

@Component({
  selector: 'ms-list',
  templateUrl: './index.html',
})
export class MsList implements OnInit, OnDestroy, AfterViewInit {
  @Output() msSelect = new EventEmitter<any>();
  @Output() msDelete = new EventEmitter<any>();

  progress: any = { mode: 'determine', value: 0, color: 'primary' };

  private sub: Subscription;

  observer: IntersectionObserver;

  @ViewChild('items', { static: true, read: ViewContainerRef })
  itemsRef: ViewContainerRef;

  @ViewChild('divider', { static: true, read: TemplateRef })
  dividerRef: TemplateRef<any>;

  @ViewChild('calendar', { static: true, read: TemplateRef })
  calendarRef: TemplateRef<any>;

  @ViewChild('overlay', { static: true })
  overlayRef: MsPanel;

  @ViewChild('oldOverlay', { static: true })
  oldOverlayRef: any;

  private itemFactory;

  private _startY;

  private height: any;

  private isRefreshing = false;
  isLoading = false;

  private offsetTop = 0;
  direction = 1;

  private touched = false;
  isRefreshed = false;

  isSelected = false;

  scrollTop = 0;

  step;

  @Input() overlayData: any;
  @Input() selectable: any;

  private items: Array<{ component; info }> = new Array();
  private extra: any;
  private selectedItems: Array<{ component; info }> = new Array();

  private sortedItems: Array<{ id; sortKey }> = new Array();

  rate: any;
  data: any;

  isOverlay = false;

  @Input() actions: Array<any>;
  @Input() multiple = false;
  @Input() double = false;

  isEvent = false; //temporary
  group = undefined;

  range: { from: Date; to: Date } = { from: new Date(), to: new Date() };

  rangeDist: { from: any; to: any } = { from: 0, to: 0 };

  //temporary
  currentFrom: any = new Date();

  @Input() url: any;

  itemUrl: any;

  // filter
  offset: Array<any>;

  isPull = false;

  lastProgressState: any;

  status: Subscription;
  edit: Subscription;
  share: Subscription;
  clone: Subscription;
  code: Subscription;
  dialogSub: Subscription;
  join: Subscription;
  recommend: Subscription;
  delete: Subscription;

  isAdmin = false;

  reset(): void {
    this.items = new Array();
    this.selectedItems = new Array();
    this.sortedItems = new Array();
    this.offset = undefined;

    this.itemsRef.clear();

    setTimeout(() => {
      let inbox =
        this.itemsRef.element.nativeElement.parentElement.parentElement;
      this.updatePage(inbox, false);
    });
  }

  constructor(
    private resolver: ComponentFactoryResolver,
    private _renderer: Renderer2,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    public dialog: MatDialog,
    private navService: NavigationService,
    private httpService: HttpService,
    private transformService: TransformService,
    private datePipe: DatePipe,
    private cd: ChangeDetectorRef,
    private listService: ListService,
    private _snackBar: MatSnackBar
  ) {
    this.share = this.listService.share.subscribe({
      next: (evt) => {
        const idx = this.actions.findIndex((action) => action.type === 'share');
        this.openDialog(idx, evt.alias);
      },
    });

    this.join = this.listService.join.subscribe({
      next: (evt) => {
        const url = this.items[evt.alias].info.url.substring(1);
        const itemUrl = url.substring(url.indexOf('/'));

        this.httpService.save(itemUrl + '/join', {}).subscribe({
          next: (result) => {
            this._snackBar.open(
              'You successfully joined to "' +
                this.items[evt.alias].info.header.main +
                '"!',
              undefined,
              { duration: 1500 }
            );
          },
          error: (error) => {
            this.progress.mode = 'determine';
            this.progress.color = 'warn';
          },
          complete: () => {
            this.reset();
            console.log('complete');
          },
        });
      },
    });

    this.delete = this.listService.delete.subscribe({
      next: (evt) => {
        const url = this.items[evt.alias].info.url.substring(1);
        const itemUrl = url.substring(url.indexOf('/'));

        this.httpService.delete(itemUrl).subscribe({
          next: (result) => {
            this._snackBar.open(
              '"' +
                this.items[evt.alias].info.header.main +
                '" has been sucessfully deleted!',
              undefined,
              { duration: 1500 }
            );
          },
          error: (error) => {
            if (error && error.key !== undefined) {
              if (this.navService.locale !== undefined) {
                const msg = this.navService.locale[error.value];
                this._snackBar.open(msg, undefined, { duration: 1500 });
              }
            }

            this.progress.mode = 'determine';
            this.progress.color = 'warn';
          },
          complete: () => {
            this.reset();
            console.log('complete');
          },
        });
      },
    });

    this.recommend = this.listService.recommend.subscribe({
      next: (evt) => {
        const url = this.items[evt.alias].info.url.substring(1);
        const itemUrl = url.substring(url.indexOf('/'));

        this.httpService.save(itemUrl + '/recommend', {}).subscribe({
          next: (result) => {
            this._snackBar.open(
              'The "' +
                this.items[evt.alias].info.header.main +
                '" has been successfully recommended!',
              undefined,
              { duration: 1500 }
            );
          },
          error: (error) => {
            this.progress.mode = 'determine';
            this.progress.color = 'warn';
          },
          complete: () => {
            console.log('complete');
          },
        });
      },
    });

    this.clone = this.listService.clone.subscribe({
      next: (evt) => {
        const url = this.items[evt.alias].info.url.substring(1);
        const itemUrl = url.substring(url.indexOf('/'));

        this.httpService.save(itemUrl + '/clone', {}).subscribe({
          next: (result) => {
            this._snackBar.open(
              this.items[evt.alias].info.header.main + ' has been clone!',
              undefined,
              { duration: 1500 }
            );
          },
          error: (error) => {
            this.progress.mode = 'determine';
            this.progress.color = 'warn';
          },
          complete: () => {
            console.log('complete');
          },
        });
      },
    });

    this.dialogSub = this.listService.dialog.subscribe({
      next: (evt) => {
        const listUrl = evt.itemUrl.substring(0, evt.itemUrl.lastIndexOf('/'));
        if (listUrl === this.itemUrl) {
          const idx = this.actions.findIndex(
            (action) => action.type === 'list'
          );

          const url = evt.itemUrl.substring(1);
          const itemUrl = url.substring(url.indexOf('/'));

          this.openDialog(idx, undefined, itemUrl + '/items');
        }
      },
    });

    this.status = this.listService.status.subscribe({
      next: (evt) => {
        const info = this.items[evt.alias].info;
        const url = info.url.substring(1);
        const itemUrl = url.substring(url.indexOf('/'));

        if (evt.action === 'L' || evt.action === 'CA') {
          this.leave(info.name, info.isPromotion).then((resp) => {
            if (resp === true) {
              this.doStatus(itemUrl, evt);
            }
          });
        } else {
          this.doStatus(itemUrl, evt);
        }
      },
    });

    this.edit = this.listService.edit.subscribe({
      next: (evt) => {
        const idx = this.actions.findIndex((action) => action.type === 'edit');
        this.openDialog(idx, evt.alias);
      },
    });

    this.code = this.listService.code.subscribe({
      next: (evt) => {
        const idx = this.actions.findIndex((action) => action.type === 'code');
        this.openDialog(idx, evt.alias);
      },
    });

    this.listService.overlay.subscribe({
      next: (evt) => {
        this.overlayData = evt;
      },
    });

    this.navService.online.subscribe({
      next: (evt) => {
        if (!evt.online) {
          this.lastProgressState = this.progress.color;
          this.progress = {
            mode: this.progress.mode,
            value: this.progress.value,
            color: 'accent',
          };
        } else {
          this.progress = {
            mode: this.progress.mode,
            value: this.progress.value,
            color: this.lastProgressState,
          };
          this.lastProgressState = undefined;
        }
      },
    });
    router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const inbox =
          this.itemsRef.element.nativeElement.parentElement.parentElement;
        inbox.scrollTo({ top: this.scrollTop });
      }
    });
  }

  private doStatus(itemUrl: any, evt: any) {
    this.httpService.save(itemUrl + '/' + actionMap[evt.action], {}).subscribe({
      next: (result) => {
        this.reset();
      },
      error: (error) => {
        this.progress.mode = 'determine';
        this.progress.color = 'warn';
      },
      complete: () => {
        this.reset();
        console.log('complete');
      },
    });
  }

  leave(name: string, isPromotion: boolean): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      const promoMsg =
        'You might be charged by the Promoter because of being leave late!';

      const action = this.navService.groupType === 'b' ? 'cancel' : 'leave';
      const dialogRef = this.dialog.open(MsDialog, {
        data: {
          msg:
            'Would you like to ' + action + ' ' + name + ' event!' + promoMsg,
          btn: 'Yes',
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        resolve(result);
      });
    });
  }

  ngAfterViewInit(): void {
    this.overlayRef.msEnlarge.subscribe({
      next: (evt) => {
        if (evt.enlarged) {
          this.overlayRef.onEnter(evt.alias, this.items[evt.alias].info);
          this.overlayRef.show(true, true);
          this.oldOverlayRef.show(true, true);
          this.isOverlay = true;
        } else {
          this.overlayRef.show(false, false);
          this.oldOverlayRef.show(false, false);
          this.isOverlay = false;
        }
      },
    });
  }

  onDelete(alias): void {
    if (this.overlayData && this.overlayData.removable === false) {
      return;
    }

    if (alias && this.selectedItems[alias]) {
      this.selectedItems[alias].component.instance.focus(false);
      delete this.selectedItems[alias];
    }

    this.msDelete.emit(alias);
  }

  onRate(evt): void {
    for (const selectedItem in this.selectedItems) {
      if (this.selectedItems.hasOwnProperty(selectedItem)) {
        if (this.selectedItems[selectedItem].component.instance.rated) {
          if (
            this.selectedItems[selectedItem].info['createdBy'] !== undefined
          ) {
            this.httpService.index({
              from:
                this.navService.selected !== undefined
                  ? this.navService.selected['info'].id
                  : undefined,
              to: this.selectedItems[selectedItem].info['createdBy'],
              ref: selectedItem,
              rate: evt,
            });
          } else {
            this.httpService.index({
              from:
                this.navService.selected !== undefined
                  ? this.navService.selected['info'].id
                  : undefined,
              to: selectedItem,
              ref: this.selectedItems[selectedItem].info['ref'],
              rate: evt,
            });
          }

          this.selectedItems[selectedItem].component.instance.update(evt + 1);
        }
      }
    }

    if (this.isOverlay) {
      this.overlayRef.update(evt + 1);

      // overlay update delay
      setTimeout(() => {
        const scrollView = this.itemsRef.element.nativeElement.parentElement;

        const inbox = scrollView.parentElement;

        const item = this.items[this.overlayRef.alias];
        let idx = this.itemsRef.indexOf(item.component.hostView);

        // skip dividers
        let alias;
        let viewRef;
        let panel;
        while (this.itemsRef.length > idx + 1 && !alias) {
          viewRef = this.itemsRef.get(idx + 1);
          panel = viewRef['rootNodes'][0];
          alias = panel.children[0] ? panel.children[0].id : undefined;
          if (!alias) {
            idx++;
          }
        }

        if (this.itemsRef.length > idx + 1) {
          const oldAlias = this.overlayRef.alias;

          this.oldOverlayRef.onEnter(oldAlias, this.items[oldAlias].info);
          this.oldOverlayRef.elementView.nativeElement.parentElement.classList.add(
            'animPanel'
          );

          this.overlayRef.onEnter(alias, this.items[alias].info);

          this.items[alias].component.instance.rate();

          if (this.items[alias].component.instance.visible === false) {
            this.offsetTop +=
              inbox.clientHeight / 2 + panel.getBoundingClientRect().height;
            inbox.scrollTo({ top: this.offsetTop });
          }
        } else {
          this.direction = 1;
          inbox.scrollTo({ top: inbox.scrollHeight });
        }
      }, 200);

      setTimeout(() => {
        this.oldOverlayRef.elementView.nativeElement.parentElement.classList.remove(
          'animPanel'
        );
      }, 1000);
    }

    this.rate = evt;
  }

  updatePage(inbox, isOffset = true): void {
    if (
      !this.isSelected &&
      inbox.scrollTop === 0 &&
      this.direction === -1 &&
      !this.isRefreshing &&
      this.isPull
    ) {
      this.isLoading = true;
      this.progress.mode = 'indeterminate';

      this.isRefreshing = true;

      this.createPage(this.direction).subscribe({
        complete: () => {
          if (isOffset && this.offsetTop > 0) {
            this.touched = false;
            inbox.scrollTo({ top: this.offsetTop });
          }

          this.offsetTop = 0;
          this.isLoading = false;
          this.progress.mode = 'determine';
          this.progress.value =
            (inbox.scrollTop / (inbox.scrollHeight - inbox.clientHeight)) * 100;

          this.isRefreshing = false;
          this.isRefreshed = isOffset && true;
        },
      });
    } else {
      if (this.touched) {
        this.isRefreshed = false;
      }
    }

    if (
      Math.floor(inbox.scrollTop + inbox.offsetHeight + 20) >=
        inbox.scrollHeight &&
      this.direction === 1 &&
      !this.isRefreshing
    ) {
      this.isLoading = true;
      this.progress.mode = 'query';

      this.isRefreshing = true;

      this.createPage(this.direction).subscribe({
        complete: () => {
          this.isLoading = false;

          this.progress.mode = 'determine';
          this.progress.value =
            (inbox.scrollTop / (inbox.scrollHeight - inbox.clientHeight)) * 100;

          this.isRefreshing = false;
          this.isRefreshed = isOffset && true;

          this.onRate(this.rate);
        },
      });
    } else {
      if (this.touched) {
        this.isRefreshed = false;
      }
    }
    this.progress.value =
      (inbox.scrollTop / (inbox.scrollHeight - inbox.clientHeight)) * 100;
  }

  openDialog(idx, alias?, overrideUrl?) {
    let data;
    switch (this.actions[idx].type) {
      case 'list':
        data = JSON.parse(JSON.stringify(this.actions[idx].data));
        data.url = overrideUrl;
        break;
      case 'add':
        data = {
          url:
            this.actions[idx].url !== undefined
              ? this.actions[idx].url
              : this.url,
          multiple: this.actions[idx].multiple,
          extra: this.extra,
          isAdd: true,
        };
        break;
      case 'share':
        const params = new HttpParams();

        data = JSON.parse(JSON.stringify(this.items[alias].info));

        const shareUrl = data.url.substring(1);
        data.url = shareUrl.substring(shareUrl.indexOf('/'));

        this.httpService.get(data.url + '/share', params).subscribe({
          next: (result) => {
            if (navigator.share) {
              navigator
                .share({
                  title: result['info'].title,
                  text: result['info'].msg,
                  url: location.origin + '?xlink=' + result['link'].key,
                })
                .then(() => {
                  console.log('Thanks for sharing!');
                })
                .catch(console.error);
            } else {
              console.log({
                title: result['info'].title,
                text: result['info'].msg,
                url: location.origin + '?xlink=' + result['link'].key,
              });
              alert('Not implemented for desktop!');
            }
          },
          error: (error) => {
            this.progress.mode = 'determine';
            this.progress.color = 'warn';
          },
          complete: () => {
            console.log('complete');
          },
        });
        return;
      case 'popup':
        data = {
          url:
            this.actions[idx].url !== undefined
              ? this.actions[idx].url
              : this.url,
          multiple: this.actions[idx].multiple,
          selected: this.selectedItems,
        };
        break;
      case 'edit':
        if (this.items[alias] === undefined) {
          return;
        }
        data = JSON.parse(JSON.stringify(this.items[alias].info));

        const url = data.url.substring(1);
        data.url = url.substring(url.indexOf('/'));
        break;
      case 'route':
        this.navService.selected = this.actions[idx].selected;
        this.navService.prevUrl = this.itemUrl;
        this.router.navigate([this.actions[idx].url]);
        return;
      case 'code':
        if (this.items[alias] === undefined) {
          return;
        }
        data = JSON.parse(JSON.stringify(this.items[alias].info));
        break;
      default:
        data = this.selectedItems;
    }

    const dialogRef = this.dialog.open(this.actions[idx].component, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
      data,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        switch (this.actions[idx].type) {
          case 'filter':
            break;
          case 'list':
            this.reset();
            break;
          case 'add':
            this.reset();
            break;
          case 'edit':
            // update is complex, as you might need to create a new groupKey
            this.reset();
            break;
          case 'attach':
            break;
        }
      }
    });
  }

  setting(): void {
    const dialogRef = this.dialog.open(GroupFilterComponent, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        let item = result.filter((item) => {
          return item.name === 'group';
        })[0];
        this.step = item.options[item.data[0]].value;
        this.range = { from: new Date(), to: new Date() };

        this.reset();
      }
    });
  }

  ngOnDestroy(): void {
    this.dialogSub.unsubscribe();
    this.status.unsubscribe();
    this.delete.unsubscribe();
    this.edit.unsubscribe();
    this.share.unsubscribe();
    this.clone.unsubscribe();
    this.code.unsubscribe();
    this.join.unsubscribe();
    this.recommend.unsubscribe();
    this.sub.unsubscribe();
  }

  numSequence(n: number): Array<number> {
    return Array(n);
  }

  ngOnInit(): void {
    if (this.url === undefined) {
      let url = this.activatedRoute['_routerState'].snapshot.url;
      url = url.replace('/base', '');

      this.itemUrl = url;

      url = url.substring(1);
      url = url.substring(url.indexOf('/'));

      this.url = url;
    } else {
      let url = this.activatedRoute['_routerState'].snapshot.url;
      url = url.replace('/base', '');

      url = url.substring(1);
      url = url.substring(0, url.indexOf('/'));

      this.itemUrl = '/' + url + this.url;
    }

    this.sub = this.activatedRoute.data.subscribe((v) => {
      if (this.actions === undefined) {
        this.actions = v.actions ? v.actions : new Array();
      }
      //this.multiple = v.multiple;
      this.double = v.double;
      this.isEvent = v.event;
      this.group = v.group;
    });

    // subscribe for DATA channel
    let inbox = this.itemsRef.element.nativeElement.parentElement.parentElement;

    inbox.addEventListener(
      'scroll',
      (e) => {
        const target = e === undefined ? {} : e.srcElement || e.target;
        this.scrollTop = target.scrollTop;
        this.updatePage(target);

        // hide modal - like rate bar
        if (this.touched) {
          if (!this.isOverlay) {
            for (const selectedItem in this.selectedItems) {
              if (this.selectedItems.hasOwnProperty(selectedItem)) {
                if (this.selectedItems[selectedItem].component.instance.rated) {
                  this.selectedItems[selectedItem].component.instance.focus(
                    false
                  );
                  delete this.selectedItems[selectedItem];
                }
              }
            }
            if (this.rate && this.double) {
              this.actions.pop();
            }
            this.rate = undefined;
          }
        }
      },
      { passive: true }
    );

    inbox.addEventListener(
      'touchstart',
      (e) => {
        this.isSelected = false;
        this._startY = e.touches[0].pageY;
      },
      { passive: true }
    );

    inbox.addEventListener(
      'touchend',
      (e) => {
        this.touched = true;
        // this.isRefreshing = false;
      },
      { passive: true }
    );

    inbox.addEventListener(
      'touchmove',
      (e) => {
        const y = e.touches[0].pageY;
        this.direction = y > this._startY ? -1 : 1;
        this.updatePage(inbox);
      },
      { passive: true }
    );

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        const id = entry.target.children[0].id;
        if (this.items[id]) {
          const component = this.items[id].component;
          component.instance.show(entry.isIntersecting);
        }
      });
    });

    this.itemFactory = this.resolver.resolveComponentFactory(MsPanel);

    this.direction = 1;

    const url = getUrl(this.activatedRoute.snapshot);
    this.isAdmin = url.indexOf('admin') !== -1;

    if (!this.isAdmin) {
      this.progress.mode = 'indeterminate';

      this.httpService
        .sync()
        .then(() => {
          this.updatePage(inbox, false);
        })
        .catch((e) => {
          this.progress.mode = 'determine';
          this.progress.color = 'warn';
        });
    } else {
      this.updatePage(inbox, false);
    }
  }

  createPage(direction: number): Observable<any> {
    return new Observable((observer) => {
      let params = new HttpParams();

      if (this.offset !== undefined) {
        if (this.step === 'm') {
          let d;
          if (direction === -1) {
            d = this.range.from;
          } else if (direction === 1) {
            d = this.range.to;
          }

          let month = d.getMonth();
          let year = d.getFullYear();

          if (direction === -1) {
            month = d.getMonth() - 1;
            if (month < 0) {
              year = d.getFullYear() - 1;
              month = 11;
            }

            this.range.from = new Date(year, month, 1);
          } else if (direction === 1) {
            month = d.getMonth() + 1;
            if (month > 12) {
              year = d.getFullYear() + 1;
              month = 1;
            }

            this.range.to = new Date(year, month, 1);
          }

          this.offset[0] = new Date(year, month + 1, 1).toISOString();
        }

        for (const offsetItem of this.offset) {
          params = params.append('offset', encodeURIComponent(offsetItem));
        }
      }

      if (this.step !== undefined) {
        params = params.set('step', this.step);
      }

      if (direction !== undefined) {
        params = params.set('direction', direction.toString());
      }

      this.httpService.get(this.url, params).subscribe({
        next: (data) => {
          if (data['role'] !== undefined && data['role'] !== 'M') {
            this.actions = this.actions.filter(
              (action) => action.type !== 'add'
            );
          }

          if (data['step'] !== undefined) {
            this.step = data['step'];
          }

          if (data['offset']) {
            this.offset = data['offset'];
            this.isPull = data['scroll'] !== 1;
          }

          if (data['values']) {
            if (this.step !== 'm') {
              if (direction === -1) {
                data['values'] = data['values'].reverse();
              }

              data['values'].forEach((obj, idx) => {
                this.addToList(direction, obj);
              });
            } else {
              const date = new Date(this.offset[0]);

              const groupKey =
                date.getFullYear() + ' ' + months[date.getMonth()];

              this.addToTable(direction, groupKey, data['values']);
            }

            setTimeout(() => {
              const scrollView =
                this.itemsRef.element.nativeElement.parentElement;
              const height = scrollView.getBoundingClientRect().height;

              if (direction) {
                this.offsetTop = height - this.height;
              }
              this.height = height;

              this.progress.mode = 'determine';
              observer.complete();
            });
          }
        },
        error: (error) => {
          if (error && error.key !== undefined) {
            if (this.navService.locale !== undefined) {
              const msg = this.navService.locale[error.value];
              this._snackBar.open(msg, undefined, { duration: 1500 });
            }
          }

          this.progress.mode = 'determine';
          this.progress.color = 'warn';
        },
        complete: () => {
          console.log('complete');
        },
      });
    });
  }

  private addToTable(direction: number, groupKey, data?: Array<any>) {
    let items: Array<{
      id: string;
      title: string;
      range: { start: string; end: string };
      color: string;
    }> = new Array();

    // tslint:disable-next-line: prefer-for-of
    for (let i = 0; i < data.length; i++) {
      const item = this.transformService.transform(
        data[i],
        this.url,
        true,
        false,
        true
      );

      // filter duplicate
      if (item === undefined || this.items[item.id] !== undefined) {
        continue;
      }

      items.push(item);
    }

    const groupView = this.dividerRef.createEmbeddedView({
      title: groupKey,
    });

    const calView = this.calendarRef.createEmbeddedView({
      data: { id: new Date(groupKey), items },
    });

    if (direction === -1 || direction === 0) {
      this.itemsRef.insert(calView, 0);
      this.itemsRef.insert(groupView, 0);
    } else if (direction === 1) {
      this.itemsRef.insert(groupView);
      this.itemsRef.insert(calView);
    }
  }

  private addToList(direction: number, data?) {
    const item = this.transformService.transform(data, this.itemUrl, true);

    if (this.items.length === 0) {
      this.extra = item.extra;
    }

    // filter duplicate
    if (item === undefined || this.items[item.id] !== undefined) {
      return;
    }

    const component = this.itemFactory.create(injector);
    (component.instance as MsPanel).selectable = this.selectable;

    this.createItemFunction(component);

    let viewRef;

    let groupViewRef;
    if (item.groupKey !== undefined) {
      groupViewRef = this.createGroupCmp(item.groupKey, direction);
    }

    if (direction === -1 || direction === 0) {
      let viewIdx = 0;
      if (groupViewRef !== undefined) {
        viewIdx = this.itemsRef.indexOf(groupViewRef) + 1;
      }
      viewRef = this.itemsRef.insert(component.hostView, viewIdx);
    } else if (direction === 1) {
      viewRef = this.itemsRef.insert(component.hostView);
    }

    (component.instance as ILazy).onEnter(item.id, item);
    this.items[item.id] = { component, info: item };

    this.observer.observe(viewRef.rootNodes[0]);
  }

  // it can insert anywhere, not only top bottom - as when you add an event,
  // you don't know whether you are at the end of the list, but you need to scroll down or up to the newly added element
  private createGroupCmp(key: any, direction: number): EmbeddedViewRef<any> {
    let viewRef;

    const groupView = this.dividerRef.createEmbeddedView({
      title: key,
    });

    if (
      this.itemsRef.length === 0 ||
      (direction === -1 && this.rangeDist.from > key)
    ) {
      this.rangeDist.from = key;
      if (this.itemsRef.length === 0) {
        this.rangeDist.to = key;
      }

      viewRef = this.itemsRef.insert(groupView, 0);
    } else if (direction === 1 && this.rangeDist.to < key) {
      this.rangeDist.to = key;

      viewRef = this.itemsRef.insert(groupView);
    }
    return viewRef;
  }

  private createItemFunction(component: any) {
    (component.instance as MsPanel).msSelect.subscribe({
      next: (evt) => {
        const alreadySelected = this.selectedItems[evt.alias];

        let hasRated = false;

        for (const selectedItem in this.selectedItems) {
          if (this.selectedItems.hasOwnProperty(selectedItem)) {
            if (selectedItem !== evt.alias && !this.multiple) {
              this.selectedItems[selectedItem].component.instance.focus(false);
              delete this.selectedItems[selectedItem];
            }

            if (
              this.multiple &&
              !hasRated &&
              this.selectedItems[selectedItem].component.instance.rated
            ) {
              hasRated = true;
            }
          }
        }

        if (
          !alreadySelected ||
          !alreadySelected.component.instance.selected ||
          alreadySelected.component.instance.rated
        ) {
          this.selectedItems[evt.alias] = this.items[evt.alias];
          this.selectedItems[evt.alias].component.instance.focus(
            true,
            false,
            true
          );
        } else {
          alreadySelected.component.instance.focus(false);
          delete this.selectedItems[evt.alias];
        }

        if (!hasRated) {
          this.rate = undefined;

          if (this.double) {
            this.actions.pop();
          }
        }

        this.msSelect.emit(this.selectedItems);
      },
    });

    (component.instance as MsPanel).msRate.subscribe({
      next: (evt) => {
        const alreadySelected = this.selectedItems[evt.alias];

        for (const selectedItem in this.selectedItems) {
          if (this.selectedItems.hasOwnProperty(selectedItem)) {
            if (selectedItem !== evt.alias && !this.multiple) {
              this.selectedItems[selectedItem].component.instance.focus(false);
              delete this.selectedItems[selectedItem];

              if (this.double) {
                this.actions.pop();
              }
            }
          }
        }

        if (
          !alreadySelected ||
          !alreadySelected.component.instance.rated ||
          alreadySelected.component.instance.selected ||
          evt.always
        ) {
          this.selectedItems[evt.alias] = this.items[evt.alias];
          this.selectedItems[evt.alias].component.instance.focus(
            true,
            true,
            false
          );

          this.rate = evt.rate - 1;

          if (!evt.always && this.double) {
            let pairObj = JSON.parse(JSON.stringify(pairRate));

            const url = this.items[evt.alias].info.url.substring(1);
            const baseUrl = '/' + url.substring(0, url.indexOf('/'));

            pairObj['url'] = baseUrl + '/' + pairObj['url'] + '/' + evt.alias;
            pairObj['selected'] = this.selectedItems[evt.alias];
            this.actions.push(pairObj);
          }
        } else {
          alreadySelected.component.instance.focus(false);
          delete this.selectedItems[evt.alias];
          this.rate = undefined;

          if (this.double) {
            this.actions.pop();
          }
        }
      },
    });

    (component.instance as MsPanel).msEnlarge.subscribe({
      next: (evt) => {
        if (evt.enlarged) {
          this.overlayRef.onEnter(evt.alias, this.items[evt.alias].info);
          this.overlayRef.show(true, true);
          this.oldOverlayRef.show(true, true);
          this.overlayRef.focus(false, evt.rated, false);
          this.isOverlay = true;
        } else {
          this.overlayRef.show(false, false);
          this.oldOverlayRef.show(false, false);
          this.isOverlay = false;
        }
      },
    });
  }
}
