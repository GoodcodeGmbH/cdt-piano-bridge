//
//  PianoSdkWrapper.swift
//  PianoSDKReactNative
//
//  Created by Admin on 23/05/23.
//

import Foundation
import PianoComposer
import PianoOAuth
import React

// NO_FLIPPER=1 pod install - to archive build for app store or testflight
// pod install - for testing

@objc(PianoSdkWrapper)
public class PianoSdkWrapper: NSObject {
  
  private var composer: PianoComposer?
  
  private var showLoginCallback: RCTResponseSenderBlock?
  private var showTemplateCallback: RCTResponseSenderBlock?
  
  
  // MARK: Init
  
  override init() {}
  
  
  // MARK: Functions
  
  @objc func `init`(_ aid: String, endpoint: String, facebookAppId: String?) {
    debugPrint("PianoSdkWrapper - init aid:\(aid) endpoint:\(endpoint) facebookAppId:\(facebookAppId ?? "")")
    
    PianoID.shared.endpointUrl = endpoint
    PianoID.shared.aid = "DtvhlLYXsu"
    PianoID.shared.signUpEnabled = true
    PianoID.shared.isSandbox = false
    
    composer = PianoComposer(aid: "DtvhlLYXsu")
      .endpointUrl(endpoint)
      .delegate(self)
  }
  
  @objc func signIn(_ callback: @escaping RCTResponseSenderBlock) {
    debugPrint("PianoSdkWrapper - signIn")
    
    PianoID.shared.widgetType = .login
    PianoID.shared.signIn { result, error, success in
      debugPrint("PianoSdkWrapper - signIn result:\(result.debugDescription) error:\(error.debugDescription) success:\(success)")
      self.onAccessToken(token: result?.token, error: error, callback: callback)
    }
  }
  
  @objc func register(_ callback: @escaping RCTResponseSenderBlock) {
    debugPrint("PianoSdkWrapper - register")
    
    PianoID.shared.widgetType = .register
    PianoID.shared.signIn { result, error, success in
      debugPrint("PianoSdkWrapper - signIn result:\(result.debugDescription) error:\(error.debugDescription) success:\(success)")
      self.onAccessToken(token: result?.token, error: error, callback: callback)
    }
  }
  
  @objc func signOut(_ accessToken: String, callback: @escaping RCTResponseSenderBlock) {
    debugPrint("PianoSdkWrapper - signOut accessToken:\(accessToken)")
    
    PianoID.shared.signOut(token: accessToken)
    
    callback([NSNull(), ["success": true]])
  }
  
  @objc func refreshToken(_ refreshToken: String, callback: @escaping RCTResponseSenderBlock) {
    debugPrint("PianoSdkWrapper - refreshToken refreshToken:\(refreshToken)")
    
    PianoID.shared.refreshToken(refreshToken) { token, error in
      debugPrint("PianoSdkWrapper - refreshToken token:\(token.debugDescription) error:\(error.debugDescription)")
      self.onAccessToken(token: token, error: error, callback: callback)
    }
  }
  
  @objc func setUserToken(_ accessToken: String) {
    debugPrint("PianoSdkWrapper - setUserToken accessToken:\(accessToken)")
    
    composer = composer?.userToken(accessToken)
  }
  
  @objc func setGaClientId(_ gaClientId: String) {
    debugPrint("PianoSdkWrapper - setGaClientId gaClientId:\(gaClientId)")
    
    PianoID.shared.googleClientId = gaClientId
    
    composer = composer?.gaClientId(gaClientId)
  }
  
  @objc func clearStoredData() {
    debugPrint("PianoSdkWrapper - clearStoredData")
    
    PianoComposer.clearStoredData()
  }
  
