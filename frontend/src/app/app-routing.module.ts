import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuardService as AuthGuard } from './auth-guard.service';
import { LoginComponent } from './login/login.component';

// https://github.com/Mokkapps/angular-manual-lazy-load-demo/blob/master/src/app/lazy-loader.service.ts
// firebase notification lazy load
// https://github.com/mebjas/html5-qrcode - qr code scanning

// filter, add, attach

// business also can attach event organizer's event as a group event -
// can set random - (groups has been rated together as in the case of dating)
// jobs, group invite, idea sharing
const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  // dating - can browse events from organizer
  {
    path: 'dating',
    loadChildren: () =>
      import('./dating/dating.module').then((m) => m.DatingModule),
    canLoad: [AuthGuard],
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule { }
