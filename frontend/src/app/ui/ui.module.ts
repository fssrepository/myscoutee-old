import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import {
  MsEditor,
  MsList,
  MsPanel,
  MsChat,
  MsQrcode,
  MsAction,
  MsCalendar,
  MsDialog,
  MsMenu,
  MsFrame,
  SvgLoad
} from '../../lib';
import { MaterialModule } from './material.module';

const msComponents = [
  MsEditor,
  MsList,
  MsPanel,
  MsChat,
  MsQrcode,
  MsAction,
  MsCalendar,
  MsDialog,
  MsMenu,
  MsFrame,
  SvgLoad
  // MsContainer,
  // https://stackoverflow.com/questions/39112891/angular-2-rc5-router-outlet-inside-another-router-outlet
];
@NgModule({
  declarations: [...msComponents],
  imports: [CommonModule, MaterialModule],
  exports: [...msComponents, MaterialModule],
})
export class UIModule {}
