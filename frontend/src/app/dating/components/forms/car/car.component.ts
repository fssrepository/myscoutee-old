import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
} from '@angular/material/legacy-dialog';
import { HttpService } from 'src/app/services/http.service';
import { EditorComponent } from '../../editor/editor.component';

@Component({
  selector: 'app-car',
  templateUrl: './car.component.html',
})
export class CarFormComponent implements OnInit {
  formGroup: UntypedFormGroup;

  images: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<CarFormComponent>,
    public dialog: MatDialog,
    private httpService: HttpService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.data = data.value ? data.value : { images: [] };
    this.url = data.url;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      data: this.fb.group({
        regNum: [this.data.regNum, Validators.required],
        make: [this.data.make, Validators.required],
        model: [this.data.model, Validators.required],
        color: [this.data.color, Validators.required],
        year: [
          this.data.year,
          [Validators.required, Validators.pattern('^[0-9]{4}$')],
        ],
        capacity: [
          this.data.capacity,
          [Validators.required, Validators.pattern('^[0-9]+$')],
        ],
      }),
      images: [
        [...this.data.images],
        [Validators.required, Validators.minLength(1)],
      ],
    });

    this.images = (this.data.images as Array<string>).map(
      (image) =>
        location.origin + '/backend' + this.url + '/images/' + image['name']
    );
  }

  help(key) {
    console.log(key);
  }

  numSequence(n: number): Array<number> {
    return Array(n);
  }

  openDialog(idx) {
    const dialogRef = this.dialog.open(EditorComponent, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
      data: {
        idx,
        src:
          this.images[idx] !== undefined
            ? this.images[idx].split('?')[0] + '_orig'
            : this.images[idx],
        value: this.formGroup.controls['images'].value[idx],
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        const images = this.formGroup.controls['images'].value;
        images[result.idx] = { name: result.id, mtx: result.mtx };
        this.formGroup.controls['images'].setValue(images);
        this.data.images[result.idx] = images[result.idx];

        this.images = (this.data.images as Array<string>).map(
          (image) =>
            location.origin +
            '/backend' +
            this.url +
            '/images/' +
            image['name'] +
            '?' +
            Date.now()
        );
      }
    });
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      let raw = this.formGroup.getRawValue();
      raw.data.images = raw.images;
      this.httpService.save(this.url, raw.data).subscribe({
        next: (value) => {
          this.dialogRef.close(value);
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
  }

  back(): void {
    this.dialogRef.close();
  }
}
