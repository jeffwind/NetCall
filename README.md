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
目前支持的请求协议有GET和POST。GET方式使用注解**@CallGet**，POST方式使用注解**@CallPost**。
  
### 2.全局baseUrl
**@CallGet**和 **@CallPost**中都有baseUrl参数，该参数可以省略，省略后会使用全局的baseUrl。  
通过静态方法`NetCall.setDefaultBaseUrl(String baseUrl)`设置这个全局baseUrl参数。设置完后**@CallGet**和**@CallPost**如果不写baseUrl，将以设置的全局baseUrl作为本次请求的baseUrl。
  
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
**@ParamQuery**如果不填value，则以变量名“id”作为请求的参数名，也就是请求会由`greeting?name=world`变为`greeting?id=world`  
**@ParamQuery**的encoded参数表示是否utf-8编码，默认值为执行编码操作。

  
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
**@ParamHeader**如果不填value，则以变量名作为请求的参数名。  
**@ParamHeader**的encoded参数表示是否utf-8编码，默认值为执行编码操作。

  
## 5.报文内容

报文内容只有在**@CallPost**请求方式时才会生效。
只支持键值对**@ParamForm**、纯字符串**@ParamString**、文件字节流**@ParamFile**其中之一。
普遍情况下大家使用的是键值对**@ParamForm**，如果想要传递其他格式的内容可以使用**@ParamString**，如果想要上传文件给服务器可以使用**@ParamFile**。

  
## 6.@ParamForm---报文键值对

报文键值对是报文内容的一种格式，是当前网络请求经常使用的方式。键值对可以通过@ParamQuery组合在URL中传递给服务器，也可以通过本注解的报文内容方式传给服务器。通过报文内容方式可以解决URL参数方式字节长度限制的问题；并且https下会对数据进行加密，而URL参数方式是不加密的。
  
**@ParamForm**例子
```java
public class CallTestPost extends BaseCall {

    @ParamForm
    private long id;

    @ParamForm(value = "name", encoded = false)
    private String nickName;

……
}
```
**@ParamForm**如果不填value，则以变量名作为请求的参数名，也就是上面例子的id作为参数名。  
**@ParamForm**的encoded参数表示是否utf-8编码，默认值为执行编码操作。

  
## 7.@ParamString---报文字符串

```java
public class CallTestString extends BaseCall {

    @ParamString(mediaType = NetCallConstant.MEDIA_APPLICATION_JSON)
    private String data;

……
}
```
**@ParamString**在一个类中不能有多个，假如有多个的话只有一个会生效。
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

**@ParamFile**在一个类中不能有多个，假如有多个的话只有一个会生效。  
mediaType表示上传的数据类型。

## 9.Repsonse返回数据
**Repsonse**类

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

**BaseCall**类提供了很多可重写的方法，具体如下。可以重写以下方法，控制网络请求的各个阶段，代替各种注解的使用。

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