  @objc func getExperience(_ config: [String:Any],
                           showLoginCallback: @escaping RCTResponseSenderBlock,
                           showTemplateCallback: @escaping RCTResponseSenderBlock) {
    debugPrint("PianoSdkWrapper - getExperience config:\(config)")
    
    self.showLoginCallback = showLoginCallback
    self.showTemplateCallback = showTemplateCallback
    
    if let value = config["tag"] as? String {
      composer = composer?.tag(value)
    }
    
    if let value = config["tags"] as? [String] {
      composer = composer?.tags(value)
    }
    
    if let value = config["zone"] as? String {
      composer = composer?.zoneId(value)
    }
    
    if let value = config["referer"] as? String {
      composer = composer?.referrer(value)
    }
    
    if let value = config["url"] as? String {
      composer = composer?.url(value)
    }
    
    if let value = config["contentAuthor"] as? String {
      composer = composer?.contentAuthor(value)
    }
    
    if let value = config["contentCreated"] as? String {
      composer = composer?.contentCreated(value)
    }
    
    if let value = config["contentIsNative"] as? Bool {
      composer = composer?.contentIsNative(value)
    }
    
    if let value = config["contentSection"] as? String {
      composer = composer?.contentSection(value)
    }
    
    if let value = config["debug"] as? Bool {
      composer = composer?.debug(value)
    }
    
    if let customVariables = config["customVariables"] as? [String:Any] {
      for item in customVariables {
        if let value = item.value as? String {
          composer = composer?.customVariable(name: item.key, value: value)
        } else if let value = item.value as? [String] {
          composer = composer?.customVariable(name: item.key, array: value)
        }
      }
    }
    
    if let value = config["accessToken"] as? String {
      composer = composer?.userToken(value)
    }
    
    self.composer = composer?.execute()
  }
  
  
  // MARK: Helpers
  
  private func onAccessToken(token: PianoIDToken?, error: Error?, callback: @escaping RCTResponseSenderBlock) {
    guard error == nil else {
      callback([NSNull(), ["error": error!.localizedDescription]])
      return
    }
    
    guard let token = token else {
      callback([NSNull(), ["error": "unknown error"]])
      return
    }
    
    let resultDictionary: [String: Any] = [
      "accessToken": token.accessToken,
      "refreshToken": token.refreshToken,
      "expiresIn": String(token.expiresIn),
      "expiresInTimestamp": String(token.expirationDate.timeIntervalSince1970)
    ]
    
    callback([NSNull(), resultDictionary])
  }
  
  private func getParams(for event: XpEvent) -> [String:Any] {
    var resultDictionary: [String: Any] = [
      "eventName": "showLogin"
    ]
    
    if let eventModuleParams = event.eventModuleParams {
      let dict: [String: Any] = [
        "moduleId": eventModuleParams.moduleId,
        "moduleName": eventModuleParams.moduleName
      ]
      resultDictionary["eventModuleParams"] = dict
    }
    
    if let eventExecutionContext = event.eventExecutionContext {
      var dict: [String: Any] = [
        "experienceId": eventExecutionContext.experienceId,
        "executionId": eventExecutionContext.executionId,
        "trackingId": eventExecutionContext.trackingId,
        "splitTests": eventExecutionContext.splitTestEntries.map({ ["variantId": $0.splitTestVariantId, "variantName": $0.splitTestVariantName] }), //
        "currentMeterName": eventExecutionContext.currentMeterName,
        "region": eventExecutionContext.region,
        "countryCode": eventExecutionContext.countryCode,
        "accessList": eventExecutionContext.accessList.map({
          [
            "resourceId": $0.rid,
            "resourceName": $0.resourceName,
            "daysUntilExpiration": $0.daysUntilExpiration,
            "expireDate": $0.expireDate
          ] as [String : Any]
        }),
        "activeMeters": eventExecutionContext.activeMeters.map({
          [
            "meterName": $0.meterName,
            "views": $0.views,
            "viewsLeft": $0.viewsLeft,
            "maxViews": $0.maxViews,
            "totalViews": $0.totalViews
          ] as [String : Any]
        })
      ]
      if let user = eventExecutionContext.user {
        dict["user"] = [
          "userId": user.uid,
          "firstName": user.firstName,
          "lastName": user.lastName,
          "email": user.email
        ]
      }
      resultDictionary["eventExecutionContext"] = dict
    }

    return resultDictionary
  }
  
}

// - MARK: PianoComposerDelegate

extension PianoSdkWrapper: PianoComposerDelegate {
  
