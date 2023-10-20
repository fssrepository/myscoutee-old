import { HttpEventType } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import {
  AbstractControl,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
} from '@angular/material/legacy-dialog';
import { DataService } from 'src/app/services/data.service';
import { HttpService } from 'src/app/services/http.service';

// https://www.mokkapps.de/blog/manually-lazy-load-modules-and-components-in-angular/
// https://juristr.com/blog/2019/10/lazyload-module-ivy-viewengine/

@Component({
  selector: 'app-school',
  templateUrl: './school.component.html',
})
export class SchoolComponent implements OnInit {
  formGroup: UntypedFormGroup;

  types: any;
  professions: any;

  isEdit: any;

  actions = [
    {
      type: 'add',
      icon: 'add',
    },
    {
      component: SchoolComponent,
      type: 'file',
      icon: 'file_upload',
      color: 'warn',
    },
  ];

  slotsNum = 0;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<SchoolComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialog: MatDialog,
    private dataService: DataService,
    private httpService: HttpService
  ) {
    this.types = this.dataService.categories;
    this.professions = this.dataService.professions;

    this.data = data.value ? data.value : {};
    this.url = data.url;

    const lastPart = this.url.substr(this.url.lastIndexOf('/') + 1);
    const uuidRegex =
      /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

    this.isEdit = lastPart.match(uuidRegex) !== null ? true : false;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({});
    let slots = [];
    if (!this.isEdit) {
      slots = [{}];
    } else {
      slots = [this.data];
    }

    this.initSlots(slots, this.formGroup);
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      const raw = this.formGroup.getRawValue();

      // convert to UTC - in every form it needs to be done
      const data = (raw.slots as Array<object>).map((slot) => {
        const slotStartDate = new Date(slot['range'].start);
        const offset = slotStartDate.getTimezoneOffset() * 60000;
        slot['range'].start = new Date(
          slotStartDate.getTime() - offset
        ).toISOString();

        const slotEndDate = new Date(slot['range'].end);
        slot['range'].end = new Date(
          slotEndDate.getTime() - offset
        ).toISOString();
        if (slot['key'] === null) {
          delete slot['key'];
        }
        return slot;
      });

      this.httpService.save(this.url, data).subscribe({
        next: (values) => {
          this.dialogRef.close(values);
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

  initSlots(pSlots, formGroup) {
    const slots = pSlots.map((slot) => this.createSlot(slot));
    this.slotsNum = slots.length;

    formGroup.addControl(
      'slots',
      new UntypedFormArray(slots, [Validators.required, Validators.minLength(1)])
    );
  }

  createSlot(data): UntypedFormGroup {
    const offset = new Date().getTimezoneOffset() * 60000;
    const dateFrom = new Date(Date.now() - offset);

    return this.fb.group({
      key: [data.key ? data.key : undefined],
      type: [data.type, Validators.required],
      name: [data.name, Validators.required],
      locationKey: [data.locationKey, Validators.required], // google
      range: this.fb.group({
        start: [
          data.range ? new Date(data.range.start) : dateFrom,
          Validators.required,
        ],
        end: [
          data.range ? new Date(data.range.end) : null,
          Validators.required,
        ],
      }),
      role: [data.role, Validators.required],
    });
  }

  addSlot(event?, opts = {}) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    (this.formGroup.controls['slots'] as UntypedFormArray).insert(
      0,
      this.createSlot(opts)
    );

    this.slotsNum++;
  }

  removeSlot(event, i) {
    event.preventDefault();
    event.stopPropagation();

    (this.formGroup.controls['slots'] as UntypedFormArray).removeAt(i);

    this.slotsNum--;
  }

  back(): void {
    this.dialogRef.close();
  }

  doAction(event, action, fileInput?) {
    if (action.type === 'file' && fileInput !== undefined) {
      fileInput.click();
    } else if (action.type === 'add') {
      this.addSlot(event);
    }
  }

  selectFile(e?): void {
    this.progress.mode = 'indeterminate';

    const formData = new FormData();
    formData.append('file', e.target.files[0]);

    const url = this.url + '/parse';
    this.httpService.upload(url, formData).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress) {
          this.progress.mode = 'determine';
          this.progress.value = Math.round(100 * (event.loaded / event.total));
        } else if (event.type === HttpEventType.Response) {
          (event.body as Array<any>).map((data) =>
            this.addSlot(undefined, data)
          );
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
}

const DateTimeRangeValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const start = new Date(control.get('start').value);
  const end = new Date(control.get('end').value);
  return start !== null && end !== null && start < end ? null : { range: true };
};
