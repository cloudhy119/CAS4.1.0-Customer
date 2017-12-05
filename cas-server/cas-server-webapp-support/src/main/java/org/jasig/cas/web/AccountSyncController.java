package org.jasig.cas.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.ccgx.sync.bean.AppUser;
import org.jasig.cas.ccgx.sync.bean.SyncUserBean;
import org.jasig.cas.ccgx.sync.bean.UnifiedUser;
import org.jasig.cas.util.custom.DBUtil;
import org.jasig.cas.util.custom.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * app同步、绑定账号接口
 * 
 * @author huangyun
 *
 */
@Controller("accountSyncController")
@RequestMapping(value = "/inter/syncacc")
public class AccountSyncController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountSyncController.class);

	@NotNull
	@Autowired(required = true)
	private DBUtil dbUtil;

	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
		JdbcTemplate jdbcTemplate = dbUtil.getJdbcTemplate();
		ModelAndView mv = new ModelAndView();
		try {
			String reqBody = charReader(request);
			//TODO 应有校验请求方的合法性，可考虑用系统内的账号密码校验
			SyncUserBean syncUser = JacksonUtil.json2Object(reqBody, SyncUserBean.class);
			jdbcTemplate.getDataSource().getConnection().setAutoCommit(false); //取消自动提交，控制事务
			String optType = syncUser.getOptType();
			List<UnifiedUser> unifieUserList = syncUser.getUnifiedUserList();
			if("add".equals(optType)) { //新增统一登录账号，可顺带绑定应用系统账号
				if(unifieUserList != null && !unifieUserList.isEmpty()) {
					for(UnifiedUser unifiedUser : unifieUserList) {
						String unifiedUserId = UUID.randomUUID().toString().replace("-", "");
						String sql = "insert into cas_user (id, account, password, salt, status, login_time) "
								+ "values (?, ?, ?, ?, ?, ?)";
						int result = jdbcTemplate.update(sql, unifiedUserId, unifiedUser.getUnifiedAccount(), unifiedUser.getUnifiedPassword(), "salt", "1", new Date());
						if(result > 0) {
							List<AppUser> appUserList = unifiedUser.getAppUserList();
							if(appUserList != null && !appUserList.isEmpty()) {
								for(AppUser appUser : appUserList) {
									String appUserId = UUID.randomUUID().toString().replace("-", "");
									sql = "insert into app_user (id, cas_account, app_account, app_code, status, update_time) "
											+ "values (?, ?, ?, ?, ?, ?)";
									jdbcTemplate.update(sql, appUserId, unifiedUser.getUnifiedAccount(), appUser.getAppAccount(), appUser.getAppCode(), "1", new Date());
								}
							}
						}
					}
				}
			} else if("edit".equals(optType)) { //编辑统一登录账号，其实只是修改密码，不做关于应用系统账号的操作
				if(unifieUserList != null && !unifieUserList.isEmpty()) {
					for(UnifiedUser unifiedUser : unifieUserList) {
						String sql = "update cas_user set password = ? where account = ?";
						jdbcTemplate.update(sql, unifiedUser.getUnifiedPassword(), unifiedUser.getUnifiedAccount());
					}
				}
			} else if("delete".equals(optType)) { //删除统一登录账号，同时删除所关联的所有应用系统账号
				if(unifieUserList != null && !unifieUserList.isEmpty()) {
					for(UnifiedUser unifiedUser : unifieUserList) {
						String sql = "delete from cas_user where account = ?";
						jdbcTemplate.update(sql, unifiedUser.getUnifiedAccount());
						sql = "delete from app_user where cas_account = ?";
						jdbcTemplate.update(sql, unifiedUser.getUnifiedAccount());
					}
				}
			} else if("bind".equals(optType)) { //绑定应用系统账号
				if(unifieUserList != null && !unifieUserList.isEmpty()) {
					for(UnifiedUser unifiedUser : unifieUserList) {
						String sql = "select count(1) from cas_user where account = ?";
						Map<String, Object> countMap = jdbcTemplate.queryForMap(sql, unifiedUser.getUnifiedAccount());
						if(countMap != null && !countMap.isEmpty()) {
							List<AppUser> appUserList = unifiedUser.getAppUserList();
							if(appUserList != null && !appUserList.isEmpty()) {
								for(AppUser appUser : appUserList) {
									//数据库表app_user需要建立app_account和app_code的联合唯一索引，不允许重复
									sql = "insert into app_user (id, cas_account, app_account, app_code, status, update_time) "
											+ "values (?, ?, ?, ?, ?, ?)";
									String appUserId = UUID.randomUUID().toString().replace("-", "");
									jdbcTemplate.update(sql, appUserId, unifiedUser.getUnifiedAccount(), appUser.getAppAccount(), appUser.getAppCode(), "1", new Date());
								}
							}
						} else {
							LOGGER.warn(unifiedUser.getUnifiedAccount() + " 统一登录账号不存在");
						}
					}
				}
			} else if("unbind".equals(optType)) { //解绑应用系统账号
				if(unifieUserList != null && !unifieUserList.isEmpty()) {
					for(UnifiedUser unifiedUser : unifieUserList) {
						String sql = "select count(1) from cas_user where account = ?";
						Map<String, Object> countMap = jdbcTemplate.queryForMap(sql, unifiedUser.getUnifiedAccount());
						if(countMap != null && !countMap.isEmpty()) {
							List<AppUser> appUserList = unifiedUser.getAppUserList();
							if(appUserList != null && !appUserList.isEmpty()) {
								for(AppUser appUser : appUserList) {
									//数据库表app_user需要建立app_account和app_code的联合唯一索引，不允许重复
									sql = "delete from app_user where cas_account = ? and app_account = ? and app_code = ?";
									jdbcTemplate.update(sql, unifiedUser.getUnifiedAccount(), appUser.getAppAccount(), appUser.getAppCode());
								}
							}
						} else {
							LOGGER.warn(unifiedUser.getUnifiedAccount() + " 统一登录账号不存在");
						}
					}
				}
			} else {
				LOGGER.warn(optType + " 不合法的optType！");
			}
			MappingJackson2JsonView view = new MappingJackson2JsonView();
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("status", Boolean.TRUE);
			attributes.put("desc", "操作成功");
			view.setAttributesMap(attributes);
			mv.setView(view);
			jdbcTemplate.getDataSource().getConnection().commit(); //提交事务
		} catch (Exception e) {
			LOGGER.error("", e);
			try {
				jdbcTemplate.getDataSource().getConnection().rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			} //异常则回滚
			MappingJackson2JsonView view = new MappingJackson2JsonView();
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("status", Boolean.FALSE);
			attributes.put("desc", e);
			view.setAttributesMap(attributes);
			mv.setView(view);
		} finally {
			try {
				jdbcTemplate.getDataSource().getConnection().setAutoCommit(true);
			} catch (SQLException e) {
				LOGGER.error("", e);
			}
		}
		return mv;
	}

	private String charReader(HttpServletRequest request) throws IOException {
		BufferedReader br = request.getReader();
		String str, wholeStr = "";
		while ((str = br.readLine()) != null) {
			wholeStr += str;
		}
		return wholeStr;
	}

}
