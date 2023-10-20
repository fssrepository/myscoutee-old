import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
} from '@angular/material/legacy-dialog';
import { DataService } from 'src/app/services/data.service';
import { HttpService } from 'src/app/services/http.service';
import { EditorComponent } from '../../editor/editor.component';

const DESC_MIN = 10;
const DESC_MAX = 160;
@Component({
  selector: 'app-group',
  templateUrl: './group.component.html',
})
export class GroupFormComponent implements OnInit {
  formGroup: UntypedFormGroup;

  images: any;

  types: any;
  visibilities: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<GroupFormComponent>,
    public dialog: MatDialog,
    private httpService: HttpService,
    private dataService: DataService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {

    this.types = this.dataService.groupTypes;
    this.visibilities = this.dataService.groupVisibilities;

    this.data = data.value ? data.value : { images: [] };
    this.url = data.url;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      data: this.fb.group({
        type: [this.data.type, Validators.required],
        visibility: [this.data.visibility, Validators.required],
        name: [this.data.name, Validators.required],
        desc: [
          this.data.desc !== undefined ? this.data.desc : '',
          [
            Validators.required,
            Validators.minLength(DESC_MIN),
            Validators.maxLength(DESC_MAX),
          ],
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
