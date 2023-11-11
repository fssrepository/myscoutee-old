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

cd /home/raxim/workspace/myscoutee/server/docker/images/mosquitto/
docker build -t eclipse-mosquitto:2.0.18-ext_auth .

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

export declare class FacebookModule {
    static forRoot(): ModuleWithProviders<any>;
}

Debugging mosquitto:

1) download mosquitto source and copy over backup/mosquitto-debug/.vscode
2) open /home/raxim/workspace/myscoutee/server/docker/images/mosquitto/plugins/mosquitto_external_auth
3) set the target to mosquitto
4) CMake Debug (debug menu) -> launch.json (there is a Make Debug also)
5) settings.json shold contain "C_Cpp.default.configurationProvider": "ms-vscode.cmake-tools", (C/C++ intellisense fix)
6) to bild export CC=gcc (even for nginx)
7) gdb command line -> target extended-remote localhost:8888 (don't shutdown gdb server)

Remote Debug:

1) share the src folder from ubuntu to the docker and run mosquitto build on it
2) the generated mosquitto executable should be attached by the gdbserver (can beoutside of the container seems also)
   start.sh contains the command (the ubuntu is using libc, while alpine linux is using musl-libc which are not compatible)
3) Attach (gdb) -> launch.json