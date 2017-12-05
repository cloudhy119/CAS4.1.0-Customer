package org.jasig.cas.ccgx.sync.bean;

public class AppUser {
	
	/**
	 * 客户端代码
	 */
	private String appCode;
	/**
	 * 应用系统的账号名
	 */
	private String appAccount;
	
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	public String getAppAccount() {
		return appAccount;
	}
	public void setAppAccount(String appAccount) {
		this.appAccount = appAccount;
	}

}
