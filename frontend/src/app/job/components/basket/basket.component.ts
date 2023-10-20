import { Component, Inject, OnInit } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-basket',
  templateUrl: './basket.component.html',
})
export class BasketComponent implements OnInit {
  items: Array<any> = new Array();

  selectedIdx: any;
  overlayData: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };

  url: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<BasketComponent>,
    private activatedRoute: ActivatedRoute,
    private httpService: HttpService
  ) {
    /*if (data.url !== undefined) {
      this.url = data.url;
    } else {*/
    let url = this.activatedRoute['_routerState'].snapshot.url.replace(
      '/base',
      ''
    );

    url = url.substring(1);
    this.url = url.substring(url.indexOf('/'));

    //}
  }

  ngOnInit(): void {
    if (this.data.selected !== undefined) {
      this.onSelect(this.data.selected);
    }
  }

  onClick(): void {
    // attach the selected members
    if (this.items.length > 0) {
      if (this.data.save !== false) {
        this.progress.mode = 'indeterminate';

        const keys = new Array();
        for (const item of this.items) {
          keys.push(item.info.id);
        }

        this.httpService.save(this.url, keys).subscribe({
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
      } else {
        this.dialogRef.close(this.items);
      }
    }
  }

  onBasket(idx): void {
    if (idx === this.selectedIdx) {
      this.selectedIdx = undefined;
      this.overlayData = undefined;
    } else {
      this.selectedIdx = idx;
      this.overlayData = this.items[idx].info;
    }
  }

  onDelete(): void {
    this.items.splice(this.selectedIdx, 1);
    this.onBasket(this.selectedIdx);
  }

  onSelect(evt): void {
    const items = new Array();
    for (const item in evt) {
      if (evt.hasOwnProperty(item)) {
        items.push(evt[item]);
      }
    }
    this.items = items; // [].concat(this.items.concat(items));
  }

  back(): void {
    if (this.data.reset) {
      this.dialogRef.close({});
    } else {
      this.dialogRef.close();
    }
  }
}
