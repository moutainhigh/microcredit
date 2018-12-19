package banger.moduleIntf;

import banger.domain.loan.LoanApplyInfo;
import banger.framework.collection.DataTable;
import net.sf.json.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * Created by wanggl on 2017/7/21.
 */
public interface ILoanHandOverProvider {


    List<LoanApplyInfo> queryHandOverLoanIds(Map<String, Object> condition);
    /**
     *修改贷款申请表
     * @param applyInfo 实体对像
     * @param loginUserId 登入用户Id
     */
    void updateApplyInfo(LoanApplyInfo applyInfo,Integer loginUserId);

    /**
     * 分配贷款
     * @param ids
     * @param memberUserId
     * @param loginerId
     * @return
     */
    Map<String, Object> loanAllotSignSave(String ids, Integer memberUserId, Integer loginerId);

    /**
     * 根据贷款id查询贷款信息
     * @param loanId
     * @return
     */
    LoanApplyInfo getApplyInfoById(Integer loanId);

    /**
     * 根据贷款id 查出审批决议主记录
     * @param loanId
     * @return
     */
    DataTable getApprovalDataTableByLoanId(Integer loanId);


    /**
     * 根据自定义表单信息和贷款信息ID获取表数据
     * @param tableName
     * @param id
     * @return
     */
    DataTable getDataTableByLoanId(String tableName, String presType, Integer id);

    JSONArray getLoanOrientationTreeJson();

    JSONArray getBizAndPrdTypeTreeJson();

    JSONArray queryCrmPrdTypeTreeJson();

    String queryCrmPrdTypeJson(Map<String, Object> condition);

    String refreshLoanAccount(Integer userId);


    String refreshLoanAccountTab(Integer loanId);


    String sendLoanMsg(LoanApplyInfo loanApplyInfo, String[] userIds);



}
