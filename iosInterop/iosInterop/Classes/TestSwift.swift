//
//  TestSwift.swift
//  iosInterop
//
//  Created by Vithorio Polten on 17/05/19.
//

import Foundation

@objc public protocol MyTestProtocol {
    var onSuccess: (Any?) -> () { get set }
    var onError: (Error?) -> () { get set }
}

@objc public protocol OnCompleted {
    func onSuccess(value: Any?)
    func onError(error: Error)
}
