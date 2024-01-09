# myscoutee

#server folder

docker:
-------
gradle build -x test
docker-compose build --no-cache (build context)
docker-compose up

ng serve --ssl

docker dependencies:
--------------------

cd /home/raxim/workspace/myscoutee/server/docker/images/mosquitto/
docker build --no-cache -t eclipse-mosquitto:2.0.18-ext_auth .
docker build --no-cache -t myscoutee-nginx:0.1.0 .