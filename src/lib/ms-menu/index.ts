import {
  Component,
  ElementRef,
  OnInit,
  QueryList,
  Renderer2,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { MsFrame } from './ms-frame';

export * from './ms-frame';

@Component({
  selector: 'ms-menu',
  templateUrl: './index.html',
})
export class MsMenu implements OnInit {
  @ViewChildren('hover') hovers: QueryList<ElementRef>;

  // @ViewChild("info") info: ElementRef;

  //@ViewChild("overlay") overlayRef: ElementRef;

  private selectedDiv;

  constructor(private _renderer: Renderer2) {}

  onEntered(div) {
    if (this.selectedDiv !== undefined && this.selectedDiv !== div) {
      let obj = this.hovers.find((hover) =>
        hover instanceof ElementRef
          ? hover.nativeElement.id === this.selectedDiv.id
          : hover['id'] === this.selectedDiv.id
      );
      if (obj && !(obj instanceof ElementRef)) {
        (<MsFrame>obj).highlighted = false;
      } else {
        this._renderer.removeClass(this.selectedDiv, 'f_hover_highlighted');
      }

      this.selectedDiv = undefined;
    }

    let classList = div !== undefined && div !== null && div.classList;
    if (classList.contains('f_hover')) {
      let obj = this.hovers.find((hover) =>
        hover instanceof ElementRef
          ? hover.nativeElement.id === div.id
          : hover['id'] === div.id
      );
      if (obj && !(obj instanceof ElementRef)) {
        (<MsFrame>obj).highlighted = true;
      } else {
        this._renderer.addClass(div, 'f_hover_highlighted');
      }

      this.selectedDiv = div;
    }
  }

  onLeaved() {
    if (this.selectedDiv !== undefined) {
      let obj = this.hovers.find((hover) =>
        hover instanceof ElementRef
          ? hover.nativeElement.id === this.selectedDiv.id
          : hover['id'] === this.selectedDiv.id
      );
      if (obj && !(obj instanceof ElementRef)) {
        (<MsFrame>obj).highlighted = false;
      } else {
        this._renderer.removeClass(this.selectedDiv, 'f_hover_highlighted');
      }

      this.selectedDiv = undefined;
    }
  }

  //style binding should be reduced, so style attribute should be generated in javascript unfortunatelly
  //https://css-tricks.com/transitions-only-after-page-load/
  //it might not be scale, maybe the whole thing willbe a translate up - like slide menu on android!!
  //cannot do [style.--translate], so css variable use has been removed
  //removing menu - full screen mode - the panel transform-origin: center center
  //https://css-tricks.com/almanac/properties/t/transform-origin/ - very nice example
  ngOnInit() {
    //it's not needed, as we are not moving out container until the mouseUp is not triggered
    /*this._renderer.listen(document, "touchmove", (event) => {
            event.stopPropagation();

            let handle = this.overlayRef.nativeElement.children[0];

            let touch = event.targetTouches[0];
            let translateStr = "translate(" + (touch.pageX - handle.clientWidth / 2) + "px," + (touch.pageY - handle.clientHeight / 2) + "px)";
            this._renderer.setStyle(handle, "transform", translateStr);
        });*/
  }
}
