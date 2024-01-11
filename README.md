# myscoutee

Swiping (e.g.: Tinder) or traditional dating websites are very exhausting and non-fair. Intead of swipes you can rate/prioritise people 1-10 to decide, who you would like to met first. You can rate for pairs also to help others.
   
Based on mutual priority each person is being assigned to a generated group event with chat. (graph algorithm)
   
Mutual priorities can be changed at any moment, hence the social graph can be improved. (similar to AI reinforced training)
   
Group dating allows to shift business model towards affiliate systems (e.g.: Booking.com) from subscription.

You can invite people to an event based on mutual priorities too and even choose events from organisers.
   
The algorithm can be used even on job site. (rate on jobs or ideas etc.)

https://github.com/fssrepository/myscoutee/assets/45578746/8d0b41d7-0da9-475d-9202-fc5698442534

https://github.com/fssrepository/myscoutee/assets/45578746/e3e2323b-ca66-48eb-90b4-ed6287b51016


##### MVP readiness with some issues. (some menu has been commented out - don't remove, where the functionality does exist in the backend)
##### As i'm unable to continue based on my tricky situation, pls. try to fork instead of clone!
##### Just notify me about your website to let me know that my effort is not wasted.

firebase:
---------

You need to have a firebase project. (cloud messaging + web app)

docker:
-------
cd ./server

gradle build -x test

docker-compose build --no-cache (build context)

docker-compose up


cd ./frontend

ng serve --ssl

docker dependencies:
--------------------

cd /home/raxim/workspace/myscoutee/server/docker/images/mosquitto/

docker build --no-cache -t eclipse-mosquitto:2.0.18-ext_auth .

docker build --no-cache -t myscoutee-nginx:0.1.0 .

Other:
------

Preliminarly it was kotlin, mysql/neo4j and ionic based (own android and ios plugins), which has been transformed to java, mongodb, pwa.
