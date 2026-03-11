import { NgModule } from '@angular/core';
import { MatBadgeModule } from '@angular/material/badge';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatDialogModule } from '@angular/material/dialog'; //fix from this
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';

const materialModules = [
  MatIconModule,
  MatToolbarModule,
  MatBadgeModule,
  MatDatepickerModule,
  MatNativeDateModule,
  MatExpansionModule,
  MatRippleModule,
  MatBottomSheetModule,
  MatDialogModule,
  MatSliderModule,
  MatSlideToggleModule,
  MatListModule,
  MatChipsModule,
  MatCardModule,
  MatSnackBarModule,
  MatFormFieldModule,
  MatRadioModule,
  MatSelectModule,
  MatButtonModule,
  MatProgressBarModule,
  MatInputModule,
  MatTabsModule,
];

@NgModule({
  imports: [...materialModules],
  exports: [...materialModules],
})
export class MaterialModule { }
