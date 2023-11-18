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
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { ListService } from 'src/app/services/list.service';
import { ILazy } from '../interface';
import { MsAction } from '../ms-action';

/* changeDetection: ChangeDetectionStrategy.OnPush*/
@Component({
  selector: 'ms-chat',
  templateUrl: './index.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [':host { grid-column: 1 / span 2 }']
})
export class MsChat implements OnDestroy, OnInit, ILazy {

  @Input() alias: string;
  @Input() data: any;
  @Input() visible: boolean;

  constructor(
    private cd: ChangeDetectorRef,
    private router: Router,
  ) { }

  ngOnInit() {

  }

  onEnter(alias: any, msg): void {
    this.alias = alias;

    // default load
    if (msg) {
      this.data = msg;
      this.cd.markForCheck();
    }
  }
  onLeave() { }

  content(): void {
    if (this.data.children && this.data.url) {
      this.router.navigate([this.data.url]);
    }
  }

  show(isShow, isEnlarged = false): void {
    this.visible = isShow;
    this.cd.markForCheck();
  }

  ngOnDestroy() { }
}
