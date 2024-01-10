# myscoutee

The project's aim is to create fairness into the dating world, which makes less exhaustive experience in both end.
   
Instead of swipe, you rate people 1-10 and based on that you will be placed in a graph.
(stronger connections are going to be grouped together)
   
Based on mutual priority each person is being assigned to a generated group event with chat. (graph algorithm)
   
After meeting with each other they can re-prioritize each other, hence the priority graph will be improved. (AI)
   
Business model is less dependent on subscription fees and more dependent on affiliate systems and individual event organizers. (e.g.: Booking.com)
   
When you want to create an event, then you can invite people based on mutual priority next to the manual invitation. (who you met before on any random event -> hence you are not new to each other, or your level 1 connections)
   
The algorithm can be used even on job site. (rate on jobs or ideas etc.)

https://github.com/fssrepository/myscoutee/assets/45578746/8d0b41d7-0da9-475d-9202-fc5698442534

https://github.com/fssrepository/myscoutee/assets/45578746/e3e2323b-ca66-48eb-90b4-ed6287b51016


##### MVP readiness with some issues. (trying to fix for the time being, mostly on UI to ease the understanding)
##### As i'm unable to continue based on my tricky situation, pls. try to fork instead of clone!
##### Just notify me about your website to let me know that my effort is not wasted.
##### Some menu has been commented out. (don't remove, just needs fixing)

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
