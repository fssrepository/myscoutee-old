import { HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { Router } from '@angular/router';
import firebase from 'firebase/app';
import { NavigationService } from 'src/app/navigation.service';
import { HttpService } from 'src/app/services/http.service';
import { environment } from 'src/environments/environment';
import { MsDialog } from 'src/lib';

@Component({
  selector: 'app-settings',
  templateUrl: './setting.component.html',
})
export class SettingComponent implements OnInit {
  progress: any = { mode: 'determine', value: 0, color: 'primary' };

  url: any;

  formGroup: UntypedFormGroup;

  groups: any = [];

  selected;

  current;

  constructor(
    private router: Router,
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<SettingComponent>,
    public dialog: MatDialog,
    private httpService: HttpService,
    private navService: NavigationService
  ) {
    this.url = '/user';
  }

  ngOnInit(): void {
    const params = new HttpParams();

    this.httpService.get(this.url, params).subscribe({
      next: (data) => {
        this.init(data);
      },
      error: (error) => {
        this.progress.mode = 'determine';
        this.progress.color = 'warn';
      },
      complete: () => {
        console.log('complete');
      },
    });
  }

  onSelect(evt) {
    const idx = this.groups.findIndex((group) => group.value === evt.value);
    this.selected = this.groups[idx];
  }

  init(data) {
    this.current = data['user'].group;

    this.formGroup = this.fb.group({
      data: this.fb.group({
        key: [data['user'].group, Validators.required],
      }),
    });

    const groups = [];
    for (const item of data['groups']) {
      groups.push({
        value: item.group.key,
        viewValue: item.group.name,
        system: item.group.system,
      });
    }
    this.groups = groups;

    this.onSelect({ value: data['user'].group });
  }

  leave() {
    this.openConfirmation(
      'Would you like to leave the ' + this.selected.viewValue + ' group!'
    );
  }

  openConfirmation(msg) {
    const dialogRef = this.dialog.open(MsDialog, {
      data: {
        msg,
        btn: 'Yes',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        const params = new HttpParams();

        this.httpService
          .save(this.url + '/groups/' + this.selected.value + '/leave', params)
          .subscribe({
            next: (data) => {
              this.init(data);
            },
            error: (error) => {
              this.progress.mode = 'determine';
              this.progress.color = 'warn';
            },
            complete: () => {
              console.log('complete');
            },
          });
      }
    });
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      const raw = this.formGroup.getRawValue();

      const formData = new FormData();

      const groupForm = new Blob([JSON.stringify(raw.data)], {
        type: 'application/json',
      });
      formData.append('group', groupForm);

      this.httpService.save(this.url, formData).subscribe({
        next: (value) => {
          const group = value['user'].group;
          const groups = value['groups'];

          let groupType;
          for (const item of groups) {
            if (item.group.key === group) {
              groupType = environment.groupTypes[item.group.type];
              this.navService.groupName = item.group.name[0];
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
          }

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
    // stale value !!!
    this.dialogRef.close();
  }

  logout(): void {
    firebase
      .auth()
      .signOut()
      .then(() => {
        console.log('logout');
        this.navService.reset.next();
        this.back();
      })
      .catch((error) => {
        // An error happened.
      });
  }
}
