package org.jasig.cas.ccgx.sync.bean;

import java.util.ArrayList;
import java.util.List;

public class SyncUserBean {
	
	/**
	 * add/edit/delete/bind/unbind
	 */
	private String optType;
	
	private List<UnifiedUser> unifiedUserList = new ArrayList<UnifiedUser>();

	public String getOptType() {
		return optType;
	}

	public void setOptType(String optType) {
		this.optType = optType;
	}

	public List<UnifiedUser> getUnifiedUserList() {
		return unifiedUserList;
	}

	public void setUnifiedUserList(List<UnifiedUser> unifiedUserList) {
		this.unifiedUserList = unifiedUserList;
	}

}
