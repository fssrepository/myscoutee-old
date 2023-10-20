import { HttpEventType } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { Canvas } from 'src/lib/offscreen';
import { HttpService } from '../../../services/http.service';
import { EventBusService } from '../../../services/event-bus.service';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
})
export class EditorComponent implements OnInit {
  private mtx: any;
  private img: any;
  private size: any;
  private file: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };

  constructor(
    public dialogRef: MatDialogRef<EditorComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private eventBusService: EventBusService,
    public httpService: HttpService,
    private domSanitizer: DomSanitizer
  ) {}

  onChange(evt) {
    if (evt.img) {
      this.img = evt.img;
    }

    if (evt.size) {
      this.size = evt.size;
    }

    if (evt.mtx) {
      this.mtx = evt.mtx;
    }
  }

  ngOnInit(): void {}

  onClick(): void {
    const canvas = new Canvas(this.size.width, this.size.width);
    const img = canvas.load(this.img, this.mtx, 0.9);

    const edited = new Blob([img], { type: 'application/octet-stream' });

    this.progress.mode = 'indeterminate';

    const url =
      'images' +
      (this.data.value !== undefined ? '/' + this.data.value.name : '');

    const formData = new FormData();
    /*
    for (const paramName in params) {
      formData.append(imgCanvas[],img);
    }
    */
    formData.append('orig', this.file);
    formData.append('edited', edited);

    this.httpService.upload(url, formData).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress) {
          this.progress.mode = 'determine';
          this.progress.value = Math.round(100 * (event.loaded / event.total));
        } else if (event.type === HttpEventType.Response) {
          if (event.status === 201 || event.status === 200) {
            this.dialogRef.close({
              id: event.body['name'],
              idx: this.data.idx,
              mtx: this.mtx,
            });
          }
        }
      },
      error: (error) => {
        this.progress.mode = 'determine';
        this.progress.color = 'warn';
      },
      complete: () => {
        this.progress.mode = 'determine';
      },
    });
  }

  selectFile(e?) {
    const reader = new FileReader();
    reader.onload = (event) => {
      this.data.src = reader.result as string;
    };
    if (e.target.files[0]) {
      this.file = e.target.files[0];
      reader.readAsDataURL(e.target.files[0]);
    }
  }

  rotate(value) {
    this.eventBusService.rotate.emit({ degree: value });
  }

  back(): void {
    this.dialogRef.close();
  }
}
