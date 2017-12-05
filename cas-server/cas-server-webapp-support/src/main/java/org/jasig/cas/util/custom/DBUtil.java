package org.jasig.cas.util.custom;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据库操作工具栏
 * @author Huangyun
 *
 */
public class DBUtil {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 用以加载dataSource
	 * 构造函数参数要对应deployerConfigContext.xml中attributeRepository的配置
	 * @param dataSource
	 */
	public DBUtil(final DataSource dataSource) {
		setDataSource(dataSource);
	}
	
	/**
     * Method to set the datasource and generate a JdbcTemplate.
     *
     * @param dataSource the datasource to use.
     */
    public final void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }
    
    /**
     * Method to return the jdbcTemplate.
     *
     * @return a fully created JdbcTemplate.
     */
    public final JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public final DataSource getDataSource() {
        return this.dataSource;
    }

}
