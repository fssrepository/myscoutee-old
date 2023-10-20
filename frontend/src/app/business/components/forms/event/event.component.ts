import { Component, Inject, OnInit } from '@angular/core';
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DataService } from 'src/app/services/data.service';
import { HttpService } from 'src/app/services/http.service';

const DESC_MIN = 10;
const DESC_MAX = 160;

@Component({
  selector: 'app-event',
  templateUrl: './event.component.html',
})
export class EventFormComponent implements OnInit {
  formGroup: UntypedFormGroup;

  categories: any;
  currencies: any;
  types: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  isAdd: any;

  isPriority = true;

  isMain = true;

  category = 'l';

  // main event flag - only one main event does exist, which contains assign car and signal chat group
  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<EventFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dataService: DataService,
    private httpService: HttpService
  ) {
    this.categories = this.dataService.eventCategories;
    this.currencies = this.dataService.currencies;

    if (data.extra !== undefined) {
      if (data.extra.main !== undefined) {
        this.isMain = data.isAdd ? false : data.extra.main;
      }

      if (data.extra.category !== undefined) {
        this.category = data.extra.category;
      }
    }

    if (data.url.indexOf('items') === -1) {
      this.types = this.dataService.eventTypes.filter((obj) => {
        return obj.value === 'g';
      });
    } else {
      this.types = this.dataService.eventTypes;
    }

    if (data.url.indexOf('promotions') !== -1) {
      this.isPriority = false;
    }

    this.data = data.value ? data.value : { type: 'g', amount: {} };
    this.url = data.url;
  }

  validatorsForChatKey(type) {
    const validators = new Array();
    if (type === 'g') {
      validators.push(Validators.required);
    }

    validators.push(
      Validators.pattern(
        '^https?://(www.)?[-a-zA-Z0-9@:%._+~#=]{1,256}.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@,!:%_+.~#?&//=]*)$'
      )
    );
    return validators;
  }

  // if random, then you can set activity profile date from and you rated greater than
  ngOnInit(): void {
    const chatKeyValidators = new Array();

    chatKeyValidators.push(
      Validators.pattern(
        '^https?://(www.)?[-a-zA-Z0-9@:%._+~#=]{1,256}.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@,!:%_+.~#?&//=]*)$'
      )
    );

    const chatUrlValidators = this.validatorsForChatKey(this.data.type);

    const offset = new Date().getTimezoneOffset() * 60000;
    const dateFrom = new Date(Date.now() - offset);
    const isoDateFrom = dateFrom.toISOString().substring(0, 16);
    dateFrom.setMinutes(dateFrom.getMinutes() + 1);
    const isoDateTo = dateFrom.toISOString().substring(0, 16);

    this.formGroup = this.fb.group({
      data: this.fb.group({
        type: [this.data.type, Validators.required],
        optional: [
          this.data.optional ? this.data.optional : false,
          Validators.required,
        ],
        ticket: [
          this.data.ticket ? this.data.ticket : false,
          Validators.required,
        ],
        chatKey: [this.data.chatKey, [...chatUrlValidators]],
        category: [
          {
            value:
              this.category !== undefined ? this.category : this.data.category,
            disabled: !this.isMain,
          },
          Validators.required,
        ],
        position: [
          this.data.position !== undefined
            ? this.data.position.x + ', ' + this.data.position.y
            : '',
          Validators.required,
        ],
        capacity: this.fb.group(
          {
            min: [
              this.data.capacity ? this.data.capacity.min : 0,
              Validators.required,
            ],
            max: [
              this.data.capacity ? this.data.capacity.max : 0,
              Validators.required,
            ],
          },
          { validator: RangeValidator }
        ),
        name: [this.data.name, Validators.required],
        urlRef: [this.data.urlRef, [...chatKeyValidators]], // google
        range: this.fb.group(
          {
            start: [
              this.data.range
                ? this.data.range.start.substring(0, 16)
                : isoDateFrom,
              Validators.required,
            ],
            end: [
              this.data.range
                ? this.data.range.end.substring(0, 16)
                : isoDateFrom,
              Validators.required,
            ],
          },
          { validator: DateTimeRangeValidator }
        ),
        desc: [
          this.data.desc !== undefined ? this.data.desc : '',
          [
            Validators.required,
            Validators.minLength(DESC_MIN),
            Validators.maxLength(DESC_MAX),
          ],
        ],
        telNum: [
          this.data.telNum,
          this.data.type === 'c' ? Validators.required : undefined,
        ],
        priority: [
          this.data.priority ? this.data.priority : false,
          Validators.required,
        ],
      }),
      rule: this.fb.group({
        balanced: [
          this.data.rule && this.data.rule.balanced
            ? this.data.rule.balanced
            : false,
          Validators.required,
        ],
        met: [
          this.data.rule && this.data.rule.met ? this.data.rule.met : false,
          Validators.required,
        ],
        rate: [
          this.data.rule && this.data.rule.rate ? this.data.rule.rate : 0,
          [Validators.required, Validators.pattern('^[0-9]+$')],
        ],
        range: this.fb.group(
          {
            start: [
              this.data.rule && this.data.rule.range
                ? this.data.rule.range.start
                : undefined,
            ],
            end: [
              this.data.rule && this.data.rule.range
                ? this.data.rule.range.end
                : undefined,
            ],
          },
          { validator: DateTimeRangeValidator }
        ),
      }),
      expense: this.fb.group({
        amount: this.fb.group({
          value: [
            this.data.amount.value,
            [
              Validators.required,
              Validators.pattern('^[0-9]+[\\.]?[0-9]{0,}$'),
            ],
          ],
          currency: [this.data.amount.currency, Validators.required],
        }),
      }),
    });
  }

  onTypeChange(event) {
    (this.formGroup.controls['data'] as UntypedFormGroup).controls[
      'chatKey'
    ].clearValidators();

    const chatUrlValidators = this.validatorsForChatKey(event.value);
    (this.formGroup.controls['data'] as UntypedFormGroup).controls[
      'chatKey'
    ].setValidators([...chatUrlValidators]);

    (this.formGroup.controls['data'] as UntypedFormGroup).controls[
      'chatKey'
    ].updateValueAndValidity();
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      const raw = this.formGroup.getRawValue();
      raw.data.amount = raw.expense.amount;
      raw.data.rule = raw.rule;
      raw.data.key = this.data.key;

      raw.data.position = raw.data.position
        .split(',')
        .map((item) => item.trim());

      this.httpService.save(this.url, raw.data).subscribe({
        next: (value) => {
          console.log(value);
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

const RangeValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const min = control.get('min').value;
  const max = control.get('max').value;

  return min <= max ? null : { range: true };
};

const DateTimeRangeValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const startS = control.get('start').value;
  const endS = control.get('end').value;

  let start;
  if (startS !== null) {
    start = new Date(startS);
  }

  let end;
  if (endS !== null) {
    end = new Date(endS);
  }

  return start === undefined || end === undefined || start < end
    ? null
    : { range: true };
};
