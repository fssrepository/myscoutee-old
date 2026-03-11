import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-event',
  templateUrl: './event.component.html',
})
export class EventComponent implements OnInit {
  constructor(public dialogRef: MatDialogRef<EventComponent>) {}

  ngOnInit(): void {}

  onClick(): void {
    this.dialogRef.close();
  }
}
