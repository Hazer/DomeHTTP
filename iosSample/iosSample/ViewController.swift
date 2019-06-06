//
//  ViewController.swift
//  iosSample
//
//  Created by Vithorio Polten on 19/04/19.
//  Copyright © 2019 Vithorio Polten. All rights reserved.
//

import UIKit
import iosInterop
import PromiseKit
import DomeHTTP

extension ApiResult {
    func onSuccess<T>(callback: @escaping (T) -> Void) -> ApiResult {
        self.success { (wrapped) -> KotlinUnit in
            let value = wrapped as! T
            callback(value)
            return KotlinUnit()
        }
        return self
    }
}

class ViewController: UIViewController, OnResult, OnCompleted {
    
    
    
    @IBOutlet weak var lblTodoTitle: UILabel!
    
    @IBOutlet weak var lblCreatedTodoTitle: UILabel!
    
    func emitted(result: ApiResult) {
        print(result)
        
        result.onSuccess { (todo: Todo) in
            print(todo.title)
            self.lblTodoTitle.text = todo.title
        }.failure { throwable in
            print("Failed")
            print(throwable)
            return KotlinUnit()
        }
        
        result.success { res in
            let todo = res as! Todo
            
            print(todo.title)
            return KotlinUnit()
            
        }
    }
    
    func onSuccess(value: Any?) {
        print("Created todo: \(value)")
    }
    
    func onError(error: Error) {
        print("Failed creating todo: \(error)")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        let sample = Sample()
        
        let me = sample.checkMe()
        
        print(me)

//        print(sample.dome)
        
//        DispatchQueue.main.async {
        let api = ApiSample()
        api.asyncTest(onResult: self)
        
        api.createTodo(callback: self)


        
//        sample.createTodo(callback: )

//        sample.promiseTest()
//            .map { todoAny in
//                print("map \(todoAny)")
//                todoAny as! Todo
//            }.done { todo in
//                print("Done \(todo)")
//            }.catch { error in
//                print("error \(error)")
//        }
    
        print("Executed")
    }


}

