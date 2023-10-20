import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { ListService } from 'src/app/services/list.service';
import { ILazy } from '../interface';
import { MsAction } from '../ms-action';

/* changeDetection: ChangeDetectionStrategy.OnPush*/
@Component({
  selector: 'ms-panel',
  templateUrl: './index.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MsPanel implements OnDestroy, OnInit, ILazy {
  @ViewChild('panel') elementView: ElementRef;

  msSelect = new Subject<any>();
  msRate = new Subject<any>();
  msEnlarge = new Subject<any>();

  @Input() alias: string;
  @Input() data: any;
  @Input() visible: boolean;
  @Input() removable: boolean;
  @Input() selectable: boolean;
  @Input() actionable: boolean;

  @Output() msRemove = new EventEmitter<any>();

  currentIdx = 0;

  focused: any = false;
  selected: any = false;
  enlarged: any = false;
  rated: any = false;

  mapLink: any;

  constructor(
    private cd: ChangeDetectorRef,
    public dialog: MatDialog,
    private router: Router,
    private menu: MatBottomSheet,
    private listService: ListService
  ) {}

  ngOnInit() {
    this.actionable = true;
  }

  slide(idx): void {
    this.currentIdx = idx;
  }

  content(type): void {
    if (this.data.children && this.data.url) {
      if (this.data.url.indexOf('templates') !== -1) {
        this.listService.dialog.emit({ itemUrl: this.data.url });
      } else {
        this.router.navigate([this.data.url]);
      }
    }
  }

  more(): void {
    this.menu.open(MsAction, {
      data: {
        alias: this.alias,
        actions: this.data.actions,
        chat: this.data.chat,
        positions: this.data.positions,
      },
    });
  }

  onEnter(alias: any, msg): void {
    this.alias = alias;

    // default load
    if (msg) {
      this.data = msg;
      this.cd.markForCheck();
    }
  }

  enlarge(isEnlarge): void {
    if (this.data.rate !== undefined) {
      this.rate(isEnlarge && this.rated);
    }

    this.msEnlarge.next({
      pos: this.elementView.nativeElement.getBoundingClientRect(),
      alias: this.alias,
      enlarged: isEnlarge,
      rated: this.rated,
    });
  }

  rate(isAlways = false): void {
    this.rated = true;
    this.cd.markForCheck();
    this.msRate.next({
      rate: this.data.rate,
      alias: this.alias,
      always: isAlways,
    });
  }

  remove(): void {
    this.msRemove.emit(this.alias);
  }

  select(): void {
    this.selected = true;
    this.cd.markForCheck();
    this.msSelect.next({ alias: this.alias });
  }

  focus(isFocused, isRated = false, isSelected = false): void {
    this.focused = isFocused;
    if (isFocused === false && !isRated && !isSelected) {
      this.rated = false;
      this.selected = false;
    } else {
      this.rated = isRated;
      this.selected = isSelected;
    }
    this.cd.markForCheck();
  }

  update(rate): void {
    if (this.data) {
      this.data.rate = rate;
      this.cd.markForCheck();
    }
  }

  show(isShow, isEnlarged = false): void {
    this.visible = isShow;
    this.enlarged = isEnlarged;
    this.cd.markForCheck();
  }

  onLeave() {}

  ngOnDestroy() {}
}
