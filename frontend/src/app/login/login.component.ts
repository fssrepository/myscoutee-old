import { Component, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import firebase from 'firebase/compat/app';
import * as firebaseui from 'firebaseui';
import { NavigationService } from '../navigation.service';
import { HttpService } from '../services/http.service';

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
  // signInFlow: 'popup',
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
    private domSanitizer: DomSanitizer
  ) {}
  ngOnInit(): void {
    if (this.navService.locale === undefined) {
      this.httpService.get('/messages').subscribe({
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
        complete: () => {},
      });
    } else {
      this.msg = this.navService.locale;
      this.initFirebase();
    }
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