  @objc public func showLogin(composer: PianoComposer, event: XpEvent, params: ShowLoginEventParams?) {
    debugPrint("PianoSdkWrapper - showLogin")
    
    var resultDictionary: [String: Any] = getParams(for: event)

    var dict: [String: Any] = [:]
    
    if let value = params?.userProvider {
      dict["userProvider"] = value
    }
    
    resultDictionary["eventData"] = dict
    
    showLoginCallback?([NSNull(), resultDictionary])
  }
  
  /**
   Show template event
   */
  @objc public func showTemplate(composer: PianoComposer, event: XpEvent, params: ShowTemplateEventParams?) {
    debugPrint("PianoSdkWrapper - showTemplate")
    
    var resultDictionary: [String: Any] = getParams(for: event)

    var dict: [String: Any] = [:]
    
    if let value = params?.templateId {
      dict["templateId"] = value
    }
    
    if let value = params?.templateVariantId {
      dict["templateVariantId"] = value
    }
    
    if let value = params?.displayMode.description {
      dict["displayMode"] = value
    }
    
    if let value = params?.containerSelector {
      dict["containerSelector"] = value
    }
    
    if let value = params?.delayBy {
      let delayBy: [String:Any] = ["type": value.type, "value": value.value]
      dict["delayBy"] = delayBy
    }
    
    if let value = params?.showCloseButton {
      dict["showCloseButton"] = value
    }
    
    if let value = params?.templateUrl {
      dict["url"] = value
    }
    
    resultDictionary["eventData"] = dict
    
    showTemplateCallback?([NSNull(), resultDictionary])
  }
  
  /**
   Show form event
   */
  @objc public func showForm(composer: PianoComposer, event: XpEvent, params: ShowFormEventParams?) {
    debugPrint("PianoSdkWrapper - showForm")
  }
  
  /**
   Show recommendations event
   */
  @objc public func showRecommendations(composer: PianoComposer, event: XpEvent, params: ShowRecommendationsEventParams?) {
    debugPrint("PianoSdkWrapper - showRecommendations")
  }
  
  /**
   Set response variable event
   */
  @objc public func setResponseVariable(composer: PianoComposer, event: XpEvent, params: SetResponseVariableParams?) {
    debugPrint("PianoSdkWrapper - setResponseVariable")
  }
  
  /**
   Non site action event
   */
  @objc public func nonSite(composer: PianoComposer, event: XpEvent) {
    debugPrint("PianoSdkWrapper - nonSite")
  }
  
  /**
   User segment true event
   */
  @objc public func userSegmentTrue(composer: PianoComposer, event: XpEvent) {
    debugPrint("PianoSdkWrapper - userSegmentTrue")
  }
  
  /**
   User segment false event
   */
  @objc public func userSegmentFalse(composer: PianoComposer, event: XpEvent) {
    debugPrint("PianoSdkWrapper - userSegmentFalse")
  }
  
  /**
   Meter active event
   */
  @objc public func meterActive(composer: PianoComposer, event: XpEvent, params: PageViewMeterEventParams?) {
    debugPrint("PianoSdkWrapper - meterActive")
  }
  
  /**
   Meter expired event
   */
  @objc public func meterExpired(composer: PianoComposer, event: XpEvent, params: PageViewMeterEventParams?) {
    debugPrint("PianoSdkWrapper - meterExpired")
  }
  
  /**
   Exeperience execution failed
   */
  @objc public func experienceExecutionFailed(composer: PianoComposer, event: XpEvent, params: FailureEventParams?) {
    debugPrint("PianoSdkWrapper - experienceExecutionFailed")
  }
  
  /**
   Exeperience execute event
   */
  @objc public func experienceExecute(composer: PianoComposer, event: XpEvent, params: ExperienceExecuteEventParams?) {
    debugPrint("PianoSdkWrapper - experienceExecute")
  }
  
  /**
   Event fired by composer when async task was completed and all experience event fired
   */
  @objc public func composerExecutionCompleted(composer: PianoComposer) {
    debugPrint("PianoSdkWrapper - composerExecutionCompleted")
    
    let alert = UIAlertController(title: "Experience", message: "Composer Execution completed", preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
    UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil)

  }
  
}
