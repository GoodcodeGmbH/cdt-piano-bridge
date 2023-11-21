package com.pianosdkreactnative;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PianoSDKPackage implements ReactPackage {

    /**
     * @param reactContext {ReactApplicationContext} - react context.
     * @return {List<ViewManager>} - always empty list.
     */
    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     * @param reactContext {ReactApplicationContext} - react context.
     * @return {List<NativeModule>} - list of native modules to register.
     */
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        // create empty list of native modules
        List<NativeModule> modules = new ArrayList<>();

        // add new piano sdk module to native modules
        modules.add(new PianoSDKModule(reactContext));

        return modules;
    }
}