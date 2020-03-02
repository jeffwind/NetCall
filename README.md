# NetCall
Android网络框架。使用注解的方式声明网络接口数据，一个类实现一个网络接口。NetCall还集成缓存机制以及https双项认证接口。

## 一、简介
CallNet是Android端一个网络请求框架，使用简单。它要求每个网络接口封装成一个类。同时，它具有缓存机制，可以缓存网络数据，并设置在一定时间内同样的请求不再从网络上获取，只从本地获取。缓存一定时间没有使用会自动清理。同时他支持https双向认证的简单配置使用。

## 二、简单用法
假如请求接口为https://192.168.31.159:8443/rest/greeting?name=world  
首先写一个继承自BaseCall的网络请求类，如下
```java
@CallGet(value="rest/greeting",baseUrl="https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
public class CallTest extends BaseCall {

    @ParamQuery("name")
    private String id;

    public CallTest(String name) {
        this.id = name;
    }
}
```
其中`@CallResp(BeanTest.class)`为返回的数据类，`@ParamQuery`声明的是请求参数。  

调用处如下
```java
CallTest callTest = new CallTest(text);
callTest.call(new Callback() {
    @Override
    public void onResp(Response response) {
...
        if (response.isSuccess()) {
            resultTv.setText(response.getBean().toString());
        } else {
            Toast.makeText(MainActivity.this, "Error：" + response.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
});
```
返回数据类如下
```java
public class BeanTest implements IBean {
    //{"id":1,"content":"Hello, World!"}
    private int id;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "BeanTest{id=" + id + ", content='" + content +"\'}";
    }
}
```
AndroidManifest.xml需要增加网络权限
  `<uses-permission android:name="android.permission.INTERNET" />`

build.gradle需要引入okhttp以及gson库，如下。
```java
implementation 'com.squareup.okhttp3:okhttp:4.3.1'
implementation 'com.google.code.gson:gson:2.8.6'
```
<br/>
  
## 三、用法介绍
### 1.支持的协议
目前支持的请求协议有GET和POST。GET方式使用注解*@CallGet*，POST方式使用注解*@CallPost*。
  
### 2.全局baseUrl
*@CallGet*和 *@CallPost*中都有baseUrl参数，该参数可以省略，省略后会使用全局的baseUrl。  
通过静态方法`NetCall.setDefaultBaseUrl(String baseUrl)`设置这个全局baseUrl参数。设置完后*@CallGet*和*@CallPost*如果不声明baseUrl，将以设置的全局baseUrl作为本次请求的baseUrl。
  
### 3.@ParamQuery---URL参数
@ParamQuery声明的参数会作为URL参数传递给服务器。
假如请求接口为https://192.168.31.159:8443/rest/greeting?name=world
请求的例子如下
```java
@CallGet("rest/greeting")
@CallResp(BeanTest.class)
public class CallTest extends BaseCall {

    @ParamQuery(value = "name", encoded = false)
    private String id;

    public CallTest(String name) {
        this.id = name;
    }
}
```
*@ParamQuery*如果不填value，则以变量名“id”作为请求的参数名，也就是请求会由`greeting?name=world`变为`greeting?id=world`  
*@ParamQuery*的encoded参数表示是否utf-8编码，默认值为执行编码操作。

  
## 4.@ParamHeader---报文头

```java
@CallGet("rest/header")
@CallResp(BeanTest.class)
public class CallTestHeader extends BaseCall {

    @ParamQuery("name", encoded = false)
    private String id;

    public CallTestHeader(String name) {
        this.id = name;
    }
}
```
*@ParamHeader*如果不填value，则以变量名作为请求的参数名。  
*@ParamHeader*的encoded参数表示是否utf-8编码，默认值为执行编码操作。

  
## 5.报文内容

报文内容只有在*@CallPost*请求方式时才会生效。
只支持键值对*@ParamForm*、纯字符串*@ParamString*、文件字节流*@ParamFile*其中之一。
普遍情况下大家使用的是键值对*@ParamForm*，如果想要传递其他格式的内容可以使用*@ParamString*，如果想要上传文件给服务器可以使用*@ParamFile*。

  
## 6.@ParamForm---报文键值对

报文键值对是报文内容的一种格式，是当前网络请求经常使用的方式。键值对可以通过@ParamQuery组合在URL中传递给服务器，也可以通过本注解的报文内容方式传给服务器。通过报文内容方式可以解决URL参数方式字节长度限制的问题；并且https下会对数据进行加密，而URL参数方式是不加密的。
  
*@ParamForm*例子
```java
public class CallTestPost extends BaseCall {

    @ParamForm
    private long id;

    @ParamForm(value = "name", encoded = false)
    private String nickName;

……
}
```
*@ParamForm*如果不填value，则以变量名作为请求的参数名，也就是上面例子的id作为参数名。  
*@ParamForm*的encoded参数表示是否utf-8编码，默认值为执行编码操作。

  
## 7.@ParamString---报文字符串

