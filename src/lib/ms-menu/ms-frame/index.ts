import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output,
  QueryList,
  Renderer2,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import * as svg_prop from './svg-path';
import { Interpolate, Segment } from './repositories';
import { Calc } from './repositories/utils';

export const HEART_DELTA = 36; //deg

export const HEART_FRAME = {
  type: 'HEART',
  limit: { min: 0, max: 10 },
  num: 5,
  delta: HEART_DELTA,
};

export * from './svg-load';

/*
"svg": {
                "url": "", //svg foreign-object svg url - can render div to canvas
                "delta": "", //alignment value, where the first button has been placed - calculate whether the line is closed or opened - first and last value is the same
                "func":"" //distribution method
            }, //type : freeform - extrenal link - https://greensock.com/forums/topic/11187-accessing-svg-paths-in-external-file/
            "fab": true, //no background, mouse-pointer: none  
*/

/* bugs
 * 1, limit cannot be overflown
 * 2, when first handle touches second one, is going forward without the second one in some cases
 * 3, blue line is getting updated
 * 4, put ruler marker lines on the path (half / quater is thicker)
 */

@Component({
  selector: 'ms-frame',
  templateUrl: './index.html',
})
export class MsFrame implements OnInit, AfterViewInit {
  @Input() alias: any;
  @Input() data;

  @ViewChild('path') path;
  @ViewChild('svg') svg;
  @ViewChildren('handle') handleRef: QueryList<ElementRef>;

  @Input() highlighted;
  @Input() id;

  @ViewChild('wrapper', { static: true }) wrapper: ElementRef;

  @Input() type;

  @ViewChildren('pointHandle') pointHandleRef: QueryList<ElementRef>;

  frame_width;

  isInitialized: boolean;
  resizeTimeout: any;
  segment: Segment;

  handles: Array<{
    x: number;
    y: number;
    picked: boolean;
    selected: boolean;
    value: number;
  }>; //out means picked and removed from the path (can be moved flexible)
  points;

  selectedDiv;

  pressTimer;

  isPicked;
  isPickedMoved;

  offset;

  isGroup: boolean = false;

  percentRange: { from: number; to: string };

  nextPointIdx;
  isPrevUp;

  clonedItem;

  @Output() entered: EventEmitter<any> = new EventEmitter();
  @Output() leaved: EventEmitter<any> = new EventEmitter();

  @Output() changed: EventEmitter<any> = new EventEmitter();

  constructor(
    private _renderer: Renderer2,
    private cd: ChangeDetectorRef,
    private sanitizer: DomSanitizer
  ) {
    this.isInitialized = false;
    this.handles = new Array();
  }

  sanitize(style) {
    return this.sanitizer.bypassSecurityTrustStyle(style);
  }

  ngOnInit(): void {
    this.frame_width = 100;

    this.isPicked = false;
    this.isPickedMoved = false;

    if (this.type === 'path') {
      this.data = HEART_FRAME;

      this.data.closed = true;
      if (this.data.delta === undefined) {
        this.data.delta = HEART_DELTA;
      }

      this.cd.detectChanges();
      this.distribute();
      this.cd.detectChanges();
    }

    this.init();
  }

  ngAfterViewInit(): void {}

  remove(idx) {
    this.handles.splice(idx, 1);
  }

