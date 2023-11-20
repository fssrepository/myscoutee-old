import {
  AfterViewInit,
  Component,
  HostListener,
  NgZone,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import {
  ActivatedRoute,
  ChildActivationStart,
  Event,
  Params,
  Router,
  RouterOutlet,
} from '@angular/router';
import { SwPush, SwUpdate } from '@angular/service-worker';
import firebase from 'firebase/compat/app';
import 'firebase/compat/auth';
import 'firebase/compat/messaging';
import { environment } from 'src/environments/environment';
import { slideInAnimation } from './animation';
import { NavigationService } from './navigation.service';
import { HttpService } from './services/http.service';
import { ListService } from './services/list.service';
import { MqttService } from './services/mqtt.service';

// pdf js file upload
// https://codepen.io/Shiyou/pen/JNLwVO?html-preprocessor=pug

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  animations: [
    slideInAnimation,
    // animation triggers go here
  ],
})
export class AppComponent implements OnInit, OnDestroy, AfterViewInit {
  prevUrl: any;
  links: any;
  itemLinks: any;
  subLinks: any;
  basePath: any;

  items: Array<any> = new Array();
  itemKeys: Array<any> = new Array();

  selectedIdx: any;
  overlayData: any;

  timer: any;

  /*
    openSnackBar(item: any, action: string) {
    let snack = this._snackBar.open(item.idx, action, {
      verticalPosition: 'top',
    });

    snack.afterDismissed().subscribe(() => {
      item.callback('auuuu');
    });
  }*/

  constructor(
    private router: Router,
    private zone: NgZone,
    private navService: NavigationService,
    private updates: SwUpdate,
    private push: SwPush,
    private httpService: HttpService,
    private activatedRoute: ActivatedRoute,
    private listService: ListService,
    private domSanitizer: DomSanitizer,
    private mqttService: MqttService
  ) {
    // this.initFacebookService();

    this.activatedRoute.queryParams.subscribe((params: Params) => {
      if (params['xlink'] !== undefined) {
        const regex =
          /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
        if (regex.test(params['xlink'])) {
          this.navService.xlink = params['xlink'];
        }
      }
    });

    this.navService.reset.subscribe(() => {
      this.prevUrl = undefined;
      this.links = undefined;
      this.itemLinks = undefined;
      this.subLinks = undefined;
      this.basePath = undefined;
    });
    this.navService.online.next({ online: navigator.onLine });
  }
  ngAfterViewInit(): void { }

  @HostListener('window:online', ['$event'])
  @HostListener('window:offline', ['$event'])
  online(evt): void {
    this.navService.online.next({ online: evt.type === 'online' });
  }

