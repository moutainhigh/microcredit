package banger.framework.sql.mapping;

import java.util.Map;

import banger.framework.util.ReflectorUtil;
import banger.framework.util.StringUtil;

public class SqlResultMap {
	private String id;
	private String className;
	private Map<String,String> properties;
	
	public String getId()
	{
		return this.id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getClassName()
	{
		return this.className;
	}
	
	public void setClassName(String name)
	{
		this.className = name;
	}
	
	public Map<String,String> getProperties()
	{
		return this.properties;
	}
	
	public void setProperties(Map<String,String> properties)
	{
		this.properties = properties;
	}
	
	/**
	 * 得到返回类型
	 * @return
	 */
	public Class<?> getClassForName(){
		if (!StringUtil.isNullOrEmpty(this.className)) {
			return ReflectorUtil.getClassForName(this.className);
		}
		return null;
	}
}
