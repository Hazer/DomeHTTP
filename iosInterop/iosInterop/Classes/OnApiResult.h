//
//  OnApiResult.h
//  iosInterop
//
//  Created by Vithorio Polten on 17/05/19.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface OnApiResult<Result> : NSObject

- (void)onResult:(Result)result;

@end

NS_ASSUME_NONNULL_END
