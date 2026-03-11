export class Canvas {
  private ctx: CanvasRenderingContext2D;
  private center: { x: any; y: any };

  constructor(height = 600, width = 600) {
    const canvas = document.createElement('canvas');
    this.ctx = canvas.getContext('2d');
    this.ctx.canvas.height = height;
    this.ctx.canvas.width = width;
  }

  public load(img, mtx, quality = 1.0) {
    const ratioWidth = this.ctx.canvas.width / img.naturalWidth;
    const ratioHeight = this.ctx.canvas.height / img.naturalHeight;

    let currRatio: number;
    if (ratioWidth > ratioHeight) {
      currRatio = ratioHeight;
    } else {
      currRatio = ratioWidth;
    }

    this.center = {
      x: this.ctx.canvas.width / 2 - (img.naturalWidth * currRatio) / 2,
      y: this.ctx.canvas.height / 2 - (img.naturalHeight * currRatio) / 2,
    };

    let matrix = new DOMMatrix();

    matrix = matrix.scale(currRatio, currRatio);

    // moveTo
    let pt = new DOMPoint(
      this.center.x + mtx.pos.x,
      this.center.y + mtx.pos.y
    ).matrixTransform(matrix.inverse());

    matrix = matrix.translate(pt.x, pt.y);

    // scale
    pt = new DOMPoint(
      this.ctx.canvas.width / 2 + mtx.pos.x,
      this.ctx.canvas.height / 2 + mtx.pos.y
    ).matrixTransform(matrix.inverse());

    matrix = matrix
      .translate(pt.x, pt.y)
      .scale(mtx.scale)
      .translate(-pt.x, -pt.y);

    // rotate
    pt = new DOMPoint(
      this.ctx.canvas.width / 2 + mtx.pos.x,
      this.ctx.canvas.height / 2 + mtx.pos.y
    ).matrixTransform(matrix.inverse());

    matrix = matrix
      .translate(pt.x, pt.y)
      .rotate(mtx.angle)
      .translate(-pt.x, -pt.y);

    this.draw(img, matrix);

    return this.ctx.canvas.toDataURL('image/jpeg', quality);
  }

  private draw(img, matrix: DOMMatrix) {
    this.ctx.setTransform(matrix);
    // Clear the entire canvas
    const p1 = new DOMPoint(0, 0).matrixTransform(matrix.inverse());
    const p2 = new DOMPoint(
      this.ctx.canvas.width,
      this.ctx.canvas.height
    ).matrixTransform(matrix.inverse());

    this.ctx.clearRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);

    this.ctx.save();
    this.ctx.setTransform(1, 0, 0, 1, 0, 0);
    this.ctx.clearRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);
    this.ctx.restore();

    if (img) {
      this.ctx.drawImage(img, 0, 0);
    }
  }
}
