import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DataService } from 'src/app/services/data.service';

interface Option {
  value: string;
  viewValue: string;
}
@Component({
  selector: 'app-event-filter',
  templateUrl: './event-filter.component.html',
})
export class EventFilterComponent implements OnInit {
  formGroup: UntypedFormGroup;

  categories: any;
  currencies: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<EventFilterComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dataService: DataService
  ) {
    this.categories = this.dataService.eventCategories;
    this.currencies = this.dataService.currencies;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      category: [''],
      city: [''],
      free: [''],
      /*amount: this.fb.group({
        from: ["", [Validators.required, Validators.pattern("^[0-9]*$")]],
        to: ["", [Validators.required, Validators.pattern("^[0-9]*$")]],
        currency: ["", Validators.required]
      }),*/
    });
  }

  onClick(): void {
    let raw;

    if (this.formGroup.valid) {
      raw = this.formGroup.getRawValue();
    }
    this.dialogRef.close(raw);
  }

  back(): void {
    this.dialogRef.close();
  }
}
