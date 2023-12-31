curl -X POST \
-H "Authorization: key=AAAAOVoCJhA:APA91bFGJh7PRYmlhJXp-OddfxZEsVD0eUx5_gqzGj57J2ysomNKFx7WXy-wj8LcZXCkwJ5r3MfcNGfDdK0JSnAuEX_7NowPier9lJpAUAn7QoPyhH4jsg031vfJpq2xG-MzXUvylJQQ" \
-H "Content-Type: application/json" \
-d '{ "notification": {
    "title": "ddddd",
    "body": "Baeeee"
  },
  "to" : "/topics/alert"
}' \
https://fcm.googleapis.com/fcm/send