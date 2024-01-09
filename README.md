# myscoutee

Licence is BSD. (only one thing, i ask for, just notify me about your website, that my effort was not wasted)
It's benefitial to fork instead of clone to track down the variations.

I) The reason why i make this project public, as my country is not that supportive to create business,
but I strongly believe that project can make the world a better place and i don't want to waste years of work of mine.
(lots of refactoring, lots of technology upgrade, cleanup and many more etc., it was also up and running on domain myscoutee.com, and has promo videos under docs/videos)

I try to fix the UI for the time being to ease the understanding. Server side has lots of very detailed tests.
Naturally a project like that never can be finished, however MVP is almost finished with some issues (either on UI or server side).

II) I don't want to try to explain the whole functionality, but I tried to open a world for you about the possibilities:

1) The project aim is to create fairness into the dating world, which makes less exhaustive experience in both end.
   It's not a typical dating site, as even job site or any other site can use the algorithm and technic.
   (even in this project you can rate for events, not only for people)
   Instead of swipe, you rate people 1-10 and based on that you will be placed in a graph.
   (stronger connections are going to be grouped together)
   Based on priority each person is being assigned to a random event. (graph algorithm)
   After meeting with each other they can re-prioritize themselves, hence the priority graph will be improved. (AI)
   Business model is less dependent on subscription fees and more dependent on affiliate systems and individual event organizers. (e.g.: Booking.com)
   When you want to create an event, then you call people based on mutual priority instead of inviting individually. (who you met before on any random event -> hence you are not new to each other, or your level 1 connections)
2) Some menu has been commented out, as functionality needs to be fixed. (don't remove)
3) Some documentation is under docs folder and in the entity files.

Lots of refactoring has happened lately:
1) mysql/neo4j -> mongodb (to create the MVP faster)
2) kotlin -> java
3) mosquitto project has been customized
...

#server folder

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