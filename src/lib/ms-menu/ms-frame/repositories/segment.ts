import { Nearby } from "./iface";
import { DefaultDistributor, Calc, DEG } from "./utils";

export class Segment implements Nearby {
    public handles: Array<any>;

    private positions: Array<any>;

    private path;
    private points: Array<{ x: number, y: number, out: boolean }>;
    private bound: { rect, wRatio, hRatio };

    get _path() {
        return this.path;
    }

    get _bound() {
        return this.bound;
    }

    set _bound(bound) {
        this.bound = bound;
    }

    get _points() {
        return this.points;
    }

    constructor(path, data, bound) {
        this.path = path;
        this.bound = bound;

        let segmentData = JSON.parse(JSON.stringify(data));
        segmentData.num = data.limit.max - data.limit.min;
        segmentData.values = undefined;

        //distribution points and handles are not separate
        //handles should be distributed along points!!!
        this.points = DefaultDistributor.distribute(segmentData, this.path, bound);

        this.handles = DefaultDistributor.distribute(data, this.path, bound, segmentData.num);

        for (let i = 0; i < this.handles.length; i++) {
            this.handles[i].x = this.points[this.handles[i].value].x;
            this.handles[i].y = this.points[this.handles[i].value].y;
        }

        if (data.values !== undefined) {
            this.positions = data.values;
        } else {
            this.positions = [];
        }
    }

    update(bound) {

    }

    public nearby(idx, point, handleSize): any {
        let points = new Array<{ idx, point }>();

        let svgX = point.x - this.bound.rect.left;
        let svgY = point.y - this.bound.rect.top;

        let svgPoint = { x: svgX, y: svgY };

        let pointIdx;
        let minDistance = Infinity;
        for (let i = 0; i < this.points.length; i++) {
            let distance = Calc.distance2(this.points[i], svgPoint);
            if (minDistance > distance) {
                minDistance = distance;
                pointIdx = i;
            }
        };

        this.handles[idx] = this.points[pointIdx];
        this.positions[idx] = pointIdx;

        points.push({ idx: idx, point: this.handles[idx] });

        for (let i = 0; i < idx; i++) {
            if (this.positions[i] >= this.positions[idx]) {
                this.handles[i] = this.handles[idx];
                this.positions[i] = this.positions[idx];

                points.push({ idx: i, point: this.handles[i] });
            }
        }

        for (let i = idx + 1; i < this.handles.length; i++) {
            if (this.positions[i] <= this.positions[idx]) {
                this.handles[i] = this.handles[idx];
                this.positions[i] = this.positions[idx];

                points.push({ idx: i, point: this.handles[i] });
            }
        }

        return points;
    }
}