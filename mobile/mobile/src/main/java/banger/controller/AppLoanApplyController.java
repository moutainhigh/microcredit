package banger.controller;

import banger.action.AppBaseController;
import banger.common.annotation.FuncPermitAnnotation;
import banger.common.annotation.NoTokenAnnotation;
import banger.common.annotation.TokenRepeatAnnotation;
import banger.common.interceptor.NoLoginInterceptor;
import banger.common.session.AppSessionManage;
import banger.constant.AppParamsConst;
import banger.domain.config.AutoBaseField;
import banger.domain.config.AutoBaseTemplate;
import banger.domain.config.AutoFieldWrapper;
import banger.domain.customer.CustBasicInfo;
import banger.domain.customer.CustCustomerBlack;
import banger.domain.customer.CustPotentialCustomers;
import banger.domain.enumerate.*;
import banger.domain.html5.IntoFileInfo;
import banger.domain.loan.*;
import banger.domain.permission.PmsDept_Query;
import banger.domain.permission.PmsRole;
import banger.domain.permission.PmsUser;
import banger.domain.permission.PmsUser_Query;
import banger.domain.system.SysBasicConfig;
import banger.framework.collection.DataRow;
import banger.framework.collection.DataTable;
import banger.framework.pagesize.IPageSize;
import banger.framework.sql.command.SqlTransaction;
import banger.framework.util.ConvertTypeUtil;
import banger.framework.util.DateUtil;
import banger.framework.util.IdCardUtil;
import banger.framework.util.StringUtil;
import banger.framework.web.dojo.JsonTools;
import banger.moduleIntf.*;
import banger.response.AppJsonResponse;
import banger.response.CodeEnum;
import banger.service.intf.IAppLoanApplyService;
import banger.service.intf.IAppLoanTemplateService;
import banger.util.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 贷款申请
 * @author zhusw
 *
 */
@RequestMapping("/api")
@Controller
public class AppLoanApplyController extends AppBaseController {
	private static final long serialVersionUID = -7305734846109770428L;
	public static final String SAVE_LOAN_APPLY_INFO_PARAMS = "loanTypeId,loanName,loanIdentifyType,loanIdentifyNum,loanApplyAmount,loanTelNum,createUser,clientTime";
	public static final String REFUSE_LOAN_APPLY_INFO_PARAMS = "loanId,loanRefuseType,loanRefuseReason,createUser,userId";
	public static final String SUBMIT_LOAN_APPLY_INFO_PARAMS = "loanId,createUser";
	public static final String RESULT_LOAN_AUDIT_INFO_PARAMS = "";
	public static final String RESULT_LOAN_APPLY_LIST_PARAMS = "belongUserName,loanId,loanTitle,loanProcessType,loanProcessTypeName,loanTypeId,loanClassId,loanTypeName,loanName,loanIdentifyType,loanIdentifyTypeName,loanIdentifyNum,loanIdentifyNumX,loanTelNum,loanTelNumX,loanApplyAmount,createUser,loanRefuseReasonDisplay,loanProcessStateDisplay,loanCollectionStateDisplay,loanMonitorStateDisplay,loanAmountFormat,loanRepayAmount,loanRepayDate,loanRepayAmountFormat,loanMonitorDate,loanMonitorTypeName,submitEnable,refuseEnable,gobackEnable,allotEnable,loanAccountAmount,loanAccountAmountFormat,loanStep,nextRepaymentAmount,nextRepaymentAmountFormat";
	
	@Resource
	private ILoanModule loanModule;
	@Resource
	IConfigModule configModule;
	
	@Autowired
	private IAppLoanApplyService appLoanApplyService;


	@Autowired
	private IAppLoanTemplateService appLoanTemplateService;

	@Resource
	private ICustomerBlackProvider customerBlackProvider;

	@Resource
	private ICustomerMarketProvider customerMarketProvider;

	@Resource
	private ICurrentAuditStatusProvider currentAuditStatusService;

	@Resource
	private IIntoCustomersProvider intoCustomersProvider;

	@Resource
	private IInfoAddedFileProvider iInfoAddedFileProvider;

	@Resource
	private IIndustryGuidelinesProvider industryGuidelinesProvider;

	@Resource
    private ILoanHandOverProvider loanHandOverProvider;

	@Resource
	private ITeamGroupProvider teamGroupProvider;

	@Resource
	private IPermissionService permissionService;

	@Autowired
	IPotentialCustomersProvider iPotentialCustomersProvider;

	@Autowired
	private IBasicConfigProvider basicConfigProvider;

	@Resource
	private ILoanSurveryFlowProvider loanSurveryFlowProvider;

	@Resource
	private ILoanApplyProvider loanApplyService;

	@Resource
	private ILoanOperationProvider loanOperationService;

	@Autowired
	private ICustomerModuleIntf customerModuleIntf;
	@Autowired
	private IPermissionModule permissionModule;
	@Autowired
	private ISystemModule systemModule;

	@Resource
	private IRepayPlanInfoProvider repayPlanInfoProvider;

	@Resource
	private ILoanTaskProvider loanTaskProvider;



	/**
	 * 得到贷款申请字段数据
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanApplyField")
	public ResponseEntity<String> getLoanApplyField(HttpServletRequest request,HttpServletResponse response){
		try {
			String jsonString = appLoanApplyService.getLoanApplyFieldJsonString();
			return new ResponseEntity<String>(jsonString, HttpStatus.OK);
		}catch(Exception e){
			log.error("获取申请贷款表单数据异常",e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 * 得到贷款类型列表数据
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanTypeList")
	public ResponseEntity<String> getLoanTypeList(HttpServletRequest request,HttpServletResponse response){
		try {
			String jsonString = appLoanApplyService.getLoanTypeJsonString();
			return new ResponseEntity<String>(jsonString, HttpStatus.OK);
		}catch(Exception e){
			log.error("获取得到贷款类型列表数据异常",e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}
	/**
	 * 得到贷款合同类型列表数据
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanContractTypeList")
	public ResponseEntity<String> getLoanContractTypeList(HttpServletRequest request,HttpServletResponse response){
		try {
			String jsonString = appLoanApplyService.getLoanContractTypeJsonString();
			return new ResponseEntity<String>(jsonString, HttpStatus.OK);
		}catch(Exception e){
			log.error("获取得到贷款合同类型列表数据异常",e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 *
	 **合同提交
	 */

	@RequestMapping(value = "/v1/loanContractSubmit")
	@NoLoginInterceptor
	public ResponseEntity<String> loanContractSubmit(HttpServletRequest request,HttpServletResponse respons){

		try {
			String loanId = this.getParameter("loanId");
			String userId = this.getParameter("userId");
			String loginUserId = this.getParameter("loginUserId");
			if(StringUtil.isNullOrEmpty(loanId)||StringUtil.isNullOrEmpty(userId)||
					StringUtil.isNullOrEmpty(loginUserId)){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
			}
			PmsUser pmsUser = permissionModule.getPmsUserByUserId(Integer.valueOf(userId));
			if(pmsUser==null||pmsUser.getUserIsdel()==1||pmsUser.getUserStatus()==0){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_143), HttpStatus.OK);
			}
			LoanApplyInfo applyInfo = loanApplyService.getApplyInfoById(Integer.parseInt(loanId));
			//主合同
			if(StringUtil.isNullOrEmpty(applyInfo.getLoanContractNumber())) {
				applyInfo.setLoanContractNumber(loanModule.getContractCode(OperationCode.CODE_100.getCode(), true));
			}
			//授权流水号
			if(StringUtil.isNullOrEmpty(applyInfo.getAuthorizationCode())){
				applyInfo.setAuthorizationCode(loanModule.getAuthorizedSerialCode(true));
			}
			//得到合同模板信息
			String jsonString = appLoanApplyService.getLoanContractTemplateJsonString(applyInfo.getLoanContractTypeId(), Integer.parseInt(loanId));

