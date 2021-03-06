# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/yuhc/workspace/DATA/android/SDK/android-sdk-macosx2/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-ignorewarnings

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.support.v4.app.Fragment

#-keep class org.apache.log4j.**{*;}
-keep class org.apache.log4j{*;}
-keep class de.mindpipe.android.logging.log4j.**{*;}
-keep class com.j256.ormlite.**{*;}
-keep class com.loopj.android.http.**{*;}
-keep class org.apache.http.**{*;}
-keep class com.google.code.gson.**{*;}
-keep class com.centerm.epos.bean.**{*;}

-keep class com.centerm.smartpos.aidl.**{*;}
-keep class com.barcodejni.**{*;}
-keep class com.centerm.smartzbar.**{*;}
-keep class com.centerm.smartzbarlib.**{*;}
-keep class com.zbar.**{*;}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepclassmembers class com.centerm.cpay.payment.bean.**{
    <fields>;
    void set*(***);
    *** get*();
}

-keep class com.centerm.epos.transcation.pos.manager.**{ *; }
-keep class * implements com.centerm.epos.helper.IMenuHelper{*;}
-keep class * implements com.centerm.epos.base.ITradePresent{*;}
-keep class * implements com.centerm.epos.printer.IPrintRransData{*;}
-keep class * implements com.centerm.epos.printer.IPrinterCallBack{*;}
-keep class * implements com.centerm.epos.printer.PrintSaleData{*;}
-keep class * implements com.centerm.epos.transcation.pos.data.I8583Field{*;}
-keep class * implements com.centerm.epos.printer.IPrintSlipHelper{*;}
-keep class * implements com.centerm.epos.redevelop.IAppVersion{*;}
-keep class * implements com.centerm.epos.configure.ProjectRuntimeInit{*;}
-keep class * implements com.centerm.component.pay.mvp.presenter.IPayEntryPresenter{*;}
-keep class com.centerm.epos.common.TransCode{*;}
-keep class * implements com.centerm.epos.common.ITransCode{*;}
-keep class * implements com.centerm.epos.redevelop.IPullCardTip{*;}
-keep class * implements com.centerm.epos.redevelop.ISaveLogo{*;}
-keep class * implements com.centerm.epos.redevelop.ICommonManager{*;}
-keep class * implements com.centerm.epos.redevelop.IIsNeedReverse{*;}
-keep class * implements com.centerm.epos.redevelop.IOtherTransDatasInit{*;}
-keep class * implements com.centerm.epos.db.IProjectDBTable{*;}
-keep class * implements com.centerm.epos.transcation.pos.controller.ITradeUIController{*;}
-keep class * extends com.centerm.epos.base.BaseFragment
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

#-dontobfuscate
#-dontoptimize
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}