```java
public class CallTestString extends BaseCall {

    @ParamString(mediaType = NetCallConstant.MEDIA_APPLICATION_JSON)
    private String data;

……
}
```
*@ParamString*在一个类中不能有多个，假如有多个的话只有一个会生效。
mediaType表示上传的数据类型。

  
## 8.@ParamFile---报文数据流

```java
public class CallTestFile extends BaseCall {

    @ParamFile(mediaType = NetCallConstant.MEDIA_IMG_JPG)
    private String path;

    public CallTestFile(String path) {
        this.path = path;
    }
}
```

*@ParamFile*在一个类中不能有多个，假如有多个的话只有一个会生效。  
mediaType表示上传的数据类型。

## 9.Repsonse返回数据
*Repsonse*类

```java
/**
 * 获取数据返回的数据
 */
public class Response {

    private BaseCall call;
    /** 网络状态码 */
    private int statusCode;
    /** 本次是否是获取网络数据后返回的内容 */
    private boolean isAfterNet;
    /** 本次是否获取数据成功 */
    private boolean isSuccess;
    /** 返回的数据 */
    private String respStr;
    /** 返回的数据类 */
    private IBean bean;
    /** 异常信息 */
    private Exception exception;
    /** 是否已经缓存已经成功返回数据 */
    private boolean isCacheSuccess;
    /** 是否只使用缓存，false才需要获取网络数据，该值由CallCache.cacheOnlyTime()决定 */
    private boolean isCacheOnly;
    /** 是否缓存与服务器数据相同 */
    private boolean isCacheEqual;
 }
```

如果需要自己定义数据结构，可以继承自该类，并重写BaseCall的protected Response getResponse(okhttp3.Response response, Exception ex)方法。返回自己的Response类。

  
## 10.BaseCall类可重写的方法

*BaseCall*类提供了很多可重写的方法，具体如下。可以重写以下方法，控制网络请求的各个阶段，代替各种注解的使用。

```java
/** 获取baseUrl */
protected String onBaseUrl()

/** 获取Url后缀 */
protected String onSuffixUrl()

/** 获取url中?后面的键值对 */
protected Params onParamQuery()

/** 获取post报文的键值对 */
protected Params onParamForm()

/** 获取报文头的键值对 */
protected Params onParamHeader()

/** 获取post的String参数，下标0代表mediaType，下标1代表发送的String内容 */
protected String[] onParamString()

/** 获取post的File流参数，下标0代表mediaType，下标1代表发送的文件路径 */
protected String[] onParamFile()

/** 可重写该方法修改发送内容 */
protected void onInterceptCall(SendData data)

/** 保存返回的数据，在辅助线程中执行 */
protected boolean onSaveCache(Response response)

/** 可重写该方法构造ClientBuilder */
protected OkHttpClient.Builder onHttpClientBuilder()

/** 将okhttp3的Response转化为NetCall的结果数据类 */
protected Response getResponse(okhttp3.Response response, Exception ex) 

/** 将okhttp3的Response转化为NetCall的结果数据类 */
protected void onResponse(Response response)

/** 本BaseCall作为{@link #nextCall(BaseCall)}的参数的话，当轮到自己请求网络，会回调该方法 */
protected void onNextCallTurn(BaseCall lastCall, Response lastCallResponse)
```

  
## 11.日志打印
通过静态方法`NetCall.setNeedLog(true)`打开或关闭日志模式，默认为关闭。打印的日志内容是网络请求的URL以及返回的数据。


默认的日志tag为“NetCall”，可以通过静态方法NetCall.setLogTag(String logTag)设置新的tag。
可以通过设置log过滤器过滤需要的信息。


# 四、缓存Cache
## 1.缓存机制
缓存机制作用是将网络获取到的数据保存到本地，在下一次进行相同请求时直接从本地缓存中读取数据。他的好处在于不通过网络马上响应请求返回数据，加快了数据的加载，以及在无网环境下依然可以使用。
当通过`{@link BaseCall#call(Callback)}`发起网络请求，首先会从缓存获取数据，通过`{@link Callback#onResp(Response)}`返回数据。如果缓存超时，则重新从服务器获取数据，如果服务器数据与缓存不一致，则再一次调用`{@link Callback#onResp(Response)}`返回最新数据。服务器数据与缓存数据一致，则更新缓存最后使用时间，流程结束。

  
## 2.简单使用
例子：
```java
@CallGet(value = "rest/greeting", baseUrl = "https://192.168.31.159:8443/")
@CallResp(value = BeanTest.class)
@CallCache(cacheOnlyTime = -1, deleteTime = 10)
public class CallTestCache extends BaseCall {

    @ParamQuery("name")
    @ParamCache("name")
    private String id;

@ParamCache
private int type;

    public CallTestCache(String name, int type) {
        this.id = name;
        this.type = type;
    }
}
```
  
