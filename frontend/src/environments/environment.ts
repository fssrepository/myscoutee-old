// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  serverUrl: "http://localhost:8080/",
  firebase: {
    apiKey: 'AIzaSyA1zXvagfmEpGd8iN7NAgovCd11knGOYSs',
    authDomain: 'myscoutee.firebaseapp.com',
    projectId: 'myscoutee',
    storageBucket: 'myscoutee.appspot.com',
    messagingSenderId: '117896229531',
    appId: '1:117896229531:web:4edd7330be29c1e7404c3e',
  },
  vapid: 'BEqCwlbXuzTypczUuJRV7X8K2rNXDjEXLng95-vE0PeC_Fex5QR9upQMtU03jzG35_qUZQP-f1cBi5U_w-m1cm8',
  groupTypes: {
    d: 'dating',
    b: 'business',
    t: 'job',
    a: 'admin',
  }
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
