# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

##---------------Begin: common  ----------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontoptimize
-dontpreverify
-dontshrink
-verbose
-dontwarn
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-ignorewarnings
#把混淆类中的方法名也混淆了
-useuniqueclassmembernames

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification
-keeppackagenames doNotKeepAThing
-repackageclasses 'com.bestv'
#-repackageclasses com.repeat

-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,MethodParameters
-keep class *.R$ {
*;
}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService


-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    void on*(...);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keepclassmembers class * extends java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

 -keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keepclasseswithmembernames class * {
    native <methods>;
    public static final <fields>;
}

-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# 对于带有回调函数onXXEvent()的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
}
  -keepattributes *JavascriptInterface*
  #-------------- okhttp3 start-------------
  # OkHttp3
  # https://github.com/square/okhttp
  # okhttp
  -keepattributes Signature
  -keepattributes *Annotation*
  -keep class com.squareup.okhttp.* { *; }
  -keep interface com.squareup.okhttp.** { *; }
  -dontwarn com.squareup.okhttp.**

  # okhttp 3
  -keepattributes Signature
  -keepattributes *Annotation*
  -keep class okhttp3.** { *; }
  -keep interface okhttp3.** { *; }
  -dontwarn okhttp3.**

  # Okio
  -dontwarn com.squareup.**
  -dontwarn okio.**
  -keep public class org.codehaus.* { *; }
  -keep public class java.nio.* { *; }
  #----------okhttp end--------------

  #过滤glide
  #-keep public class * implements com.bumptech.glide.module.GlideModule
  #-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  #  **[] $VALUES;
  #  public *;
  #}
  -dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
  -keep public class * implements com.bumptech.glide.AppGlideModule
  -keep public class * extends com.bumptech.glide.AppGlideModule
  -keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
  }
  # 从glide4.0开始，GifDrawable没有提供getDecoder()方法，
  # 需要通过反射获取gifDecoder字段值，所以需要保持GifFrameLoader和GifState类不被混淆
  -keep class com.bumptech.glide.load.resource.gif.GifDrawable$GifState{*;}
  -keep class com.bumptech.glide.load.resource.gif.GifFrameLoader {*;}

  ##---------------end: common  ----------


  ##---------------Begin: project  ----------
  -keep class com.bestv.pgc.beans.** { *; }
  -keep class com.bestv.pgc.preloader.tool.model.** { *; }

    -keep class com.andview.refreshview.** { *; }

     -keep class com.bestv.pgc.util.BestvAgent{ *; }

          -keep class com.bestv.pgc.util.OnPariseListening{ *; }
