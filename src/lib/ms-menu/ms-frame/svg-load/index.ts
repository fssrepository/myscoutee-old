import { Component, ElementRef, OnInit, Renderer2, ViewChild, ChangeDetectorRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

//http://www.petercollingridge.co.uk/tutorials/svg/interactive/javascript/

@Component({
    selector: 'svg-load',
    templateUrl: './index.html'
})
export class SvgLoad implements OnInit {

    //<object id="svg-object" data="path/to/external.svg" type="image/svg+xml"></object>
    //preload when the page was loading, make opacity and and show, when javascript changed the attributes

    svg_url;
    id = "path_123";
    rs_url;

    @ViewChild("container") container: ElementRef;
    @ViewChild("svgObj") svgObjRef: ElementRef;

    constructor(private _renderer: Renderer2, private sanitizer: DomSanitizer, private cd: ChangeDetectorRef) {

    }

    sanitize(url) {
        return this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }

    ngOnInit() {
        this.svg_url = "assets/img/heart.svg";
        this.rs_url = this.sanitize(this.svg_url);

        /*let xhr = new XMLHttpRequest();
        xhr.open("GET", this.svg_url);
        xhr.overrideMimeType("image/svg+xml");
        xhr.send("");

        xhr.onreadystatechange = () => {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    let doc = xhr.responseXML.documentElement;
                    this.appendStroke(doc);
                    this._renderer.appendChild(this.container.nativeElement, doc);
                }
            }
        }*/
    }

    onload(event) {
        let target = event === undefined ? {} : event.srcElement || event.target;
        this.appendStroke(target.contentDocument.documentElement);
    }

    appendStroke(doc: HTMLElement) {
        let strokeWidth = 16;
        let linkNS = "http://www.w3.org/1999/xlink";
        let svgNS = "http://www.w3.org/2000/svg";

        let width = parseInt(doc.getAttribute("width"));
        let height = parseInt(doc.getAttribute("height"));
        doc.setAttribute("xmlns:xlink", linkNS);
        doc.setAttribute("width", "100%");
        doc.setAttribute("height", "100%");
        doc.setAttribute("viewBox", "0 0 " + (width + strokeWidth) + " " + height);
        doc.setAttribute("preserveAspectRatio", "none");

        let path = doc.getElementsByTagName("path")[0];
        
        if(path === undefined) return;

        path.setAttribute("fill", "none");
        path.setAttribute("id", this.id);

        let g = doc.getElementsByTagName("g")[0];
        g.setAttribute("transform", "translate(" + strokeWidth + " 0)");

        let usePath = document.createElementNS(svgNS, 'use');

        //href has different namespace in use
        usePath.setAttributeNS(linkNS, "xlink:href", "#" + this.id);
        usePath.setAttribute("stroke-width", "1");
        usePath.setAttribute("stroke", "black");
        g.append(usePath);

        usePath = document.createElementNS(svgNS, 'use');
        usePath.setAttributeNS(linkNS, "xlink:href", "#" + this.id);
        usePath.setAttribute("stroke-width", strokeWidth.toString());
        usePath.setAttribute("stroke", "#387ef5");
        usePath.setAttribute("stroke-dashoffset", "100");
        usePath.setAttribute("stroke-dasharray", "1790");
        g.append(usePath);
    }
}