			Map<String, Object> map = JsonTools.parseJSON2Map(jsonString);
			if (map != null && map.size() > 0) {
				List<AutoBaseTemplate> templateList = configModule.getAutoTemplateProvider().getTemplateListByLoanType(String.valueOf(applyInfo.getLoanContractTypeId()), LoanProcessTypeEnum.CONTRACT.type,null);
				boolean is_Have_Loan_Contract = false;
				if(CollectionUtils.isNotEmpty(templateList)){
					Map<String, Object> customerMap = new HashMap<String, Object>();
					for (AutoBaseTemplate autoTemplate : templateList) {
						String tableName = autoTemplate.getTableName();
						DataTable dataTable = loanApplyService.getLoanTemplateDataById(tableName, Integer.valueOf(loanId));
						List<Map<String, Object>> list = (List<Map<String, Object>>) map.get(tableName);
						if (CollectionUtils.isNotEmpty(list)) {
							for (Map<String, Object> dataMap : list) {
								setCustomerMap(customerMap, dataMap);
								dataTable.setName(tableName);
								dataTable.addColumn("ID");
								String dataId = (String) customerMap.get("ID");
								dataTable.newRow();
								DataRow dataRow = dataTable.getRow(0);
								dataRow.set("LOAN_ID", loanId);
								dataRow.set("LOAN_PROCESS_TYPE", LoanProcessTypeEnum.CONTRACT.type);
								dataRow.set("CREATE_DATE", new Date());
								dataRow.set("CREATE_USER", loginUserId);
								dataRow.set("UPDATE_DATE", new Date());
								dataRow.set("UPDATE_USER", loginUserId);

								List<AutoBaseField> fieldList = autoTemplate.getFields();
								setDataRowByFieldList(dataRow, fieldList, customerMap);
								if("LOAN_CONTRACT".equals(tableName)){
									is_Have_Loan_Contract = true;
									String isGuaranteeContract = (String)dataRow.get("IS_GUARANTEE_CONTRACT");
									String isCollateralContract = (String)dataRow.get("IS_COLLATERAL_CONTRACT");
									String isPledgeContract = (String)dataRow.get("IS_PLEDGE_CONTRACT");
									if(StringUtil.isNullOrEmpty(isGuaranteeContract)||"02".equals(isGuaranteeContract)){
										applyInfo.setGuaranteeContractNo("");
									}else{
										applyInfo.setGuaranteeContractNo(StringUtil.isNullOrEmpty(applyInfo.getGuaranteeContractNo()) ? loanModule.getContractCode(OperationCode.CODE_101.getCode(), true) : applyInfo.getGuaranteeContractNo());
									}
									if(StringUtil.isNullOrEmpty(isCollateralContract)||"02".equals(isCollateralContract)){
										applyInfo.setMortgageContractNo("");
									}else {
										applyInfo.setMortgageContractNo(StringUtil.isNullOrEmpty(applyInfo.getMortgageContractNo())? loanModule.getContractCode(OperationCode.CODE_101.getCode(), true) : applyInfo.getMortgageContractNo());

									}
									if(StringUtil.isNullOrEmpty(isPledgeContract)||"02".equals(isPledgeContract)){
										applyInfo.setPledgeContractNo("");
									}else {
										applyInfo.setPledgeContractNo(StringUtil.isNullOrEmpty(applyInfo.getPledgeContractNo()) ? loanModule.getContractCode(OperationCode.CODE_101.getCode(), true) : applyInfo.getPledgeContractNo());
									}
								}

//								loanApplyService.saveLoanTemplateInfo(dataTable);

							}
						}
					}
				}
				if(!is_Have_Loan_Contract){
					applyInfo.setGuaranteeContractNo("");
					applyInfo.setMortgageContractNo("");
					applyInfo.setPledgeContractNo("");
				}
				applyInfo.setContractCheckUser(Integer.valueOf(userId));
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.SIGN.type);
				applyInfo.setContractSubmitDate(new Date());
				PmsUser_Query pmsUser_query = permissionService.getUserById(Integer.valueOf(loginUserId));
				PmsDept_Query dept = permissionService.getDeptById(pmsUser_query.getUserDeptId());
				//合同编码
				//一般 最高额 卡贷宝
				if(applyInfo.getLoanContractTypeId()!=null){
					if(applyInfo.getLoanContractTypeId()==3){
						applyInfo.setContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getLoanContractNumber())?"":"中农商银（"+dept.getDeptName()+"）个借字["+DateUtil.getYear(new Date())+"]第"+ applyInfo.getLoanContractNumber().substring(9,16)+"号");
						applyInfo.setGuaranteeContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getGuaranteeContractNo()) ? "" :"中农商银（" + dept.getDeptName() + "）保字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getGuaranteeContractNo().substring(9, 16) + "号");
						applyInfo.setMortgageContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getMortgageContractNo()) ? "" :"中农商银（" + dept.getDeptName() + "）抵字[" + DateUtil.getYear(new Date()) + "]第" +  applyInfo.getMortgageContractNo().substring(9, 16) + "号");
						applyInfo.setPledgeContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getPledgeContractNo()) ? "" : "中农商银（" + dept.getDeptName() + "）质字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getPledgeContractNo().substring(9, 16) + "号");
					}else if(applyInfo.getLoanContractTypeId()==4){
						applyInfo.setContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getLoanContractNumber())?"":"中农商银（"+dept.getDeptName()+"）高借字["+DateUtil.getYear(new Date())+"]第"+ applyInfo.getLoanContractNumber().substring(9,16)+"号");
						applyInfo.setGuaranteeContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getGuaranteeContractNo()) ? "" :"中农商银（" + dept.getDeptName() + "）高保字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getGuaranteeContractNo().substring(9, 16) + "号");
						applyInfo.setMortgageContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getMortgageContractNo()) ? "" :"中农商银（" + dept.getDeptName() + "）高抵字[" + DateUtil.getYear(new Date()) + "]第" +  applyInfo.getMortgageContractNo().substring(9, 16) + "号");
						applyInfo.setPledgeContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getPledgeContractNo()) ? "" : "中农商银（" + dept.getDeptName() + "）高质字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getPledgeContractNo().substring(9, 16) + "号");
					}else if(applyInfo.getLoanContractTypeId()==5){
						applyInfo.setContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getLoanContractNumber())?"":"中农商银（"+dept.getDeptName()+"）卡高借字["+DateUtil.getYear(new Date())+"]第"+ applyInfo.getLoanContractNumber().substring(9,16)+"号");
						applyInfo.setGuaranteeContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getGuaranteeContractNo()) ? "" : "中农商银（" + dept.getDeptName() + "）高保字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getGuaranteeContractNo().substring(9, 16) + "号");
						applyInfo.setMortgageContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getMortgageContractNo()) ? "" : "中农商银（" + dept.getDeptName() + "）高抵字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getMortgageContractNo().substring(9, 16) + "号");
						applyInfo.setPledgeContractCode(banger.common.tools.StringUtil.isNullOrEmpty(applyInfo.getPledgeContractNo()) ? "" : "中农商银（" + dept.getDeptName() + "）高质字[" + DateUtil.getYear(new Date()) + "]第" + applyInfo.getPledgeContractNo().substring(9, 16) + "号");
					}
				}
				if (!banger.common.tools.StringUtil.isNullOrEmpty(loanId)) {
					//保存贷款信息前核对借款人、担保人、抵质押物客户信息，根据接口QRY00402查询客户编码
					String result = "success";
					//借款人
					result = checkCusNoInfo("LBIZ_PERSONAL_INFO", Integer.parseInt(loanId),null);
					if (!"success".equals(result)) {
//						renderText(false, result, "json");
//						return;
						return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);
					}
					Map<String, String> map1 = new HashMap<String, String>();
					map1.put("IS_GUARANTEE_CONTRACT", "LBIZ_LOAN_GUARANTEE");
					map1.put("IS_COLLATERAL_CONTRACT", "LBIZ_PLEDGE_ITEM");
					map1.put("IS_PLEDGE_CONTRACT", "LBIZ_PLEDGE_ITEM");
					DataTable table = loanApplyService.getLoanTemplateDataById(LoanProcessTypeEnum.CONTRACT.type,"LOAN_CONTRACT", Integer.parseInt(loanId));
					if (table != null && table.rowSize() > 0) {
						DataRow row = table.getRow(0);
						//担保、抵质押
						for (Map.Entry<String, String> entry : map1.entrySet()) {
							if ("01".equals(row.get(entry.getKey()))) {
								Integer mortgageOrPledge = "IS_GUARANTEE_CONTRACT".equals(entry.getKey())?null:("IS_COLLATERAL_CONTRACT".equals(entry.getKey())?1:2);
								result = checkCusNoInfo(entry.getValue(), Integer.parseInt(loanId),mortgageOrPledge);
								if (!"success".equals(result)) {
//									renderText(false, result, "json");
//									return;
									return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);

								}
							}
						}
