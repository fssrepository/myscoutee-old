import { Component, HostListener, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';
import firebase from 'firebase/compat/app';
import * as firebaseui from 'firebaseui';
import { NavigationService } from '../navigation.service';
import { HttpService } from '../services/http.service';
import { MatDialog } from '@angular/material/dialog';
import { MsDialog } from 'src/lib';
import { environment } from 'src/environments/environment';

const uiConfig = {
  callbacks: {
    signInSuccessWithAuthResult(authResult, redirectUrl): boolean {
      return false;
    },
    uiShown(): void {
      console.log('aaaaa');
      document.getElementById('loader').style.display = 'none';
    },
  },
  signInFlow: 'popup',
  signInOptions: [
    firebase.auth.GoogleAuthProvider.PROVIDER_ID,
    firebase.auth.FacebookAuthProvider.PROVIDER_ID,
    firebase.auth.EmailAuthProvider.PROVIDER_ID,
  ],
  tosUrl: 'https://www.termsfeed.com/live/db6d4315-9907-46cd-929f-868630e81f69', // terms
  privacyPolicyUrl:
    'https://www.termsfeed.com/live/bab86d2c-6ed7-4fa2-8f8e-eda206765d42',
};

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  server = true;
  msg;

  constructor(
    private navService: NavigationService,
    private httpService: HttpService,
    private domSanitizer: DomSanitizer,
    private router: Router,
    public dialog: MatDialog,
  ) { }
  ngOnInit(): void {
    if (environment.disableFirebaseAuth) {
      this.navService.token = 'dev-auth-disabled';
      this.router.navigate(['dating']);
      return;
    }

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
          this.msg = this.navService.locale;

          this.initFirebase();
        },
        error: (error) => {
          console.log('Server is down!');
        },
        complete: () => { },
      });
    } else {
      this.msg = this.navService.locale;
      this.initFirebase();
    }
  }

  private deferredPrompt: any;

  @HostListener('window:beforeinstallprompt', ['$event'])
  onBeforeInstallPrompt(event: any) {
    // Prevent Chrome 67 and earlier from automatically showing the prompt
    event.preventDefault();
    // Stash the event so it can be triggered later
    this.deferredPrompt = event;

    // Show a custom message or button to the user using a dialog
    const dialogRef = this.dialog.open(MsDialog, {
      data: {
        msg: 'Add to home screen to enable notification!',
        btn: 'Yes'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        // User clicked "OK," trigger the add-to-home-screen prompt
        this.deferredPrompt.prompt();
        this.deferredPrompt.userChoice
          .then((choiceResult: any) => {
            if (choiceResult.outcome === 'accepted') {
              console.log('User accepted the A2HS prompt');
            } else {
              console.log('User dismissed the A2HS prompt');
            }
            // Reset the deferred prompt variable
            this.deferredPrompt = null;
          });
      } else {
        // User clicked "Cancel," do something else or nothing
      }
    });
  }

  initFirebase() {
    setTimeout(() => {
      const ui =
        firebaseui.auth.AuthUI.getInstance() ||
        new firebaseui.auth.AuthUI(firebase.auth());
      ui.start('#firebaseui-auth-container', uiConfig);
    });
  }
}
