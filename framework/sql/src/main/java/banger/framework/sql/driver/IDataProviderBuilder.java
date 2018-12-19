package banger.framework.sql.driver;

import banger.framework.sql.config.IDbConfig;

/**
 * 数据库操作组件供应构造器
 */
public interface IDataProviderBuilder {
	
	/**
	 * 构造数据库组件供应接口
	 * @return
	 */
	IDataProvider build();

	/**
	 * 构造数据库组件供应接口
	 * @param configName 配置名称
	 * @return
	 */
    IDataProvider build(String configName);

    /**
     * 构造数据库组件供应接口
     * @param config 配置接口
     * @return
     */
    IDataProvider build(IDbConfig config);
}