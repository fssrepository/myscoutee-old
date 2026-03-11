import { Nearby } from "./iface";
import { Calc, DEG } from "./utils";

//https://github.com/rveciana/svg-path-properties/

export class Interpolate implements Nearby {
    private path;

    constructor(path) {
        this.path = path;
    }

    public nearby(idx, point, bound) : any {
        let pathLength = this.path.getTotalLength();
        var precision = 8,
            best,
            bestLength,
            bestDistance = Infinity;

        // linear scan for coarse approximation
        for (var scan, scanLength = 0, scanDistance; scanLength <= pathLength; scanLength += precision) {
            if ((scanDistance = Calc.distance2(scan = this.path.getPointAtLength(scanLength), point)) < bestDistance) {
                best = scan, bestLength = scanLength, bestDistance = scanDistance;
            }
        }

        // binary search for precise estimate
        precision /= 2;
        while (precision > 0.5) {
            var before,
                after,
                beforeLength,
                afterLength,
                beforeDistance,
                afterDistance;
            if ((beforeLength = bestLength - precision) >= 0 && (beforeDistance = Calc.distance2(before = this.path.getPointAtLength(beforeLength), point)) < bestDistance) {
                best = before, bestLength = beforeLength, bestDistance = beforeDistance;
            } else if ((afterLength = bestLength + precision) <= pathLength && (afterDistance = Calc.distance2(after = this.path.getPointAtLength(afterLength), point)) < bestDistance) {
                best = after, bestLength = afterLength, bestDistance = afterDistance;
            } else {
                precision /= 2;
            }
        }

        var len2 = bestLength + (bestLength === pathLength ? -0.1 : 0.1);
        var rotation = Calc.getRotation(best, this.path.getPointAtLength(len2));

        return {
            point: best,
            rotation: rotation * DEG,
            // distance: Math.sqrt(bestDistance),
        };
    }
}