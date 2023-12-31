//
//  ConverterUtils.swift
//  App
//
//  Created by zetor on 2018. 07. 03..
//

import Foundation
import UIKit

public class ConvertUtils {
    public static func convert(bound:[String:Double]!, zoomScale : CGFloat, deviation : CGFloat = 0.0) -> CGRect? {
        let x = CGFloat(bound["left"]!) * CGFloat(zoomScale);
        let y = CGFloat(bound["top"]!) * CGFloat(zoomScale) + deviation;
        let width = CGFloat(bound["right"]!) * CGFloat(zoomScale) - x;
        let height = CGFloat(bound["bottom"]!) * CGFloat(zoomScale) - y;
        return CGRect(x:x, y:y, width:width, height: height);
    }
}
