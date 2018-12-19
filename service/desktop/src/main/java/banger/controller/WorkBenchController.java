package banger.controller;


import banger.common.BaseController;
import banger.common.tools.ServerRealPathUtil;
import banger.domain.permission.WebLoginInfo;
import banger.domain.system.SysBasicConfig;
import banger.domain.system.SysWorkingTable;
import banger.domain.task.TskTaskInfoQuery;
import banger.framework.util.DateUtil;
import banger.framework.util.StringUtil;
import banger.framework.util.SystemUtil;
import banger.moduleIntf.IBasicConfigProvider;
import banger.moduleIntf.IPermissionService;
import banger.moduleIntf.IWorkingTableProvider;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wumh on 14-3-3.
 */
@Controller
@RequestMapping("/desktop")
public class WorkBenchController extends BaseController {
	private static final long serialVersionUID = -435231307460507790L;
	@Autowired
	private IPermissionService permissionModule;
	private static Map<String, String> fileMap = new HashMap<String, String>();
	@Autowired
	private IWorkingTableProvider workingTableProvider;
	@Autowired
	private IBasicConfigProvider basicConfigProvider;
	@Value("${into.info.h5}")
	private String h5;
	//登陆后的主页面
	@RequestMapping("/getMainPage")
	public String getMainPage(HttpServletRequest request){
		String menuJson;
		String str;
		if(this.getLoginInfo() == null){
			return "/permission/vm/login";
		}if(this.getLoginInfo().getUserId()==0){
			if("superuser".equals(this.getLoginInfo().getAccount())) {
				menuJson = this.defaultMenuJson();
				request.setAttribute("isManager", true);
				request.setAttribute("isTrayInstalled", this.getLoginInfo().getIsTrayInstalled());
				request.setAttribute("userAccount", this.getLoginInfo().getAccount());
				//this.setServerIPAttrs();
				request.setAttribute("menuJson", menuJson);
				request.setAttribute("su",false);

				request.setAttribute("userId",0);

				return "/desktop/vm/main";
			}else{
				if("true".equals(h5)){
					str=",intoPiecesManage,loanUse,intoPiecesInfo,trialRule,interfaceHistoryList,msgHistoryList";
				}else{
					str="";
				}

				menuJson = permissionModule.getMenuByMenuIds("tableTemplateManage,tableTemplateList,loanTypeList,systemConfig," +
						"sysDataDict,basicConfig,loanInfoAddedOwner,functionSwitch,interfaceSwitch,modelScore,modelTemplateInfo,modelScoreInfo,modelConfig,investigateReport,basicData,contractTemplate,SurveyFlowConfig,formSettings," +
						"appUpdateVersion,appUpdate,inportGuide,mapParamsSet,contractTypeList"+str
				);
				request.setAttribute("isManager", true);
				request.setAttribute("isTrayInstalled", this.getLoginInfo().getIsTrayInstalled());
				request.setAttribute("userAccount", this.getLoginInfo().getAccount());
				//this.setServerIPAttrs();
				request.setAttribute("menuJson", menuJson);
				request.setAttribute("su",true);

				this.setAttribute("cofigPlatForm", "true");

				request.setAttribute("userId",0);

				return "/desktop/vm/main";
			}
		}else{
			menuJson = permissionModule.getMenuTreeByRoleIds(this.getLoginInfo().getRoleIds(),
					this.getLoginInfo().getUserId(),this.getLoginInfo().getAccount());
			String userAccount = this.getLoginInfo().getAccount();
			if(this.getLoginInfo().getRoleIds().contains("1") || this.getLoginInfo().getRoleIds().contains("2") ){
				// 系统管理员
				request.setAttribute("isManager",true);
			} else {
				request.setAttribute("isManager",false);
			}
			request.setAttribute("isTrayInstalled",this.getLoginInfo().getIsTrayInstalled());
			request.setAttribute("userAccount",userAccount);
			WebLoginInfo userInfo = (WebLoginInfo)this.getLoginInfo();
			request.setAttribute("isAddUserFunc",userInfo.getFuncCodes().contains("addUser"));
			request.setAttribute("menuJson",menuJson);
			/*
			if(this.isCofigPlatFormManager()){
				this.setAttribute("cofigPlatForm", "true");
			}
			*/
			request.setAttribute("su",false);

			request.setAttribute("userId",this.getLoginInfo().getUserId());

			//this.setServerIPAttrs();
			return "/desktop/vm/main";
		}
	}