  init() {
    if (!this.isInitialized) {
      /*this.handleRef.forEach((handle, index) => {
        this.initHandle(index, handle.nativeElement);
      });*/

      // with document - preventDefault should be removed

      // touch events is not triggered while dragging to a different frame,
      // only document.elementFromPoint simulates whther the mouse it's on an other frame!!!
      this._renderer.listen(
        this.wrapper.nativeElement,
        'touchstart',
        (event) => {
          event.preventDefault();
          event.stopPropagation();

          if (this.offset === undefined) {
            this.offset = this.wrapper.nativeElement.getBoundingClientRect();

            this.pointHandleRef.forEach((handle, i) => {
              const rect = handle.nativeElement.getBoundingClientRect();
              this.points[i].x = rect.x;
              this.points[i].y = rect.y;
            });
          }

          const touch = event.targetTouches[0];

          this.selectedDiv = document.elementFromPoint(
            touch.clientX,
            touch.clientY
          );
          const handleIdx = parseInt(this.selectedDiv.textContent, 10);

          if (isNaN(handleIdx)) {
            return false;
          }

          this.handles[handleIdx].selected = true;
          this.handleGroup(handleIdx);

          this.pressTimer = window.setTimeout(() => {
            this.handles[handleIdx].picked = true;
            this.handleGroup(handleIdx);
            this.isPicked = true;
          }, 500);
        }
      );

      this._renderer.listen(
        this.wrapper.nativeElement,
        'touchmove',
        (event) => {
          event.preventDefault();
          event.stopPropagation();

          const handleIdx = parseInt(this.selectedDiv.textContent, 10);

          if (isNaN(handleIdx)) {
            return false;
          }

          clearTimeout(this.pressTimer);
          if (this.isPickedMoved) {
            this.handles[handleIdx].picked = false;
          } else {
            this.pressTimer = window.setTimeout(() => {
              this.handles[handleIdx].picked = true;
              this.isPicked = true;
            }, 500);
          }

          const touch = event.targetTouches[0];

          const div = document.elementFromPoint(touch.clientX, touch.clientY);

          const classList = div !== undefined && div !== null && div.classList;
          if (
            classList.contains !== undefined &&
            classList.contains('f_hover')
          ) {
            this.entered.emit(div);
          }

          //removing pointer events - the underneath element has been selected
          if (this.selectedDiv !== undefined) {
            const ptOffset = {
              x: touch.clientX - this.offset.x,
              y: touch.clientY - this.offset.y,
            };

            if (this.isPicked) {
              this.handles[handleIdx].x = ptOffset.x;
              this.handles[handleIdx].y = ptOffset.y;
              this.handles[handleIdx].picked = true;
            }

            let pointIdx;
            let minDistance = Infinity;
            //it was this.segment._points.length for path
            for (let i = 0; i < this.points.length; i++) {
              const distance = Calc.distance2(this.points[i], ptOffset);
              if (minDistance > distance) {
                minDistance = distance;
                pointIdx = i;
              }
            }

            if (this.type === 'path') {
              /*
            moving group to bigger value direction which has got some lower element, realizes late that the lower value element shouldn't move - not a big issue
            */
              if (minDistance > 250) {
                if (this.handles[handleIdx].picked) {
                  this.handles[handleIdx].x = ptOffset.x;
                  this.handles[handleIdx].y = ptOffset.y;
                } else {
                  const itp = new Interpolate(this.segment._path);
                  // this wRatio thing on pageX is crazy, no idea why it's working
                  const pt = {
                    x:
                      (touch.clientX - this.offset.x) /
                      this.segment._bound.wRatio,
                    y:
                      (touch.clientY - this.offset.y) /
                      this.segment._bound.hRatio,
                  };
                  const result = itp.nearby(undefined, pt, undefined);
                  const loc = result.point;

                  loc.x *= this.segment._bound.wRatio;
                  loc.y *= this.segment._bound.hRatio;

                  this.handles[handleIdx].x = loc.x;
                  this.handles[handleIdx].y = loc.y;

                  if (this.handles[handleIdx].value !== pointIdx) {
                    this.handleGroup(handleIdx, pointIdx);
                  }

                  this.handles[handleIdx].value = pointIdx;

                  this.isPickedMoved = true;
                }
                this.handleGroup(handleIdx);
              } else {
                // TODO: check if the handle has been moved by pointIdx !== this.handles[handleIdx].value
                this.handles[handleIdx].x = this.points[pointIdx].x;
                this.handles[handleIdx].y = this.points[pointIdx].y;
                this.handles[handleIdx].picked = false;

                this.handleGroup(handleIdx, pointIdx);
                this.handles[handleIdx].value = pointIdx;

                this.isPickedMoved = true;
              }
              this.changed.emit(pointIdx);
            }
          }
        }
      );

      this._renderer.listen(this.wrapper.nativeElement, 'touchend', (event) => {
        event.preventDefault();
        event.stopPropagation();

        clearTimeout(this.pressTimer);

        if (this.selectedDiv !== undefined) {
          this.leaved.emit();

          const handleIdx = parseInt(this.selectedDiv.textContent, 10);

          if (isNaN(handleIdx)) {
            return false;
          }

          const touch = event.changedTouches[0];
          const ptOffset = {
            x: touch.clientX - this.offset.x,
            y: touch.clientY - this.offset.y,
          };

          if (this.type === 'path') {
            let pointIdx;
            let minDistance = Infinity;
            for (let i = 0; i < this.segment._points.length; i++) {
              let distance = Calc.distance2(this.segment._points[i], ptOffset);
              if (minDistance > distance) {
                minDistance = distance;
                pointIdx = i;
              }
            }

            if (this.handles[handleIdx].picked) {
              if (minDistance <= 500) {
                this.handles[handleIdx].picked = false;
              }
            }

            if (!this.handles[handleIdx].picked) {
              this.handles[handleIdx].x = this.segment._points[pointIdx].x;
              this.handles[handleIdx].y = this.segment._points[pointIdx].y;
              this.handles[handleIdx].value = pointIdx;
              this.changed.emit(pointIdx);
            }
          }

          this.handles[handleIdx].selected = false;

          this.handleGroup(handleIdx);

          this.isPickedMoved = false;
          this.isPicked = false;

          this.selectedDiv = undefined;
          this.nextPointIdx = undefined;
          this.isPrevUp = undefined;
        }
      });

      this.isInitialized = true;
    }
  }

