import {
  HttpClient,
  HttpErrorResponse,
  HttpHeaders,
  HttpParams
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { NavigationService } from '../navigation.service';
import { MockApiService } from './mock-api.service';

const AUTH_FIREBASE_HEADER = 'X-Authorization-Firebase';
const AUTH_LINK_HEADER = 'X-Authorization-Link';

// batch rate
// https://www.raymondcamden.com/2013/06/06/IndexedDB-and-Date-Example
@Injectable({
  providedIn: 'root',
})
export class HttpService {
  private openReq;
  private db;

  private toDate;
  private isPending = false;

  constructor(
    private httpClient: HttpClient,
    private navService: NavigationService,
    private mockApiService: MockApiService
  ) {
    if (this.isMockEnabled()) {
      return;
    }

    // different db name for different profile
    this.openReq = indexedDB.open('myscoutee_db', 1);

    this.openReq.onsuccess = (e) => {
      this.db = e.target.result;
    };

    this.openReq.onupgradeneeded = (e) => {
      this.db = e.target.result;

      if (!this.db.objectStoreNames.contains('likes')) {
        var os = this.db.createObjectStore('likes', { autoIncrement: true });
        os.createIndex('created', 'created', { unique: false });
      }
    };

    //batch like
    setInterval(() => {
      this.sync();
    }, 30000);
  }
  // private REST_API_SERVER = 'http://localhost:4200';

  public get(urlPart: string, params: HttpParams = new HttpParams()) {
    if (this.isMockEnabled()) {
      return this.mockApiService.get(urlPart, params);
    }

    let headers = new HttpHeaders();

    if (this.navService.token !== undefined) {
      headers = headers.append(AUTH_FIREBASE_HEADER, this.navService.token);
    }

    if (this.navService.xlink !== undefined) {
      headers = headers.append(AUTH_LINK_HEADER, this.navService.xlink);
    }

    return this.httpClient
      .get(location.origin + '/backend' + urlPart, { headers, params })
      .pipe(catchError(this.handleError));
  }

  public index(like) {
    if (this.isMockEnabled() || !this.db) {
      return;
    }

    let objStore = this.db
      .transaction(['likes'], 'readwrite')
      .objectStore('likes');
    like.rate++;
    objStore.add({ created: new Date(), like });
  }

  public sync(): Promise<any> {
    if (this.isMockEnabled() || !this.db) {
      return Promise.resolve({});
    }

    return new Promise((resolve, reject) => {
      if (!this.isPending) {
        this.batch().then((res) => {
          if (res.length === 0) {
            resolve({});
          }

          this.isPending = true;

          let headers = new HttpHeaders();
          headers = headers.append(AUTH_FIREBASE_HEADER, this.navService.token);

          console.log(location.origin + '/backend/profiles/like');
          console.log(headers);
          console.log(res);

          this.httpClient
            .post(location.origin + '/backend/profiles/like', res, {
              headers,
            })
            .pipe(catchError(this.handleError))
            .subscribe({
              error: (error) => {
                this.isPending = false;
                console.log("Can't save!");
                reject({});
              },
              complete: () => {
                const objStore = this.db
                  .transaction(['likes'], 'readwrite')
                  .objectStore('likes');

                let range = IDBKeyRange.upperBound(this.toDate);
                objStore.delete(range);

                this.isPending = false;

                resolve({});
              },
            });
        });
      }
    });
  }

  async batch(): Promise<any> {
    if (this.isMockEnabled() || !this.db) {
      return Promise.resolve([]);
    }

    return new Promise<any>((resolve, reject) => {
      const objStore = this.db
        .transaction(['likes'], 'readonly')
        .objectStore('likes');

      let index = objStore.index('created');

      this.toDate = new Date();

      let result = new Array();

      let range = IDBKeyRange.upperBound(this.toDate);
      index.openCursor(range).onsuccess = (e) => {
        var cursor = e.target.result;
        if (cursor) {
          result.push(cursor.value['like']);
          cursor.continue();
        } else {
          resolve(result);
        }
      };
    });
  }

  public save(
    urlPart: string,
    body: any,
    params: HttpParams = new HttpParams()
  ) {
    const lastPart = urlPart.substr(urlPart.lastIndexOf('/') + 1);
    const uuidRegex =
      /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

    if (lastPart.match(uuidRegex) !== null) {
      return this.patch(urlPart, body, params);
    } else {
      return this.post(urlPart, body, params);
    }
  }

  public delete(
    urlPart: string,
    params: HttpParams = new HttpParams()
  ) {
    if (this.isMockEnabled()) {
      return this.mockApiService.delete(urlPart);
    }

    let headers = new HttpHeaders();
    headers = headers.append(AUTH_FIREBASE_HEADER, this.navService.token);

    return this.httpClient
      .delete(location.origin + '/backend' + urlPart, { headers, params })
      .pipe(catchError(this.handleError));
  }

  public post(
    urlPart: string,
    body: any,
    params: HttpParams = new HttpParams()
  ) {
    if (this.isMockEnabled()) {
      return this.mockApiService.post(urlPart, body, params);
    }

    let headers = new HttpHeaders();
    headers = headers.append(AUTH_FIREBASE_HEADER, this.navService.token);

    console.log(location.origin + '/backend' + urlPart);
    console.log("token" + headers);
    console.log("params" + params);

    return this.httpClient
      .post(location.origin + '/backend' + urlPart, body, { headers, params })
      .pipe(catchError(this.handleError));
  }

  public patch(
    urlPart: string,
    body: any,
    params: HttpParams = new HttpParams()
  ) {
    if (this.isMockEnabled()) {
      return this.mockApiService.patch(urlPart, body, params);
    }

    let headers = new HttpHeaders();
    headers = headers.append(AUTH_FIREBASE_HEADER, this.navService.token);

    return this.httpClient
      .patch(location.origin + '/backend' + urlPart, body, { headers, params })
      .pipe(catchError(this.handleError));
  }

  public upload(
    urlPart: string,
    body: any,
    params: HttpParams = new HttpParams()
  ) {
    if (this.isMockEnabled()) {
      return this.mockApiService.upload(urlPart);
    }

    let headers = new HttpHeaders();
    headers = headers.append(AUTH_FIREBASE_HEADER, this.navService.token);

    return this.httpClient
      .post(location.origin + '/backend/' + urlPart, body, {
        headers,
        params,
        reportProgress: true,
        observe: 'events',
      })
      .pipe(catchError(this.handleError));
  }

  handleError(error: HttpErrorResponse) {
    let errorMessage = 'Unknown error!';
    if (error.error instanceof ErrorEvent) {
      // Client-side errors
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side errors
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.log(errorMessage);
    return throwError(error.error);
  }

  private isMockEnabled(): boolean {
    return environment.mockUiData === true;
  }
}
