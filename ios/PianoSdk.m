//
//  PianoSDKModule.m
//  PianoSDKReactNative
//
//  Created by Admin on 23/05/23.
//

//#import "PianoSdk.h"

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(PianoSdk, PianoSdkWrapper, NSObject)
  RCT_EXTERN_METHOD(init:(NSString *)aid endpoint:(NSString *)endpoint facebookAppId:(NSString *)facebookAppId)
  RCT_EXTERN_METHOD(signIn:(RCTResponseSenderBlock)handler)
  RCT_EXTERN_METHOD(register:(RCTResponseSenderBlock)handler)
  RCT_EXTERN_METHOD(signOut:(NSString *)accessToken callback:(RCTResponseSenderBlock)callback)
  RCT_EXTERN_METHOD(refreshToken:(NSString *)refreshToken callback:(RCTResponseSenderBlock)callback)
  RCT_EXTERN_METHOD(setUserToken:(NSString *)accessToken)
  RCT_EXTERN_METHOD(setGaClientId:(NSString *)gaClientId)
  RCT_EXTERN_METHOD(clearStoredData)
  RCT_EXTERN_METHOD(getExperience:(NSDictionary<NSString *,id> * _Nonnull)config showLoginCallback:(RCTResponseSenderBlock)showLoginCallback showTemplateCallback:(RCTResponseSenderBlock)showTemplateCallback)
@end

//@implementation PianoSdk
/*
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(init:(NSString *)aid endpoint:(NSString *)endpoint facebookAppId:(NSString *)facebookAppId)
{
  [[PianoSdkWrapper shared] startWithAid:aid endpoint:endpoint facebookAppId:facebookAppId];
}

RCT_EXPORT_METHOD(signIn:(RCTResponseSenderBlock)handler)
{
  [[PianoSdkWrapper shared] signInCallback:handler];
}

RCT_EXPORT_METHOD(register:(RCTResponseSenderBlock)handler)
{
  [[PianoSdkWrapper shared] register];
}

RCT_EXPORT_METHOD(signOut:(NSString *)accessToken)
{
  [[PianoSdkWrapper shared] signOutWithAccessToken:accessToken];
}

RCT_EXPORT_METHOD(refreshToken:(NSString *)refreshToken)
{
  [[PianoSdkWrapper shared] refreshTokenWithRefreshToken:refreshToken];
}

RCT_EXPORT_METHOD(setUserToken:(NSString *)accessToken)
{
  [[PianoSdkWrapper shared] setUserTokenWithAccessToken:accessToken];
}

RCT_EXPORT_METHOD(setGaClientId:(NSString *)gaClientId)
{
  [[PianoSdkWrapper shared] setGaClientIdWithGaClientId:gaClientId];
}

RCT_EXPORT_METHOD(clearStoredData)
{
  [[PianoSdkWrapper shared] clearStoredData];
}

RCT_EXPORT_METHOD(getExperience:(NSDictionary<NSString *,id> * _Nonnull)config showLoginCallback:(RCTResponseSenderBlock)showLoginCallback showTemplateCallback:(RCTResponseSenderBlock)showTemplateCallback)
{
  [[PianoSdkWrapper shared] getExperienceWithConfig: config];
  //[[PianoSdkWrapper shared] getExperienceWithConfig: config showLoginCallback:showLoginCallback showTemplateCallback:showTemplateCallback];
}

RCT_EXPORT_METHOD(sampleMethod:(NSString *)stringArgument numberParameter:(nonnull NSNumber *)numberArgument callback:(RCTResponseSenderBlock)callback)
{
    // TODO: Implement some actually useful functionality
    callback(@[[NSString stringWithFormat: @"numberArgument: %@ stringArgument: %@", numberArgument, stringArgument]]);
}
*/
//@end
