import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { HttpService } from 'src/app/services/http.service';
import { DataService } from 'src/app/services/data.service';

@Component({
  selector: 'app-feedback',
  templateUrl: './feedback.component.html',
})
export class FeedbackFormComponent implements OnInit {
  formGroup: UntypedFormGroup;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<FeedbackFormComponent>,
    public dialog: MatDialog,
    private dataService: DataService,
    private httpService: HttpService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.data = data.value ? data.value : { images: [] };
    this.url = data.url;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      data: this.fb.group({
        rate: [this.data.rate, Validators.required],
        desc: [
          this.data.desc !== undefined ? this.data.desc : '',
          [
            Validators.required,
            Validators.minLength(10),
            Validators.maxLength(160),
          ],
        ],
      }),
    });
  }

  onRate(evt): void {
    this.data.rate = evt.value;
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      let raw = this.formGroup.getRawValue();
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
