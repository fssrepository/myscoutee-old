import { Renderer2 } from '@angular/core';

export class Transform {
  private _mtx: { scale; angle; pos: { x; y } };

  constructor(mtx = { scale: 1, angle: 0, pos: { x: 0, y: 0 } }) {
    this._mtx = mtx;
  }

  public scale(uiScale) {
    this._mtx.scale += uiScale;
    return this._mtx;
  }

  public move(uiDiff) {
    this._mtx.pos.x += uiDiff.x;
    this._mtx.pos.y += uiDiff.y;
    return this._mtx;
  }

  public rotate(deg) {
    this._mtx.angle += deg;
    return this._mtx;
  }

  get mtx() {
    return this._mtx;
  }
}

//tab/canvas zoom
export class Magnifier {
  private dragStart: { x; y };
  private dist;

  private touches: Array<any>;

  constructor(private element, private _renderer: Renderer2, private _transform: Transform) {
    this.touches = new Array();
  }

  listen(_callback) {
    this._renderer.listen(this.element, 'mousewheel', (event) => {
      event.preventDefault();

      let uiScale = Math.sign(event.wheelDelta) * 0.1;
      let uiOrigin = {
        x: event.pageX - this.element.offsetLeft,
        y: event.pageY - this.element.offsetTop,
      };

      let tr = this._transform.scale(uiScale);
      _callback(tr);
    });

    this._renderer.listen(this.element, 'touchstart', (event) => {
      this.updateTouch(event);

      if (this.touches[0] && this.touches[1]) {
      } else if (this.touches[0] || this.touches[1]) {
        let touch = event.targetTouches[0];
        if (this.dragStart === undefined) {
          this.dragStart = {
            x: touch.pageX - this.element.offsetLeft,
            y: touch.pageY - this.element.offsetTop,
          };
        }
      }
    });

    this._renderer.listen(this.element, 'touchmove', (event) => {
      this.updateTouch(event);

      if (this.touches[0] && this.touches[1]) {
        let touch1 = this.touches[0];
        let touch2 = this.touches[1];

        let start;
        let end;
        if (touch1.pageX >= touch2.pageX) {
          start = touch1;
          end = touch2;
        } else {
          start = touch2;
          end = touch1;
        }

        let diff = { x: end.pageX - start.pageX, y: end.pageY - start.pageY };
        let dist = Math.sqrt(diff.x * diff.x + diff.y * diff.y);

        if (this.dist === undefined) {
          this.dist = dist;
        }

        let uiScale = dist / this.dist - 1;
        this.dist = dist;

        let tr = this._transform.scale(uiScale);
        _callback(tr);
      } else if (this.touches[0] || this.touches[1]) {
        let touch = this.touches[0] || this.touches[1];

        if (this.dragStart !== undefined) {
          let dragEnd = {
            x: touch.pageX - this.element.offsetLeft,
            y: touch.pageY - this.element.offsetTop,
          };
          let uiDiff = {
            x: dragEnd.x - this.dragStart.x,
            y: dragEnd.y - this.dragStart.y,
          };
          this.dragStart = dragEnd;
          let tr = this._transform.move(uiDiff);
          _callback(tr);
        }
      }
    });

    this._renderer.listen(this.element, 'touchend', (event) => {
      for (let touch of event.changedTouches) {
        delete this.touches[touch.identifier];
      }

      if (this.touches[0] || this.touches[1]) {
        let touch = this.touches[0] || this.touches[1];
        this.dragStart = {
          x: touch.pageX - this.element.offsetLeft,
          y: touch.pageY - this.element.offsetTop,
        };
      } else {
        this.dragStart = undefined;
      }

      this.dist = undefined;
    });
  }

  public rotate(deg) {
    return this._transform.rotate(deg);
  }

  private updateTouch(event) {
    if (event.targetTouches.length === 1) {
      this.touches[event.targetTouches[0].identifier] = event.targetTouches[0];
    }

    if (event.targetTouches.length === 2) {
      this.touches[event.targetTouches[1].identifier] = event.targetTouches[1];
    }
  }

  get transform(): Transform {
    return this._transform;
  }
}
