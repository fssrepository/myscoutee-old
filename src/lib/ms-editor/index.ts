import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { EventBusService } from 'src/app/services/event-bus.service';
import { Magnifier, Transform } from './magnifier';

//no rotate buttons - it should be on multi touch
//it will be zoomable calendar - create element, not just for image
@Component({
  selector: 'ms-editor',
  templateUrl: './index.html',
})
export class MsEditor implements OnInit, OnChanges {
  @Input() data: any;

  private alias: any;
  private magnifier: Magnifier;

  @ViewChild('editor', { static: true }) editorRef: ElementRef;
  @ViewChild('item', { static: true }) itemRef: ElementRef;

  @Output() changed: EventEmitter<any> = new EventEmitter();

  constructor(
    private _renderer: Renderer2,
    private navService: EventBusService
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.notify();
  }

  loaded(evt) {
    if (this.data.value !== undefined) {
      this.update(this.data.value.mtx);
    }
  }

  // init
  ngOnInit(): void {
    //this.notify();
    /*QRCode.toCanvas(this.canvasRef.nativeElement, 'sample text', function (error) {
            if (error) console.error(error)
            console.log('success!');
        });*/

    if (this.data.src !== undefined) {
      this.data.src = this.data.src + '?' + Date.now();
    } else {
      this.data.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    }

    let transform = new Transform();
    if (this.data.value !== undefined) {
      transform = new Transform(this.data.value.mtx);
    }

    this.magnifier = new Magnifier(
      this.editorRef.nativeElement,
      this._renderer,
      transform
    );

    this.magnifier.listen((mtx) => {
      this.update(mtx);
    });

    this.navService.rotate.subscribe((tr) => {
      const mtx = this.magnifier.rotate(tr.degree);
      this.update(mtx);
    });
  }

  update(mtx) {
    this._renderer.setStyle(
      this.itemRef.nativeElement,
      'transform',
      'translate(' +
      mtx.pos.x +
      'px,' +
      mtx.pos.y +
      'px) scale(' +
      mtx.scale +
      ',' +
      mtx.scale +
      ') rotate(' +
      mtx.angle +
      'deg)'
    );
    this.changed.emit({ mtx });
  }

  notify(): void {
    setTimeout(() => {
      this.changed.emit({
        mtx: this.magnifier.transform.mtx,
        img: this.itemRef.nativeElement,
        size: {
          height: this.editorRef.nativeElement.clientHeight,
          width: this.editorRef.nativeElement.clientWidth,
        },
      });
    });
  }
}
