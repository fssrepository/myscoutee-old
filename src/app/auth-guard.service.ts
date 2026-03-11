import { Injectable } from '@angular/core';
import { Route, Router, UrlSegment, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { NavigationService } from './navigation.service';
import { HttpService } from './services/http.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService  {
  constructor(
    public navService: NavigationService,
    public httpService: HttpService,
    public router: Router
  ) {}
  canLoad(
    route: Route,
    segments: UrlSegment[]
  ):
    | boolean
    | UrlTree
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree> {
    if (environment.disableFirebaseAuth) {
      return true;
    }

    return new Observable<boolean>((observer) => {
      if (this.navService.token === undefined) {
        observer.next(false);
        observer.complete();
      } else {
        observer.next(true);
        observer.complete();
      }
    });
  }
}
