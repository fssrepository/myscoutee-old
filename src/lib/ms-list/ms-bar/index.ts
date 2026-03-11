import {
    Component, EventEmitter, Input, OnDestroy, OnInit, Output
} from '@angular/core';
import { Router } from '@angular/router';
import { NavigationService } from 'src/app/navigation.service';
import { MqttService } from 'src/app/services/mqtt.service';
import * as uuid from 'uuid';

const DIFF_TYPING = 3000;

const PREFIX = "channels/pages"
const MAN_AVATAR_URLS = [
    'https://randomuser.me/api/portraits/men/11.jpg',
    'https://randomuser.me/api/portraits/men/23.jpg',
    'https://randomuser.me/api/portraits/men/32.jpg',
    'https://randomuser.me/api/portraits/men/41.jpg',
    'https://randomuser.me/api/portraits/men/52.jpg',
    'https://randomuser.me/api/portraits/men/55.jpg',
    'https://randomuser.me/api/portraits/men/66.jpg',
    'https://randomuser.me/api/portraits/men/71.jpg',
    'https://randomuser.me/api/portraits/men/76.jpg',
    'https://randomuser.me/api/portraits/men/82.jpg',
    'https://randomuser.me/api/portraits/men/87.jpg',
    'https://randomuser.me/api/portraits/men/94.jpg',
];
const WOMAN_AVATAR_URLS = [
    'https://randomuser.me/api/portraits/women/5.jpg',
    'https://randomuser.me/api/portraits/women/12.jpg',
    'https://randomuser.me/api/portraits/women/21.jpg',
    'https://randomuser.me/api/portraits/women/33.jpg',
    'https://randomuser.me/api/portraits/women/44.jpg',
    'https://randomuser.me/api/portraits/women/50.jpg',
    'https://randomuser.me/api/portraits/women/57.jpg',
    'https://randomuser.me/api/portraits/women/65.jpg',
    'https://randomuser.me/api/portraits/women/72.jpg',
    'https://randomuser.me/api/portraits/women/77.jpg',
    'https://randomuser.me/api/portraits/women/84.jpg',
    'https://randomuser.me/api/portraits/women/91.jpg',
];

@Component({
    selector: 'ms-bar',
    templateUrl: './index.html',
    styles: [':host { position: fixed !important; bottom: 0; width: 100%; height: 48px }']
})
export class MsBar implements OnInit, OnDestroy {
    @Input() alias: string;
    @Output() add: EventEmitter<any> = new EventEmitter();

    btnDisabled: boolean = true;
    lastTyping: number;
    message: string = "";

    url: string;

    constructor(
        private mqttService: MqttService,
        private navService: NavigationService,
        private router: Router
    ) {
        this.url = PREFIX + this.router.url;
    }

    ngOnDestroy() {
        this.mqttService.unregister(this.url);
    }

    ngOnInit() {
        this.mqttService.register(this.url, (msg) => {
            this.handleMsg(msg);
        });

        setInterval(() => {
            let now = new Date().getTime();
            if (this.lastTyping && (now - this.lastTyping) >= DIFF_TYPING) {
                this.lastTyping = undefined;
                this.typing(false);
            }
        }, DIFF_TYPING);
        /*let avatarsBottom = new Array();
        for (let i = 0; i < 12; i++) {
            avatarsBottom.push("assets/img/profiles/man1.jpg");
        }

        this.avatarsBottom = avatarsBottom;*/
    }

    private fallbackAvatar(profile): string {
        const gender = profile && profile['gender'] ? profile['gender'].toString().toLowerCase() : '';
        const pool = gender === 'w' || gender === 'f' ? WOMAN_AVATAR_URLS : MAN_AVATAR_URLS;

        const seed = profile && profile['key'] ? profile['key'].toString() : 'default-avatar';
        let hash = 0;
        for (let i = 0; i < seed.length; i++) {
            hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
        }

        return pool[hash % pool.length];
    }

    handleMsg(msg) {
        //console.log("msg arrived: " + msg);

        msg = msg.substring(0, msg.length - 1);

        let msgObj = JSON.parse(msg);
        const profile = this.navService.user ? this.navService.user['profile'] : undefined;

        if (msgObj.message.type === "p") {
            if (profile && msgObj.message.from === profile.key) {
                return;
            }

            let msgReadObj = JSON.parse(msg);
            msgReadObj.message.type = "r";
            msgReadObj.reads = new Array<string>();
            if (profile && profile['images'] && profile['images'][0]) {
                msgReadObj.reads.push(profile['images'][0]);
            } else if (profile) {
                msgReadObj.reads.push(this.fallbackAvatar(profile));
            }

            this.mqttService.publish(this.url, JSON.stringify(msgReadObj));
        } else if (msgObj.message.type === "r") {
            console.log("read");
            console.log(msgObj);
        }
        this.add.emit(msgObj);
    }

    send() {
        const profile = this.navService.user ? this.navService.user['profile'] : undefined;
        if (!profile) {
            return;
        }

        let fromImage = profile["images"] && profile["images"][0]
            ? profile["images"][0]
            : this.fallbackAvatar(profile);
        let payload = {
            from: fromImage, message: {
                type: "p", value: this.message, ref: uuid.v4(),
                key: uuid.v4(), from: profile.key
            }
        };
        this.add.emit(payload);

        this.mqttService.publish(this.url, JSON.stringify(payload));
        this.message = "";
        this.lastTyping = undefined;
        this.btnDisabled = true;
    }

    msgChange(msg) {
        this.message = msg;
        if (this.message !== "") {
            this.btnDisabled = false;
        }

        if (!this.lastTyping) {
            this.typing(true);
        }
        this.lastTyping = new Date().getTime();
    }

    typing(isTyping) {
        const profile = this.navService.user ? this.navService.user['profile'] : undefined;
        if (!profile) {
            return;
        }

        let fromImage = profile["images"] && profile["images"][0]
            ? profile["images"][0]
            : this.fallbackAvatar(profile);
        let payload = { from: fromImage, message: { type: "w", value: isTyping, from: profile.key } };

        this.mqttService.publish(this.url, JSON.stringify(payload));
    }

    isConfirmNeeded() {
        return false;
    }
}