  handleGroup(handleIdx, pointIdx?) {
    if (!this.isGroup) {
      if (pointIdx === undefined) return;

      let hasOverlay = false;
      for (let i = 0; i < this.handles.length; i++) {
        if (this.handles[i].value === pointIdx) {
          hasOverlay = true;
          break;
        }
      }

      let sectionNum = this.points.length;

      let isUp =
        this.nextPointIdx === pointIdx
          ? this.isPrevUp
          : this.nextPointIdx !== undefined
          ? pointIdx > this.nextPointIdx
          : pointIdx > this.handles[handleIdx].value;

      if (pointIdx !== this.handles[handleIdx].value) {
        if (isUp) {
          for (let i = 0; i < this.handles.length; i++) {
            if (hasOverlay && handleIdx !== i) {
              let value =
                (sectionNum + ((this.handles[i].value + 1) % sectionNum)) %
                sectionNum;
              this.handles[i].x = this.points[value].x;
              this.handles[i].y = this.points[value].y;
              this.handles[i].value = value;
            }
          }
          this.nextPointIdx =
            (sectionNum + ((pointIdx + 1) % sectionNum)) % sectionNum;
        } else {
          for (let i = 0; i < this.handles.length; i++) {
            if (hasOverlay && handleIdx !== i) {
              let value =
                (sectionNum + ((this.handles[i].value - 1) % sectionNum)) %
                sectionNum;
              this.handles[i].x = this.points[value].x;
              this.handles[i].y = this.points[value].y;
              this.handles[i].value = value;
            }
          }
          this.nextPointIdx =
            (sectionNum + ((pointIdx - 1) % sectionNum)) % sectionNum;
        }
        this.isPrevUp = isUp;
      }
    } else {
      for (let i = 0; i < this.handles.length; i++) {
        if (i > handleIdx) {
          if (this.handles[i].value <= this.handles[handleIdx].value) {
            this.handles[i].x = this.handles[handleIdx].x;
            this.handles[i].y = this.handles[handleIdx].y;
            this.handles[i].value = this.handles[handleIdx].value;
            this.handles[i].selected = this.handles[handleIdx].selected;
            this.handles[i].picked = this.handles[handleIdx].picked;
          } else {
            if (this.handles[i].picked) {
              if (this.handles[i].value === this.handles[handleIdx].value) {
                this.handles[i].x =
                  this.points[this.handles[handleIdx].value].x;
                this.handles[i].y =
                  this.points[this.handles[handleIdx].value].y;
              }
            } else if (this.handles[i].selected) {
              this.handles[i].x = this.points[this.handles[i].value].x;
              this.handles[i].y = this.points[this.handles[i].value].y;
            }
            this.handles[i].selected = false;
          }
        }

        if (i < handleIdx) {
          if (this.handles[i].value >= this.handles[handleIdx].value) {
            this.handles[i].x = this.handles[handleIdx].x;
            this.handles[i].y = this.handles[handleIdx].y;
            this.handles[i].value = this.handles[handleIdx].value;
            this.handles[i].selected = this.handles[handleIdx].selected;
            this.handles[i].picked = this.handles[handleIdx].picked;
          } else {
            if (this.handles[i].picked) {
              if (this.handles[i].value === this.handles[handleIdx].value) {
                this.handles[i].x =
                  this.points[this.handles[handleIdx].value].x;
                this.handles[i].y =
                  this.points[this.handles[handleIdx].value].y;
              }
            } else if (this.handles[i].selected) {
              this.handles[i].x = this.points[this.handles[i].value].x;
              this.handles[i].y = this.points[this.handles[i].value].y;
            }
            this.handles[i].selected = false;
          }
        }
      }
    }
  }

