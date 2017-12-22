package org.jasig.cas.services.persondir.custom;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.util.custom.DBUtil;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AttributeNamedPersonImpl;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

/**
 * 给cas客户端返回更多自定义的信息（cas默认只返回username）
 * @author Huangyun
 *
 */
public class CustomStubPersonAttributeDao extends StubPersonAttributeDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomStubPersonAttributeDao.class);
	private DBUtil dbUtil;
	private String appInfoSQL;
	
	/**
	 * 用以加载dataSource
	 * 构造函数参数要对应deployerConfigContext.xml中attributeRepository的配置
	 * @param dataSource
	 */
	public CustomStubPersonAttributeDao(final DBUtil dbUtil, final String appInfoSQL) {
		this.dbUtil = dbUtil;
		this.appInfoSQL = appInfoSQL;
	}
	
	@Override
    public IPersonAttributes getPerson(String uid) {
		Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
//		String sql = appInfoSQL;
//		try {
//			List<Map<String, Object>> values = dbUtil.getJdbcTemplate().queryForList(sql, uid);
//			if(values != null && !values.isEmpty()) {
//				for(Map<String, Object> value : values) {
//					String appCode = (String) value.get("app_code");
//					String appAccount = (String) value.get("app_account");
//					/*
//					 * 这里必须注意！appCode不能为数字，必须以字母开头，否则客户端解析会报错！！！！
//					 */
//					attributes.put(appCode, Collections.singletonList((Object)appAccount));
//				}
//			}
//		} catch (DataAccessException e) {
//			LOGGER.error("", e);
//		}
		return new AttributeNamedPersonImpl(attributes);
    }

}
