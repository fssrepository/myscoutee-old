import {
  Component,
  ElementRef,
  Inject,
  OnInit,
  ViewChild
} from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA
} from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { HttpService } from 'src/app/services/http.service';
import { DataService } from 'src/app/services/data.service';
import { NavigationService } from 'src/app/navigation.service';
import { EditorComponent } from '../../editor/editor.component';

const AUDIO_LENGTH = 10000;
@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  formGroup: UntypedFormGroup;

  professions: any;
  physiques: any;
  religions: any;
  languages: any;
  statuses: any;

  images: any;

  progress: any = { mode: 'determine', value: 100, color: 'primary' };

  private url: any;

  private pos: any;

  isPlay = false;
  isRecord = false;
  voiceData;
  interval;

  recordPos = 0;
  playPos = 0;
  playLimit = AUDIO_LENGTH;

  voiceUrl: any;

  @ViewChild('voiceObj', { static: true }) voiceObj: ElementRef;

  constructor(
    private fb: UntypedFormBuilder,
    public dialogRef: MatDialogRef<ProfileComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialog: MatDialog,
    private dataService: DataService,
    private navService: NavigationService,
    private httpService: HttpService,
    private domSanitizer: DomSanitizer
  ) {
    this.professions = this.dataService.professions;
    this.physiques = this.dataService.physiques;
    this.religions = this.dataService.religions;
    this.languages = this.dataService.languages;
    this.statuses = this.dataService.statuses;

    this.data = data.value['profile'];
    this.url = data.url;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      data: this.fb.group({
        firstName: [this.data.firstName, Validators.required],
        gender: [this.data.gender],
        birthday: [new Date(this.data.birthday), [Validators.required]],
        smoker: [this.data.smoker],
        marital: [this.data.marital],
        hasChild: [this.data.hasChild],
        height: [this.data.height],
        physique: [this.data.physique],
        languages: [[...this.data.languages]],
        religion: [this.data.religion],
        profession: [this.data.profession],
        status: [this.data.status !== undefined ? this.data.status : 'I'],
      }),
      images: [
        [...this.data.images],
        [Validators.required, Validators.minLength(3)],
      ],
      voice: [this.data.voice],
    });

    this.images = (this.data.images as Array<string>).map(
      (image) =>
        location.origin + '/backend' + this.url + '/images/' + image['name']
    );

    this.voiceUrl = location.origin + '/backend' + this.url + '/voice/' + this.data.voice;
  }

  onHeight(evt): void {
    this.data.height = evt.value;
  }

  numSequence(n: number): Array<number> {
    return Array(n);
  }

  noCache(img) {
    return img + '?' + Date.now();
  }

  openDialog(idx) {
    const dialogRef = this.dialog.open(EditorComponent, {
      maxWidth: '100vw',
      maxHeight: '100vh',
      height: '100%',
      width: '100%',
      data: {
        idx,
        src:
          this.images[idx] !== undefined
            ? this.images[idx].split('?')[0] + '_orig'
            : this.images[idx],
        value: this.formGroup.controls['images'].value[idx],
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        const images = this.formGroup.controls['images'].value;
        images[result.idx] = { name: result.id, mtx: result.mtx };
        this.formGroup.controls['images'].setValue(images);
        this.data.images[result.idx] = images[result.idx];

        this.images = (this.data.images as Array<string>).map(
          (image) =>
            location.origin +
            '/backend' +
            this.url +
            '/images/' +
            image['name'] +
            '?' +
            Date.now()
        );
      }
    });
  }

  onClick(): void {
    if (this.formGroup.valid) {
      this.progress.mode = 'indeterminate';

      let raw = this.formGroup.getRawValue();
      raw.data.position = this.navService.pos;
      raw.data.images = raw.images;

      const formData = new FormData();

      const profile = new Blob([JSON.stringify(raw.data)], {
        type: 'application/json',
      });
      formData.append('profile', profile);

      if (this.voiceData !== undefined) {
        formData.append('voice', this.voiceData);
      }

      this.httpService.save(this.url, formData).subscribe({
        next: (value) => {
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

  record(): void {
    if (this.isRecord) {
      return;
    }
    // check/request permission on android
    navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then((stream) => {
        const chunks = [];
        /*var options = {
              audioBitsPerSecond : 128000,
              videoBitsPerSecond : 2500000,
              mimeType : 'video/mp4'
              }*/
        const recorder = new MediaRecorder(stream /*,options*/);

        recorder.ondataavailable = (e) => {
          chunks.push(e.data);

          if (recorder.state === 'inactive') {
            // ;codecs=h264 can be used to be able to play on ios
            const blob = new Blob(chunks, { type: 'audio/webm' });
            this.voiceData = blob;
            this.voiceUrl = this.domSanitizer.bypassSecurityTrustUrl(
              URL.createObjectURL(blob)
            );
          }
        };

        recorder.start();
        this.isRecord = true;

        this.interval = setInterval(() => {
          this.recordPos++;
        }, 500);

        setTimeout(() => {
          setTimeout(() => {
            this.recordPos = 0;
            this.isRecord = false;
          }, 500);

          clearInterval(this.interval);
          recorder.stop();
        }, AUDIO_LENGTH);
      })
      .catch(console.error);
  }

  play(): void {
    if (this.voiceObj === undefined || this.isPlay) {
      return;
    }

    this.isPlay = true;

    this.voiceObj.nativeElement['load']();

    const method = this.isPlay ? 'play' : 'pause';
    this.voiceObj.nativeElement[method]();
  }

  updateTime(evt): void {
    this.playPos = this.voiceObj.nativeElement.currentTime * 1000;
  }

  ended(evt): void {
    this.isPlay = false;
    this.playPos = 0;
  }
}