  ngOnInit(): void {

    //this.mqttService.init({url: "wss://localhost:1883", options : {username: , password: }});

    navigator.geolocation.getCurrentPosition((position) => {
      this.navService.pos = [
        position.coords.longitude,
        position.coords.latitude,
      ];
    });

    if (this.navService.locale === undefined) {
      this.httpService.get('/i18n_messages').subscribe({
        next: (value) => {
          const evt = value['msg'];

          if (evt['expl.videos']) {
            evt['expl.videos'] = evt['expl.videos'].map((video) =>
              this.domSanitizer.bypassSecurityTrustResourceUrl(video)
            );
          }

          this.navService.locale = evt;
        },
        error: (error) => {
          console.log('Server is down!');
        },
        complete: () => { },
      });
    }

    this.router.events.subscribe((routerEvent: Event) => {
      if (routerEvent instanceof ChildActivationStart) {
        const routeConfig = routerEvent.snapshot.routeConfig;

        const url = routerEvent.snapshot['_routerState'].url;

        if (routeConfig) {
          if (routeConfig.children) {
            let baseUrl: string;
            if (routeConfig.data.type === 'P') {
              const rootPath = routeConfig.path.replace('/:id', '');
              const urlParts = url.split(rootPath);
              baseUrl = urlParts[0] + rootPath;
            } else {
              let startIdx = 0;
              if (url.indexOf('/') === 0) {
                startIdx = 1;
              }
              baseUrl = url.substr(startIdx, url.lastIndexOf('/') - 1);
            }

            const links = routeConfig.children
              .filter(
                (route) => route.redirectTo === undefined && !route.data.dialog
              )
              .map((route) => ({
                path:
                  baseUrl +
                  '/' +
                  (routeConfig.data.type === 'P'
                    ? route.path.replace(':id', this.navService.profileId)
                    : route.path),
                data: route.data,
              }));

            if (routeConfig.path.indexOf(':id') !== -1) {
              this.prevUrl =
                this.navService.prevUrl !== undefined
                  ? this.navService.prevUrl
                  : baseUrl.substr(0, baseUrl.lastIndexOf('/'));
              this.itemLinks = links;
              this.subLinks = undefined;

              if (this.navService.selected !== undefined) {
                if (
                  this.itemKeys[this.navService.selected['info'].id] ===
                  undefined
                ) {
                  this.items.push(this.navService.selected);
                  this.itemKeys[this.navService.selected['info'].id] = true;
                }
              }
            } else {
              if (links.length === 1 && links[0].data.icon === undefined) {
                this.subLinks = undefined;
              } else {
                this.subLinks = links;
              }
            }
          } else if (routeConfig.loadChildren) {
            this.links = routeConfig['_loadedRoutes']
              .filter(
                (route) => route.redirectTo === undefined && !route.data.dialog
              )
              .map((route) => ({
                path:
                  routeConfig.path +
                  '/' +
                  route.path +
                  (route.data.type === 'P'
                    ? '/' + this.navService.profileId
                    : ''),
                data: route.data,
              }));

            this.subLinks = undefined;
          }
        } else {
          this.subLinks = undefined;
        }
      }
    });

    this.updates.available.subscribe((_) =>
      this.updates.activateUpdate().then(() => {
        console.log('reload for update');
        document.location.reload();
      })
    );

    this.push.notificationClicks.subscribe((click) =>
      console.log('notification click', click)
    );

    if (firebase.apps.length === 0) {
      firebase.initializeApp(environment.firebase);

      firebase.auth().onIdTokenChanged((user) => {
        if (user) {
          user
            .getIdToken()
            .then((token) => {
              this.navService.token = token;
            })
            .catch((error) => { });

          if (this.timer !== undefined) {
            clearInterval(this.timer);
          }

          this.timer = setInterval(() => {
            const currentUser = firebase.auth().currentUser;
            currentUser
              .getIdToken()
              .then((token) => {
                this.navService.token = token;
              })
              .catch((error) => { });
          }, 25 * 60 * 1000);
        }
      });

      firebase.auth().onAuthStateChanged((user) => {
        if (user) {
          console.log('user-logged');

          user
            .getIdToken()
            .then((token) => {
              this.navService.token = token;

              console.log("/user");
              this.httpService.get('/user').subscribe({
                next: (value) => {

                  console.log(value);
                  this.navService.user = value['user'];

                  let username = value['user'].profile.key;
                  this.mqtt(username, token);

                  this.msg().then((x) => {
                    console.log('Messaging initialized!');
                  });

                  /*const group = value['user'].group;
                  const groups = value['groups'];

                  let groupType;
                  for (const item of groups) {
                    if (item.group.key === group) {
                      groupType = environment.groupTypes[item.group.type];
                      this.navService.groupName = item.group.name[0];
                      this.navService.groupType = item.group.type;
                      break;
                    }
                  }

                  const notif = {};
                  for (const like of value['likes']) {
                    notif[groupType + '/' + like.root] = like.value;
                  }
                  this.navService.notif = notif;

                  if (
                    value['user'].profile === undefined ||
                    value['user'].profile.status === 'I'
                  ) {
                    this.router.navigate([groupType + '/user']);
                  } else {
                    this.router.navigate([groupType]);
                  }*/

                  this.router.navigate(['dating']);
                },
                error: (err) => {
                  this.router.navigate(['dating/user']);
                },
              });

              // this.zone.run(() => {
              //  this.router.navigate(['dating']);
              // });
            })
            .catch((error) => {
              console.log(error);
            });
        } else {
          if (this.timer !== undefined) {
            clearInterval(this.timer);
          }

          console.log('user-not-logged');
          this.navService.token = undefined;
          this.router.navigate(['login']);
        }
      });
    }

    // this.router.navigate(['test']);
  }

  mqtt(username, password) {
    this.mqttService.init(
      {
        url: "ws://localhost:8883",
        options:
          { username: username, password: password }
      });

      this.mqttService.register("channels/users/" + username, (str) =>  {
        //console.log(str);
      });
  }

  async msg() {
    const registration = await navigator.serviceWorker.ready;

    firebase.messaging().onMessage((msg) => {
      console.log("fcm message");
      console.log(msg);
    });

    firebase
      .messaging()
      .getToken({
        vapidKey: environment.vapid,
        serviceWorkerRegistration: registration,
      })
      .then((currentToken) => {
        if (currentToken) {
          this.httpService
            .post('/tokens', { deviceKey: currentToken })
            .subscribe({
              next: (value) => {
                console.log(value);
              },
              error: (err) => {
                console.log('Token registration error!');
              },
            });
        } else {
          console.log(
            'No registration token available. Request permission to generate one.'
          );
        }
      })
      .catch((err) => {
        console.log('An error occurred while retrieving token. ', err);
      });
  }

  ngOnDestroy(): void { }

  back(): void {
    this.items = new Array();
    this.itemKeys = new Array();

    this.navService.selected = undefined;

    this.itemLinks = undefined;
    const prevUrl =
      this.navService.prevUrl !== undefined
        ? this.navService.prevUrl
        : this.prevUrl;
    this.navService.prevUrl = undefined;
    this.router.navigate([prevUrl]);
  }

  prepareRoute(outlet: RouterOutlet) {
    return (
      outlet && outlet.activatedRouteData && outlet.activatedRouteData.animation
    );
  }

  onBasket(idx) {
    if (idx === this.selectedIdx) {
      this.selectedIdx = undefined;
      this.overlayData = undefined;
    } else {
      this.selectedIdx = idx;
      this.overlayData = this.items[idx].info;
    }

    if (this.overlayData) {
      this.overlayData.removable = false;
    }

    this.listService.overlay.emit(this.overlayData);
  }
}