	//工作台页面
	@RequestMapping("/getWorkbench")
	public String getWorkbench(HttpServletRequest request){
		@SuppressWarnings("deprecation")
		File file = new File(request.getRealPath(""));
		int webSize = (int) (file.getFreeSpace() / (1024 * 1024 * 1024));

		request.setAttribute("sysDateAndWeek", getNowTimeAndWeek());
		request.setAttribute("userAccount",this.getLoginInfo().getAccount());

		Map<String,Long> memory = SystemUtil.getMemoryInfo();

		request.setAttribute("usedMemory", memory.get("usedMemory")/1024/1024);
		request.setAttribute("maxMemory", memory.get("maxMemory")/1024/1024);
		request.setAttribute("initMemory", memory.get("initMemory")/1024/1024);
		request.setAttribute("webSize", webSize);
		//总用户数
		Integer userTotolNum = permissionModule.queryTotolUserNum();
		request.setAttribute("userTotolNum",userTotolNum);

		//新的
		//其他角色页面
		//获取登录人员角色
		Integer userId = getLoginInfo().getUserId();
		//团队id
		String groupIds = getLoginInfo().getTeamGroupIdByRole();
		//角色id
		String[] roleIds = (StringUtil.isNotEmpty(getLoginInfo().getRoleIds()))?getLoginInfo().getRoleIds().split(","):new String[0];
		//日程
		SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMdd");
		String timess = dateformat1.format(new Date());
		Date today = DateUtil.clearTime(new Date());
		for (int i = 0; i < roleIds.length; i++) {
			if("4".equals(roleIds[i])){
				Map<String,Object> condition_cus = new HashMap<String, Object>();
				String teamGroupIds=getLoginInfo().getTeamGroupIdByRole(false);
				condition_cus.put("createUser", userId);
				condition_cus.put("today", today);
				condition_cus.put("state",1);
				SysWorkingTable workingTable_cus = workingTableProvider.queryCustScheduleWorkingTable(condition_cus);
				Map<String,Object> condition = new HashMap<String, Object>();
				condition.put("today", today);
				Integer otherSchedule = workingTableProvider.queryScheduleCount(condition);
				request.setAttribute("otherSchedule",otherSchedule);
				request.setAttribute("workingTable_cus",workingTable_cus);
			}
		}
//		未完成的贷款任务
		Map<String,Object> condition_task = new HashMap<String, Object>();
//		String teamGroupIds=getLoginInfo().getTeamGroupIdByRole(false);
		condition_task.put("assignUserId", userId);
		condition_task.put("taskStatus","1");
		condition_task.put("createUser", userId);
		List<TskTaskInfoQuery> list = workingTableProvider.queryTaskInfoList(condition_task);
		if(list != null && list.size() > 0){
			request.setAttribute("taskNum",list.size());
		}else{
			request.setAttribute("taskNum",0);
		}

		Map<String,Object> condition = new HashMap<String,Object>();
		//不加数据权限
//		if (StringUtils.isNotBlank(groupIds)){
//			condition.put("groupIds", groupIds);
//		}else{
//			condition.put("groupIds", "-1");
//		}
		//放贷页面加数据权限，根据页面来的
		String teamGroupIds = getLoginInfo().getUserGroupPermit();
		if (StringUtils.isNotBlank(teamGroupIds)) {
			condition.put("groupId",teamGroupIds.split(","));
		}
		if (roleIds != null) {
			for (int i = 0; i < roleIds.length; i++) {
				//如果是后台人员，查出放贷，是主管或团队主管查出 分配
				if("5".equals(roleIds[i]) || "2".equals(roleIds[i]) || "3".equals(roleIds[i])){
					if(teamGroupIds == null || "".equals(teamGroupIds)){
						teamGroupIds = getLoginInfo().getTeamGroupId()+"";
					}else{
						//如果有团队，加上自己的团队， 没有就按数据权限来
						if(getLoginInfo().getTeamGroupId() != null){
							teamGroupIds = teamGroupIds + "," + getLoginInfo().getTeamGroupId();
						}
					}
					condition.put("groupId",teamGroupIds.split(","));
					if("null".equals(condition.get("groupId"))){
						condition.put("groupId","-1".split(","));
					}
					break;
				}
			}
		}

		//如果不是后台 主管 团队主管 又没有团队 加上可能的条件防止报错
		if("null".equals(condition.get("groupId")) || "".equals(teamGroupIds) || "".equals(condition.get("groupId")) || teamGroupIds == null || "null".equals(teamGroupIds)){
			condition.put("groupId","-1".split(","));
		}
		condition.put("belongId",userId);

		//开关，不为null则查出
		condition.put("afterLoan",1);

		//单独查询个人审批
		Integer approvalCount = workingTableProvider.queryApprovalCount(condition);

		//显示催收贷款(当前日期+催收设置天数>=应还款时间)
		SysBasicConfig cssz = basicConfigProvider.getSysBasicConfigByKey("cfsz");
		Date collectionDate = DateUtil.addDay(DateUtil.getCurrentDate(),cssz.getConfigValue());
		condition.put("collectionDate", collectionDate);
		//催收拆分,1表示还款提醒,2表示不良催收计划还款时间>DateUtil.getCurrentDate()
		condition.put("nowDate", DateUtil.getNeedTime(0,0,0,0));

		condition.put("contractCheckUser",userId);
		Integer signCount = workingTableProvider.querySignCount(condition);

		SysWorkingTable workingTable =  workingTableProvider.queryManageCount(condition);
//		condition.remove("belongId");
//		SysWorkingTable workingTable2 = workingTableProvider.queryManageCount(condition);
//		request.setAttribute("approvalCount",);
		request.setAttribute("sysDateAndWeek", getNowTimeAndWeek());
		request.setAttribute("workingTable",workingTable);
		request.setAttribute("approvalCount",approvalCount);
		request.setAttribute("signCount", signCount);
//		request.setAttribute("workingTable2",workingTable2);
		//角色
		for (int i = 0; i < roleIds.length; i++) {
			if("2".equals(roleIds[i])){
				request.setAttribute("zhuguan",true);
			}else if("3".equals(roleIds[i])){
				request.setAttribute("tuandui",true);
			}else if("4".equals(roleIds[i])){
				request.setAttribute("jingli",true);
			}else if("5".equals(roleIds[i])){
				request.setAttribute("houtai",true);
			}else if("6".equals(roleIds[i])){
				request.setAttribute("shenpi",true);
			}else if("1".equals(roleIds[i])){
				request.setAttribute("admin",true);
			}else if("14".equals(roleIds[i])){
				request.setAttribute("shouquan",true);
			}else{

			}
		}

		if(roleIds.length==0)
			request.setAttribute("admin",true);
		//当前日期
		request.setAttribute("timess",timess);

		return "/desktop/vm/workBench";
	}

