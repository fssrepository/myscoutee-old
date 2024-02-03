* Swiping (e.g.: Tinder) or traditional dating websites are very exhausting and non-fair. Intead of swipes you can rate/prioritise people 1-10 to decide, who you would like to met first. You can rate for pairs also to help others.
* Based on mutual priority each person is being assigned to a group chat, where the participants can decide how they would like to met with each other. (graph algorithm) They can organise event for themselves or just choose from a promoted/organised one.
* If you have met someone already, you can invite him/her to an event based on mutual priorities too. (organise or choose from promoted ones) Multi-stage/multi-slot events are also possible. (e.g.: FIFA like competition)
* Mutual priorities can be changed at any moment, hence the social graph can be improved. (similar to AI reinforced training)
* Group dating allows to shift business model towards affiliate systems (e.g.: Booking.com) from subscription.
* The algorithm can be used even on job site. (rate on jobs or ideas etc.)

https://github.com/fssrepository/myscoutee/assets/45578746/8d0b41d7-0da9-475d-9202-fc5698442534

https://github.com/fssrepository/myscoutee/assets/45578746/e3e2323b-ca66-48eb-90b4-ed6287b51016

Contribution:
-------------
* MVP readiness with some issues - just notify me about your website that my effort is not wasted.
* You need to have a firebase project and configure it properly in the repo. (cloud messaging + web app)

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
Preliminarly it was kotlin, mysql/neo4j and ionic based (own android and ios plugins), which has been transformed to java, mongodb, pwa to speed up the development.
