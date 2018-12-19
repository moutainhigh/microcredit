package banger.service.intf;

import java.util.List;
import java.util.Map;

import banger.domain.loan.LoanCrossCheckQuanyiquan;
import banger.framework.pagesize.IPageList;
import banger.framework.pagesize.IPageSize;
import banger.domain.loan.LoanCrossCheckIncome;

/**
 * 交叉检验收入表业务访问接口
 */
public interface ICrossCheckIncomeService {

	/**
	 * 新增交叉检验收入表
	 * @param crossCheckIncome 实体对像
	 * @param loginUserId 登入用户Id
	 */
	void insertCrossCheckIncome(LoanCrossCheckIncome crossCheckIncome, Integer loginUserId);

	/**
	 *修改交叉检验收入表
	 * @param crossCheckIncome 实体对像
	 * @param loginUserId 登入用户Id
	 */
	void updateCrossCheckIncome(LoanCrossCheckIncome crossCheckIncome, Integer loginUserId);

	/**
	 * 通过主键删除交叉检验收入表
	 * @param id 主键Id
	 */
	void deleteCrossCheckIncomeById(Integer id);

	/**
	 * 通过主键得到交叉检验收入表
	 * @param id 主键Id
	 */
	LoanCrossCheckIncome getCrossCheckIncomeById(Integer id);

	/**
	 * 查询交叉检验收入表
	 * @param condition 查询条件
	 * @return
	 */
	List<LoanCrossCheckIncome> queryCrossCheckIncomeList(Map<String, Object> condition);

	/**
	 * 分页查询交叉检验收入表
	 * @param condition 查询条件
	 * @param page 分页对像
	 * @return
	 */
	IPageList<LoanCrossCheckIncome> queryCrossCheckIncomeList(Map<String, Object> condition, IPageSize page);


	/**
	 * 通过贷款id获取交叉检验 收入检验详情
	 * @param loanId
	 * @return
	 */
	LoanCrossCheckIncome getCrossCheckIncomeByLoanId(Integer loanId);

	/**
	 * 保存收入检验信息
	 * @param lcci
	 */
	void saveLoanCrossCheckIncome(LoanCrossCheckIncome lcci,Integer userId);

	/**
	 *修改交叉检验收入表 (app端传入空值时赋值字段为null)
	 * @param crossCheckIncome 实体对像
	 * @param loginUserId 登入用户Id
	 */
	void updateNullCrossCheckIncome(LoanCrossCheckIncome crossCheckIncome, Integer loginUserId);
}