添加@CallCache后，就已经开始使用缓存机制。
类参数注解@ParamCache用于判断两个请求是否返回相同的结果。对于两个Call，他们所有被这个注解修饰的变量值都相同的话，两个Call将被视为完全相同网络请求，将获取到同一份缓存数据。

## 3.缓存有效期
缓存有效期由`@CallCache(cacheOnlyTime = -1, deleteTime = 10)`中的cacheOnlyTime声明，单位为秒。默认-1，-1表示不启用。
在有效期内，如果有缓存数据则直接返回缓存数据而不再请求网络。再次请求网络后会更新缓存最后使用时间，自动更新有效期限。

  
## 4.缓存清理
库自带缓存清理机制，对于长时间没有再使用的缓存会对其定期自动清理。可以为不同的请求设定不同的缓存保留时间。
缓存的保留时间由`@CallCache(cacheOnlyTime = -1, deleteTime = 10)`中的deleteTime声明，单位为秒，默认为保留半个月。
如果缓存在保留时间都没有更新过，则清理机制会自动清理掉这个缓存。也可以通过静态方法`NetCall.clearExpriredCache()`主动清理缓存。

  
## 5.缓存路径
默认的缓存路径在`Context.getCacheDir() + "/netcall/"`，即“data/data/包名/cache”。
通过静态方法`NetCall.setCacheRootPath(String path)`重新设置缓存路径。在重设缓存路径后，所有的缓存操作都将在新路径下进行，包括缓存读写和缓存清理操作。

  
## 6.日志打印
本身Cache机制会打印日志以供调试，默认不打印这些日志，通过静态方法`NetCall.setNeedLog(true)`和静态方法`NetCall.setNeedLogCache(true)`打开日志打印。
```java
NetCall.setNeedLog(true);
NetCall.setNeedLogCache(true);
```
`NetCall.setNeedLog(boolean needCache)`是否打开日志模式。
`NetCall.setNeedLogCache(boolean needCache)`是否打开cache日志模式，要使用的话同时需要设置NetCall.setNeedLog为true


# 五、https认证
如果想要使用https的连接，可以直接使用，不需要添加任何代码。如果想要使用https单向认证或者双项认证的功能。这个库提供了一种较为简单的方式实现，避免了写太多的代码。

## 1.单向认证
例子
```java
@CallGet(value = "rest/https", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
@CallHttps(trustCrtPaths = {"jeffwind_server.crt"})
public class CallTestHttps extends BaseCall {
    
}
```

这是一个无参数的网络连接。
单向认证的作用是客户端认证服务器身份，也即客户端判断返回的数据是否真的由目标服务器发送过来，避免连接被劫持。单向认证需要证书，这里支持crt证书文件。上面例子*trustCrtPaths*可以填入多个crt证书路径。
证书路径可以选择放在文件系统或者assets中。默认是从assets中读取，如果想要从文件系统中读取，可以使用*@CallHttps*注解的*pathType*参数，选择文件是从文件管理器获取还是assets获取。

## 2.双向认证
例子
```java
@CallGet(value = "rest/https", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
@CallHttps(trustCrtPaths = {"server.crt"}, clientBksPath = "client.bks", clientBksPsw = "123456")
public class CallTestHttps extends BaseCall {

}
```
双项认证是在单向认证的基础上增加了服务器对客户端身份的认证。因此我们需要传递给服务器，以供其认证客户端的身份，再决定是否响应这个请求。
*@CallHttps* 的*clientBksPath*参数为双项认证证书文件路径，支持bks文件。*clientBksPsw*参数为密码。

  
## 3.HostnameVerifier选择
域名校验*HostnameVerifier*默认是不校验的。如果需要对域名进行校验，可以使用`BaseCall.setHttpsData(IHttpsData httpsData)`方法。  
*IHttpData*接口提供`HostnameVerifier getHostNameVerifier()`方法进行重写。  
如果只需要重写该方法，可以直接继承 *HttpsDataDef* 类，该类继承自 *IHttpData*。

  
## 4.重写证书获取方法
如果@HttpsCall注解不能满足需求，或者认为该注解使用起来不够安全，*BaseCall* 提供了另一种证书获取方式：`BaseCall.setHttpsData(IHttpsData httpsData)`。
其中IHttpsData类如下
```java
/**
 * https信息
 */
public interface IHttpsData {

    /** 域名过滤 */
    HostnameVerifier getHostNameVerifier();

    /** 返回服务器认证证书，用作客户端认证服务器，可为null或空数组 */
    InputStream[] getTrustCertStream();

    /** 返回客户端认证证书，用作服务器认证客户端，可为null */
    InputStream getClientCertStream();

    /** 返回客户端认证密码，用作服务器认证客户端，可为null */
    String getClientCertPsw();
}
```
可以通过重写这些方法获取https证书数据流。
