package org.jasig.cas.ccgx.sync.bean;

import java.util.ArrayList;
import java.util.List;

public class UnifiedUser {
	
	/**
	 * 统一登录账号名
	 */
	private String unifiedAccount;
	/**
	 * 统一登录密码（密文）
	 */
	private String unifiedPassword;
	/**
	 * 统一登录账号所关联的各应用系统的账号
	 */
	private List<AppUser> appUserList = new ArrayList<AppUser>();
	
	public String getUnifiedAccount() {
		return unifiedAccount;
	}
	public void setUnifiedAccount(String unifiedAccount) {
		this.unifiedAccount = unifiedAccount;
	}
	public String getUnifiedPassword() {
		return unifiedPassword;
	}
	public void setUnifiedPassword(String unifiedPassword) {
		this.unifiedPassword = unifiedPassword;
	}
	public List<AppUser> getAppUserList() {
		return appUserList;
	}
	public void setAppUserList(List<AppUser> appUserList) {
		this.appUserList = appUserList;
	}
}
