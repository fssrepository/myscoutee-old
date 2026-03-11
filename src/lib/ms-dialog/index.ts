import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'ms-dialog',
  templateUrl: 'index.html',
})
export class MsDialog {
  constructor(
    public dialogRef: MatDialogRef<MsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onClick(val): void {
    this.dialogRef.close(val);
  }
}
