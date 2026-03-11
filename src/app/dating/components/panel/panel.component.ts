import { HttpParams } from '@angular/common/http';
import {
  Component,
  ComponentFactoryResolver,
  ComponentRef,
  Injector,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { HttpService } from 'src/app/services/http.service';
import { MsPanel } from 'src/lib';
import { DataService } from '../../../services/data.service';
import { TransformService } from '../../../services/transform.service';

const injector = Injector.create({
  providers: [{ provide: MsPanel, deps: [] }],
});

@Component({
  selector: 'app-panel',
  templateUrl: './panel.component.html',
})
export class PanelComponent implements OnInit, OnDestroy {
  @ViewChild('panel', { static: true, read: ViewContainerRef })
  panelRef: ViewContainerRef;

  private sub: Subscription;

  actions: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };

  value: any;

  private url: any;
  private itemUrl: any;

  private comp: ComponentRef<MsPanel>;

  constructor(
    private cfr: ComponentFactoryResolver,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
    public dataService: DataService,
    private httpService: HttpService,
    public transformService: TransformService
  ) {
    let url = this.activatedRoute['_routerState'].snapshot.url;
    this.itemUrl = url;

    url = url.substring(1);
    this.url = url.substring(url.indexOf('/'));

    this.progress.mode = 'indeterminate';

    this.httpService.get(this.url).subscribe({
      next: (value) => {
        this.value = value;

        const componentFactory = this.cfr.resolveComponentFactory(MsPanel);
        const component = componentFactory.create(injector);

        let msg;
        if (this.value['profile'].status !== 'P') {
          msg = this.transformService.transform(value, this.itemUrl);
        }

        component.instance.onEnter('', msg);

        component.instance.show(true, true);
        this.panelRef.insert(component.hostView);
        this.comp = component;
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

  doAction(idx) {
    switch (this.actions[idx].type) {
      case 'route':
        this.router.navigate([this.actions[idx].url], {
          relativeTo: this.activatedRoute,
        });
        break;
      case 'share':
        const params = new HttpParams();

        this.httpService.get(this.url + '/share', params).subscribe({
          next: (result) => {
            if (navigator.share) {
              navigator
                .share({
                  title: result['info'].title,
                  text: result['info'].msg,
                  url: location.origin + '?xlink=' + result['link'].key,
                })
                .then(() => {
                  console.log('Thanks for sharing!');
                })
                .catch(console.error);
            } else {
              console.log({
                title: result['info'].title,
                text: result['info'].msg,
                url: location.origin + '?xlink=' + result['link'].key,
              });
              alert('Not implemented for desktop!');
            }
          },
          error: (error) => {
            this.progress.mode = 'determine';
            this.progress.color = 'warn';
          },
          complete: () => {
            console.log('complete');
          },
        });
        break;
      default:
        this.openDialog(idx);
    }
  }

  openDialog(idx) {
    var dialogRef = this.dialog.open(this.actions[idx].component, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
      data: { value: this.value, url: this.url },
    });

    dialogRef.afterClosed().subscribe((result) => {
      switch (this.actions[idx].type) {
        case 'filter':
          break;
        case 'add':
          break;
        case 'attach':
          break;
        case 'logout':
          break;
        case 'edit':
          if (result !== undefined) {
            this.value = result;
            this.comp.instance.onEnter(
              '',
              this.transformService.transform(result, this.itemUrl, false, true)
            );
            this.panelRef.insert(this.comp.hostView);
          }
          break;
      }

      dialogRef = null;
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  ngOnInit(): void {
    this.sub = this.activatedRoute.data.subscribe((v) => {
      this.actions = v.actions;
    });
  }
}
