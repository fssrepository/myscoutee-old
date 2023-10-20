import { HttpParams } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import {
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
} from '@angular/material/legacy-dialog';
import { HttpService } from 'src/app/services/http.service';
import { DataService } from '../../../services/data.service';

@Component({
  selector: 'app-reward',
  templateUrl: './reward.component.html',
})
export class RewardComponent implements OnInit {
  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  rewards: any;

  constructor(
    public dialogRef: MatDialogRef<RewardComponent>,
    public dialog: MatDialog,
    private dataService: DataService,
    private httpService: HttpService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.url = data.url;
  }

  ngOnInit(): void {
    const params = new HttpParams();

    this.httpService.get(this.url + '/rewards', params).subscribe({
      next: (result) => {
        this.rewards = result;
      },
      error: (error) => {
        this.progress.mode = 'determine';
        this.progress.color = 'warn';
      },
      complete: () => {
        console.log('complete');
      },
    });
  }

  back(): void {
    this.dialogRef.close();
  }
}
