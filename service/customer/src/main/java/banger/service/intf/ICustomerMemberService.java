package banger.service.intf;

import banger.domain.customer.IntoCustomerMember;
import banger.framework.pagesize.IPageList;
import banger.framework.pagesize.IPageSize;

import java.util.List;
import java.util.Map;

/**
 * 进件客户分配表业务访问接口
 */
public interface ICustomerMemberService {

	/**
	 * 新增进件客户分配表
	 * @param customerMember 实体对像
	 * @param loginUserId 登入用户Id
	 */
	void insertCustomerMember(IntoCustomerMember customerMember, Integer loginUserId);

	/**
	 *修改进件客户分配表
	 * @param customerMember 实体对像
	 * @param loginUserId 登入用户Id
	 */
	void updateCustomerMember(IntoCustomerMember customerMember, Integer loginUserId);

	/**
	 * 查询进件客户分配表
	 * @param condition 查询条件
	 * @return
	 */
	List<IntoCustomerMember> queryCustomerMemberList(Map<String, Object> condition);

	/**
	 * 分页查询进件客户分配表
	 * @param condition 查询条件
	 * @param page 分页对像
	 * @return
	 */
	IPageList<IntoCustomerMember> queryCustomerMemberList(Map<String, Object> condition, IPageSize page);

}