  //Two.js - there is an example to follow the line
  initHandle(idx, handle) {}

  distribute() {
    let path = this.path.nativeElement;
    let rect = path.getBoundingClientRect();
    let svg = this.svg.nativeElement;
    let viewBoxVal = svg.viewBox.baseVal;
    let wRatio = rect.width / viewBoxVal.width;
    let hRatio = rect.height / viewBoxVal.height;
    let bound = { rect: rect, wRatio: wRatio, hRatio: hRatio };

    let str = path.getAttribute('d');
    path = new svg_prop.svgPathProperties(str);

    this.segment = new Segment(path, this.data, bound);
    this.points = this.segment._points;
    this.handles = JSON.parse(JSON.stringify(this.segment.handles));

    let rules = this.handles.map((handle, idx) => {
      return (
        '.mat-tab-label:nth-child(' +
        (idx + 1) +
        ') { --posX: ' +
        handle.x +
        'px; --posY: ' +
        handle.y +
        'px;}'
      );
    });

    this.createCSSClass(rules);

    let fromPart = (2 * path.getTotalLength()) / this.points.length;
    let from = path.getTotalLength() - fromPart;

    //dasharray is tricky - from dashoffset to "100 1790"
    //100 is the length and 1790 is the length of space - it won't add any new section, as there is no space left (1890 is the total length)
    let toPart = (5 * path.getTotalLength()) / this.points.length;
    let to = toPart + ' ' + (path.getTotalLength() - toPart);
    this.percentRange = { from: from, to: to };
  }

  createCSSClass(rules) {
    var style = document.createElement('style');
    style.type = 'text/css';
    document.head.appendChild(style);

    let cssRulesAppended = rules.join(' ');

    style.appendChild(document.createTextNode(cssRulesAppended));
  }

  // resize not working
  @HostListener('window:resize')
  onResize() {
    if (this.type === 'path') {
      /*this.offset = this.wrapper.nativeElement.getBoundingClientRect();

      let path = this.path.nativeElement;
      let rect = path.getBoundingClientRect();
      let svg = this.svg.nativeElement;
      let viewBoxVal = svg.viewBox.baseVal;
      let wRatio = rect.width / viewBoxVal.width;
      let hRatio = rect.height / viewBoxVal.height;

      for (let handle of this.handles) {
        handle.x *= wRatio / this.segment._bound.wRatio;
        handle.y *= hRatio / this.segment._bound.hRatio;
      }

      for (let point of this.points) {
        point.x *= wRatio / this.segment._bound.wRatio;
        point.y *= hRatio / this.segment._bound.hRatio;
      }

      let rules = this.handles.map((handle, idx) => {
        return ".mat-tab-label:nth-child("+(idx + 1)+") { --posX: "+ handle.x +"px; --posY: " + handle.y + "px;}"
      });

      this.createCSSClass(rules);

      this.segment._bound.wRatio = wRatio;
      this.segment._bound.hRatio = hRatio;*/

      setTimeout(() => {
        this.distribute();
      }, 1500);
    }
  }
}
