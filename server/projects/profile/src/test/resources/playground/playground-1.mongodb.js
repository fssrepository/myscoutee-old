/* global use, db */
// MongoDB Playground
// To disable this template go to Settings | MongoDB | Use Default Template For Playground.
// Make sure you are connected to enable completions and to be able to run a playground.
// Use Ctrl+Space inside a snippet or a string literal to trigger completions.
// The result of the last command run in a playground is shown on the results panel.
// By default the first 20 documents will be returned with a cursor.
// Use 'console.log()' to print to the debug output.
// For more documentation on playgrounds please refer to
// https://www.mongodb.com/docs/mongodb-vscode/playgrounds/

// Select the database to use.
use('myscoutee_db');

// Here we run an aggregation and open a cursor to the results.
// Use '.toArray()' to exhaust the cursor to return the whole result set.
// You can use '.hasNext()/.next()' to iterate through the cursor page by page.
db.getCollection('likes').aggregate([
    {
        "$match": {
            "$and": [
                {
                    "createdBy.$id": BinData(3, "qkqdQP7vzFQuTdpr1xojmg==")
                },
                {
                    "from.$id": BinData(3, "qkqdQP7vzFQuTdpr1xojmg==")
                },
                {
                    "to.$id": BinData(3, "Vklw1AyW0lvQi29sJuk/hA==")
                },
                {
                    "ref": null
                }
            ]
        }
    },
    {
        "$lookup": {
            "from": "likes",
            "localField": "cnt",
            "foreignField": "cnt",
            "as": "allLikes"
        }
    },
    {
        "$unwind": {
            "path": "$allLikes",
            "preserveNullAndEmptyArrays": true
        }
    },
    {
        "$group": {
            "_id": "$cnt",
            "likes": {
                "$push": "$$ROOT"
            }
        }
    }
]);
