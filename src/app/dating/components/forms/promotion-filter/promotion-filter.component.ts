import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DataService } from 'src/app/services/data.service';

// Group-Filter has the common filter settings handling - this component will be removed
interface Option {
  value: string;
  viewValue: string;
}
@Component({
  selector: 'app-promotion-filter',
  templateUrl: './promotion-filter.component.html',
})
export class PromotionFilterComponent implements OnInit {
  formGroup: UntypedFormGroup;

  types: any;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<PromotionFilterComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dataService: DataService
  ) {
    this.types = this.dataService.templateTypes;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      type: [this.data.type],
      rated: [this.data.rated],
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
