import { Component, Input, OnInit } from '@angular/core';
import { monthlyTable } from './utils';
@Component({
  selector: 'ms-calendar',
  templateUrl: './index.html',
})
export class MsCalendar implements OnInit {
  @Input() data: any;

  id: any;
  rows: Array<Array<any>>;
  items: Array<Array<any>> = new Array();
  days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  ngOnInit(): void {
    this.init();
  }

  init(): void {
    if (this.data) {
      this.id = this.data.id;
      this.rows = monthlyTable(this.data.id);

      this.data.items.forEach((item) => {
        const start = new Date(item.range.start);
        const end = new Date(item.range.end);

        const startDay = start.getDate();
        const endDay = end.getDate();

        let isFound = false;
        let isRangeFound = false;

        for (let i = 0; i < this.rows.length; i++) {
          let calItem = JSON.parse(JSON.stringify(item));
          calItem.cal = { from: undefined, to: undefined, width: undefined };

          for (let j = 0; j < this.rows[i].length; j++) {
            let cell = this.rows[i][j];

            if (cell === undefined) {
              continue;
            }

            if (cell.title === startDay) {
              calItem.cal.from = j;
              isFound = true;
            }

            if (cell.title === endDay) {
              calItem.cal.to = j;
            }
          }

          if (isFound) {
            if (
              calItem.cal.from !== undefined &&
              calItem.cal.to === undefined
            ) {
              calItem.cal.to = 6;
            } else if (
              calItem.cal.from === undefined &&
              calItem.cal.to !== undefined
            ) {
              calItem.cal.from = 0;
              isRangeFound = true;
            } else if (
              calItem.cal.from !== undefined &&
              calItem.cal.to !== undefined
            ) {
              isRangeFound = true;
            } else if (
              calItem.cal.from === undefined &&
              calItem.cal.to === undefined
            ) {
              calItem.cal.from = 0;
              calItem.cal.to = 6;
            }

            if (this.items[i] === undefined) {
              this.items[i] = new Array();
            }

            calItem.cal.width = calItem.cal.to - calItem.cal.from + 1;

            this.items[i].push(calItem);

            if (isRangeFound) {
              break;
            }
          }
        }
      });
    }
  }

  constructor() {
    this.rows = new Array();
  }

  onCellClick(cell): void {
    console.log(cell);
  }
}
