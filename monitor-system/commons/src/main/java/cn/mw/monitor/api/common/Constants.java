package cn.mw.monitor.api.common;

public interface Constants {
	// 响应请求成功
	String HTTP_RES_CODE_200_VALUE = "成功";
	// 响应请求成功
	String HTTP_RES_CODE_201_VALUE = "tooltip";
	// 响应请求成功但是需要警告
	String HTTP_RES_CODE_300_VALUE = "warning";
	// 没有权限
	String HTTP_RES_CODE_301_VALUE = "no permission";
	// 超时
	String HTTP_RES_CODE_302_VALUE = "timeout";
	// 账号被挤
	String HTTP_RES_CODE_303_VALUE = "kickout";
	// 系统错误
	String HTTP_RES_CODE_500_VALUE = "fail";
	// 响应请求成功code
	Integer HTTP_RES_CODE_200 = 200;
	// 业务正常处理但需要返回提示信息
	Integer HTTP_RES_CODE_201 = 201;
	// 响应请求成功但是需要警告的code
	Integer HTTP_RES_CODE_300 = 300;
	// 没有操作权限
	Integer HTTP_RES_CODE_301 = 301;
	// 登陆超时
	Integer HTTP_RES_CODE_302 = 302;
	// 账号被挤
	Integer HTTP_RES_CODE_303 = 303;
	// 需要验证码
	Integer HTTP_RES_CODE_401 = 401;
	// 系统错误
	Integer HTTP_RES_CODE_500 = 500;
	// 系统错误
	Integer HTTP_RES_CODE_202 = 202;
	// 参数分割符
	String SPLIT_CODE = "\\|";
	String CONN_CODE = "|";

	//上传图片url前缀
	String UPLOAD_BASE_URL = "/upload/";

	String MWAPI_BASE_URL = "/mwapi/basics/";

	//未知
	String UNKNOWN = "未知";
}
