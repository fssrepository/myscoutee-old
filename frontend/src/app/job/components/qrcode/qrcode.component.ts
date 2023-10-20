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
  selector: 'app-qrcode',
  templateUrl: './qrcode.component.html',
})
export class QrcodeComponent implements OnInit {
  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;
  
  camera = false;
  qrCode = 'sample text';

  constructor(
    public dialogRef: MatDialogRef<QrcodeComponent>,
    public dialog: MatDialog,
    private dataService: DataService,
    private httpService: HttpService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.url = data.url;
  }

  ngOnInit(): void {
    const params = new HttpParams();

    this.httpService.get(this.url + '/code', params).subscribe({
      next: (result) => {
        this.qrCode = result['code'];
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

  onScanned(evt): void {
    this.httpService.post(this.url + '/verify', { code: evt }).subscribe({
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

  turnCamera(on = false): void {
    this.camera = on;
  }

  back(): void {
    this.dialogRef.close();
  }
}