	@RequestMapping("/getResourcesPage")
	public String getResourcesPage(HttpServletRequest request) {
		// 扫描resources目录
		String path = getRootPath();
		String resourcesPath = path + File.separator + "resources";
		fileMap.clear();
		fileMap = GetSql(resourcesPath);
		if (fileMap != null) {
			request.setAttribute("fileMap", fileMap);
			request.setAttribute("fileSize", fileMap.size());
		}
		return "/desktop/vm/resources";
	}


	/**
	 * 递归调用查找指定文件加下所有文件
	 * @param path
	 * @return
	 */
	public static  Map<String, String> GetSql(String path){
		File rootDir = new File(path);
		if (!rootDir.exists()) {
			rootDir.mkdir();
		}
		if(!rootDir.isDirectory()){
			fileMap.put(rootDir.getName(), rootDir.getAbsolutePath());
		}else{
			String[] fileList =  rootDir.list();
			for (int i = 0; i < fileList.length; i++) {
				path = rootDir.getAbsolutePath()+ File.separator +fileList[i];
				GetSql(path);
			}
		}
		return fileMap;
	}

	//获取菜单menu
	@RequestMapping("/getSysMenuJson")
	public String getSysMenuJson(){
		String menuJson = permissionModule.getMenuTreeByRoleIds(this.getLoginInfo().getRoleIds(),
				this.getLoginInfo().getUserId(),this.getLoginInfo().getAccount());
		this.renderText(menuJson);
		return null;
	}

