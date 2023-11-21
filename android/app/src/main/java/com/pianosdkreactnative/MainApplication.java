package com.pianosdkreactnative;

import android.app.Application;

import com.facebook.react.BuildConfig;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultReactNativeHost;
import com.facebook.soloader.SoLoader;
import com.pianosdkreactnative.utilities.Constant;

import java.util.List;

public class MainApplication extends Application implements ReactApplication {

    private final ReactNativeHost reactNativeHost = new DefaultReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        /**
         * @return {List<ReactPackage>} - list contains piano sdk package.
         */
        @Override
        protected List<ReactPackage> getPackages() {
            // create list of packages
            List<ReactPackage> packages = new PackageList(this).getPackages();

            // add new piano sdk package to list
            packages.add(new PianoSDKPackage());

            return packages;
        }

        @Override
        protected String getJSMainModuleName() {
            return Constant.JS_MAIN_MODULE_NAME;
        }

        @Override
        protected boolean isNewArchEnabled() {
            return true;
        }

        @Override
        protected Boolean isHermesEnabled() {
            return true;
        }
    };

    // getter
    @Override
    public ReactNativeHost getReactNativeHost() {
        return this.reactNativeHost;
    }

    /**
     * Initialize so loader and react native flipper.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        SoLoader.init(this, /* native exopackage */ false);

        ReactNativeFlipper.initializeFlipper(this, this.getReactNativeHost().getReactInstanceManager());
    }
}
