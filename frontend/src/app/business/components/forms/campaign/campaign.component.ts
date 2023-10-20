import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver,
  Inject,
  Injector,
  OnInit,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
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
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { DataService } from 'src/app/services/data.service';
import { HttpService } from 'src/app/services/http.service';
import { TransformService } from 'src/app/services/transform.service';
import { MsPanel } from 'src/lib';
import { BasketComponent } from '../../basket/basket.component';
import { EventFormComponent } from '../event/event.component';
export interface Option {
  value: string;
  viewValue: string;
}

const templateTypes = [
  { value: 'e', viewValue: 'Event' },
  { value: 'i', viewValue: 'Idea' },
  { value: 'j', viewValue: 'Job' },
];

const injector = Injector.create({
  providers: [{ provide: MsPanel, deps: [] }],
});

@Component({
  selector: 'app-campaign',
  templateUrl: './campaign.component.html',
})
export class CampaignFormComponent implements OnInit, AfterViewInit {
  @ViewChild('templatePanel', { static: true, read: ViewContainerRef })
  templatePanelRef: ViewContainerRef;

  @ViewChild('groupPanel', { static: true, read: ViewContainerRef })
  groupPanelRef: ViewContainerRef;

  formGroup: UntypedFormGroup;

  images: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };
  private url: any;

  types: any;
  groupTypes: any;

  title: any = 'Promotion';

  slotsNum = 0;

  constructor(
    private cfr: ComponentFactoryResolver,
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<CampaignFormComponent>,
    public dialog: MatDialog,
    private httpService: HttpService,
    private dataService: DataService,
    public transformService: TransformService,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.data = data.value ? data.value : { slots: [] };
    this.url = data.url;

    this.types = templateTypes;

    const groupTypes = JSON.parse(JSON.stringify(this.dataService.groupTypes));
    groupTypes.push({ value: 'g', viewValue: 'Group' });
    this.groupTypes = groupTypes;
  }

  onGroupType(event) {
    if (event.value === 'g') {
      this.updatePanel(this.groupPanelRef, { type: 'evt' });
    } else {
      this.groupPanelRef.clear();
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.data.item) {
        const msg = this.transformService.transform({ event: this.data.item });
        this.updatePanel(this.templatePanelRef, msg);
      }

      if (this.data.group) {
        const msg = this.transformService.transform({ event: this.data.group });
        this.updatePanel(this.groupPanelRef, msg);
      }
    });
  }

  ngOnInit(): void {
    const offset = new Date().getTimezoneOffset() * 60000;
    const dateFrom = new Date(Date.now() - offset);
    const isoDateFrom = dateFrom.toISOString().substring(0, 16);
    dateFrom.setMinutes(dateFrom.getMinutes() + 1);
    const isoDateTo = dateFrom.toISOString().substring(0, 16);

    const formGroup = this.fb.group({
      data: this.fb.group({
        name: [this.data.name, Validators.required],
        // load own groups and save the choosen one to refId
        count: [this.data.count ? this.data.count : 0, Validators.required],
        type: [this.data.type /*, Validators.required*/],
        item: [this.data.item /*, Validators.required*/],
        groupType: [this.data.groupType, Validators.required],
        group: [this.data.group /*,Validators.required*/],
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
        // slots: [[...this.data.slots], [Validators.required]],
      }),
    });

    this.initSlots(this.data.slots, formGroup);

    this.formGroup = formGroup;
  }

  numSequence(n: number): Array<number> {
    return Array(n);
  }

  onTemplateSelect() {
    const data = {
      url: '/promotions/templates',
      actions: [
        {
          component: EventFormComponent,
          type: 'add',
          icon: 'add',
        },
        {
          component: EventFormComponent,
          type: 'edit',
        },
        {
          component: BasketComponent,
          type: 'list',
          data: {
            actions: [
              {
                component: EventFormComponent,
                type: 'add',
                icon: 'add',
              },
              {
                component: EventFormComponent,
                type: 'edit',
              },
            ],
            reset: true,
            dialog: true,
          },
        },
      ],
      save: false,
    };

    const dialogRef = this.dialog.open(BasketComponent, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
      data,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        this.formGroup.controls['data']
          .get('item')
          .setValue({ key: result[0].info.id });

        const tValue = result[0].info.value;
        if (tValue !== undefined && tValue.capacity !== undefined) {
          const slots = this.formGroup.controls['slots'] as UntypedFormArray;
          slots.clear();

          const range = this.formGroup.get('data').get('range');
          this.addSlot(undefined, {
            capacity: tValue.capacity,
            range: {
              min: new Date(range.get('start').value),
              start: new Date(range.get('start').value),
            },
          });
        }

        this.updatePanel(this.templatePanelRef, result[0].info);
      }
    });
  }

  private updatePanel(panelRef, data: any) {
    panelRef.clear();

    const componentFactory = this.cfr.resolveComponentFactory(MsPanel);
    const component = componentFactory.create(injector);

    component.instance.onEnter('', data);

    component.instance.show(true, true);
    panelRef.insert(component.hostView);
  }

  onGroupSelect() {
    const data = {
      url: '/user/groups',
      save: false,
    };

    const dialogRef = this.dialog.open(BasketComponent, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
      data,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        this.formGroup.controls['data']
          .get('group')
          .setValue({ key: result[0].info.id });

        this.updatePanel(this.groupPanelRef, result[0].info);
      }
    });
  }

  onType(event) {
    this.title = this.types.filter(
      (type) => type.value === event.value
    )[0].viewValue;

    this.updatePanel(this.templatePanelRef, { type: 'evt' });
  }

  initSlots(pSlots, formGroup) {
    const slots = pSlots.map((slot) => this.createSlot(slot));
    this.slotsNum = slots.length;

    formGroup.addControl(
      'slots',
      new UntypedFormArray(slots, [Validators.required, Validators.minLength(1)])
    );
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      const raw = this.formGroup.getRawValue();

      // convert to UTC - in every form it needs to be done
      raw.data.slots = (raw.slots as Array<object>).map((slot) => {
        const slotStartDate = new Date(slot['range'].start);
        const offset = slotStartDate.getTimezoneOffset() * 60000;
        slot['range'].start = new Date(
          slotStartDate.getTime() - offset
        ).toISOString();
        return slot;
      });

      raw.data.key = this.data.key;
      delete raw.slots;

      this.httpService.save(this.url, raw.data).subscribe({
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
  }

  back(): void {
    this.dialogRef.close();
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

  createSlot(data): UntypedFormGroup {
    return this.fb.group({
      range: this.fb.group(
        {
          min: [data.range ? data.range.start : undefined],
          start: [
            data.range ? data.range.start : undefined,
            Validators.required,
          ],
          end: [data.range ? data.range.end : new Date()],
        },
        { validator: DateMinValidator }
      ),
      numOfItems: [
        data.numOfItems ? data.numOfItems : 0,
        [Validators.required, Validators.pattern('^[0-9]+$')],
      ],
      capacity: this.fb.group(
        {
          min: [data.capacity ? data.capacity.min : 0, Validators.required],
          max: [data.capacity ? data.capacity.max : 0, Validators.required],
        },
        { validator: RangeValidator }
      ),
    });
  }
}

const RangeValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const min = control.get('min').value;
  const max = control.get('max').value;

  return min <= max ? null : { range: true };
};

const DateMinValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const min = new Date(control.get('min').value);
  const start = new Date(min.toDateString());
  const end = new Date(control.get('start').value);

  return start !== null && end !== null && start <= end
    ? null
    : { range: true };
};

const DateTimeRangeValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const start = new Date(control.get('start').value);
  const end = new Date(control.get('end').value);
  return start !== null && end !== null && start < end ? null : { range: true };
};