	/**
	 * 下载帮助手册
	 * @throws IOException
	 */
	@RequestMapping("/downloadHelpFile")
	public void downloadHelpFile() throws IOException {
		String path = ServerRealPathUtil.getServerRealPath() + File.separator + "help";
		File file = new File(path + File.separator + "helpFile.docx");
		if (file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				this.getResponse().addHeader(
						"Content-Disposition",
						"attachment;filename="
								+ URLEncoder.encode("帮助手册.docx",
								"utf-8").replace("+", "%20"));
				this.getResponse().setContentType("application/msword"); // 设置返回的文件类型
				OutputStream output = this.getResponse().getOutputStream(); // 得到向客户端输出二进制数据的对象
				BufferedInputStream bis = new BufferedInputStream(fis);// 输入缓冲流
				BufferedOutputStream bos = new BufferedOutputStream(output);// 输出缓冲流
				byte data[] = new byte[4096];// 缓冲字节数
				int size = 0;
				size = bis.read(data);
				while (size != -1) {
					bos.write(data, 0, size);
					size = bis.read(data);
				}
				bis.close();
				bos.flush();// 清空输出缓冲流
				bos.close();
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				log.error("下载帮助手册 error:"+e.getMessage(),e);
			} catch (IOException e) {
				e.printStackTrace();
				log.error("下载帮助手册 error:"+e.getMessage(),e);
			}
		}
	}


	/**
	 * 下载资源文件
	 * @throws IOException
	 */
	@RequestMapping("/downloadResources")
	public void downloadResources(HttpServletRequest request) throws IOException {
		String fileName =  URLDecoder.decode(this.getParameter("fileName"), "UTF-8");
		String path = getRootPath();
		String resourcesPath = path + File.separator + "resources";
		String filePath = resourcesPath + File.separator + fileName;
		File file = new File(filePath);
		if(file.exists()){
			try {
				FileInputStream fis = new FileInputStream(file);
				//服务器文件路径
				this.getResponse().addHeader(
						"Content-Disposition",
						"attachment;filename="
								+ URLEncoder.encode(fileName,
								"utf-8").replace("+", "%20"));
				this.getResponse().setContentType("text/plain; charset=utf-8");
				this.getResponse().addHeader("Content-Length", "" + file.length());
				OutputStream output = this.getResponse().getOutputStream(); //得到向客户端输出二进制数据的对象
				BufferedInputStream bis = new BufferedInputStream(fis);//输入缓冲流
				BufferedOutputStream bos = new BufferedOutputStream(output);//输出缓冲流

				byte data[] = new byte[4096];//缓冲字节数

				int size = 0;
				size = bis.read(data);
				while (size != -1) {
					bos.write(data, 0, size);
					size = bis.read(data);
				}
				bis.close();
				bos.flush();//清空输出缓冲流
				bos.close();
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private String defaultMenuJson(){
		JSONArray ja  = new JSONArray();
		JSONObject jo2 = new JSONObject();

		Map<String,JSONArray> jaMap = new HashMap<String,JSONArray>();
		jo2.put("id","syncConfig");
		jo2.put("display", "系统配置");
		jaMap.put("syncConfig",new JSONArray());
		ja.add(jo2);

		JSONObject jo25 = new JSONObject();
		jo25.put("id","resource");
		jo25.put("display", "表单模版维护");
		jo25.put("hasCloseConfirm",false);
		jo25.put("title", "表单模版维护");
		jo25.put("url", "../tableTemplate/getTableInfoListPage.html");
		jo25.put("lock", false);
		jaMap.get("syncConfig").add(jo25);

//		JSONObject jo26 = new JSONObject();
//		jo26.put("id","inportGuide");
//		jo26.put("display", "行业指引导入");
//		jo26.put("hasCloseConfirm",false);
//		jo26.put("title", "行业指引导入");
//		jo26.put("url", "../loanIndustryGuidelines/importGuideLinesPage.html");
//		jo26.put("lock", false);
//		jaMap.get("syncConfig").add(jo26);

		JSONObject jo28 = new JSONObject();
		jo28.put("id","importCustTable");
		jo28.put("display","客户表单导入");
		jo28.put("hasCloseConfirm",false);
		jo28.put("title","客户自动导入");
		jo28.put("url","../custAutoImport/importCustTabPage.html");
		jo28.put("lock",false);
		jaMap.get("syncConfig").add(jo28);


		JSONObject jo1 = new JSONObject();
		jo1.put("id","sysMonitor");
		jo1.put("display", "系统监控");
		jaMap.put("sysMonitor",new JSONArray());
		ja.add(jo1);

		JSONObject jo27 = new JSONObject();
		jo27.put("id","sqlCountMonitor");
		jo27.put("display", "SQL性能统计");
		jo27.put("hasCloseConfirm",false);
		jo27.put("title", "SQL性能统计");
		jo27.put("url", "../monitor/getSqlMonitorListPage.html");
		jo27.put("lock", false);
		jaMap.get("sysMonitor").add(jo27);

		JSONObject jo29 = new JSONObject();
		jo29.put("id","sqlDebugPage");
		jo29.put("display", "数据调试");
		jo29.put("hasCloseConfirm",false);
		jo29.put("title", "数据调试");
		jo29.put("url", "../monitor/getSqlDebugPage.html");
		jo29.put("lock", false);
		jaMap.get("sysMonitor").add(jo29);

		JSONObject jo30 = new JSONObject();
		jo30.put("id","socketDebugPage");
		jo30.put("display", "接口调试");
		jo30.put("hasCloseConfirm",false);
		jo30.put("title", "接口调试");
		jo30.put("url", "../monitor/getSocketDebugPage.html");
		jo30.put("lock", false);
		jaMap.get("sysMonitor").add(jo30);

		for(int i=0;i<ja.size();i++){
			JSONObject jo = (JSONObject)ja.get(i);
			jo.put("sub", jaMap.get(jo.getString("id")));
		}

		return ja.toString();
	}

	private String getSettingMenuJson(){
		JSONArray ja  = new JSONArray();
		JSONObject jo2 = new JSONObject();

		Map<String,JSONArray> jaMap = new HashMap<String,JSONArray>();
		jo2.put("id","syncConfig");
		jo2.put("display", "系统配置");
		jaMap.put("syncConfig",new JSONArray());
		ja.add(jo2);

		JSONObject jo25 = new JSONObject();
		jo25.put("id","resource");
		jo25.put("display", "表单模版维护");
		jo25.put("hasCloseConfirm",false);
		jo25.put("title", "表单模版维护");
		jo25.put("url", "../tableTemplate/getTableInfoListPage.html");
		jo25.put("lock", false);
		jaMap.get("syncConfig").add(jo25);

		for(int i=0;i<ja.size();i++){
			JSONObject jo = (JSONObject)ja.get(i);
			jo.put("sub", jaMap.get(jo.getString("id")));
		}
		return ja.toString();
	}


	/**
	 * 得到系统时间和星期几
	 * @return
	 */
	private String getNowTimeAndWeek(){
		Calendar ca = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
		String[] week = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
		int i = ca.get(Calendar.DAY_OF_WEEK);
		return  df.format(ca.getTime())+"     "+ week[i-1];
	}

}
