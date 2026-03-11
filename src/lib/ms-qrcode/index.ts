import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';

declare const QRCode: any;
declare const Html5Qrcode: any;

@Component({
  selector: 'ms-qrcode',
  templateUrl: './index.html',
})
export class MsQrcode implements OnInit, OnDestroy, OnChanges {
  private html5Qrcode;
  private lastResult;

  @ViewChild('qrCode', { static: true }) qrCodeViewRef: ElementRef;

  @Output() msScanned = new EventEmitter<any>();

  @Input() camera: any;
  @Input() qrCode: any;

  constructor() {}

  onScanSuccess(qrMessage): void {
    if (qrMessage !== this.lastResult) {
      this.lastResult = qrMessage;
      this.turnCamera(false, qrMessage);
    }
  }

  ngOnInit(): void {}

  ngOnChanges(): void {
    if (this.html5Qrcode === undefined) {
      this.html5Qrcode = new Html5Qrcode('reader', false);
    }
    this.turnCamera(this.camera);
  }

  turnCamera(on = false, sQRCode?): void {
    if (on === true) {
      const config = { fps: 10 };
      this.html5Qrcode
        .start(
          { facingMode: 'environment' },
          config,
          (qrMessage) => this.onScanSuccess(qrMessage),
          (err) => {}
        )
        .catch((err) => {
          // Stop failed, handle it.
        });
    } else {
      this.html5Qrcode
        .stop()
        .then((ignore) => {
          console.log('camera stopped');
          if (sQRCode !== undefined) {
            this.msScanned.emit({ qrCode: sQRCode });
          }
        })
        .catch((err) => {
          // Stop failed, handle it.
        });

      setTimeout(() => {
        QRCode.toCanvas(
          this.qrCodeViewRef.nativeElement,
          this.qrCode,
          (error) => {
            if (error) {
              console.error(error);
            }
            console.log('success!');
          }
        );
      });
    }
  }

  ngOnDestroy(): void {
    this.turnCamera(false);
  }
}
