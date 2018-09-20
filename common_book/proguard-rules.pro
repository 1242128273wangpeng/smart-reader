# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in H:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-dontwarn org.apache.**
-keep class org.apache.** {*; }
-ignorewarnings                # ���ƾ���
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.quduquxie.bean.** { *; }

-keep class net.lzbook.kit.data.search.**{*;}
-keep class net.lzbook.kit.data.update.**{*;}
-keep class net.lzbook.kit.data.recommend.**{*;}
-keep class net.lzbook.kit.net.Result {*;}
-keep class net.lzbook.kit.utils.user.bean.**{*;}
-keep class com.ding.basic.bean.**{*;}

-keep public class * extends com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for ALiFeedBack  ----------
-keep class com.alibaba.sdk.android.feedback.impl.FeedbackServiceImpl {*;}
-keep class com.alibaba.sdk.android.feedback.impl.FeedbackAPI {*;}
-keep class com.alibaba.sdk.android.feedback.util.IWxCallback {*;}
-keep class com.alibaba.sdk.android.feedback.util.IUnreadCountCallback{*;}
-keep class com.alibaba.sdk.android.feedback.FeedbackService{*;}
-keep public class com.alibaba.mtl.log.model.LogField {public *;}
-keep class com.taobao.securityjni.**{*;}
-keep class com.taobao.wireless.security.**{*;}
-keep class com.ut.secbody.**{*;}
-keep class com.taobao.dp.**{*;}
-keep class com.alibaba.wireless.security.**{*;}
-keep class com.ta.utdid2.device.**{*;}

##---------------End: proguard configuration for ALiFeedBack  ----------




-optimizationpasses 5
-dontusemixedcaseclassnames
-dontshrink
-dontoptimize
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-dontwarn android.support.**
-dontwarn android.support.v4.**
-dontwarn android.**
-dontwarn com.umeng.socialize.**
-dontnote com.umeng.socialize.**
-dontwarn com.umeng.message.**
-dontnote com.umeng.message.**
-dontwarn info.monitorenter.*.**
-dontnote info.monitorenter.*.**
-dontwarn org.mozilla.intl.*.**
-dontnote org.mozilla.intl.*.**
-dontnote com.baidu.*.**
-dontwarn com.baidu.*.**
-dontwarn org.jsoup.nodes.*.**
-dontnote org.jsoup.nodes.*.**

#umeng start
-dontwarn com.umeng.comm.**
-dontwarn com.umeng.commm.**
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-keep class  activeandroid.** {*;}
-keep class com.umeng.** {*;}
-keep class android.** {*;}
-ignorewarnings

-keep class org.apache.http.** {*;}
-dontwarn  org.apache.http.**
-keep class org.apache.http.* {*;}
-dontwarn  org.apache.http.*
-keep,allowshrinking class org.android.agoo.service.* {
    public <fields>;
    public <methods>;
}
-keep,allowshrinking class com.umeng.message.* {
    public <fields>;
    public <methods>;
}

-keep public class com.umeng.community.example.R$*{
    *;
}

-keep class com.umeng.comm.push.UmengPushImpl {
    public * ;
}
#-keep class android.support.v4.** {*;}
#-dontwarn android.webkit.WebView

