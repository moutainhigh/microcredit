package banger.service.intf;

import java.util.List;
import java.util.Map;

import banger.domain.html5.IntoIntefaceRecordQuery;
import banger.framework.pagesize.IPageList;
import banger.framework.pagesize.IPageSize;
import banger.domain.html5.IntoIntefaceRecord;

/**
 * 进件接口记录表业务访问接口
 */
public interface IIntefaceRecordService {

	/**
	 * 新增进件接口记录表
	 * @param intefaceRecord 实体对像
	 *
	 */
	Integer insertIntefaceRecord(IntoIntefaceRecord intefaceRecord);

	/**
	 *修改进件接口记录表
	 * @param intefaceRecord 实体对像
	 * @param loginUserId 登入用户Id
	 */
	void updateIntefaceRecord(IntoIntefaceRecord intefaceRecord, Integer loginUserId);

	/**
	 * 通过主键删除进件接口记录表
	 * @param REQUEST_ID 主键Id
	 */
	void deleteIntefaceRecordById(Integer requestId);

	/**
	 * 通过主键得到进件接口记录表
	 * @param REQUEST_ID 主键Id
	 */
	IntoIntefaceRecord getIntefaceRecordById(Integer requestId);

	/**
	 * 查询进件接口记录表
	 * @param condition 查询条件
	 * @return
	 */
	List<IntoIntefaceRecord> queryIntefaceRecordList(Map<String, Object> condition);

	/**
	 * 分页查询进件接口记录表
	 * @param condition 查询条件
	 * @param page 分页对像
	 * @return
	 */
	IPageList<IntoIntefaceRecordQuery> queryIntefaceRecordList(Map<String, Object> condition, IPageSize page);
	/**
	 * 查询ocr,人脸识别详情
	 *
	 * @return
	 */
    IntoIntefaceRecordQuery IntoIntefaceRecordQuery(Integer id);
}
