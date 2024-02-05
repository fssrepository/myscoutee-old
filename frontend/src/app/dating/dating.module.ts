import { CommonModule, DatePipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UIModule } from '../ui/ui.module';
import {
  BasketComponent,
  CarFormComponent,
  EditorComponent,
  EventComponent,
  EventFilterComponent,
  EventFormComponent,
  FeedbackFormComponent,
  GroupFilterComponent,
  GroupFormComponent,
  HelpComponent,
  PanelComponent,
  ProfileComponent,
  QrcodeComponent,
  RewardComponent,
  SettingComponent,
  SchoolComponent,
} from './components';
import { DatingRoutingModule } from './dating-routing.module';
@NgModule({
  declarations: [
    CarFormComponent,
    ProfileComponent,
    SettingComponent,
    EventFormComponent,
    EventFilterComponent,
    GroupFilterComponent,
    PanelComponent,
    EditorComponent,
    BasketComponent,
    EventComponent,
    FeedbackFormComponent,
    QrcodeComponent,
    HelpComponent,
    GroupFormComponent,
    RewardComponent,
    SchoolComponent,
  ],
  imports: [
    UIModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DatingRoutingModule,
  ],
  providers: [DatePipe],
})
export class DatingModule {}
