package banger.dao.intf;

import banger.domain.task.LoanUserTask;

import java.util.Date;

/**
 * Created by zhusw on 2017/7/18.
 */
public interface ILoanTaskDao {

    /**
     * 统计用户放款信息
     * @param beginDate     任务开始日期
     * @param endDate       任务结束日期
     * @param userId        用户Id
     * @param groupId       团队Id
     * @return
     */
    LoanUserTask getUserTaskAmountByCondtion(Date beginDate, Date endDate, Integer userId ,Integer groupId);

    /**
     * 统计用户放款信息
     * @param beginDate     任务开始日期
     * @param endDate       任务结束日期
     * @param groupId       团队Id
     * @return
     */
    LoanUserTask getGroupTaskAmountByCondtion(Date beginDate, Date endDate, Integer groupId);

}