//						renderText(true, "核对成功", "json");
					} else {
//						renderText(false, "贷款状态异常", loanId);
						return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,"贷款状态异常"), HttpStatus.OK);
					}
				}

				loanApplyService.updateApplyInfo(applyInfo, Integer.valueOf(loginUserId));
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.valueOf(loanId), LoanOperationType.LOAN_CONTRACT_SUBMIT.typeName, "", Integer.valueOf(loginUserId), LoanProcessTypeEnum.CONTRACT.type);
				loanHandOverProvider.sendLoanMsg(applyInfo,null);
				return  new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_100), HttpStatus.OK);
			}else {
				return  new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
			}


		}catch (Exception e) {
			log.error("合同提交失败",e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
		}
	}

	//核对客户号信息
	private String checkCusNoInfo(String tableName,Integer loanId,Integer mortgageOrPledge){
		//1.首先调用省信贷客户查询接口，查询到 则更新客户编码，更新为存量客户
		//2.查询不到调用核心客户查询接口 查询到 更新客户编码
		//3.还是查询不大的话 不允许提交 提示借款人尚未再核心开户
		DataTable table = null;
		if(mortgageOrPledge!=null){
			table = loanModule.getPledgeDataTableByLoanId(LoanProcessTypeEnum.INVESTIGATE.type, loanId, 1);
		}else {
			table = loanApplyService.getLoanTemplateDataById(LoanProcessTypeEnum.INVESTIGATE.type,tableName, loanId);
		}
		if(table!=null || table.rowSize()>0){
			table.setName(tableName);
			for (int r = 0; r < table.getRows().length; r++) {
				DataRow row = table.getRow(r);
				Map<String, String> map = new HashMap<String, String>();
				if("LBIZ_LOAN_GUARANTEE".equals(tableName)){
					map.put("证件号码", (String) row.get("ID_CARD"));
					map.put("证件类型", (String) row.get("IDENTIFY_TYPE"));
				}else if("LBIZ_PLEDGE_ITEM".equals(tableName)){
					map.put("证件号码",(String)row.get("ID_NUM"));
				}else if("LBIZ_PERSONAL_INFO".equals(tableName)){
					map.put("证件号码", (String) row.get("IDENTIFY_NUM"));
					map.put("证件类型", (String) row.get("IDENTIFY_TYPE"));
				}
				//可能存在空记录 筛掉
				if(banger.common.tools.StringUtil.isNullOrEmpty(map.get("证件号码"))){
					continue;
				}
				String cusName="";
				if("LBIZ_PERSONAL_INFO".equals(tableName)){
					cusName = (String)row.get("CUSTOMER_NAME");
				}else if("LBIZ_PLEDGE_ITEM".equals(tableName)){
					cusName = (String)row.get("PLEDGE_OWNER");
				}else if("LBIZ_LOAN_GUARANTEE".equals(tableName)){
					cusName = (String)row.get("FULL_NAME");
				}
				//信贷客户查询
				String returnStr = loanModule.relatedDataQuery(loanId, map, SocketCodeTypeEnum.CHNCMI4050);
				JSONObject jsonObject = JSONObject.fromObject(returnStr);
				if(jsonObject.getBoolean("success") && !banger.common.tools.StringUtil.isNullOrEmpty(jsonObject.getString("cus_id"))){
					row.set("CUSTOMER_NUM",jsonObject.getString("cus_id"));
					row.set("IS_STOCK_CUST","01");
					//20-正式客户  91-合并注销 00-草稿客户（正式） 01-临时客户 02-草稿客户（担保） 03-担保客户
					//1.抵质人和借款人必须是正式客户
					String cus_status = jsonObject.getString("cus_status");
					if(("LBIZ_PERSONAL_INFO".equals(tableName)|| "LBIZ_PLEDGE_ITEM".equals(tableName) )&& !"20".equals(cus_status)){
						return cusName + "必须是正式客户!";
					}
					//2.担保人
					if("LBIZ_LOAN_GUARANTEE".equals(tableName) && !("20".equals(cus_status)||"03".equals(cus_status))){
						return cusName + "必须是正式客户或者担保客户!";
					}
				}else{
					//核心查询
					row.set("IS_STOCK_CUST","02");
					returnStr = loanModule.queryCusInfo(loanId, map, SocketCodeTypeEnum.QRY00402);
					jsonObject = JSONObject.fromObject(returnStr);
					if(jsonObject.getBoolean("success") && !banger.common.tools.StringUtil.isNullOrEmpty(jsonObject.getString("cus_no"))){
						row.set("CUSTOMER_NUM",jsonObject.getString("cus_no"));
					}else {
						//查询失败不允许保存
						if( "-1".equals(jsonObject.getString("code"))){
							return cusName+"核心查询客户失败!原因："+jsonObject.getString("msg");
						}else{
							//借款人，抵质押人必须在核心已开户
							if("LBIZ_PERSONAL_INFO".equals(tableName)||"LBIZ_PLEDGE_ITEM".equals(tableName)){
								return cusName + "在核心尚未开户!原因："+jsonObject.getString("msg");
							}
						}
					}
				}
			}
			loanApplyService.saveLoanTemplateInfo(table,true);
		}
		return "success";
	}
	/**
	 * 合同驳回
	 */
	@RequestMapping(value = "/v1/contractGiveBack")
	@NoLoginInterceptor
	public ResponseEntity<String> contractGiveBack(HttpServletRequest request,HttpServletResponse respons){

		try {
			String loanId = this.getParameter("loanId");
			String userId = this.getParameter("userId");
			if(StringUtil.isNullOrEmpty(loanId)||StringUtil.isNullOrEmpty(userId)){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
			LoanApplyInfo applyInfo = loanApplyService.getApplyInfoById(Integer.parseInt(loanId));
			Integer loginUserId = Integer.valueOf(userId);
			if (LoanProcessTypeEnum.CONTRACT.type.equals(applyInfo.getLoanProcessType())) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("loanId", loanId);
				paramMap.put("isValid", "1");
				paramMap.put("auditResult", LoanAuditResultEnum.FAMILY.value);
				paramMap.put("processId", applyInfo.getLoanAuditProcessId());
				//查出当前审核人的审核状态信息，并更新
				List<LoanCurrentAuditStatus> loanCurrentAuditStatuses = currentAuditStatusService.queryCurrentAuditStatusList(paramMap);
				if (CollectionUtils.isNotEmpty(loanCurrentAuditStatuses)) {
					LoanCurrentAuditStatus loanCurrentAuditStatus = loanCurrentAuditStatuses.get(0);
					loanCurrentAuditStatus.setAuditResult(LoanAuditResultEnum.PERSONAL.value);
					currentAuditStatusService.updateCurrentAuditStatus(loanCurrentAuditStatus, loginUserId);
				}
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.APPROVAL.type);
				loanApplyService.updateApplyInfo(applyInfo, loginUserId);
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.valueOf(loanId), LoanOperationType.LOAN_CONTRACT_BACK.typeName, "", loginUserId, LoanProcessTypeEnum.CONTRACT.type);
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_100), HttpStatus.OK);
			}else {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_126), HttpStatus.OK);
			}
		}catch (Exception e) {
			log.error("合同签订失败",e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
		}
	}


	/**
	 * http://127.0.0.1:8080/api/v1/getLoanApplyList.html?userId=1
	 * 得到贷款申请字段数据
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanApplyList")
	public ResponseEntity<String> getLoanApplyList(HttpServletRequest request,HttpServletResponse response){
		String offset = this.getParameter("offset");
		String userId = this.getParameter("userId");
		String loanProcessTypes = this.getParameter("loanProcessTypes");
		String listType = this.getParameter("listType");
		Map<String,Object> map = RequestMapUtil.getMapOfRequest(request, "loanName,loanTypeIds,loanProcessTypes,loanCollectionState,loanCollectionReminderState,loanBadCollectionState,MonitorState,createDate,createDateEnd,loanApplyAmount,loanApplyAmountEnd");
		ConvertTypeUtil.convertArrayToMap(map,"loanProcessTypes");
		ConvertTypeUtil.convertArrayToMap(map,"loanTypeIds");
		ConvertTypeUtil.convertDateToMap(map,"createDate,createDateEnd");
		ConvertTypeUtil.convertDecimalToMap(map,"loanApplyAmount,loanApplyAmountEnd");

		try {
			Integer loginUserId = this.getUserId();
			if(loginUserId!=null){
				if (LoanProcessTypeEnum.APPROVAL.type.equals(listType)) {
					map.put("auditUserId", loginUserId.toString());
				}else if(LoanProcessTypeEnum.SIGN.type.equals(listType)){
					map.put("contractCheckUser",loginUserId.toString());
				} else {
					map.put("userId", loginUserId);			//这里要权限判断
					Integer[] roleIds = permissionService.getRoleIdsByUserId(loginUserId);
					map.put("roleIds", roleIds);
				}
//				if(StringUtil.isNotEmpty(loanProcessTypes))
//					map.put("loanRefuseUser", 0);
				String orderby = this.getOrderBy(map);
				if(StringUtil.isNotEmpty(orderby))
					map.put("orderby",orderby);
				IPageSize page = PageSizeUtil.getPageLimit(StringUtil.isNotEmpty(offset)?Integer.parseInt(offset):0,AppParamsConst.PAGE_SIZE_PROUCT_LIST);
				//显示催收贷款(当前日期+催收设置天数>=应还款时间)
//				SysBasicConfig cssz = basicConfigProvider.getSysBasicConfigByKey("cfsz");
//				Date collectionDate = DateUtil.addDay(DateUtil.getCurrentDate(),cssz.getConfigValue());
//				map.put("collectionDate",collectionDate);
//				map.put("collectionType",collectionType);
				map.put("nowDate",DateUtil.getNeedTime(0,0,0,0));
//				if (map.containsKey("loanCollectionReminderState")) {
//					map.put("collectionType", "1");
//				}
//				if (map.containsKey("loanBadCollectionState")) {
//					map.put("collectionType", "2");
//				}
//				if (map.containsKey("loanCollectionReminderState") && map.containsKey("loanBadCollectionState")) {
//					map.put("collectionType", null);
//				}
				List<LoanApplyInfo> applyList = loanModule.getLoanApplyProvider().getLoanApplyList(map,page);
				this.setLoanApplyInfoList(applyList);
				JSONArray resArray = AppJsonUtil.toJsonArray(applyList, RESULT_LOAN_APPLY_LIST_PARAMS);
				return new ResponseEntity<String>(AppJsonResponse.success(resArray), HttpStatus.OK);
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_97), HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorUtil.logError("查询贷款申请列表接口异常",log,e,request);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	private void setLoanApplyInfoList(List<LoanApplyInfo> applyList){
		List<LoanType> typeList = loanSurveryFlowProvider.getAllLoanTypeList();
		Map<Integer,LoanType> typeMap = new HashMap<Integer, LoanType>();
		Date date21 = getNextDay(21);

		for(LoanType loanType : typeList){
			typeMap.put(loanType.getTypeId(),loanType);
		}
		for(LoanApplyInfo loanApplyInfo : applyList) {
			loanApplyInfo.setLoanRepayDate(date21);

			if(LoanProcessTypeEnum.CLEARANCE.type.equals(loanApplyInfo.getLoanProcessType())){
				loanApplyInfo.setLoanCollectionState("");
				loanApplyInfo.setLoanMonitorState("");
			}
			LoanApplyInfo_Query loanApplyInfoQuery = (LoanApplyInfo_Query)loanApplyInfo;
			if(loanApplyInfoQuery.getLoanTypeId()!=null && typeMap.containsKey(loanApplyInfoQuery.getLoanTypeId())){
				LoanType loanType = typeMap.get(loanApplyInfoQuery.getLoanTypeId());
				if(loanType!=null && loanType.getFlowId()!=null && loanType.getFlowId().intValue()>0){
					loanApplyInfoQuery.setLoanStep(true);			//设置了贷款流程才显示
				}
			}
		}
	}

	private Date getNextDay(int day){
		Date date = new Date();
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		int currDay = ca.get(Calendar.DAY_OF_MONTH);
		if(currDay>day){
			ca.add(Calendar.MONTH,1);
		}
		ca.set(Calendar.DAY_OF_MONTH,day);
		return ca.getTime();
	}

	private String getOrderBy(Map<String,Object> map){
		String loanProcessTypes = this.getParameter("loanProcessTypes");

//		String loanCollectionState = this.getParameter("loanCollectionState");
		String loanMonitorState = this.getParameter("loanMonitorState");

		if("AfterLoan".equals(loanProcessTypes)){
			if("Monitor".equals(loanMonitorState) ){
				map.put("loanMonitorState","Monitor");
				return "ORDER BY LOAN_MONITOR_DATE ASC";
			}
		}else if ("Collection".equals(loanProcessTypes)){
			map.put("isOverdue",this.getParameter("isOverdue"));
			map.put("isEnough",this.getParameter("isEnough"));
			map.put("loanProcessTypes",new String[]{LoanProcessTypeEnum.AFTER_LOAN.type});
			return "ORDER BY OVERDUE_LIMIT DESC";
		}
		return "";
	}
	
	/**
	 * http://127.0.0.1:8080/loan/api/v1/getLoanTemplate.html?loanId=172&loanTypeId=1&loanProcessType=Approval
	 * 得到贷款模板字段
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanTemplate")
	public ResponseEntity<String> getLoanTemplate(HttpServletRequest request,HttpServletResponse response){
		String loanTypeId = this.getParameter("loanTypeId");
		String loanProcessType = this.getParameter("loanProcessType");
		String loanId = this.getParameter("loanId");
		try {
			if(StringUtil.isNotEmpty(loanTypeId) && StringUtil.isNotEmpty(loanProcessType)){
				String processType = LoanProcessTypeEnum.valueOfType(loanProcessType).type;
				if(StringUtil.isNotEmpty(loanId)){
					String jsonString = appLoanApplyService.getLoanTemplateJsonString(Integer.parseInt(loanTypeId),processType,Integer.parseInt(loanId));
					return new ResponseEntity<String>(jsonString, HttpStatus.OK);
				}else{
					String jsonString = appLoanApplyService.getLoanTemplateJsonString(Integer.parseInt(loanTypeId),processType);
					return new ResponseEntity<String>(jsonString, HttpStatus.OK);
				}
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
			}
		}catch(Exception e){
			ErrorUtil.logError("得到贷款模板字段数据异常",log,e,request);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	
	/**
	 *
	 * 得到贷款模板字段
	 * @param request http://localhost:8080/loan/api/v1/getLoanTemplateField.html?loanId=4&loanTypeId=1&loanProcessType=Apply&tableId=1
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanTemplateField")
	public ResponseEntity<String> getLoanTemplateField(HttpServletRequest request,HttpServletResponse response){
		String loanTypeId = this.getParameter("loanTypeId");
		String loanProcessType = this.getParameter("loanProcessType");
		String tableId = this.getParameter("tableId");
		String loanId = this.getParameter("loanId");
		String listAddFlag = this.getParameter("listAddFlag");
		
		Integer loginUserId = this.getUserId();
		LoanApplyInfo loanInfo = loanModule.getLoanApplyProvider().getApplyInfoById(Integer.parseInt(loanId));
		if(loginUserId!=null && loanInfo!=null){
			if(!loanInfo.getLoanBelongId().equals(loginUserId)){
				String groupIds = AppSessionManage.getSession(loginUserId).getTeamGroupIdByRole(false);
				String checkData = permissionModule.checkDataByUserId(groupIds, loginUserId);
				if (StringUtils.isNotBlank(checkData)) {
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_99), HttpStatus.OK);
				}
			}
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_127), HttpStatus.OK);
		}
		
		try {
			if(StringUtils.isNotBlank(listAddFlag)&&listAddFlag.equals("spouse")){
				//查询配偶信息，拼装担保人模 版
				String processType = LoanProcessTypeEnum.valueOfType(loanProcessType).type;
				String jsonString = appLoanApplyService.getGuaFieldBySpouse(Integer.parseInt(loanTypeId),processType,Integer.parseInt(tableId),Integer.parseInt(loanId),listAddFlag);
				return new ResponseEntity<String>(jsonString, HttpStatus.OK);
			}else{
				if(StringUtil.isNotEmpty(loanTypeId) && StringUtil.isNotEmpty(loanProcessType)){
					String processType = LoanProcessTypeEnum.valueOfType(loanProcessType).type;
					if(StringUtil.isNotEmpty(tableId)){
						if(StringUtil.isNotEmpty(loanId)){
							String jsonString = appLoanApplyService.getLoanTemplateFieldJsonString(Integer.parseInt(loanTypeId),processType,Integer.parseInt(tableId),Integer.parseInt(loanId),listAddFlag);
							return new ResponseEntity<String>(jsonString, HttpStatus.OK);
						}else{
							String jsonString = appLoanApplyService.getLoanTemplateFieldJsonString(Integer.parseInt(loanTypeId),processType,Integer.parseInt(tableId));
							return new ResponseEntity<String>(jsonString, HttpStatus.OK);
						}
					}else{
						String jsonString = appLoanApplyService.getLoanTemplateFieldJsonString(Integer.parseInt(loanTypeId),processType);
						return new ResponseEntity<String>(jsonString, HttpStatus.OK);
					}
				}else{
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
				}
			}
		}catch(Exception e){
			ErrorUtil.logError("得到贷款模板字段数据异常",log,e,request);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}


	/**
	 * 保存贷款申请信息
	 * @param request  {loanTypeId:"",loanName:"",loanIdentifyType:"",loanIdentifyNum:"",loanTelNum:"",createUser:""}
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@SqlTransaction				//开启数据库事务
	@RequestMapping(value = "/v1/saveLoanApplyInfo", method = RequestMethod.POST)
	@TokenRepeatAnnotation
	public ResponseEntity<String> saveLoanApplyInfo(HttpServletRequest request,HttpServletResponse response){
		String reqJson = HttpParseUtil.getJsonStr(request);
		if(StringUtils.isBlank(reqJson)){
			log.error("保存贷款申请信息接口，参数为空");
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
		LoanApplyInfo loanApplyInfo = null;
		Integer applyId = -1;
		Integer userId = null;
		Integer potentialId=0;
		try {
			JSONObject reqObj = JSONObject.fromObject(reqJson);

			userId =  reqObj.containsKey("userId")? reqObj.getInt("userId"):null;
			potentialId= reqObj.containsKey("potentialId")? reqObj.getInt("potentialId"):0;

			String containsKeys = AppJsonUtil.containsKeys(reqObj, SAVE_LOAN_APPLY_INFO_PARAMS);
			if(containsKeys == null){
				loanApplyInfo = (LoanApplyInfo) JSONObject.toBean(reqObj,LoanApplyInfo.class);
				///是否启用三要素校验控制开关 1 启用 2 关闭
				SysBasicConfig sysBasicConfig = basicConfigProvider.getSysBasicConfigByKey("sysjy");
				if(StringUtils.equals(sysBasicConfig.getConfigStatus(),"1")){
					//true 客户归属于当前客户经理 false 不归属于
					boolean isBelong = isCustomerBelongToUser(loanApplyInfo,userId);
					if(!isBelong){
						//客户不归属与当前客户经理
						return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_137,"客户不归属于当前客户经理，信息保存失败！"), HttpStatus.OK);
					}
				}
				if(reqObj.containsKey("applyId")){
					applyId = reqObj.getInt("applyId");
				}
			} else {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,"参数不完整！"), HttpStatus.OK);
			}

			if(loanApplyInfo.getLoanApplyAmount()!=null && loanApplyInfo.getLoanApplyAmount().doubleValue()<=0){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_135, "申请金额必须大于0"), HttpStatus.OK);
			}

			loanApplyInfo.setCreateUser(userId);//创建用户
			loanApplyInfo.setUpdateUser(userId);//更新用户

		} catch (Exception e) {
			log.error("保存贷款申请信息接口，json解析出错|"+reqJson,e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
		}

		String checkResult = IdCardUtil.checkIdCard(loanApplyInfo.getLoanIdentifyType(),loanApplyInfo.getLoanIdentifyNum());	//身份证格式，校验
		if(!"pass".equals(checkResult)){
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_128), HttpStatus.OK);
		}

		boolean isBlack = customerBlackProvider.isExistCustomerBlack(loanApplyInfo.getLoanName(),loanApplyInfo.getLoanIdentifyType(),loanApplyInfo.getLoanIdentifyNum());

		if(!isBlack) {
			if (applyId.intValue() > 0) {			//进件Id不为空
 				loanModule.getLoanApplyProvider().saveApplyInfo(loanApplyInfo,applyId);
				intoCustomersProvider.updateIntoCustomerLoanId(applyId,loanApplyInfo.getLoanId());
				Map<String,Object> condition = new HashMap();
				condition.put("applyId",applyId);
				List<IntoFileInfo> intoFileInfoList = intoCustomersProvider.queryFileInfoList(condition);

				for (IntoFileInfo intoFileInfo : intoFileInfoList){
					LoanInfoAddedFiles loanInfoAddedFiles = new LoanInfoAddedFiles();
					loanInfoAddedFiles.setOwnerId(0);
					loanInfoAddedFiles.setClassId(0);
					loanInfoAddedFiles.setLoanId(loanApplyInfo.getLoanId());
					loanInfoAddedFiles.setLoanProcessType("Apply");
					loanInfoAddedFiles.setIsDel(0);
					if(intoFileInfo.getFileName()!=null){
						String suffix = intoFileInfo.getFileName().substring(intoFileInfo.getFileName().lastIndexOf("."));
						String name = intoFileInfo.getFileName().substring(0, intoFileInfo.getFileName().lastIndexOf("."));

						loanInfoAddedFiles.setFileId(name);
						loanInfoAddedFiles.setFileFix(suffix);
						loanInfoAddedFiles.setFileName(intoFileInfo.getFileName());
					}
					loanInfoAddedFiles.setFileType("Image");
					loanInfoAddedFiles.setFilePath(intoFileInfo.getFilePath());
					loanInfoAddedFiles.setFileSize(intoFileInfo.getFileSize());
					loanInfoAddedFiles.setFileSrcName(intoFileInfo.getFileSrcName());
					loanInfoAddedFiles.setFileThumbImagePath(intoFileInfo.getFileThumbImagePath());
					loanInfoAddedFiles.setFileThumbImageName(intoFileInfo.getFileThumbImageName());
					loanInfoAddedFiles.setCallTime(0);
					loanInfoAddedFiles.setMonitorId(0);
					iInfoAddedFileProvider.insertInfoAddedFiles(loanInfoAddedFiles,userId);
				}
			}else{
				if (potentialId>0){
					loanApplyInfo.setPotentialCustomerId(potentialId);
				}
				loanModule.getLoanApplyProvider().saveApplyInfo(loanApplyInfo);
			}
			JSONObject rjo = new JSONObject();
			rjo.put("loanId", loanApplyInfo.getLoanId());
			rjo.put("loanTypeId", loanApplyInfo.getLoanTypeId());
			rjo.put("loanProcessType", loanApplyInfo.getLoanProcessType());
			rjo.put("loanCustomerId", loanApplyInfo.getLoanCustomerId());
			return new ResponseEntity<String>(AppJsonResponse.success(rjo), HttpStatus.OK);
		}else {
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_117), HttpStatus.OK);
		}
	}

	/**
	 * 根据三要素判断客户是否归属于当前客户经理
	 * @param loanApplyInfo
	 * @return
	 */
	private boolean isCustomerBelongToUser(LoanApplyInfo loanApplyInfo, Integer loginUserId){
		String customerName = loanApplyInfo.getLoanName();  //贷款人姓名
		String identifyNum = loanApplyInfo.getLoanIdentifyNum();   //证件号码
		String identifyType = loanApplyInfo.getLoanIdentifyType();  //证件类型
		CustBasicInfo custBasicInfo = customerModuleIntf.getCustomerByCardNameType(customerName,identifyType,identifyNum);
		if(custBasicInfo != null && loginUserId != null){
			if(custBasicInfo.getBelongUserId().intValue() != loginUserId.intValue()){
				return false;
			}
		}
		return true;
	}

	/**
	 * 保存贷款模板信息
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/saveLoanTemplateInfo", method = RequestMethod.POST)
	public ResponseEntity<String> saveLoanTemplateInfo(HttpServletRequest request,HttpServletResponse response){
		String reqJson = null;
		try {
			reqJson=HttpParseUtil.getJsonStr(request);
			if(StringUtils.isBlank(reqJson)){
				log.error("保存贷款申请信息接口，参数为空");
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
			try {
				JSONObject reqObj = JSONObject.fromObject(reqJson);
				String resultJson = appLoanTemplateService.saveLoanTemplateInfo(reqObj);
				return new ResponseEntity<String>(resultJson, HttpStatus.OK);
			} catch (Exception e) {
				log.error("保存贷款申请信息接口，json解析出错|"+reqJson,e);
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
			}
			
			//return new ResponseEntity<String>(AppJsonResponse.success(rjo), HttpStatus.OK);
			
		} catch (Exception e) {
			log.error("查询意向客户详情接口异常|"+reqJson,e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 * 拒绝贷款申请信息
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/refuseLoanApplyInfo", method = RequestMethod.POST)
	public ResponseEntity<String> refuseLoanApplyInfo(HttpServletRequest request,HttpServletResponse response){
		String reqJson = null;
		try {
			reqJson=HttpParseUtil.getJsonStr(request);
			if(StringUtils.isBlank(reqJson)){
				log.error("保存贷款申请信息接口，参数为空");
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
			LoanApplyInfo loanApplyInfo = null;
			boolean isBlack = false;
			Integer userId;
			try {
				JSONObject reqObj = JSONObject.fromObject(reqJson);
				String containsKeys = AppJsonUtil.containsKeys(reqObj, REFUSE_LOAN_APPLY_INFO_PARAMS);
				if(containsKeys == null){
					if(reqObj.containsKey("isBlack")){
						isBlack = reqObj.getBoolean("isBlack");
					}
					userId = reqObj.getInt("userId");
					loanApplyInfo = (LoanApplyInfo) JSONObject.toBean(reqObj,LoanApplyInfo.class);
					if ("99".equals(loanApplyInfo.getLoanRefuseReason()) && StringUtil.isNullOrEmpty(loanApplyInfo.getLoanRefuseRemark())) {
						return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,"拒绝理由选择其他时，备注不能为空"), HttpStatus.OK);
					}
				}else {
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,containsKeys+"参数不能为空！"), HttpStatus.OK);
				}
			} catch (Exception e) {
				log.error("保存贷款申请信息接口，json解析出错|"+reqJson,e);
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
			}

			loanModule.getLoanApplyProvider().refuseApplyInfo(loanApplyInfo, userId);

			if(isBlack){
				LoanApplyInfo loanApplyInfoQuery = loanModule.getLoanApplyProvider().getApplyInfoById(loanApplyInfo.getLoanId());
				if(loanApplyInfoQuery!=null){
					CustCustomerBlack custCustomerBlack = new CustCustomerBlack();
					custCustomerBlack.setIsDel(0);
					custCustomerBlack.setCustomerName(loanApplyInfoQuery.getLoanName());
					custCustomerBlack.setCardType(loanApplyInfoQuery.getLoanIdentifyType());
					custCustomerBlack.setCardNo(loanApplyInfoQuery.getLoanIdentifyNum());
					custCustomerBlack.setCustomerId(loanApplyInfo.getLoanCustomerId());
					custCustomerBlack.setApproveStatus(2);
					custCustomerBlack.setCustomerId(loanApplyInfo.getLoanCustomerId());
					custCustomerBlack.setApproveDate(new Date());
					custCustomerBlack.setApproveUserId(loanApplyInfo.getCreateUser());
					custCustomerBlack.setCreateUser(loanApplyInfo.getCreateUser());
					customerBlackProvider.saveCustomerBlack(custCustomerBlack);
				}
			}

			JSONObject rjo = new JSONObject();
			rjo.put("loanId", loanApplyInfo.getLoanId());
			rjo.put("loanTypeId", loanApplyInfo.getLoanTypeId());
			rjo.put("loanProcessType", loanApplyInfo.getLoanProcessType());

			return new ResponseEntity<String>(AppJsonResponse.success(rjo), HttpStatus.OK);

		} catch (Exception e) {
			log.error("查询意向客户详情接口异常|"+reqJson,e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 * 贷款申请信息提交
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@SqlTransaction				//开启数据库事务
	@RequestMapping(value = "/v1/submitLoanApplyInfo", method = RequestMethod.POST)
	public ResponseEntity<String> submitLoanApplyInfo(HttpServletRequest request,HttpServletResponse response){
		String reqJson=HttpParseUtil.getJsonStr(request);
		if(StringUtils.isBlank(reqJson)){
			log.error("保存贷款申请信息接口，参数为空");
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
		LoanApplyInfo loanApplyInfo = null;
		try {
			JSONObject reqObj = JSONObject.fromObject(reqJson);
			String containsKeys = AppJsonUtil.containsKeys(reqObj, SUBMIT_LOAN_APPLY_INFO_PARAMS);
			if(containsKeys == null){
				loanApplyInfo = (LoanApplyInfo) JSONObject.toBean(reqObj,LoanApplyInfo.class);
			}else {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,containsKeys+"参数不能为空！"), HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error("保存贷款申请信息接口，json解析出错|"+reqJson,e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
		}

		String result = loanModule.getLoanApplyProvider().submitLoanApplyInfo(loanApplyInfo);

		if("success".equals(result)) {
			JSONObject rjo = new JSONObject();
			rjo.put("loanId", loanApplyInfo.getLoanId());
			rjo.put("loanTypeId", loanApplyInfo.getLoanTypeId());
			rjo.put("loanProcessType", loanApplyInfo.getLoanProcessType());
			return new ResponseEntity<String>(AppJsonResponse.success(rjo), HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_121,result), HttpStatus.OK);
		}
	}

	/**
	 * http://127.0.0.1:8080/loan/api/v1/getLoanAuditStateInfo.html?loanId=12&processId=1
	 * 得到贷款审批状态信息
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanAuditStateInfo")
	public ResponseEntity<String> getLoanAuditStateInfo(HttpServletRequest request,HttpServletResponse response){
		String loanId = this.getParameter("loanId");
		String processId = this.getParameter("processId");
		try {
			if (StringUtil.isNotEmpty(loanId) && StringUtil.isNotEmpty(processId)) {
				String result = appLoanApplyService.getLoanAuditStateInfoJsonString(Integer.parseInt(loanId),Integer.parseInt(processId));
				return new ResponseEntity<String>(result, HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
			}
		}catch (Exception e) {
			log.error("得到贷款审批状态信息接口异常 error:"+e.getMessage(),e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 * 退回贷款申请
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@SqlTransaction				//开启数据库事务
	@RequestMapping(value = "/v1/gobackLoanApplyInfo")
	public ResponseEntity<String> gobackLoanApplyInfo(HttpServletRequest request,HttpServletResponse response){
		String reqJson = HttpParseUtil.getJsonStr(request);
		if (StringUtils.isBlank(reqJson)) {
			log.error("退回贷款申请接口，参数为空");
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
		String loanId = "";
		String remark = "";
		String loanProcessType = "";
		Integer userId;
		try {
			JSONObject reqObj = JSONObject.fromObject(reqJson);
			String containsKeys = AppJsonUtil.containsKeys(reqObj, "userId,loanId,loanProcessType");
			if(containsKeys == null){
				loanId = reqObj.containsKey("loanId")?reqObj.getString("loanId"):"";
				userId = reqObj.containsKey("userId")?reqObj.getInt("userId"): null;
				loanProcessType = reqObj.containsKey("loanProcessType")?reqObj.getString("loanProcessType"):"";
				remark = reqObj.containsKey("remark")?reqObj.getString("remark"):"";
				if(StringUtil.isNullOrEmpty(loanId)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,"loanId参数不能为空！"), HttpStatus.OK);
				}
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,containsKeys+"参数不能为空！"), HttpStatus.OK);
			}
		}catch (Exception e) {
			log.error("退回贷款申请接口，json解析出错|"+reqJson,e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
		}

		String result = loanModule.getLoanApplyProvider().gobackLoanApplyInfo(Integer.parseInt(loanId),loanProcessType, userId, remark);
		if("success".equals(result)) {
			JSONObject rjo = new JSONObject();
			rjo.put("loanId", loanId);
			return new ResponseEntity<String>(AppJsonResponse.success(rjo), HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_121,result), HttpStatus.OK);
		}
	}

	/**
	 * @Author: yangdw
	 * @params:  * @param null
	 * @Description:根据行业等级获取行业信息
	 * @Date: 10:41 2017/9/11
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getIndustryGuidelinesByGroup", method = RequestMethod.POST)
	public ResponseEntity<String> getIndustryGuidelines(HttpServletRequest request,HttpServletResponse response){
		String reqJson = null;
		try{
			reqJson= HttpParseUtil.getJsonStr(request);
			if(StringUtils.isBlank(reqJson)){
				log.error("移动端开始调取获取根据行业等级获取行业信息接口，参数为空");
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
			JSONObject reqObj = JSONObject.fromObject(reqJson);
			if(!reqObj.containsKey("industryGrade")){
				log.error("移动端开始调取获取根据行业等级获取行业信息接口，参数为空");
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_127), HttpStatus.OK);
			}
			String industryGrade = reqObj.getString("industryGrade");
			String industryParName = null;
			String industryParGrade = null;
			if (StringUtil.isNotEmpty(industryGrade)) {
				if (!"grade_first".equals(industryGrade.toLowerCase())) {
					if(!reqObj.containsKey("industryParName") || !reqObj.containsKey("industryParGrade")){
						log.error("移动端开始调取获取根据行业等级获取行业信息接口，参数为空");
						return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_127), HttpStatus.OK);
					}
					industryParName = reqObj.getString("industryParName");
					industryParGrade = reqObj.getString("industryParGrade");
				}
				//根据行业等级获取行业信息
				Map<String, Object> map = new HashedMap();
				map.put("industryGrade", industryGrade);
				map.put("industryParGrade", industryParGrade);
				map.put("industryParName", industryParName);

				List<LoanIndustryGuidelines> list = industryGuidelinesProvider.getIndustryGuidelinesByGroup(map);

				JSONArray oja = new JSONArray();
				for(LoanIndustryGuidelines option : list){
					JSONObject joo = new JSONObject();
					if("grade_first".equalsIgnoreCase(industryGrade)){
						joo.put("dictValue", option.getGradeFirst());
						joo.put("dictName", option.getGradeFirst());
						oja.add(joo);
					} else if ("grade_Second".equalsIgnoreCase(industryGrade)) {
						joo.put("dictValue", option.getGradeSecond());
						joo.put("dictName", option.getGradeSecond());
						oja.add(joo);
					} else if ("grade_third".equalsIgnoreCase(industryGrade)) {
						joo.put("dictValue", option.getGradeThird());
						joo.put("dictName", option.getGradeThird());
						oja.add(joo);
					}
				}

				return new ResponseEntity<String>(AppJsonResponse.success(oja), HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
			}
		}catch (Exception e){
			log.error("移动端开始调取获取根据行业等级获取行业信息接口异常:"+e.getMessage(),e);
			e.printStackTrace();
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
		}
	}

	/**
	 * 分配贷款
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping("/v1/allotLoan")
	@FuncPermitAnnotation(values="loanAllot")
	public ResponseEntity<String> allotLoan(HttpServletRequest request, HttpServletResponse response) {
		String reqJson = null;
		try {
			reqJson = HttpParseUtil.getJsonStr(request);
			if (StringUtils.isBlank(reqJson)) {
				log.error("分配贷款接口，参数为空");
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
				String memberUserId="";
			    String loginUserId="";
			    String loanId="";
			try {
				JSONObject reqObj = JSONObject.fromObject(reqJson);
				String containsKeys = AppJsonUtil.containsKeys(reqObj, "loanId,memberUserId,loginUserId");
				if (containsKeys == null) {
					loanId=reqObj.containsKey("loanId")?reqObj.getString("loanId"):"";
					memberUserId=reqObj.containsKey("memberUserId")?reqObj.getString("memberUserId"):"";
					loginUserId=reqObj.containsKey("loginUserId")?reqObj.getString("loginUserId"):"";

				} else {
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102, containsKeys + "参数不能为空！"), HttpStatus.OK);
				}
			} catch (Exception e) {
				log.error("分配贷款接口，json解析出错|" + reqJson, e);
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
			}
		 Map<String,Object> resultMap=loanHandOverProvider.loanAllotSignSave(loanId,Integer.parseInt(memberUserId),Integer.parseInt(loginUserId));
			boolean success = Boolean.parseBoolean(resultMap.get("success").toString());
			if (success) {
				JSONObject result=JSONObject.fromObject(resultMap);
				return  new ResponseEntity<String>(AppJsonResponse.success(result), HttpStatus.OK);
			} else {
				String message = (String) resultMap.get("message");
				return  new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR, message), HttpStatus.OK);
			}

		} catch (Exception e) {
			log.error("分配贷款接口异常 error:" + e.getMessage(), e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 *获取跳转分配贷款提交页面信息
	 */
	@NoLoginInterceptor
	@RequestMapping("/v1/toAllotLoan")
	public ResponseEntity<String> toAllotLoan(HttpServletRequest request, HttpServletResponse response) {
		String loanId = this.getParameter("loanId");
		//存团队成员信息
		JSONArray array=new JSONArray();
		//返回结果json对象
		JSONObject result=new JSONObject();
		JSONObject o=new JSONObject();
		try {
			if (StringUtil.isNotEmpty(loanId)) {
				LoanApplyInfo loanApplyInfo = loanHandOverProvider.getApplyInfoById(Integer.parseInt(loanId));
				if (loanApplyInfo != null&&loanApplyInfo.getLoanApplyUserId()!=null) {
					Integer belongId = loanApplyInfo.getLoanApplyUserId();
					if(permissionService.getUserById(belongId)!=null) {
						String belongName = permissionService.getUserById(belongId).getUserName();
						o.put("belongId", belongId);
						o.put("belongName", belongName);
					}
					Integer teamId = teamGroupProvider.queryGroupIdByUserId(belongId.toString());
					if(teamId!=null){
						List<PmsUser> list=teamGroupProvider.getCusManageListGroupId(teamId);
						for(int i=0;i<list.size();i++){
							JSONObject object=new JSONObject();
							object.put("userId",list.get(i).getUserId());
							object.put("userName",list.get(i).getUserName());
							if(belongId==list.get(i).getUserId()){
								array.add(0,object);
								continue;
							}
							array.add(i,object);
						}

						result.put("member",array);
						result.put("belong",o);
						return new ResponseEntity<String>(AppJsonResponse.success(result), HttpStatus.OK);
					}

					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_121,"该申请人没有团队"), HttpStatus.OK);
				}
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_119,"贷款信息不存在"), HttpStatus.OK);
			}
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);

		} catch (Exception e) {
			log.error("跳转到分配贷款接口异常 error:" + e.getMessage(), e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	/**
	 * 退回贷款申请
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/newLbizId")
	public ResponseEntity<String> newLbizId(HttpServletRequest request,HttpServletResponse response){
		String tableName = this.getParameter("tableName");
		if(StringUtil.isNotEmpty(tableName)) {
			try {
				Integer id = loanModule.getLoanApplyProvider().newLbizId(tableName);
				JSONObject jo = new JSONObject();
				jo.put("id", id);
				return new ResponseEntity<String>(AppJsonResponse.success(jo), HttpStatus.OK);
			}catch (Exception e) {
				log.error("程序异常 error:" + e.getMessage(), e);
			}
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
		}else {
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102, "tableName" + "参数不能为空！"), HttpStatus.OK);
		}
	}

	/**
	 *放贷驳回
	 */
	@NoLoginInterceptor
	@SqlTransaction				//开启数据库事务
	@RequestMapping("/v1/gobackLoan")
	public ResponseEntity<String> gobackLoan(HttpServletRequest request,HttpServletResponse response) {
		String loanId = this.getParameter("loanId");
		String userId = this.getParameter("userId");
		try {
				if(StringUtil.isNullOrEmpty(loanId)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102,"loanId参数不能为空！"), HttpStatus.OK);
				}
			LoanApplyInfo applyInfo = loanHandOverProvider.getApplyInfoById(Integer.parseInt(loanId));

			if (LoanProcessTypeEnum.LOAN.type.equals(applyInfo.getLoanProcessType())) {
				updateLoanInfo(applyInfo,Integer.parseInt(userId),loanId);
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.CONTRACT.type);
//				applyInfo.setSyncStatus(-1);
				applyInfo.setContractCheckUser(0);
				loanApplyService.updateApplyInfo(applyInfo, Integer.parseInt(userId));
//				 记录操作日志
				loanOperationService.addLoanOperation(Integer.valueOf(loanId), LoanOperationType.LOAN_CONTRACT_BACK.typeName, "", Integer.parseInt(userId), LoanProcessTypeEnum.LOAN.type);
			}
			JSONObject rjo = new JSONObject();
			rjo.put("loanId", loanId);
			return new ResponseEntity<String>(AppJsonResponse.success(rjo), HttpStatus.OK);
		}catch (Exception e){
			log.error("放款驳回失败",e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
		}
	}

	//更新主表中的 放款起始日期 放款金额 放款人用户ID
	private void updateLoanInfo(LoanApplyInfo applyInfo,Integer loginUserId,String id){
		Map<String,String> map = new HashMap<String, String>();
		AutoBaseTemplate template = configModule.getAutoTemplateProvider().getTemplateListByTables(new String[]{"LBIZ_LOAN_GRANT"}).get(0);
		DataTable datatable = loanHandOverProvider.getDataTableByLoanId(template.getTableName(), LoanProcessTypeEnum.LOAN.type, Integer.parseInt(id));
		if(datatable!=null && datatable.rowSize()>0){
			for (int r = 0; r < datatable.getRows().length; r++) {
				template.setData(datatable.getRows()[r]);
				List<AutoBaseField> fieldList = template.getFields();
				for (int i = 0; i < fieldList.size(); i++) {
					AutoFieldWrapper field = (AutoFieldWrapper) fieldList.get(i);
					Object value = "";
					if (null != template.getData()) {
						value = banger.framework.reader.Reader.objectReader().getValue(template.getData(), field.getColumn());
					}
					String key = template.getName() + "." + field.getFieldName();
					map.put(key, null != value ? value.toString() : "");
				}
			}
		}
		if (StringUtils.isNotBlank(org.apache.commons.collections4.MapUtils.getString(map, "放贷信息.放款起始日期")) && StringUtils.isNotBlank(org.apache.commons.collections4.MapUtils.getString(map, "放贷信息.放款金额"))) {
			applyInfo.setLoanCreditDate(DateUtil.parser(org.apache.commons.collections4.MapUtils.getString(map, "放贷信息.放款起始日期"), "yyyy-MM-dd"));//贷款放款日期
			applyInfo.setLoanCreditAmount(BigDecimal.valueOf(Double.valueOf(org.apache.commons.collections4.MapUtils.getString(map, "放贷信息.放款金额"))));//贷款放款金额
		}
		applyInfo.setLoanCreditUserId(loginUserId);//贷款放款人ID

	}

	/**
	 *保存放贷信息
	 */
	@NoLoginInterceptor
	@SqlTransaction
	@RequestMapping(value="/v1/loanSave",method = RequestMethod.POST)
	public ResponseEntity<String> loanSave(HttpServletRequest request,HttpServletResponse response) {
		String reqJson = HttpParseUtil.getJsonStr(request);
		if(StringUtils.isBlank(reqJson)){
			log.error("保存贷款信息接口，参数为空");
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
	}

	/**
	 * 放款
	 * @param request
	 * @param response
	 * @return
	 */
	@NoLoginInterceptor
	@SqlTransaction				//开启数据库事务
	@RequestMapping("/v1/lendLoan")
	public ResponseEntity<String> lendLoan(HttpServletRequest request,HttpServletResponse response) {
		String loanId = this.getParameter("loanId");
		String userId = this.getParameter("userId");
		Object repaymentMode, proposalLimit, money, loanRatioDate = null, loanBackDate = null;
		DataTable table = null;
		try {
			if(StringUtil.isNotEmpty(loanId)&&StringUtil.isNotEmpty(userId)) {
				LoanApplyInfo applyInfo = loanHandOverProvider.getApplyInfoById(Integer.parseInt(loanId));
				//黑名单判断
				boolean isBlack = customerBlackProvider.isExistCustomerBlack(Integer.parseInt(loanId));
				if (isBlack)
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_117), HttpStatus.OK);

				Integer result = loanModule.getLoanApplyProvider().checkTemplateRequireField(applyInfo.getLoanTypeId(),applyInfo.getLoanProcessType(),Integer.parseInt(loanId));
				if(result>100){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_121), HttpStatus.OK);
				}
				List<LoanApplyTemplate> tempList = loanModule.getLoanApplyProvider().getApplyTemplateList(applyInfo.getLoanTypeId(),applyInfo.getLoanProcessType(),Integer.parseInt(loanId));
				for(LoanApplyTemplate temp : tempList){
					if("LBIZ_LOAN_GRANT".equals(temp.getTableName())){
						table = loanModule.getLoanApplyProvider().getApplyTemplateData(applyInfo.getLoanTypeId(), applyInfo.getLoanProcessType(), temp.getId(), Integer.parseInt(loanId));
						repaymentMode = table.getRow(0).get("LOAN_BACK_MODE");
						proposalLimit = table.getRow(0).get("LOAN_LIMIT");
						money = table.getRow(0).get("LOAN_AMOUNT");
						loanRatioDate = table.getRow(0).get("LOAN_RATIO_DATE");
						loanBackDate = table.getRow(0).get("LOAN_BACK_DATE");
						String massage = repayPlanInfoProvider.checkPlansLoan(repaymentMode,proposalLimit,money,loanRatioDate,loanBackDate,Integer.parseInt(loanId),applyInfo);
						if(StringUtils.isNotBlank(massage)) {
							return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,massage), HttpStatus.OK);
						}
					}
				}
				if (LoanProcessTypeEnum.LOAN.type.equals(applyInfo.getLoanProcessType())) {
					applyInfo.setLoanProcessType(LoanProcessTypeEnum.AUTHORIZED.type);
					loanApplyService.updateApplyInfo(applyInfo, Integer.parseInt(userId));
//				 记录操作日志
					loanOperationService.addLoanOperation(Integer.valueOf(loanId), LoanOperationType.LOAN_LOAN.typeName, "", Integer.parseInt(userId), LoanProcessTypeEnum.LOAN.type);
					loanHandOverProvider.sendLoanMsg(applyInfo,null);

				}
				if(applyInfo.getPotentialCustomerId()!=0)
				{
					CustPotentialCustomers custPotentialCustomers = new CustPotentialCustomers();
					custPotentialCustomers.setId(applyInfo.getPotentialCustomerId());
					custPotentialCustomers.setMarketingSuccess(1);
					custPotentialCustomers.setLoanId(Integer.parseInt(loanId));
					iPotentialCustomersProvider.updatePotentialCustomers(custPotentialCustomers,Integer.parseInt(userId));
				}
			}
			JSONObject rjo = new JSONObject();
			rjo.put("loanId", loanId);
			return new ResponseEntity<String>(AppJsonResponse.success(), HttpStatus.OK);
		}catch (Exception e){
			log.error("放款失败",e);
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_105), HttpStatus.OK);
		}
	}


	/**
	 *授权确认
	 */
	@NoLoginInterceptor
	@SqlTransaction
	@RequestMapping(value="/v1/authorizationConfirmApp",method = RequestMethod.POST)
	public ResponseEntity<String> authorizationConfirmApp(HttpServletRequest request,HttpServletResponse response) {
		String loanId = this.getParameter("loanId");
		String userId = this.getParameter("userId");
		try {
			if(StringUtil.isNotEmpty(loanId)&&StringUtil.isNotEmpty(userId)) {
				if("success".equals(loanModule.syncLoanAuthorizedInfo(Integer.parseInt(loanId)))){
				LoanApplyInfo applyInfo = loanHandOverProvider.getApplyInfoById(Integer.parseInt(loanId));
				//更新贷款状态，添加操作日志
				applyInfo.setSyncStatus(2);
//				applyInfo.setLoanProcessType(LoanProcessTypeEnum.AFTER_LOAN.type);
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.UNDISCLOSED.type);
				applyInfo.setLoanAfterState(AfterLoanTypeEnum.NORMAL.code);
				Integer investigateUserId = applyInfo.getLoanInvestigationUserId();
				if (investigateUserId != null) {
					Integer teamGroupId=permissionModule.getGroupIdByUserId(investigateUserId);
					applyInfo.setLoanAfterGroupId(teamGroupId);
				}
				loanApplyService.updateApplyInfo(applyInfo,Integer.parseInt(userId));
				loanApplyService.creatRepayPlanInfo(Integer.parseInt(loanId), Integer.parseInt(userId));
				loanTaskProvider.updateLoanTaskAmount(Integer.parseInt(loanId));
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.parseInt(loanId), LoanOperationType.LOAN_AUTHORIZATION_CONFIRM.typeName, "", Integer.parseInt(userId), LoanProcessTypeEnum.AUTHORIZED.type);
				loanHandOverProvider.sendLoanMsg(applyInfo,null);
				return new ResponseEntity<String>(AppJsonResponse.success(""), HttpStatus.OK);
			}else{
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_144), HttpStatus.OK);
				}
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
		}catch (Exception e){
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
	}


	/**
	 *授权取消
	 */
	@NoLoginInterceptor
	@SqlTransaction
	@RequestMapping(value="/v1/authorizationCancelApp",method = RequestMethod.POST)
	public ResponseEntity<String> authorizationCancelApp(HttpServletRequest request,HttpServletResponse response) {
		String loanId = this.getParameter("loanId");
		String userId = this.getParameter("userId");
		try {
			if(StringUtil.isNotEmpty(loanId)&&StringUtil.isNotEmpty(userId)) {
//				if("success".equals(loanModule.selectLoanAccountInfo(Integer.parseInt(loanId)).get("code"))){
//					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_145), HttpStatus.OK);
//				}
			String resultstr = loanModule.cancelLoanAccountInfo(Integer.parseInt(loanId));
			if(resultstr.contains("核心已发起撤销交易，请稍后查看授权状态！") || resultstr.contains("success")){
				LoanApplyInfo applyInfo = loanHandOverProvider.getApplyInfoById(Integer.parseInt(loanId));
				//更新贷款状态，添加操作日志
				applyInfo.setSyncStatus(-2);
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.LOAN.type);
				loanApplyService.updateApplyInfo(applyInfo,Integer.parseInt(userId));
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.parseInt(loanId), LoanOperationType.LOAN_AUTHORIZATION_CONFIRM.typeName, "", Integer.parseInt(userId), LoanProcessTypeEnum.AFTER_LOAN.type);
				return new ResponseEntity<String>(AppJsonResponse.success(resultstr.contains("核心已发起撤销交易，请稍后查看授权状态！")?"核心已发起撤销交易，请稍后查看授权状态！":"授权取消成功"), HttpStatus.OK);
			}else{
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_146), HttpStatus.OK);
				}
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
		}catch (Exception e){
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
	}

	/**
	 *【授权阶段】 退回
	 */
	@NoLoginInterceptor
	@SqlTransaction
	@RequestMapping(value="/v1/authorizationBackApp",method = RequestMethod.POST)
	public ResponseEntity<String> authorizationBackApp(HttpServletRequest request,HttpServletResponse response) {
		String loanId = this.getParameter("loanId");
		String userId = this.getParameter("userId");
		try {
			if(StringUtil.isNotEmpty(loanId)&&StringUtil.isNotEmpty(userId)) {
				LoanApplyInfo applyInfo = loanHandOverProvider.getApplyInfoById(Integer.parseInt(loanId));
				//更新贷款状态，添加操作日志
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.LOAN.type);
				loanApplyService.updateApplyInfo(applyInfo,Integer.parseInt(userId));
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.parseInt(loanId), LoanOperationType.LOAN_AUTHORIZATION_CONFIRM.typeName, "", Integer.parseInt(userId), LoanProcessTypeEnum.AFTER_LOAN.type);
				return new ResponseEntity<String>(AppJsonResponse.success(""), HttpStatus.OK);
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
		}catch (Exception e){
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}
	}


	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanContractTemplate")
	public ResponseEntity<String> getLoanContractTemplate(HttpServletRequest request,HttpServletResponse response){
		String contractTypeId = this.getParameter("contractTypeId");
		String loanId = this.getParameter("loanId");
		try {
			if(!StringUtil.isNullOrEmpty(contractTypeId)){
				if(StringUtil.isNotEmpty(loanId)){
					String jsonString = appLoanApplyService.getLoanContractTemplateJsonString(Integer.parseInt(contractTypeId), Integer.parseInt(loanId));
					return new ResponseEntity<String>(jsonString, HttpStatus.OK);
				}else{
					String jsonString = appLoanApplyService.getLoanTemplateJsonString(Integer.parseInt(contractTypeId),LoanProcessTypeEnum.CONTRACT.type);
					return new ResponseEntity<String>(jsonString, HttpStatus.OK);
				}
			}else{
				if(StringUtil.isNotEmpty(loanId)){
					String jsonString = appLoanApplyService.getLoanContractTemplateJsonString(null, Integer.parseInt(loanId));
					return new ResponseEntity<String>(jsonString, HttpStatus.OK);
				}else{
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
				}
			}
		}catch(Exception e){
			ErrorUtil.logError("得到贷款模板字段数据异常",log,e,request);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getLoanContractTemplateField")
	public ResponseEntity<String> getLoanContractTemplateField(HttpServletRequest request,HttpServletResponse response){
		String contractTypeId = this.getParameter("contractTypeId");
		String tableId = this.getParameter("tableId");
		String loanId = this.getParameter("loanId");
		try {

			if(StringUtil.isNotEmpty(contractTypeId)){
					if(StringUtil.isNotEmpty(tableId)){
						if(StringUtil.isNotEmpty(loanId)){
							String jsonString = appLoanApplyService.getContractTemplateFieldJsonString(Integer.parseInt(contractTypeId), Integer.parseInt(tableId), Integer.parseInt(loanId));
							return new ResponseEntity<String>(jsonString, HttpStatus.OK);
						}else{
							String jsonString = appLoanApplyService.getLoanTemplateFieldJsonString(Integer.parseInt(contractTypeId),LoanProcessTypeEnum.CONTRACT.type,Integer.parseInt(tableId));
							return new ResponseEntity<String>(jsonString, HttpStatus.OK);
						}
					}else{
						String jsonString = appLoanApplyService.getLoanTemplateFieldJsonString(Integer.parseInt(contractTypeId),LoanProcessTypeEnum.CONTRACT.type);
						return new ResponseEntity<String>(jsonString, HttpStatus.OK);
					}
				}else{
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
				}
		}catch(Exception e){
			ErrorUtil.logError("得到贷款模板字段数据异常",log,e,request);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

	@NoLoginInterceptor
	@RequestMapping(value = "/v1/checkLoanContractInfo")
	public ResponseEntity<String> checkLoanContractInfo(HttpServletRequest request,HttpServletResponse response){
		String contractTypeId = this.getParameter("contractTypeId");
		String userId = this.getParameter("userId");
		String loanId = this.getParameter("loanId");
		try {

			if(StringUtil.isNotEmpty(contractTypeId)&&StringUtil.isNotEmpty(loanId)&&StringUtil.isNotEmpty(userId)){
				LoanApplyInfo loanInfo = loanModule.getLoanApplyProvider().getApplyInfoById(Integer.parseInt(loanId));
				if(loanInfo!=null) {
					Integer code = loanModule.getLoanApplyProvider().checkTemplateRequireField(Integer.valueOf(contractTypeId), LoanProcessTypeEnum.CONTRACT.type, loanInfo.getLoanId());
					CodeEnum codeEnum = CodeEnum.valueOf(code);
					if (CodeEnum.CODE_100.equals(codeEnum)) {
						loanInfo.setLoanContractTypeId(Integer.valueOf(contractTypeId));
						loanApplyService.updateApplyInfo(loanInfo,Integer.valueOf(userId));
						return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_100), HttpStatus.OK);
					} else {
						return new ResponseEntity<String>(AppJsonResponse.fail(codeEnum), HttpStatus.OK);
					}
				}else{
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_119), HttpStatus.OK);
				}
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
			}
		}catch(Exception e){
			ErrorUtil.logError("得到贷款模板字段数据异常",log,e,request);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}


	private String changeDate(String loanCreditDate, Integer date) {
		Date date1 = DateUtil.parser(loanCreditDate, DateUtil.DEFAULT_DATE_FORMAT);
		Calendar c = Calendar.getInstance();
		c.setTime(date1);
		int day = c.get(Calendar.DAY_OF_MONTH);
//				1、	还款日<=起息日：首个还款日为起息日下月；
//				列：还款日为10号，起息日为2017-10-15，则首个还款日为2017-11-10
		if (date <= day) {
			c.add(Calendar.MONTH, 1);
		} else {
//				2、	还款日>起息日
			SysBasicConfig sysConfig = systemModule.getSysBasicConfig("qxsz");
			if (sysConfig!=null && sysConfig.getConfigValue() != null) {
//				a、	还款日-起息日<起息间隔：首个还款日为起息日下月
//				列：还款日为15日，起息日为2017-10-10，起息间隔为15天，则首个还款日为2017-11-15
//				b、	还款日-起息日>起息间隔：首个还款日为起息日本月
//				列：还款日为25日，起息日为2017-10-5，起息间隔为15天，则首个还款日为2017-10-25
				if ((date - day)< sysConfig.getConfigValue().intValue()) {
					c.add(Calendar.MONTH, 1);
				}
			}
		}
		c.set(Calendar.DAY_OF_MONTH, date);
		return DateUtil.format(c.getTime(), DateUtil.DEFAULT_DATE_FORMAT);
	}

	/**
	 * 得到客户所在团队的团队角色
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getRolesJsonStringByGroupId")
	public ResponseEntity<String> getRolesJsonStringByGroupId(HttpServletRequest request,HttpServletResponse response){
		String userId = this.getParameter("userId");
		Integer teamGroupId = permissionModule.getGroupIdByUserId(Integer.valueOf(userId));
		if(!StringUtil.isNullOrEmpty(userId)) {
			if(teamGroupId==null){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,"该用户没有归属团队"), HttpStatus.OK);
			}else {
				List<PmsRole> roleList = permissionModule.getTeamRolesByGroupId(teamGroupId);
				JSONObject root = new JSONObject();
				JSONArray ja = new JSONArray();
				if (roleList != null && roleList.size() > 0) {
					for (PmsRole role : roleList) {
						JSONObject jo = new JSONObject();
						jo.put("roleId", role.getRoleId());
						jo.put("roleName", role.getRoleName());
						ja.add(jo);
					}
				}
				root.put("data", ja);
				setSuccess(root);
				return new ResponseEntity<String>(root.toString(), HttpStatus.OK);
			}
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
		}
	}

	/**
	 *得到角色下的用户列表
	 *
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getUsersJsonString")
	public ResponseEntity<String> getUsersJsonString(HttpServletRequest request,HttpServletResponse response){
		String userId = this.getParameter("userId");
		String roleId = this.getParameter("roleId");
		if(!StringUtil.isNullOrEmpty(userId)&&!StringUtil.isNullOrEmpty(roleId)) {
			Integer teamGroupId = permissionModule.getGroupIdByUserId(Integer.valueOf(userId));
			List<PmsUser> list = permissionModule.queryUserListByRoleId(roleId, String.valueOf(teamGroupId));
			JSONObject root = new JSONObject();
			JSONArray ja = new JSONArray();
			if(list!=null && list.size()>0){
				for(PmsUser user : list) {
					if(user.getUserId()==Integer.parseInt(userId)){
						continue;
					}
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("userId", user.getUserId());
					jsonObject.put("userName",user.getUserName());
					jsonObject.put("userAccount",user.getUserAccount());
					ja.add(jsonObject);
				}
			}
			root.put("data",ja);
			setSuccess(root);
			return new ResponseEntity<String>(root.toString(), HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
		}
	}

	/**
	 *合同签订
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/contractSignSubmit")
	public ResponseEntity<String> contractSignSubmit(HttpServletRequest request,HttpServletResponse response){
		String loanId = this.getParameter("loanId");
		String loginUserId = this.getParameter("loginUserId");
		if(!StringUtil.isNullOrEmpty(loanId)){
			LoanApplyInfo loanInfo = loanModule.getLoanApplyProvider().getApplyInfoById(Integer.parseInt(loanId));
			if (LoanProcessTypeEnum.SIGN.type.equals(loanInfo.getLoanProcessType())) {

				String result = "success";
				//1对私客户
				result = loanModule.syncCustomerInfo(Integer.valueOf(loanId));
				if(!"success".equals(result)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);
				}
				//3是否有抵押合同编码
				result = loanModule.syncMortgageInfo(Integer.valueOf(loanId));
				if(!"success".equals(result)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);
				}
				//是否有质押合同编码
				result = loanModule.syncPledgeInfo(Integer.valueOf(loanId));
				if(!"success".equals(result)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);
				}
				//2同步担保信息
				result = loanModule.syncGuaranteeInfo(Integer.valueOf(loanId));
				if(!"success".equals(result)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);
				}

				//合同信息同步
				result = loanModule.syncContractInfo(Integer.valueOf(loanId));
				if(!"success".equals(result)){
					return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_ERROR,result), HttpStatus.OK);
				}
				loanInfo.setSyncStatus(1);
				loanInfo.setLoanProcessType(LoanProcessTypeEnum.LOAN.type);
				loanInfo.setContractSignDate(new Date());
				loanApplyService.updateApplyInfo(loanInfo,Integer.valueOf(loginUserId));
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.valueOf(loanId), LoanOperationType.CONTRACT_SIGN_SUBMIT.typeName, "", Integer.valueOf(loginUserId), LoanProcessTypeEnum.SIGN.type);
				loanHandOverProvider.sendLoanMsg(loanInfo,null);
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_100), HttpStatus.OK);

			}else {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_126), HttpStatus.OK);
			}
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
		}
	}
	/**
	 *
	 * 签订退回
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/contractSignBack")
	public ResponseEntity<String> contractSignBack(HttpServletRequest request,HttpServletResponse response){
		String loanId = this.getParameter("loanId");
		String loginUserId = this.getParameter("loginUserId");
		if(!StringUtil.isNullOrEmpty(loanId)&&!StringUtil.isNullOrEmpty(loginUserId)){
			LoanApplyInfo applyInfo = loanApplyService.getApplyInfoById(Integer.parseInt(loanId));
			if(applyInfo!=null&&LoanProcessTypeEnum.SIGN.type.equals(applyInfo.getLoanProcessType())){
				applyInfo.setLoanProcessType(LoanProcessTypeEnum.CONTRACT.type);
				applyInfo.setContractCheckUser(0);
				loanApplyService.updateApplyInfo(applyInfo, Integer.valueOf(loginUserId));
				// 记录操作日志
				loanOperationService.addLoanOperation(Integer.valueOf(loanId), LoanOperationType.CONTRACT_SIGN_BACK.typeName, "", Integer.valueOf(loginUserId), LoanProcessTypeEnum.SIGN.type);
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_100), HttpStatus.OK);
			}else{
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_126), HttpStatus.OK);
			}
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
		}
	}

	/**
	 *
	 * 根据卡号查询客户信息
	 */
	@NoLoginInterceptor
	@RequestMapping(value = "/v1/getCusInfoByAccNo")
	public ResponseEntity<String> getCusInfoByAccNo(HttpServletRequest request,HttpServletResponse response){
		String loanId = this.getParameter("loanId");
		String accNo = this.getParameter("accNo");
		if(!StringUtil.isNullOrEmpty(loanId)&&!StringUtil.isNullOrEmpty(accNo)){
			Map<String,String> map = new HashMap<String, String>();
			map.put("账户",accNo);
			try {
//				String returnMsg = loanModule.queryCusInfo(Integer.parseInt(loanId),map,SocketCodeTypeEnum.QRY00400);
				String returnMsg ="{\"data\":{\"id\":\"\",\"bal\":\"0.00\",\"brch_name\":\"\",\"acc_no\":\"340827198911132417321\",\"cus_name\":\"还款账户名称\",\"cus_id\":\"\",\"branch\":\"开户行\",\"code\":\"0001\",\"type\":\"\",\"msg\":\"\"},\"code\":\"100\",\"msg\":\"成功\"}";
//				JSONObject resultJson = new JSONObject();
//				resultJson.put("data",returnMsg);
//				setSuccess(resultJson);
				return new ResponseEntity<String>(returnMsg, HttpStatus.OK);
			}catch (Exception e){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_121), HttpStatus.OK);
			}
		}else{
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_120), HttpStatus.OK);
		}
	}

	/**
	 *
	 * @param resultJson
	 */

	private void setSuccess(JSONObject resultJson){
		resultJson.put("code", CodeEnum.CODE_100.getCode());
		resultJson.put("msg", CodeEnum.CODE_100.getMsg());
	}

	/**
	 * 将原始数据转换成表单map
	 * @param customerMap
	 * @param map
	 */
	private void setCustomerMap(Map<String, Object> customerMap, Map<String, Object> map) {
		customerMap.clear();
		for (String key : map.keySet()) {
			String val = (String) map.get(key);
			if (key.indexOf("field.") > -1) {
				String propertyName = key.substring("field.".length());
				if(val != null){
					if ("identifyNum".equals(propertyName))
						val = IdCardUtil.toUpperCase(val);
					if ("idCard".equals(propertyName))
						val = IdCardUtil.toUpperCase(val);
					customerMap.put(propertyName, val);
				}
			}
		}
	}

	private void setDataRowByFieldList(DataRow dataRow, List<AutoBaseField> fieldList, Map<String, Object> customerMap) {
		if(CollectionUtils.isNotEmpty(fieldList)){
			for (AutoBaseField baseFiled : fieldList) {
				String column = baseFiled.getColumn();
				String field = baseFiled.getField();
				Object o= MapUtils.getObject(customerMap, field);
				dataRow.set(column,o);
			}
		}
	}

	/**
	 * 刷新贷款账户
	 */
	@RequestMapping(value = "/v1/refreshLoanAccount")
	@NoTokenAnnotation
	@NoLoginInterceptor
	public ResponseEntity<String> refreshLoanAccount(HttpServletRequest request,HttpServletResponse respons){

		String userId = this.getParameter("userId");
		if(StringUtil.isNullOrEmpty(userId)){
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}

		String result = loanHandOverProvider.refreshLoanAccount(Integer.valueOf(userId));
		if (StringUtils.isNotBlank(result)) {
			if ("100".equals(result)) {
				return new ResponseEntity<String>(AppJsonResponse.success(), HttpStatus.OK);
			} else if ("155".equals(result)) {
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_155), HttpStatus.OK);
			}
		}

		return new ResponseEntity<String>(AppJsonResponse.fail(), HttpStatus.OK);

	}

	/**
	 * 刷新贷款账户
	 */
	@RequestMapping(value = "/v1/refreshLoanAccountTab")
	@NoTokenAnnotation
	@NoLoginInterceptor
	public ResponseEntity<String> refreshLoanAccountTab(HttpServletRequest request,HttpServletResponse respons){

		String loanId = this.getParameter("loanId");
		if(StringUtil.isNullOrEmpty(loanId)){
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
		}

		String result = loanHandOverProvider.refreshLoanAccountTab(Integer.valueOf(loanId));
		if ("success".equals(result)) {
			return new ResponseEntity<String>(AppJsonResponse.success(), HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
		}
	}



	/**
	 *获取跳转分配贷款提交页面信息
	 */
	@NoLoginInterceptor
	@RequestMapping("/v1/getPageSelectJson")
	@NoTokenAnnotation
	public ResponseEntity<String> getPageSelectJson(HttpServletRequest request, HttpServletResponse response) {
		try {
			String field = this.getParameter("field");

			if(StringUtil.isNullOrEmpty(field)){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}

			if(field.equals("bizType")){
				JSONArray jsona = loanHandOverProvider.getBizAndPrdTypeTreeJson();
				return new ResponseEntity<String>(AppJsonResponse.success(jsona), HttpStatus.OK);
			}else if(field.equals("businessLine")){
				JSONArray jsona = loanHandOverProvider.queryCrmPrdTypeTreeJson();
				return new ResponseEntity<String>(AppJsonResponse.success(jsona), HttpStatus.OK);
			}else if(field.equals("loanOrientation")){
				JSONArray jsona = loanHandOverProvider.getLoanOrientationTreeJson();
				return new ResponseEntity<String>(AppJsonResponse.success(jsona), HttpStatus.OK);
			}
		}catch (Exception e){
			log.error(e);
		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}


	/**
	 *获取跳转分配贷款提交页面信息
	 */
	@NoLoginInterceptor
	@RequestMapping("/v1/savePageSelectJson")
	@NoTokenAnnotation
	public ResponseEntity<String> savePageSelectJson(HttpServletRequest request, HttpServletResponse response) {
		try {
			String reqJson= HttpParseUtil.getJsonStr(request);
			if(StringUtils.isEmpty(reqJson)){
				log.error("设备列表，参数为空");
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}

			JSONObject jsonObject = new JSONObject().fromObject(reqJson);
			String field = jsonObject.containsKey("field")?jsonObject.getString("field"):"";
			String loanId = jsonObject.containsKey("loanId")?jsonObject.getString("loanId"):"";
			String json = jsonObject.containsKey("json")?jsonObject.getString("json"):"";

			Map<String,String> map = new HashMap<String, String>();
			if(StringUtil.isNullOrEmpty(field)||StringUtil.isNullOrEmpty(loanId)||StringUtil.isNullOrEmpty(json)){
				return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_102), HttpStatus.OK);
			}
			JSONObject jo = JSONObject.fromObject(json);
//			{"id":"125110","pId":"125100","name":"个人股东已置换贷款","locate":"业务品种分类>个人类产品>个人置换>个人股东置换>个人股东已置换贷款",
//					"cmiCode":"","cmiName":"","prdCode":"","prdNam":"","prdPk":""}
			if(field.equals("bizType")){
				String locate = jo.getString("locate");
				String name = jo.getString("name");
				//	业务品种	BIZ_TYPE bizType = subCode; id
				map.put("bizType",jo.getString("id"));
				//	业务品种名称	BIZ_TYPE_NAME bizTypeName = subName;
				map.put("bizTypeName",name.split("--")[0]);
				//	产品自定义名称	PRD_USERDF_NAME prdUserdfName = subOrder;
				map.put("prdUserdfName",locate.split(">")[2]);
				//	产品名称	PRD_NAME prdName = prdName;
				map.put("prdName",jo.getString("prdNam"));
				//	产品主键	PRD_PK prdPk = prdPk;
				map.put("prdPk",jo.getString("prdPk"));
				//	核心产品号	ACCOUNT_CLASS accountClass = cmiCode;
				map.put("accountClass",jo.getString("cmiCode"));
				//	核心产品名称	ACCOUNT_CLASS_NAME accountClassName = cmiName;
				map.put("accountClassName",jo.getString("cmiName"));
				//	业务品种分类名称	BIZ_TYPE_DETAIL bizTypeDetail = subOrder;
				map.put("bizTypeDetail",locate.replace("业务品种分类>", ""));
			}else if(field.equals("businessLine")){
				//	业务条线	BUSINESS_LINE businessLine = ywtxbm; id
				map.put("businessLine",jo.getString("pId"));
				//	业务条线名称	BUSINESS_LINE_NAME businessLineName = ywtx; pId
				map.put("businessLineName",jo.getString("ywtx"));
				//	主要产品分类	MAIN_PRO_TYPE mainProType = subCode; id
				map.put("mainProType",jo.getString("id"));
				//	主要产品分类名称	MAIN_PRO_TYPE_NAME mainProTypeName = prdName; name
				map.put("mainProTypeName",jo.getString("name"));
			}else if(field.equals("loanOrientation")){
				String locate = jo.getString("locate");
				//	贷款投向	LOAN_ORIENTATION
				map.put("loanOrientation",jo.getString("id"));
				//	投向名称	ORIENTATION_NAME
				map.put("orientationName",jo.getString("locate"));
			}

			loanModule.updateSurveyDataTableById(loanId,map);
			return new ResponseEntity<String>(AppJsonResponse.success(), HttpStatus.OK);
		}catch (Exception e){

		}
		return new ResponseEntity<String>(AppJsonResponse.fail(CodeEnum.CODE_101), HttpStatus.OK);
	}

}
