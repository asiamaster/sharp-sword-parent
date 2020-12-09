package com.dili.ss.constant;

/**
 *
 */
public class ResultCode {

    // --------------------  通用错误码  -------------------------

    /**
     * 200: OK - [GET/POST]：服务器成功返回用户请求的数据
     */
    public static final String OK="200";

    /**
     * 201: [POST/PUT/PATCH]：用户新建或修改数据成功。
     */
    public static final String CREATED = "201";

    /**
     * 202 Accepted - [*]：表示一个请求已经进入后台排队（异步任务）
     */
    public static final String ACCEPTED = "202";

    /**
     * 203 Non-Authoritative Information - [*]：非授权信息。请求成功。但返回的meta信息不在原始的服务器，而是一个副本
     */
    public static final String NON_AUTHORITATIVE_INFORMATION = "203";

    /**
     * 204 NO CONTENT - [DELETE]：用户删除数据成功。
     */
    public static final String NO_CONTENT = "204";

    /**
     * 400 INVALID REQUEST - [POST/PUT/PATCH]：用户发出的请求有错误，服务器没有进行新建或修改数据的操作，该操作是幂等的。
     */
    public static final String INVALID_REQUEST = "400";

    /**
     * 401：Unauthorized - [*]：表示用户没有权限（令牌、用户名、密码错误）。
     */
    public static final String UNAUTHORIZED="401";

    /**
     * 403 Forbidden - [*] 表示用户得到授权（与401错误相对），但是访问是被禁止的。
     */
    public static final String FORBIDDEN ="403";

    /**
     * 404 NOT FOUND - [*]：用户发出的请求针对的是不存在的记录，服务器没有进行操作，该操作是幂等的。
     */
    public static final String NOT_FOUND ="404";

    /**
     * 405	Method Not Allowed - [*]：客户端请求中的方法被禁止
     */
    public static final String METHOD_NOT_ALLOWED ="405";

    /**
     * 406 Not Acceptable - [GET]：用户请求的格式不可得（比如用户请求JSON格式，但是只有XML格式）。
     */
    public static final String NOT_ACCEPTABLE ="406";

    /**
     * 414 Request-URI Too Large - [*]：请求的URI过长（URI通常为网址），服务器无法处理。
     */
    public static final String REQUEST_URI_TOO_LARGE ="414";

    /**
     * 415	Unsupported Media Type - [*]：服务器无法处理请求附带的媒体格式
     */
    public static final String UNSUPPORTED_MEDIA_TYPE ="415";

    /**
     * 429 Flow Limit - [*]：Spring Cloud Gateway 限流
     */
    public static final String FLOW_LIMIT ="429";

    /**
     * 500 INTERNAL SERVER ERROR - [*]：服务器发生错误，用户将无法判断发出的请求是否成功。
     */
    public static final String INTERNAL_SERVER_ERROR ="500";

    /**
     * 501 Not Implemented - [*]：服务器不支持请求的功能，无法完成请求。
     */
    public static final String NOT_IMPLEMENTED ="501";

    /**
     * 502 Bad Gateway - [*]：服务器发生错误，用户将无法判断发出的请求是否成功。
     */
    public static final String BAD_GATEWAY ="502";

    /**
     * 503 service unavaliable - [*]：由容器抛出，自己的代码不要抛这个异常
     */
    public static final String SERVICE_UNAVALIABLE ="503";

    /**
     * 504	Gateway Time-out - [*]：充当网关或代理的服务器，未及时从远端服务器获取请求
     */
    public static final String GATEWAY_TIMEOUT ="504";

    /**
     * 505 HTTP Version not supported - [*]：服务器不支持请求的HTTP协议的版本，无法完成处理
     */
    public static final String HTTP_VERSION_NOT_SUPPORTED ="505";

    // --------------------  自定义错误码  -------------------------

    /**
     * 1000 PARAMS_ERROR: 输入参数错误(输入参数类型、值、null等错误)
     */
    public static final String PARAMS_ERROR="1000";

    /**
     * 2000 DATA_ERROR: 业务逻辑或数据错误(未查询到数据，数据验证不通过，数据发生变化等错误)
     */
    public static final String DATA_ERROR="2000";

    /**
     * 3000 CSRF_ERROR: 无效的token，或者token过期
     */
    public static final String CSRF_ERROR = "3000";

    /**
     * 4000 PAYMENT_ERROR: 支付异常
     */
    public static final String PAYMENT_ERROR="4000";

    /**
     * 5000 APP_ERROR: 服务器内部错误(系统错误，代码BUG,系统间调用超时等错误)
     */
    public static final String APP_ERROR="5000";

    /**
     * 5001 IDEMPOTENT_ERROR: 幂等接口调用失败异常
     */
    public static final String IDEMPOTENT_ERROR = "5001";

    /**
     * 5002 REMOTE_ERROR: 远程调用错误
     */
    public static final String REMOTE_ERROR="5002";

    /**
     * 6000：权限错误(未登录，数据权限不满足，功能权限不满足等错误)<br/>
     */
    public static final String NOT_AUTH_ERROR="6000";



}
