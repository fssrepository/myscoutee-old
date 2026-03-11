export function monthlyTable(jsDate: Date, data?: Array<string>) {
    let from = new Date(jsDate.getFullYear(), jsDate.getMonth(), 1);
    let to = new Date(jsDate.getFullYear(), jsDate.getMonth() + 1, 0);

    let fromDayIdx = from.getDay() || 7;
    let toDayIdx = to.getDay() || 7;

    let weekNum = Math.ceil((fromDayIdx - 1 + to.getDate()) / 7);

    let rows = new Array<Array<any>>();

    let dayNum = 1;
    for (let i = 0; i < weekNum; i++) {
        rows[i] = new Array<any>();
        if (i === 0) {
            for (let j = fromDayIdx - 1; j < 7; j++) {
                let day = new Date(from.getFullYear(), from.getMonth(), dayNum);
                rows[i][j] = { title: dayNum, date: day.getTime() };
                if (data) {
                    rows[i][j].value = data[day.getTime()];
                }
                dayNum++;
            }
        } else if (i === weekNum - 1) {
            for (let j = 0; j < toDayIdx; j++) {
                let day = new Date(from.getFullYear(), from.getMonth(), dayNum);
                rows[i][j] = { title: dayNum, date: day.getTime() };
                if (data) {
                    rows[i][j].value = data[day.getTime()];
                }
                dayNum++;
            }

            for (let j = toDayIdx; j < 7; j++) {
                rows[i][j] = undefined;
            }
        } else {
            for (let j = 0; j < 7; j++) {
                let day = new Date(from.getFullYear(), from.getMonth(), dayNum);
                rows[i][j] = { title: dayNum, date: day.getTime() };
                if (data) {
                    rows[i][j].value = data[day.getTime()];
                }
                dayNum++;
            }
        }
    }

    return rows;
}