-dontwarn com.tencent.weibo.sdk.**
-keepattributes Exceptions,InnerClasses,Signature
#-keepattributes SourceFile,LineNumberTable
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**
-keep public class com.tencent.** {*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

#umeng socia share start
-dontwarn com.umeng.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep class com.umeng.scrshot.**
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**

-keep public class com.umeng.soexample.R$*{
    public static final int *;
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepattributes Signature

#umeng socia share end

# adding push
#-keep class com.umeng.message.* {
#        public <fields>;
#        public <methods>;
#}

#-keep class com.umeng.message.protobuffer.* {
#        public <fields>;
#        public <methods>;
#}

#-keep class com.squareup.wire.* {
#        public <fields>;
#        public <methods>;
#}

#-keep class com.umeng.message.local.* {
#        public <fields>;
#        public <methods>;
#}
#-keep class org.android.agoo.impl.*{
#        public <fields>;
#        public <methods>;
#}

-dontwarn com.xiaomi.**

-dontwarn com.ut.mini.**

#-keep class org.android.agoo.service.* {*;}

#-keep class org.android.spdy.**{*;}

-keep public class com.umeng.community.example.R$*{
    public static final int *;
}
-keepattributes Exceptions,InnerClasses,Signature,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
#-keepattributes *Annotation*
#umeng end

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep public class javax.**
-keep public class android.webkit.**
-keep public class org.jsoup.**

-keep public class net.lzbook.kit.utils.webview.JSInterfaceHelper { *; }
-keep public class net.lzbook.kit.utils.webview.WebViewJsInterface { *; }

-keep class org.jsoup.** { *; }

-keepclassmembers class com.intelligent.reader.activity.SearchBookActivity {
   public *;
}


#umeng start
-repackageclass com.intelligent.reader.proguard

-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

-keep public class com.intelligent.reader.R$*{
    public static final int *;
}

#umeng push
-keep class com.umeng.message.* {
        public <fields>;
        public <methods>;
}

-keep class com.umeng.message.protobuffer.* {
        public <fields>;
        public <methods>;
}

-keep class com.squareup.wire.* {
        public <fields>;
        public <methods>;
}

-keep class com.umeng.message.local.* {
        public <fields>;
        public <methods>;
}
-keep class org.android.agoo.impl.*{
        public <fields>;
        public <methods>;
}

-keep class org.android.agoo.service.* {*;}

-keep class org.android.spdy.**{*;}
#umeng push end

-keep class com.umeng.onlineconfig.OnlineConfigAgent {
    public <fields>;
    public <methods>;
}

-keep class com.umeng.onlineconfig.OnlineConfigLog {
    public <fields>;
    public <methods>;
}

-keep interface com.umeng.onlineconfig.UmengOnlineConfigureListener {
    public <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#umeng end

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class ** {
    public void onEvent*(**);
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

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class com.intelligent.reader.R$*{
    public static final int *;
}

-keep public class com.baidu.social.R$*{
    public static final int *;
}

-keep class android.support.v4.** {*;}
-keepattributes *Annotation*
-keep public class com.intelligent.reader.activity.** { *; }
-keep public class com.intelligent.reader.adapter.** { *; }
-keep public class com.intelligent.reader.app.** { *; }
-keep public class net.lzbook.kit.utils.cache.** { *; }
-keep public class net.lzbook.kit.error.** { *; }
-keep public class net.lzbook.kit.utils.popup.** { *; }
-keep public class net.lzbook.kit.utils.download.CacheInfo { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

-keep public class com.intelligent.reader.receiver.** { *; }
-keep public class net.lzbook.kit.book.component.service.** { *; }
-keep public class net.lzbook.kit.tasks.** { *; }
-keep public class com.intelligent.reader.view.** { *; }
-keep public class net.xxx.yyy.go.spider.** { *; }

-dontwarn com.dingyueads.sdk.**
-keep class com.dingyueads.sdk.** { *;}

-dontwarn com.dycm_adsdk.**
-keep class com.dycm_adsdk.** { *;}

-keep class net.lzbook.kit.bean.** {*;}

-keep class com.dingyueads.sdk.** {
  public protected *;
}

-keep class com.tencent.gdt.**{
public protected *;
}

-keep class com.baidu.**{
    public protected *;
}

-keep class com.baidu.mobads.** {
    public protected *;
}

-keep class com.qq.** {
  public protected *;
}

-keep class com.qq.e.** {
    public protected *;
}

#广告混淆
-keepclassmembers class * extends android.app.Activity {
    public void *(android.vide.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.baidu.mobads.*.** { *; }

#360广告混淆
-keep class com.ak.android.** {*;}
-keep class android.support.v4.app.NotificationCompat**{
      public *;
}

#科大讯飞广告混淆
-keep class com.iflytek.voiceads.** {*;}

#mongo ad
#-keep public class com.adsmogo.** {*;}
#
#-keep class com.baidu.mobads.** {
#    public protected *;
#}
#
#-keep class com.qq.e.** {
#    public protected *;
#}

# WeChat
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}

-keep class com.aliyun.logsdk.** {
   *;
}
-dontwarn com.fasterxml.**

-keep public class com.intelligent.reader.BuildConfig {*;}

# keep annotated by NotProguard
-keep @net.lzbook.kit.utils.NotProguard class * {*;}

-keepclassmembers class * {
    @net.lzbook.kit.utils.NotProguard *;
}

#阿里云
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**

-keep class **$Properties

# If you do not use Rx:
-dontwarn rx.**

# 友盟推送
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**
-dontwarn com.meizu.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class com.meizu.** {*;}
-keep class org.apache.thrift.** {*;}

-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}

-keep public class **.R$*{
   public static final int *;
}

#新版百度移动统计SDK
-keep class com.baidu.bottom.** { *; }
-keep class com.baidu.kirin.** { *; }
-keep class com.baidu.mobstat.** { *; }

#头条SDK
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.androidquery.callback.** {*;}
-keep class com.bytedance.sdk.openadsdk.service.TTDownloadProvider

# ijk
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.dueeeke.videoplayer.** { *; }
-dontwarn com.dueeeke.videoplayer.**

-keep class cn.dycm.ad.** {*;}

# inmobi.**
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# skip AVID classes
-keep class com.integralads.avid.library.* {*;}


#头条SDK
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.androidquery.callback.** {*;}
-keep class com.bytedance.sdk.openadsdk.service.TTDownloadProvider

# ijk
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.dueeeke.videoplayer.** { *; }
-dontwarn com.dueeeke.videoplayer.**

-keep class cn.dycm.ad.** {*;}

# inmobi.**
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# skip AVID classes
-keep class com.integralads.avid.library.* {*;}

