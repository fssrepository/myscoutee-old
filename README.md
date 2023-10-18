# myscoutee

HLS blue: 217/88/57

Time Setup on docker
RUN apk add --update busybox-suid && apk --update add tzdata && cp /usr/share/zoneinfo/Europe/Brussels /etc/localtime

User Settings import.gradle set JAVA 17

git config --global user.email {email}
git config --global user.name {name}
git config --global user.password {personal access token - Profile -> Settings -> Develpoper Settings (last menu item)}

./gradlew bootRun

gradle build
docker-compose build --no-cache (build context)
docker-compose up