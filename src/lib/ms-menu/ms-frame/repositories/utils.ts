export const DEG = 180 / Math.PI;

export class Calc {
    public static distance2(p, point) {
        var dx = p.x - point.x,
            dy = p.y - point.y;
        return dx * dx + dy * dy;
    }

    public static getRotation(p1, p2) {
        var dx = p2.x - p1.x;
        var dy = p2.y - p1.y;
        return Math.atan2(dy, dx);
    }
}

export class DefaultDistributor {
    public static distribute(data, path, bound, value?) {
        let points = new Array();

        let totalLength = path.getTotalLength();

        let num = data.num;

        if (!data.values) {
            if (data.closed) {
                let delta = 0;

                if (data.delta !== 0)
                    delta = totalLength / 360 * data.delta; //there is a deviation at heart, it does not matter at circle

                for (let i = 0; i < num; i++) {
                    let length = delta + totalLength / num * i;

                    if (length > totalLength) {
                        length -= totalLength;
                    }

                    let pt = path.getPointAtLength(length);
                    pt.atLength = length;

                    if (value !== undefined) {
                        pt.value = Math.floor(length / totalLength * value);
                    }

                    let len2 = pt.atLength + (pt.atLength === length ? -0.1 : 0.1);
                    let rotation = Calc.getRotation(pt, path.getPointAtLength(len2));
                    pt.rotation = rotation;

                    pt.x = pt.x * bound.wRatio;
                    pt.y = pt.y * bound.hRatio;

                    //pt.id = "handle" + rand(1, 100) + "_" + i;
                    pt.id = "handle_" + i;
                    pt.txt = i;
                    pt.selected = false;
                    pt.picked = false;

                    points.push(pt);
                }
            } else {
                for (let i = 0; i < num; i++) {
                    let pt;
                    if (i > 0 && i < num - 1) {
                        let length = totalLength / (num - 1) * i;
                        pt = path.getPointAtLength(length);
                        pt.atLength = length;
                    } else if (i === 0) {
                        pt = path.getPointAtLength(0);
                        pt.atLength = 0;
                    }

                    if (pt) {
                        if (value !== undefined) {
                            pt.value = Math.floor(length / totalLength * value);
                        }

                        let len2 = pt.atLength + (pt.atLength === length ? -0.1 : 0.1);
                        let rotation = Calc.getRotation(pt, path.getPointAtLength(len2));
                        pt.rotation = rotation;

                        pt.x = pt.x * bound.wRatio;
                        pt.y = pt.y * bound.hRatio;
                        //pt.id = "handle" + rand(1, 100) + "_" + i;
                        pt.id = "handle_" + i;
                        pt.txt = i;
                        pt.selected = false;
                        pt.picked = false;

                        points.push(pt);
                    }
                }
            }
        } else {
            let num = data.values.length;
            for (let i = 0; i < num; i++) {
                let percent = data.values[i] / (data.limit.max - data.limit.min);
                let length = percent * totalLength;
                let pt = path.getPointAtLength(length);
                pt.atLength = length;

                if (value !== undefined) {
                    pt.value = Math.floor(length / totalLength * value);
                }

                let len2 = pt.atLength + (pt.atLength === length ? -0.1 : 0.1);
                let rotation = Calc.getRotation(pt, path.getPointAtLength(len2));
                pt.rotation = rotation;

                pt.x = pt.x * bound.wRatio;
                pt.y = pt.y * bound.hRatio;
                //pt.id = "handle" + rand(1, 100) + "_" + i;
                pt.id = "handle_" + i;
                pt.txt = i;
                pt.selected = false;
                pt.picked = false;

                points.push(pt);
            }
        }
        return points;
    }
}