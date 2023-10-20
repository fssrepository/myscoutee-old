import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA
} from '@angular/material/legacy-dialog';
import { DataService } from 'src/app/services/data.service';
import { HttpService } from 'src/app/services/http.service';

const DESC_MIN = 10;
const DESC_MAX = 160;
@Component({
  selector: 'app-profule-status',
  templateUrl: './profile-status.component.html',
})
export class ProfileStatusComponent implements OnInit {
  formGroup: UntypedFormGroup;

  roles: any;
  statuses: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<ProfileStatusComponent>,
    public dialog: MatDialog,
    private httpService: HttpService,
    private dataService: DataService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.roles = this.dataService.roles;
    this.statuses = this.dataService.profileStatuses;

    this.data = data.value ? data.value : {};
    this.url = data.url;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      data: this.fb.group({
        role: [this.data.role, Validators.required],
        status: [this.data.status, Validators.required],
      }),
    });
  }

  numSequence(n: number): Array<number> {
    return Array(n);
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      const raw = this.formGroup.getRawValue();
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
