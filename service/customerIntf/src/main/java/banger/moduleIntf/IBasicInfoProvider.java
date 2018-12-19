package banger.moduleIntf;

import banger.domain.customer.CustBasicInfoQuery;
import banger.domain.customer.Customer;
import banger.framework.collection.DataTable;
import banger.framework.pagesize.IPageList;
import banger.framework.pagesize.IPageSize;

import java.util.List;
import java.util.Map;

/**
 * Created by wanggl on 2017/7/21.
 */
public interface IBasicInfoProvider {

    IPageList<CustBasicInfoQuery> queryCusListByBelongId(Map<String, Object> condition, IPageSize page);

    Customer getCustomerDetailById(Integer id);

    void updateBasicInfo(Customer basicInfo, Integer loginUserId);

    DataTable getDataTableById(String tableName, Integer id);

}
