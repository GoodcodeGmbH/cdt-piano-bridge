//
//  PianoSdkModule.m
//  PianoSDKReactNative
//
//  Created by Romal on 23/08/24.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(PianoSDKModule, NSObject)

// MARK: Init
RCT_EXTERN_METHOD(init:(NSString *)aid
                  endpoint:(NSString *)endpoint
                  facebookAppId:(NSString *)facebookAppId)

// MARK: Functions
RCT_EXTERN_METHOD(signIn:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(register:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(signOut:(NSString *)accessToken
                  callback:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(refreshToken:(NSString *)refreshToken
                  callback:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(setUserToken:(NSString *)accessToken)

RCT_EXTERN_METHOD(setGaClientId:(NSString *)gaClientId)

RCT_EXTERN_METHOD(clearStoredData)

RCT_EXTERN_METHOD(getExperience:(NSDictionary *)config
                  showLoginCallback:(RCTResponseSenderBlock)showLoginCallback
                  showTemplateCallback:(RCTResponseSenderBlock)showTemplateCallback)

@end

