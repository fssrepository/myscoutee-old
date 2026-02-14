import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MockApiService {
  get(urlPart: string, params: HttpParams = new HttpParams()): Observable<any> {
    return of({ ok: false, mockDisabled: true, url: urlPart });
  }

  post(urlPart: string, body?: any, params: HttpParams = new HttpParams()): Observable<any> {
    return of({ ok: false, mockDisabled: true, url: urlPart, data: body });
  }

  patch(urlPart: string, body?: any, params: HttpParams = new HttpParams()): Observable<any> {
    return of({ ok: false, mockDisabled: true, url: urlPart, data: body });
  }

  save(urlPart: string, body?: any, params: HttpParams = new HttpParams()): Observable<any> {
    return of({ ok: false, mockDisabled: true, url: urlPart, data: body });
  }

  delete(urlPart: string): Observable<any> {
    return of({ ok: false, mockDisabled: true, url: urlPart });
  }

  upload(urlPart: string): Observable<any> {
    return of({ ok: false, mockDisabled: true, url: urlPart });
  }
}
