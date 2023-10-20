# myscoutee

HLS blue: 217/88/57

Time Setup on docker
RUN apk add --update busybox-suid && apk --update add tzdata && cp /usr/share/zoneinfo/Europe/Brussels /etc/localtime

User Settings import.gradle set JAVA 17

git config --global user.email {email}
git config --global user.name {name}
git config --global user.password {personal access token - Profile -> Settings -> Develpoper Settings (last menu item)}

./gradlew bootRun

gradle build -x test
docker-compose build --no-cache (build context)
docker-compose up

shift + fn + f5 (refresh page without cache)

ng serve --ssl (dummy https)

update angular:

npm i (install)

1) npm install -g n
2) n lts
3) n latest
4) n prune
5) npm install -g npm@latest

angular material 15 refactor -> css is wrong
https://rc.material.angular.io/guide/mdc-migration
