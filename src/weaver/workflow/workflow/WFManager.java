package weaver.workflow.workflow;

/*
 * Created on 2006-05-18
 * Copyright (c) 2001-2006 泛微软件
 * 泛微协同商务系统，版权所有。
 * 
 */
import java.io.BufferedReader;
import java.io.Serializable;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import oracle.sql.CLOB;
import weaver.car.CarInfoManager;
import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.formmode.interfaces.action.WorkflowToMode;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.systeminfo.SysMaintenanceLog;
import weaver.workflow.request.RequestCheckUser;
import weaver.workflow.request.WFFreeFlowManager;
import weaver.workflow.request.WFUrgerManager;
import weaver.workflow.ruleDesign.RuleBusiness;
import weaver.workflow.ruleDesign.RuleInterface;

/**
 * Description: 流程基本信息类
 * 
 * @author zjf
 * @version 1.0
 */

public class WFManager extends BaseBean implements Serializable {

	private static final long serialVersionUID = 806812148902448035L;

	private int wfid = 0;
	private int formid = 0;
	private String wfname = "";
	private String wfdes = "";
	private int typeid = 0;
	private int oldtypeid = 0;
	private String isbill = "";
	private String iscust = "";
	private int helpdocid = 0;
	// add by xhheng @20050204 for TD 1534，记录是否已经创建工作流
	private int isused = 0;
	private String isImportDetail = "0";

	public String getIsImportDetail() {
		return isImportDetail;
	}

	public void setIsImportDetail(String isImportDetail) {
		this.isImportDetail = isImportDetail;
	}
	
	private String action = "";
	private String isvalid = "1";
	private String needmark = "";
	// add by xhheng @ 2005/01/24 for 消息提醒 Request06
	private String messageType = "";
	// add by xwj 20051101 for td2965
	private String mailMessageType = "";

	// 微信提醒START(QC:98106)
	private String chatsType = "";
	private String chatsAlertType = "";
	private String notRemindifArchived = "";
	// 微信提醒END(QC:98106)
	private String archiveNoMsgAlert = ""; // 归档节点不需短信提醒
	private String archiveNoMailAlert = ""; // 归档节点不需邮件提醒
	// 禁止附件批量下载
	private String forbidAttDownload = "";
	// 是否跟随文档关联人赋权
	private String docRightByOperator = "";
	// add by xhheng @ 20050302 for TD 1545
	private String multiSubmit = "";
	// add by xhheng @ 20050303 for TD 1689
	private String defaultName = "";
	// add by xhheng @ 20050317 for 附件上传
	private String docCategory = "";
	private String isannexUpload = "";
	private String annexdocCategory = "";
	private String docPath = "";
	private int subCompanyId2 = -1;
	private String IsTemplate = "";
	private int Templateid = 0;
	private String isaffirmance = "";
	private String isSaveCheckForm = ""; // 流程保存是否验证必填
	private String showUploadTab = "";
	private String isSignDoc = "";
	private String showDocTab = "";
	private String isSignWorkflow = "";
	private String showWorkflowTab = "";
	// added by pony on 2006-04-13 for TD 4109
	private int isUse = 1;// 限制重复提交功能是否启用
	private int Utype = 1;// 用户类型
	private int Ttype = 1;// 时间类型
	// added end.
	private String isremak = "";
	private String orderbytype = "";// 流程审批意见排序方式 1，倒序 2，正序
	private String isShowChart = "";// 提交流程后是否显示流程图页面
	private String isShowOnReportInput = "0"; // 1：是，0或其它：否
	// added by pony on 2006-06-15 for td4527
	private int catelogType = 0;// 附件上传目录类型 0：固定目录 1：选择目录
	private int selectedCateLog = 0;// 所选择目录的对应的id
	// added end.

	// added by pony on 2006-06-26 for td4611
	private int docRightByHrmResource = 0;// 是否按人力资源字段附权。默认为不启用。
	// added end.

    private int hrmResourceShow = 0;// 人力资源条件是否显示安全级别，默认为显示
	private int titleFieldId = -1;// 标题字段id
	private int keywordFieldId = -1;// 主题词字段id

	private String nosynfields;// 不需同步字段

	private String SAPSource;// SAP数据源

	private String isModifyLog = "0";// added by cyril on 2008-07-14 for
										// td:8835

	private String ShowDelButtonByReject = "0";// 退回创建节点是否可删除 1:是,0或其它:否

	private String specialApproval = "0";// 是否特批件 1:是,0或其他:否
	private String Frequency = "0";// 次数
	private String Cycle = "1";// 周期

	private String isimportwf = "0";// 新建时是否可导入流程
	private String importReadOnlyField = ""; // 允许导入数据到只读字段
	private String fieldNotImport = ""; // 无需导入字段
	private String wfdocpath = "";// 流程保存为文档的路径
	private String wfdocowner = "";// 流程保存為文檔的所有者
	private String wfdocownertype = "";// TD14723
	private String wfdocownerfieldid = "";// TD14723
	// added by cyril on 2008-12-23 for td:9573
	private String isEdit = "0";// 是否正在图形化编辑
	private int editor = -1;// 当前编辑人
	private String editdate = "";// 编辑日期
	private String edittime = "";// 编辑时间
	private String isshared = "";
	private String isforwardrights = "";
	private String candelacc = "0";// 是否允许创建人删除附件
	private String isrejectremind = "0";// 退回是否提醒
	private String ischangrejectnode = "0";// 退回时是否可设置提醒节点
	private String newdocpath = ""; // 流程中文档字段新建时的默认目录
	private String issignview = "0";// 是否允许查看先关流程签字意见
	private String isselectrejectnode = "0";// 退回时是否可选择退回节点 td30785

	private String isneeddelacc = "0"; // 设置是否流程删除时相关的附件 qc46085
	private String smsAlertsType = "0"; // 设置短信提醒方式
	// 转发操作时是否给接收者默认赋值
	private String isForwardReceiveDef = "0";

	private String isTriDiffWorkflow = "0"; // 触发类型 add by liaodong for qc61523
											// in 2013-11-12 start
	private int dsporder = 0;
	private String isFree = "0"; // is free workflow : add by wanglu
									// 2014-09-02
	private String isoverrb = "0";//归档收回
	private String isoveriv = "0";//归档干预
	private String custompage = "";
	private String isAutoApprove = "0"; //允许自动批准
	private String isAutoCommit = "0"; //允许自动提交
	private String isAutoRemark = "1"; //自动填写用户最后一次手动操作的意见
	private int submittype = 0;

	public String getIsTriDiffWorkflow() {
		return isTriDiffWorkflow;
	}

	public void setIsTriDiffWorkflow(String isTriDiffWorkflow) {
		this.isTriDiffWorkflow = isTriDiffWorkflow;
	}

	// end

	public String getSmsAlertsType() {
		return smsAlertsType;
	}

	public void setSmsAlertsType(String smsAlertsType) {
		this.smsAlertsType = smsAlertsType;
	}

	public String getIssignview() {
		return issignview;
	}

	public void setIssignview(String issignview) {
		this.issignview = issignview;
	}

	public String getNewdocpath() {
		return newdocpath;
	}

	public void setNewdocpath(String newdocpath) {
		this.newdocpath = newdocpath;
	}

	public String getIsEdit() {
		return isEdit;
	}

	public void setIsEdit(String isEdit) {
		this.isEdit = isEdit;
	}

	public int getEditor() {
		return editor;
	}

	public void setEditor(int editor) {
		this.editor = editor;
	}

	public String getEditdate() {
		return editdate;
	}

	public void setEditdate(String editdate) {
		this.editdate = editdate;
	}

	public String getEdittime() {
		return edittime;
	}

	public void setEdittime(String edittime) {
		this.edittime = edittime;
	}

	public String getWfdocowner() {
		return wfdocowner;
	}

	public void setWfdocowner(String wfdocowner) {
		this.wfdocowner = wfdocowner;
	}
	
	/**
	 * @return the isoverrb
	 */
	public String getIsoverrb() {
		return isoverrb;
	}

	/**
	 * @param isoverrb the isoverrb to set
	 */
	public void setIsoverrb(String isoverrb) {
		this.isoverrb = isoverrb;
	}

	/**
	 * @return the isoveriv
	 */
	public String getIsoveriv() {
		return isoveriv;
	}

	/**
	 * @param isoveriv the isoveriv to set
	 */
	public void setIsoveriv(String isoveriv) {
		this.isoveriv = isoveriv;
	}

	public WFManager() {

	}

	/**
	 * 重置参数
	 */
	public void reset() {
		formid = 0;
		typeid = 0;
		wfid = 0;
		wfname = "";
		wfdes = "";
		action = "";
		isbill = "";
		iscust = "";
		helpdocid = 0;
		isvalid = "1";
		needmark = "";
		messageType = "0";
		mailMessageType = "0";// added xwj for td2965 20051101

		// 微信提醒START(QC:98106)
		chatsType = "0"; // add by fyg @ 20140319 for 微信提醒
		chatsAlertType = "0"; // add by fyg @ 20140319 for 微信提醒
		notRemindifArchived = "0"; // add by fyg @ 20140319 for 微信提醒
		// 微信提醒END(QC:98106)
		//分部id清掉
		subCompanyId2 = -1;

		archiveNoMsgAlert = "";
		archiveNoMailAlert = "";
		forbidAttDownload = "0";
		docRightByOperator = "0";
		IsTemplate = "";
		Templateid = 0;
		catelogType = 0;
		selectedCateLog = 0;
		docRightByHrmResource = 0;
        hrmResourceShow = 0;
		isaffirmance = "";
		isSaveCheckForm = "";
		isremak = "";
		isShowChart = "";
		orderbytype = "";
		isShowOnReportInput = "";
		isannexUpload = "";
		annexdocCategory = "";
		isModifyLog = "0";
		ShowDelButtonByReject = "0";

		specialApproval = "0";
		Frequency = "0";
		Cycle = "1";

		isimportwf = "0";
		importReadOnlyField = "";
		fieldNotImport = "";
		wfdocpath = "";
		wfdocowner = "";
		isEdit = "0";
		editor = -1;
		editdate = "";
		edittime = "";
		showUploadTab = "";
		isSignDoc = "";
		showDocTab = "";
		isSignWorkflow = "";
		showWorkflowTab = "";
		candelacc = "";
		isshared = "";
		isforwardrights = "";
		isrejectremind = "0";
		ischangrejectnode = "0";
		wfdocownertype = "";
		wfdocownerfieldid = "";
		newdocpath = "";
		issignview = "0";
		isselectrejectnode = "0";
		isImportDetail = "0";
		nosynfields = "";
		isneeddelacc = "0";
		SAPSource = "";
		smsAlertsType = "0";
		isTriDiffWorkflow = "0"; // add by liaodong for qc61523 in 2013-11-12
									// start
		dsporder = 0;
		isFree = "0";
		isoveriv = "0";
		isoverrb = "0";
		custompage = "";
		isAutoApprove = "0";
		isAutoCommit = "0";
		isAutoRemark = "1";
		submittype = 0;
	}

	public int getSubmittype() {
		return submittype;
	}

	public void setSubmittype(int submittype) {
		this.submittype = submittype;
	}

	public String getNosynfields() {
		return this.nosynfields;
	}

	public void setNosynfields(String nosynfields_) {
		this.nosynfields = nosynfields_;
	}

	/**
	 * 获得退回时是否可选择退回节点
	 * 
	 * @return
	 */
	public String getIsSelectrejectNode() {
		return isselectrejectnode;

	}

	/**
	 * 设置退回时是否可选择退回节点
	 * 
	 * @param isselectrejectnode
	 */
	public void setIsSelectrejectNode(String isselectrejectnode) {
		this.isselectrejectnode = isselectrejectnode;
	}

	/**
	 * 获得是否允许签字意见关联文档
	 * 
	 * @return
	 */
	public String getSignDoc() {
		return isSignDoc;
	}

	/**
	 * 设置是否允许签字意见关联文档
	 * 
	 * @param signDoc
	 */
	public void setSignDoc(String signDoc) {
		isSignDoc = signDoc;
	}

	/**
	 * 获得是否显示相关附件Tab
	 * 
	 * @return
	 */
	public String getShowUploadTab() {
		return showUploadTab;
	}

	/**
	 * 设置是否显示相关附件Tab
	 * 
	 * @param showUploadTab
	 */
	public void setShowUploadTab(String showUploadTab) {
		this.showUploadTab = showUploadTab;
	}

	/**
	 * 获得是否显示相关文档tab
	 * 
	 * @return
	 */
	public String getShowDocTab() {
		return showDocTab;
	}

	/**
	 * 设置是否显示相关文档tab
	 * 
	 * @param showDocTab
	 */
	public void setShowDocTab(String showDocTab) {
		this.showDocTab = showDocTab;
	}

	/**
	 * 获得转发权限
	 * 
	 * @return
	 */
	public String getIsforwardRights() {
		return isforwardrights;
	}

	/**
	 * 设置转发权限
	 * 
	 * @param oid
	 */
	public void setIsforwardRights(String oid) {
		this.isforwardrights = oid;
	}

	public String getIsshared() {
		return isshared;
	}

	public void setIsshared(String isshared) {
		this.isshared = isshared;
	}

	/**
	 * 获得是否允许签字意见关联流程
	 * 
	 * @return
	 */
	public String getSignWorkflow() {
		return isSignWorkflow;
	}

	/**
	 * 设置是否允许签字意见关联流程
	 * 
	 * @param signWorkflow
	 */
	public void setSignWorkflow(String signWorkflow) {
		isSignWorkflow = signWorkflow;
	}

	/**
	 * 获得是否显示相关流程Tab
	 * 
	 * @return
	 */
	public String getShowWorkflowTab() {
		return showWorkflowTab;
	}

	/**
	 * 设置是否显示相关流程tab
	 * 
	 * @param showWorkflowTab
	 */
	public void setShowWorkflowTab(String showWorkflowTab) {
		this.showWorkflowTab = showWorkflowTab;
	}

	/**
	 * 获得是否记录表单修改日志
	 * 
	 * @return 0:不记录 1:记录
	 */
	public String getIsModifyLog() {
		return isModifyLog;
	}

	public void setIsModifyLog(String isModifyLog) {
		this.isModifyLog = isModifyLog;
	}

	/**
	 * 获得是否提交确认
	 * 
	 * @return 1：提交确认
	 */
	public String getIsaffirmance() {
		return isaffirmance;
	}

	/**
	 * 设置是否提交确认
	 * 
	 * @param isaffirmance
	 */
	public void setIsaffirmance(String isaffirmance) {
		this.isaffirmance = isaffirmance;
	}

	/**
	 * 获得流程保存是否验证必填
	 * 
	 * @return the isSaveCheckForm
	 */
	public String getIsSaveCheckForm() {
		return isSaveCheckForm;
	}

	/**
	 * 设置流程保存是否验证必填
	 * 
	 * @param isSaveCheckForm
	 *            the isSaveCheckForm to set
	 */
	public void setIsSaveCheckForm(String isSaveCheckForm) {
		this.isSaveCheckForm = isSaveCheckForm;
	}

	/**
	 * 得到流程摸板ID
	 * 
	 * @return 流程摸板ID
	 */
	public int getTemplateid() {
		return Templateid;
	}

	/**
	 * 设置流程摸板ID
	 * 
	 * @param templateid
	 *            流程摸板ID
	 */
	public void setTemplateid(int templateid) {
		Templateid = templateid;
	}

	/**
	 * 得到是否是流程摸板
	 * 
	 * @return 流程摸板ID
	 */
	public String getIsTemplate() {
		return IsTemplate;
	}

	public String getIsremak() {
		return isremak;
	}

	/**
	 * 得到是否显示于报表填报
	 * 
	 * @return 是否显示于报表填报
	 */
	public String getIsShowOnReportInput() {
		return isShowOnReportInput;
	}

	/**
	 * 设置是否是流程摸板
	 * 
	 * @param isTemplate
	 *            是否是流程摸板
	 */
	public void setIsTemplate(String isTemplate) {
		IsTemplate = isTemplate;
	}

	/**
	 * 设置提交类型
	 * 
	 * @param action
	 *            提交类型
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * 得到流程ID
	 * 
	 * @return 流程ID
	 */
	public int getWfid() {
		return wfid;
	}

	/**
	 * 设置流程摸板ID
	 * 
	 * @param oid
	 */
	public void setWfid(int oid) {
		this.wfid = oid;
	}

	public void setIsremak(String oid) {
		this.isremak = oid;
	}

	/**
	 * 设置是否显示于报表填报
	 * 
	 * @param oid
	 */
	public void setIsShowOnReportInput(String oid) {
		this.isShowOnReportInput = oid;
	}

	/**
	 * 得到表单ID
	 * 
	 * @return 表单ID
	 */
	public int getFormid() {
		return formid;
	}

	/**
	 * 设置表单ID
	 * 
	 * @param oid
	 *            表单ID
	 */
	public void setFormid(int oid) {
		this.formid = oid;
	}

	/**
	 * 得到流程类型ID
	 * 
	 * @return 流程类型ID
	 */
	public int getTypeid() {
		return typeid;
	}

	/**
	 * 设置流程类型ID
	 * 
	 * @param oid
	 *            流程类型ID
	 */
	public void setTypeid(int oid) {
		this.typeid = oid;
	}

	/**
	 * 设置原流程类型ID
	 * 
	 * @param oid
	 *            流程类型ID
	 */
	public void setOldTypeid(int oid) {
		this.oldtypeid = oid;
	}

	/**
	 * 得到流程帮助文档ID
	 * 
	 * @return 流程帮助文档ID
	 */
	public int getHelpdocid() {
		return helpdocid;
	}

	/**
	 * 设置流程帮助文档ID
	 * 
	 * @param oid
	 *            流程帮助文档ID
	 */
	public void setHelpdocid(int oid) {
		this.helpdocid = oid;
	}

	/**
	 * 得到流程名称
	 * 
	 * @return 流程名称
	 */
	public String getWfname() {
		return wfname;
	}

	/**
	 * 设置流程名称
	 * 
	 * @param orderno
	 *            流程名称
	 */
	public void setWfname(String orderno) {
		this.wfname = orderno;
	}

	/**
	 * 得到是否是单据
	 * 
	 * @return 否是单据
	 */
	public String getIsBill() {
		return isbill;
	}

	/**
	 * 设置是否是单据
	 * 
	 * @param orderno
	 *            是否是单据0/1
	 */
	public void setIsBill(String orderno) {
		this.isbill = orderno;
	}

	/**
	 * 得到是否为门户工作流
	 * 
	 * @return 是否为门户工作流（0/1）
	 */
	public String getIsCust() {
		return iscust;
	}

	/**
	 * 设置是否为门户工作流
	 * 
	 * @param orderno
	 *            是否为门户工作流（0/1）
	 */
	public void setIsCust(String orderno) {
		this.iscust = orderno;
	}

	/**
	 * 得到流程描述
	 * 
	 * @return 流程描述
	 */
	public String getWfdes() {
		return wfdes;
	}

	/**
	 * 设置流程描述
	 * 
	 * @param orderno
	 *            流程描述
	 */
	public void setWfdes(String orderno) {
		this.wfdes = orderno;
	}

	/**
	 * 得到流程是否有效
	 * 
	 * @return 流程是否有效
	 */
	public String getIsValid() {
		return isvalid;
	}

	/**
	 * 流程是否有效
	 * 
	 * @param isvalid
	 *            流程是否有效
	 */
	public void setIsValid(String isvalid) {
		this.isvalid = isvalid;
	}

	/**
	 * 得到流程是否需要签意见
	 * 
	 * @return 流程是否需要签意见
	 */
	public String getNeedMark() {
		return needmark;
	}

	/**
	 * 设置流程是否需要签意见
	 * 
	 * @param needmark
	 *            流程是否需要签意见
	 */
	public void setNeedMark(String needmark) {
		this.needmark = needmark;
	}

	/**
	 * 得到流程提醒类型
	 * 
	 * @return 流程提醒类型
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * 设置流程提醒类型据
	 * 
	 * @param messageType
	 *            流程提醒类型
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * 得到流程邮件提醒类型据
	 * 
	 * @return 邮件提醒类型据
	 */
	public String getMailMessageType() {
		return mailMessageType;
	}

	/**
	 * 设置流程邮件提醒类型据
	 * 
	 * @param mailMessageType
	 *            流程邮件提醒类型据
	 */
	public void setMailMessageType(String mailMessageType) {
		this.mailMessageType = mailMessageType;
	}

	/**
	 * 得到归档节点不需短信提醒
	 * 
	 * @return the archiveNoMsgAlert
	 */
	public String getArchiveNoMsgAlert() {
		return archiveNoMsgAlert;
	}

	/**
	 * 设置归档节点不需短信提醒
	 * 
	 * @param archiveNoMsgAlert
	 *            the archiveNoMsgAlert to set
	 */
	public void setArchiveNoMsgAlert(String archiveNoMsgAlert) {
		this.archiveNoMsgAlert = archiveNoMsgAlert;
	}

	/**
	 * 得到归档节点不需邮件提醒
	 * 
	 * @return the archiveNoMailAlert
	 */
	public String getArchiveNoMailAlert() {
		return archiveNoMailAlert;
	}

	/**
	 * 设置归档节点不需邮件提醒
	 * 
	 * @param archiveNoMailAlert
	 *            the archiveNoMailAlert to set
	 */
	public void setArchiveNoMailAlert(String archiveNoMailAlert) {
		this.archiveNoMailAlert = archiveNoMailAlert;
	}

	/**
	 * 得到流程是否可以批量提交
	 * 
	 * @return 流程是否可以批量提交
	 */
	public String getMultiSubmit() {
		return multiSubmit;
	}

	/**
	 * 设置流程是否可以批量提交
	 * 
	 * @param multiSubmit
	 *            流程是否可以批量提交
	 */
	public void setMultiSubmit(String multiSubmit) {
		this.multiSubmit = multiSubmit;
	}

	/**
	 * 得到流程缺省名称
	 * 
	 * @return 流程缺省名称
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * 设置流程缺省名称
	 * 
	 * @param defaultName
	 *            流程缺省名称/1
	 */
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	/**
	 * 获得退回创建节点是否可删除
	 * 
	 * @return
	 */
	public String getShowDelButtonByReject() {
		return ShowDelButtonByReject;
	}

	/**
	 * 获得是否特批件
	 * 
	 * @return
	 */
	public String getSpecialApproval() {
		return specialApproval;
	}

	/**
	 * 设置是否特批件
	 * 
	 * @param specialApproval
	 */
	public void setSpecialApproval(String specialApproval) {
		this.specialApproval = specialApproval;
	}

	/**
	 * 设置退回创建节点是否可删除
	 * 
	 * @param showDelButtonByReject
	 */
	public void setShowDelButtonByReject(String showDelButtonByReject) {
		ShowDelButtonByReject = showDelButtonByReject;
	}

	/**
	 * 获得退回是否提醒
	 * 
	 * @return
	 */
	public String getIsrejectremind() {
		return isrejectremind;
	}

	/**
	 * 设置退回是否提醒
	 * 
	 * @param isrejectremind
	 */
	public void setIsrejectremind(String isrejectremind) {
		this.isrejectremind = isrejectremind;
	}

	/**
	 * 获得退回时是否可设置提醒节点
	 * 
	 * @return
	 */
	public String getIschangrejectnode() {
		return ischangrejectnode;
	}

	/**
	 * 设置退回时是否可设置提醒节点
	 * 
	 * @param ischangrejectnode
	 */
	public void setIschangrejectnode(String ischangrejectnode) {
		this.ischangrejectnode = ischangrejectnode;
	}

	/**
	 * 获得新建时是否可导入流程
	 * 
	 * @return
	 */
	public String getIsImportwf() {
		return isimportwf;
	}

	/**
	 * 设置新建时是否可导入流程
	 * 
	 * @param isimportwf
	 */
	public void setIsImportwf(String isimportwf) {
		this.isimportwf = isimportwf;
	}

	/**
	 * 获得是否允许导入数据到只读字段
	 * 
	 * @return importReadOnlyField
	 */
	public String getImportReadOnlyField() {
		return importReadOnlyField;
	}

	/**
	 * 设置是否允许导入数据到只读字段
	 * 
	 * @return
	 */
	public void setImportReadOnlyField(String importReadOnlyField) {
		this.importReadOnlyField = importReadOnlyField;
	}

	/**
	 * 获得无需导入字段
	 * 
	 * @return
	 */
	public String getFieldNotImport() {
		return fieldNotImport;
	}

	/**
	 * 设置无需导入字段
	 * 
	 * @param isimportwf
	 */
	public void setFieldNotImport(String fieldNotImport) {
		this.fieldNotImport = fieldNotImport;
	}

	public void setDsporder(int dsporder) {
		this.dsporder = dsporder;
	}

	public int getDsporder() {
		return this.dsporder;
	}

	public void setIsFree(String isFree) {
		this.isFree = isFree;
	}

	public String getIsFree() {
		return this.isFree;
	}

	public void setCustompage(String custompage) {
		this.custompage = custompage;
	}

	public String getCustompage() {
		return this.custompage;
	}
	public String getIsAutoApprove() {
        return this.isAutoApprove;
    }
    public void setIsAutoApprove(String isAutoApprove) {
        this.isAutoApprove = isAutoApprove;
    }
    public String getIsAutoCommit() {
        return this.isAutoCommit;
    }
    public void setIsAutoCommit(String isAutoCommit) {
        this.isAutoCommit = isAutoCommit;
	}
	public String getIsAutoRemark() {
		return isAutoRemark;
	}
	public void setIsAutoRemark(String isAutoRemark) {
		this.isAutoRemark = isAutoRemark;
	}

	public void getWfInfo() throws Exception {
		// //得到订单的信息，在修改和显示详细信息时使用
		String sql = "select * from workflow_base where id=?";
		ConnStatement statement = null;
		statement = new ConnStatement();
		try {
			statement.setStatementSql(sql);
			statement.setInt(1, this.wfid);
			statement.executeQuery();
			if (!statement.next()) {
				statement.close();
				return;
			}

			this.setWfid(statement.getInt("id"));

			this.setWfname(Util
					.null2String(statement.getString("workflowname")));
			this
					.setWfdes(Util.null2String(statement
							.getString("workflowdesc")));
			this.setTypeid(statement.getInt("workflowtype"));
			this.setFormid(statement.getInt("formid"));
			this.setIsBill(Util.null2String(statement.getString("isbill")));
			this.setIsCust(Util.null2String(statement.getString("iscust")));
			this.setHelpdocid(Util.getIntValue(
					statement.getString("helpdocid"), 0));
			this.setIsValid(Util.null2String(statement.getString("isvalid")));
			this.setNeedMark(Util.null2String(statement.getString("needmark")));
			this.setMessageType(Util.null2String(statement
					.getString("messageType")));
			this.setMultiSubmit(Util.null2String(statement
					.getString("multiSubmit")));
			this.setDefaultName(Util.null2String(statement
					.getString("defaultName")));
			this.setDocCategory(Util.null2String(statement
					.getString("docCategory")));
			this.setDocPath(Util.null2String(statement.getString("docPath")));
			this.subCompanyId2 = statement.getInt("subcompanyid");
			this.setMailMessageType(Util.null2String(statement
					.getString("mailMessageType")));// added by xwj for td2965
													// 20051101
			this.setArchiveNoMsgAlert(Util.null2String(statement
					.getString("archiveNoMsgAlert")));
			this.setArchiveNoMailAlert(Util.null2String(statement
					.getString("archiveNoMailAlert")));
			this.setForbidAttDownload(Util.null2String(statement
					.getString("forbidAttDownload")));
			this.setDocRightByOperator(Util.null2String(statement
					.getString("docRightByOperator")));
			this.setIsTemplate(Util.null2String(statement
					.getString("isTemplate")));
			this.setTemplateid(Util.getIntValue(statement
					.getString("Templateid"), 0));
			this.setCatelogType(Util.getIntValue(statement
					.getString("catelogType"), 0));
			this.setSelectedCateLog(Util.getIntValue(statement
					.getString("selectedCateLog"), 0));
			this.setDocRightByHrmResource(Util.getIntValue(statement
					.getString("docRightByHrmResource")));
            this.setHrmResourceShow(Util.getIntValue(statement
                    .getString("hrmResourceShow")));
			this.setIsaffirmance(Util.null2String(statement
					.getString("needaffirmance")));
			this.setIsSaveCheckForm(Util.null2String(statement
					.getString("isSaveCheckForm")));
			this.setIsremak(Util.null2String(statement.getString("isremarks")));
			this.setIsAnnexUpload(Util.null2String(statement
					.getString("isannexUpload")));
			this.setAnnexDocCategory(Util.null2String(statement
					.getString("annexdoccategory")));
			this.setIsShowOnReportInput(Util.null2String(statement
					.getString("isShowOnReportInput")));
			this.setTitleFieldId(Util.getIntValue(statement
					.getString("titleFieldId")));
			this.setKeywordFieldId(Util.getIntValue(statement
					.getString("keywordFieldId")));
			this.setIsShowChart(Util.null2String(statement
					.getString("isshowchart")));
			this.setOrderbytype(Util.null2String(statement
					.getString("orderbytype")));
			this.setIsModifyLog(Util.null2String(statement
					.getString("isModifyLog")));
			this.setShowDelButtonByReject(Util.null2String(statement
					.getString("ShowDelButtonByReject")));

			this.setSpecialApproval(Util.null2String(statement
					.getString("specialApproval")));
			this.setFrequency(Util
					.null2String(statement.getString("Frequency")));
			this.setCycle(Util.null2String(statement.getString("Cycle")));

			this.setIsImportwf(Util.null2String(statement
					.getString("isimportwf")));
			this.setImportReadOnlyField(Util.null2String(statement.getString("importReadOnlyField")));
			this.setFieldNotImport(Util.null2String(statement
					.getString("fieldNotImport")));
			this.setWfdocpath(Util
					.null2String(statement.getString("wfdocpath")));
			this.setWfdocowner(Util.null2String(statement
					.getString("wfdocowner")));
			this.setWfdocownertype(""
					+ Util
							.getIntValue(statement.getString("wfdocownertype"),
									0));
			this.setWfdocownerfieldid(""
					+ Util.getIntValue(
							statement.getString("wfdocownerfieldid"), 0));
			this.setIsEdit(Util.null2String(statement.getString("isEdit")));
			this.setEditor(statement.getInt("editor"));
			this.setEditdate(Util.null2String(statement.getString("editdate")));
			this.setEdittime(Util.null2String(statement.getString("edittime")));
			this.setShowUploadTab(Util.null2String(statement
					.getString("showUploadTab")));
			this.setSignDoc(Util.null2String(statement.getString("isSignDoc")));
			this.setShowDocTab(Util.null2String(statement
					.getString("showDocTab")));
			this.setSignWorkflow(Util.null2String(statement
					.getString("isSignWorkflow")));
			this.setShowWorkflowTab(Util.null2String(statement
					.getString("showWorkflowTab")));
			this.setCanDelAcc(Util
					.null2String(statement.getString("candelacc")));
			this.setIsshared(Util.null2String(statement.getString("isshared")));
			this.setIsforwardRights(Util.null2String(statement
					.getString("isforwardrights")));
			this.setIsrejectremind(Util.null2String(statement
					.getString("isrejectremind")));
			this.setIsSelectrejectNode(Util.null2String(statement
					.getString("isselectrejectnode")));
			this.setIsImportDetail(Util.null2String(statement
					.getString("isImportDetail")));
			this.setIschangrejectnode(Util.null2String(statement
					.getString("ischangrejectnode")));
			this.setNewdocpath(Util.null2String(statement
					.getString("newdocpath")));
			this.setIssignview(Util.null2String(statement
					.getString("issignview")));
			this.setNosynfields(Util.null2String(statement
					.getString("nosynfields")));
			this.setIsneeddelacc(Util.null2String(statement
					.getString("isneeddelacc")));
			this.setSAPSource(Util
					.null2String(statement.getString("SAPSource")));
			this.setSmsAlertsType(Util.null2String(statement
					.getString("smsAlertsType")));
			this.setIsForwardReceiveDef(Util.null2String(statement
					.getString("forwardReceiveDef")));

			// 微信提醒START(QC:98106)
			this.setChatsType(Util
					.null2String(statement.getString("chatsType")));
			this.setChatsAlertType(Util.null2String(statement
					.getString("chatsAlertType")));
			this.setNotRemindifArchived(Util.null2String(statement
					.getString("notRemindifArchived")));
			// 微信提醒END(QC:98106)

			this.setDsporder(Util.getIntValue(statement.getString("dsporder"),0));
			this.setIsFree(Util.null2String(statement.getString("isfree")));
			this.setIsoverrb(Util.null2String(statement.getString("isoverrb")));
			this.setIsoveriv(Util.null2String(statement.getString("isoveriv")));
			this.setCustompage(Util.null2String(statement.getString("custompage")));
			this.setIsAutoApprove(Util.null2s(statement.getString("isAutoApprove").trim(),"0"));
			this.setIsAutoCommit(Util.null2s(statement.getString("isAutoCommit").trim(),"0"));
			this.setIsAutoRemark(Util.null2s(statement.getString("isAutoRemark").trim(),"1"));
			setSubmittype(Util.getIntValue(statement.getString("submittype"),0));
		} catch (Exception e) {
			writeLog(e);
			throw e;
		} finally {
			try {
				statement.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * 通过流程模板创建工作流
	 * 
	 * @param templateid
	 *            应用的摸板ID
	 * @param istemplate
	 *            是否有效 （1：否 0：是 3:版本）
	 * @param LocIP
	 *            当前用户的IP
	 * @param userid
	 *            创建人ID
	 * @return 创建工作流的ID
	 * @throws Exception
	 */
	public int setWFTemplate(int templateid, String istemplate, int userid,
			String LocIP) throws Exception {
		Map<String,String> nodeIdMap = new HashMap<String, String>();// 新旧版本节点id关系对应map
        int returnValue = 0;
        SysMaintenanceLog syslog = new SysMaintenanceLog();
        List checkLinkRulelList = new ArrayList();
        List checkRulelList = new ArrayList();
        Map ruleLinkMap = new HashMap();
        Map ruleBatchMap = new HashMap();
        Map rulevarBatchMap = new HashMap();
        Map ruleexpbaseMap = new HashMap();
        Map rulelinkexpbaseMap = new HashMap();
        Map ruleexpsMap = new HashMap();
        Map rulelinkexpsMap = new HashMap();
        Map rulevarlinkMap = new HashMap();
        RecordSet gdrs = new RecordSet();
        RecordSet linkrs = new RecordSet();
        RecordSet linkrs1 = new RecordSet();
        RecordSet linkrs2 = new RecordSet();
        RecordSet batchrs = new RecordSet();
        RecordSet batchrs1 = new RecordSet();
        String sql = "select * from workflow_base where id=" + templateid;
        HashMap map = new HashMap();
        HashMap nodelinkmap = new HashMap();
        RecordSet rs = new RecordSet();
        rs.executeSql(sql);
        if (rs.next()) {
            // 新建工作流
            String workflowname = this.wfname;
            String workflowdesc = this.wfdes;
            int workflowtype = rs.getInt("workflowtype");
            int formid = rs.getInt("formid");
            String isbill = rs.getString("isbill");
            String iscust = rs.getString("iscust");
            int helpdocid = rs.getInt("helpdocid");
            String isvalid = "1";
            if ("1".equals(istemplate)) {
                isvalid = "0";
            }

            if ("3".equals(istemplate)) {
				istemplate = "0";
                isvalid = "3";
                workflowname = rs.getString("workflowname");
            }

            String needmark = rs.getString("needmark");
            String messageType = rs.getString("messageType");
            String multiSubmit = rs.getString("multiSubmit");
            String defaultName = rs.getString("defaultName");
            String docCategory = rs.getString("docCategory");
            String _isannexUpload = rs.getString("isannexUpload");
            String _annexdoccategory = rs.getString("annexdoccategory");
            String docPath = rs.getString("docPath");
            String isremark = rs.getString("isremarks");
            String isshowchart = rs.getString("isshowchart");
            String orderbytype = rs.getString("orderbytype");
            String isShowOnReportInput = rs.getString("isShowOnReportInput");
            int subcompanyid = rs.getInt("subcompanyid");
            String mailMessageType = rs.getString("mailMessageType");
            archiveNoMsgAlert = rs.getString("archiveNoMsgAlert");
            archiveNoMailAlert = rs.getString("archiveNoMailAlert");
            String forbidAttDownload = rs.getString("forbidAttDownload");
            String docRightByOperator = rs.getString("docRightByOperator");
            String hasaffirmance = rs.getString("needaffirmance");
            isSaveCheckForm = rs.getString("isSaveCheckForm");
            catelogType = Util.getIntValue(rs.getString("catelogType"), 0);
            selectedCateLog = Util.getIntValue(rs.getString("selectedCateLog"), 0);
            docRightByHrmResource = Util.getIntValue(rs.getString("docRightByHrmResource"), 0);
            hrmResourceShow = Util.getIntValue(rs.getString("hrmResourceShow"), 0);
            titleFieldId = Util.getIntValue(rs.getString("titleFieldId"), 0);
            keywordFieldId = Util.getIntValue(rs.getString("keywordFieldId"), 0);
            ShowDelButtonByReject = Util.null2String(rs.getString("ShowDelButtonByReject"));

            specialApproval = Util.null2String(rs.getString("specialApproval"));
            Frequency = Util.null2String(rs.getString("Frequency"));
            Cycle = Util.null2String(rs.getString("Cycle"));

            isimportwf = Util.null2String(rs.getString("isimportwf"));
            importReadOnlyField = Util.null2String(rs.getString("importReadOnlyField"));
            fieldNotImport = Util.null2String(rs.getString("fieldNotImport"));
            wfdocpath = Util.null2String(rs.getString("wfdocpath"));
            wfdocowner = Util.null2String(rs.getString("wfdocowner"));
            wfdocownertype = "" + Util.getIntValue(rs.getString("wfdocownertype"), 0);
            isImportDetail = Util.null2String(rs.getString("isImportDetail"));
            wfdocownerfieldid = Util.null2String(rs.getString("wfdocownerfieldid"));
            showUploadTab = Util.null2String(rs.getString("showUploadTab"));
            isSignDoc = Util.null2String(rs.getString("isSignDoc"));
            showDocTab = Util.null2String(rs.getString("showDocTab"));
            isSignWorkflow = Util.null2String(rs.getString("isSignWorkflow"));
            showWorkflowTab = Util.null2String(rs.getString("showWorkflowTab"));
            isrejectremind = Util.null2String(rs.getString("isrejectremind"));
            isselectrejectnode = Util.null2String(rs.getString("isselectrejectnode"));
            ischangrejectnode = Util.null2String(rs.getString("ischangrejectnode"));
            newdocpath = Util.null2String(rs.getString("newdocpath"));
            this.isshared = "" + Util.getIntValue(rs.getString("isshared"), 0);
            this.isforwardrights = "" + Util.getIntValue(rs.getString("isforwardrights"), 0);
            this.isModifyLog = "" + Util.getIntValue(rs.getString("isModifyLog"), 0);
            String _candelacc = rs.getString("candelacc");// 是否允许创建人删除附件
            String ifVersion = rs.getString("ifVersion");// 是否保留正文版本
            issignview = Util.null2String(rs.getString("issignview"));// 相关流程意见不显示
            // TD27806
            this.nosynfields = Util.null2String(rs.getString("nosynfields"));
            this.isneeddelacc = Util.null2String(rs.getString("isneeddelacc"));
            this.SAPSource = Util.null2String(rs.getString("SAPSource"));
            this.smsAlertsType = rs.getString("smsAlertsType");
            this.isTriDiffWorkflow = rs.getString("isTriDiffWorkflow"); // add
            this.isAutoApprove = rs.getString("isAutoApprove");
            this.isAutoCommit = rs.getString("isAutoCommit");
            this.isAutoRemark = rs.getString("isAutoRemark");
			this.dsporder=Util.getIntValue(rs.getString("dsporder"), 0);
			submittype = Util.getIntValue(rs.getString("submittype"),0);
            // by
            // liaodong
            // for
            // qc61523
            // in
            // 2013-11-12
            // start
            this.isForwardReceiveDef = rs.getString("forwardReceiveDef");
			this.isoveriv = Util.null2String(rs.getString("isoveriv"));
			this.isoverrb = Util.null2String(rs.getString("isoverrb"));
			this.custompage = Util.null2String(rs.getString("custompage"));
            // 微信提醒START(QC:98106)
            chatsType = Util.null2String(rs.getString("chatsType"));
            chatsAlertType = Util.null2String(rs.getString("chatsAlertType"));
            notRemindifArchived = Util.null2String(rs.getString("notRemindifArchived"));
            if ("".equals(wfdes)) {
                wfdes = Util.null2String(rs.getString("workflowdesc"));// 工作流描述
            }
            String isFree2 = Util.null2String(rs.getString("isFree"));
			//9-30 增加的字段：zzw
			String custompage4emoble=Util.null2String(rs.getString("custompage4emoble"));
            // sql="insert into
            // workflow_base(workflowname,workflowdesc,workflowtype,formid,isbill,iscust,helpdocid,isvalid,needmark,messageType,multiSubmit,defaultName,docCategory,docPath,subcompanyid,mailMessageType,docRightByOperator,isTemplate,Templateid,needaffirmance,catelogType,selectedCateLog,docRightByHrmResource,isremarks,isannexUpload,annexdoccategory,isShowOnReportInput)
            // values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // add by liaodong for qc61523 in 2013-11-12 start add field
            // isTriDiffWorkflow
			//9-30 增加一个字段 ：zzw
            sql = "insert into workflow_base(workflowname,workflowdesc,workflowtype,formid,isbill,iscust,helpdocid,isvalid,needmark,messageType,multiSubmit,defaultName,docCategory,docPath,subcompanyid,mailMessageType,docRightByOperator,isTemplate,Templateid,needaffirmance,catelogType,selectedCateLog,docRightByHrmResource,isremarks,isannexUpload,annexdoccategory,isShowOnReportInput,titleFieldId,keywordFieldId,isshowchart,orderbytype,isModifyLog,wfdocpath,wfdocowner,ShowDelButtonByReject,showUploadTab,isSignDoc,showDocTab,isSignWorkflow,showWorkflowTab,candelacc,isimportwf,isrejectremind,ischangrejectnode,wfdocownertype,wfdocownerfieldid,newdocpath,isforwardrights,ifVersion,issignview,isselectrejectnode,isImportDetail,specialApproval,Frequency,Cycle,forbidAttDownload,nosynfields,isneeddelacc,SAPSource,smsAlertsType,isTriDiffWorkflow,isSaveCheckForm,archiveNoMsgAlert,archiveNoMailAlert,forwardReceiveDef,fieldNotImport,chatsType,chatsAlertType,notRemindifArchived,isshared,isFree,isoverrb,isoveriv,custompage,isAutoApprove,isAutoCommit,isAutoRemark,custompage4emoble,hrmResourceShow,dsporder,importReadOnlyField,submittype) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            ConnStatement statement = null;
            statement = new ConnStatement();
            try {
                statement.setStatementSql(sql);
                statement.setString(1, workflowname);
                statement.setString(2, wfdes);
				if(this.typeid>0){
					subcompanyid = this.typeid;
				}
                statement.setInt(3, workflowtype);
                statement.setInt(4, formid);
                statement.setString(5, isbill);
                statement.setString(6, iscust);
                statement.setInt(7, helpdocid);
                statement.setString(8, isvalid);
                statement.setString(9, needmark);
                statement.setString(10, messageType);
                statement.setString(11, multiSubmit);
                statement.setString(12, defaultName);
                statement.setString(13, docCategory);
                statement.setString(14, docPath);
				//插入分部
				if(this.subCompanyId2!=-1){
					subcompanyid = this.subCompanyId2;
				}
                statement.setInt(15, subcompanyid);
                statement.setString(16, mailMessageType);// added by xwj for
                // td2965 20051101
                statement.setString(17, docRightByOperator);
                statement.setString(18, istemplate);
                statement.setInt(19, templateid);
                statement.setString(20, hasaffirmance);
                statement.setInt(21, catelogType);
                statement.setInt(22, selectedCateLog);
                statement.setInt(23, docRightByHrmResource);
                statement.setString(24, isremark);
                statement.setString(25, _isannexUpload);
                statement.setString(26, _annexdoccategory);
                statement.setString(27, isShowOnReportInput);
                statement.setInt(28, titleFieldId);
                statement.setInt(29, keywordFieldId);
                statement.setString(30, isshowchart);
                statement.setString(31, orderbytype);
                statement.setString(32, isModifyLog);
                statement.setString(33, wfdocpath);
                statement.setString(34, wfdocowner);
                statement.setString(35, ShowDelButtonByReject);
                statement.setString(36, showUploadTab);
                statement.setString(37, isSignDoc);
                statement.setString(38, showDocTab);
                statement.setString(39, isSignWorkflow);
                statement.setString(40, showWorkflowTab);
                statement.setString(41, _candelacc);
                statement.setString(42, isimportwf);
                statement.setString(43, isrejectremind);
                statement.setString(44, ischangrejectnode);
                statement.setInt(45, Util.getIntValue(wfdocownertype, 0));
                statement.setInt(46, Util.getIntValue(wfdocownerfieldid, 0));
                statement.setString(47, newdocpath);
                statement.setString(48, this.isforwardrights);
                statement.setString(49, ifVersion);
                statement.setString(50, issignview);
                // td30785
                statement.setString(51, isselectrejectnode);
                statement.setString(52, isImportDetail);
                statement.setString(53, specialApproval);
                statement.setString(54, Frequency);
                statement.setString(55, Cycle);
                statement.setString(56, forbidAttDownload);
                statement.setString(57, this.nosynfields);
                statement.setString(58, this.isneeddelacc);
                statement.setString(59, this.SAPSource);
                statement.setString(60, this.smsAlertsType);
                statement.setString(61, this.isTriDiffWorkflow);// add by
                // liaodong for
                // qc61523 in
                // 2013-11-12
                // start
                statement.setString(62, this.isSaveCheckForm);
                statement.setString(63, this.archiveNoMsgAlert);
                statement.setString(64, this.archiveNoMailAlert);
                statement.setString(65, this.isForwardReceiveDef);
                statement.setString(66, this.fieldNotImport);
                // 存为模板增加微信提醒
                if (this.chatsType.equals("1")) {
                    statement.setString(67, this.chatsType);
                    statement.setString(68, this.chatsAlertType);
                    statement.setString(69, this.notRemindifArchived);
                } else {
                    statement.setString(67, this.chatsType);
                    statement.setString(68, "0");
                    statement.setString(69, "0");
                }
                statement.setString(70, this.isshared);
                statement.setString(71, isFree2);
				statement.setString(72, this.isoverrb);
				statement.setString(73, this.isoveriv);
				statement.setString(74, this.custompage);
				//9-30 增加的字段：zzw
				statement.setString(75, this.isAutoApprove);
				statement.setString(76, this.isAutoCommit);
				statement.setString(77, this.isAutoRemark);
				statement.setString(78, custompage4emoble);
                statement.setInt(79, hrmResourceShow);
				//显示顺序字段
				statement.setInt(80, this.dsporder);
				statement.setString(81, this.importReadOnlyField);
				statement.setInt(82, submittype);
                statement.executeUpdate();
                ArrayList oldnodeidlist = new ArrayList();
                ArrayList nodeidlist = new ArrayList();
                ArrayList oldnodeidlist2 = new ArrayList();
                ArrayList nodeidlist2 = new ArrayList();
                ArrayList oldnodeloglist = new ArrayList();
                ArrayList oldnodeloglist2 = new ArrayList();
                sql = "select max(id) as maxid from workflow_base";
                statement.setStatementSql(sql);
                statement.executeQuery();
                if (statement.next()) {
                    returnValue = statement.getInt("maxid");
                    // 写流程日志
                    syslog.resetParameter();
                    syslog.setRelatedId(returnValue);
                    syslog.setRelatedName(workflowname);
                    syslog.setOperateType("1");
                    syslog.setOperateDesc("WrokFlow_insert");
                    syslog.setOperateItem("85");
                    syslog.setOperateUserid(userid);
                    syslog.setClientAddress(LocIP);
                    syslog.setIstemplate(Util.getIntValue(istemplate));
                    syslog.setSysLogInfo();
                    // 写流程日志End
                    // copy节点信息
                    sql = "select * from workflow_flownode ,workflow_nodebase  where (workflow_nodebase.isfreenode is null or workflow_nodebase.isfreenode !='1') and nodeid=id and workflowid=" + templateid + " order by id";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String printflowcomment = rs.getString("printflowcomment");
                        String oldnodeid = rs.getString("nodeid");
                        oldnodeidlist.add(oldnodeid);
                        String nodename = rs.getString("nodename");
                        String isstart = rs.getString("isstart");
                        String isreject = rs.getString("isreject");
                        String isreopen = rs.getString("isreopen");
                        String isend = rs.getString("isend");
                        int drawxpos = rs.getInt("drawxpos");
                        int drawypos = rs.getInt("drawypos");
                        int totalgroups = rs.getInt("totalgroups");
                        String nodetype = rs.getString("nodetype");
                        int isprintpage = rs.getInt("isprintpage");
                        String printnum = rs.getString("printnum");
                        // int isFutureViewer=rs.getInt("isFutureViewer");
                        // int isHistoryViewer=rs.getInt("isHistoryViewer");
                        String viewnodeids = rs.getString("viewnodeids");
                        oldnodeloglist.add(viewnodeids);
                        String ismode = rs.getString("ismode");
                        String showdes = rs.getString("showdes");
                        String printdes = rs.getString("printdes");
                        String Freefs = rs.getString("Freefs");
                        String nodeattribute = rs.getString("nodeattribute");
                        int passnum = rs.getInt("passnum");
                        String viewtypeall = rs.getString("viewtypeall");
                        String viewdescall = rs.getString("viewdescall");
                        String showtype = rs.getString("showtype");
                        String vtapprove = rs.getString("vtapprove");
                        String vtrealize = rs.getString("vtrealize");
                        String vtforward = rs.getString("vtforward");
                        String vtpostil = rs.getString("vtpostil");
                        String vtHandleForward = rs.getString("vtHandleForward"); // 转办
                        String vtTakingOpinions = rs.getString("vtTakingOpinions"); // 征求意见
                        String vttpostil = rs.getString("vttpostil");
                        String vtrecipient = rs.getString("vtrecipient");
                        String vtrpostil = rs.getString("vtrpostil");
                        String vtreject = rs.getString("vtreject");
                        String vtsuperintend = rs.getString("vtsuperintend");
                        String vtover = rs.getString("vtover");
                        String vdcomments = rs.getString("vdcomments");
                        String vddeptname = rs.getString("vddeptname");
                        String vdoperator = rs.getString("vdoperator");
                        String vddate = rs.getString("vddate");
                        String vdtime = rs.getString("vdtime");
                        String nodetitle = rs.getString("nodetitle");
                        String isFormSignature = rs.getString("isFormSignature");
                        String IsPendingForward = rs.getString("IsPendingForward");
                        String IsWaitForwardOpinion = rs.getString("IsWaitForwardOpinion");
                        String IsBeForward = rs.getString("IsBeForward");
                        String IsSubmitedOpinion = rs.getString("IsSubmitedOpinion");
                        String IsSubmitForward = rs.getString("IsSubmitForward");
                        String formSignatureWidth = rs.getString("formSignatureWidth");
                        String formSignatureHeight = rs.getString("formSignatureHeight");
                        String IsFreeWorkflow = rs.getString("IsFreeWorkflow");
                        String IsFreeNode = rs.getString("IsFreeNode");
                        String freewfsetcurnamecn = rs.getString("freewfsetcurnamecn");
                        String freewfsetcurnameen = rs.getString("freewfsetcurnameen");
                        String freewfsetcurnametw = rs.getString("freewfsetcurnametw");
                        String stnull = rs.getString("stnull");
                        String toexcel = rs.getString("toexcel");
                        String issignmustinput = rs.getString("issignmustinput");
                        String isfeedback = rs.getString("isfeedback");
                        String isnullnotfeedback = rs.getString("isnullnotfeedback");
                        String rejectbackflag = rs.getString("rejectbackflag");
                        String drawbackflag = rs.getString("drawbackflag");
                        String vsignupload = rs.getString("vsignupload");
                        String vsigndoc = rs.getString("vsigndoc");
                        String vsignworkflow = rs.getString("vsignworkflow");
                        String IsBeForwardSubmit = rs.getString("IsBeForwardSubmit");
                        String IsBeForwardModify = rs.getString("IsBeForwardModify");
                        String IsBeForwardPending = rs.getString("IsBeForwardPending");
                        String IsShowPendingForward = rs.getString("IsShowPendingForward");
                        String IsShowWaitForwardOpinion = rs.getString("IsShowWaitForwardOpinion");
                        String IsShowBeForward = rs.getString("IsShowBeForward");
                        String IsShowSubmitedOpinion = rs.getString("IsShowSubmitedOpinion");
                        String IsShowSubmitForward = rs.getString("IsShowSubmitForward");
                        String IsShowBeForwardSubmit = rs.getString("IsShowBeForwardSubmit");
                        String IsShowBeForwardModify = rs.getString("IsShowBeForwardModify");
                        String IsShowBeForwardPending = rs.getString("IsShowBeForwardPending");

                        // 重新补充
                        String vtintervenor = rs.getString("vtintervenor");
                        String IsBeForwardTodo = rs.getString("IsBeForwardTodo");
                        String IsShowBeForwardTodo = rs.getString("IsShowBeForwardTodo");
                        String IsBeForwardAlready = rs.getString("IsBeForwardAlready");
                        String IsShowAlreadyForward = rs.getString("IsShowAlreadyForward");
                        String IsAlreadyForward = rs.getString("IsAlreadyForward");
                        // String
                        // IsShowAlreadyForward=rs.getString("IsShowAlreadyForward");
                        String IsBeForwardSubmitAlready = rs.getString("IsBeForwardSubmitAlready");
                        String IsShowBeForwardSubmitAlready = rs.getString("IsShowBeForwardSubmitAlready");
                        String IsBeForwardSubmitNotaries = rs.getString("IsBeForwardSubmitNotaries");
                        String IsShowBeForwardSubmitNotaries = rs.getString("IsShowBeForwardSubmitNotaries");
                        // String Freefs=rs.getString("Freefs");
                        int nodeorder = rs.getInt("nodeorder");
                        int ishideinput = Util.getIntValue(rs.getString("ishideinput"), 0);
                        int ishidearea = Util.getIntValue(rs.getString("ishidearea"), 0);
                        String signfieldids = rs.getString("signfieldids");
                        String issubwfAllEnd = rs.getString("issubwfAllEnd");
                        String subwfscope = rs.getString("subwfscope");
                        String subwfdiffscope = rs.getString("subwfdiffscope");
                        String issubwfremind = rs.getString("issubwfremind");
                        String subwfremindtype = rs.getString("subwfremindtype");
                        String subwfremindoperator = rs.getString("subwfremindoperator");
                        String subwfremindobject = rs.getString("subwfremindobject");
                        String subwfremindperson = rs.getString("subwfremindperson");
                        String subwffreeforword = rs.getString("subwffreeforword");
                        /*
                         * int allowtransfer=rs.getInt("allowtransfer"); int
                         * allowcomment=rs.getInt("allowcomment"); int
                         * allowforward=rs.getInt("allowforward"); int
                         * submitCommentsOnToDo=rs.getInt("submitCommentsOnToDo");
                         * int
                         * allowForwardOnToDo=rs.getInt("allowForwardOnToDo");
                         * int
                         * allowForwardOnDone=rs.getInt("allowForwardOnDone");
                         * int
                         * allowForwardOnComplete=rs.getInt("allowForwardOnComplete");
                         * int allowforwarddone=rs.getInt("allowforwarddone");
                         * int
                         * allowforwardcomplete=rs.getInt("allowforwardcomplete");
                         * int
                         * submitCommentsOnDone=rs.getInt("submitCommentsOnDone");
                         */
                        // int
                        // submitCommentsOnComplete=rs.getInt("submitCommentsOnComplete");
                        // String tpostil=rs.getString("tpostil");
                        // String rpostil=rs.getString("rpostil");
                        String IsTakingOpinions = rs.getString("IsTakingOpinions");
                        String IsHandleForward = rs.getString("IsHandleForward");
                        String vmobilesource = rs.getString("vmobilesource");
                        // String
                        // ruleRelationship_temp=rs.getString("ruleRelationship_temp");
                        String ruleRelationship = rs.getString("ruleRelationship");
                        String pdfprint = rs.getString("pdfprint");
                        String useExceptionHandle = rs.getString("useExceptionHandle");
                        String exceptionHandleWay = rs.getString("exceptionHandleWay");
                        String flowToAssignNode = rs.getString("flowToAssignNode");
                        String notseeeachother = rs.getString("notseeeachother");
                        String subProcessSummary = rs.getString("subProcessSummary");
                        String isRejectRemind = rs.getString("isRejectRemind");
                        String isChangRejectNode = rs.getString("isChangRejectNode");
                        int isSelectRejectNode = Util.getIntValue(rs.getString("isSelectRejectNode"), 0);
                        String rejectableNodes = rs.getString("rejectableNodes");
                        String isSubmitDirectNode = rs.getString("isSubmitDirectNode");
                        int batchsubmit = Util.getIntValue(Util.null2String(rs.getString("batchsubmit")),0);
                        // sql="insert into
                        // workflow_nodebase(nodename,isstart,isreject,isreopen,isend,drawxpos,drawypos,totalgroups,nodeattribute,passnum,IsFreeNode)
                        // values(?,?,?,?,?,?,?,?,?,?,?)";
                        // statement.setStatementSql(sql);
                        // statement.setString(1,nodename);
                        // statement.setString(2,isstart);
                        // statement.setString(3,isreject);
                        // statement.setString(4,isreopen);
                        // statement.setString(5,isend);
                        // statement.setInt(6,drawxpos);
                        // statement.setInt(7,drawypos);
                        // statement.setInt(8,totalgroups);
                        // statement.setString(9,nodeattribute);
                        // statement.setInt(10,passnum);
                        // statement.setString(11,IsFreeNode);
                        // statement.executeUpdate();
                        // sql = "select max(id) as maxid from
                        // workflow_nodebase";
                        // statement.setStatementSql(sql);
                        // statement.executeQuery();

                        WFFreeFlowManager wfffmanager = new WFFreeFlowManager();
                        int nodeid = wfffmanager.getNodeNewId(nodename, drawxpos, drawypos, passnum, isstart, isreject, isreopen, isend, totalgroups, nodeattribute, IsFreeNode);
                        // 写节点日志
                        map.put(oldnodeid + "", nodeid + "");
                        syslog.resetParameter();
                        syslog.setRelatedId(returnValue);
                        syslog.setRelatedName(nodename);
                        syslog.setOperateType("1");
                        syslog.setOperateDesc("WrokFlowNode_insert");
                        syslog.setOperateItem("86");
                        syslog.setOperateUserid(userid);
                        syslog.setClientAddress(LocIP);
                        syslog.setIstemplate(Util.getIntValue(istemplate));
                        syslog.setSysLogInfo();
                        // 写节点日志End
                        nodeidlist.add("" + nodeid);
						nodeIdMap.put(oldnodeid,""+nodeid);

                        // sql="insert into
                        // workflow_flownode(workflowid,nodeid,nodetype,isFutureViewer,isHistoryViewer,viewnodeids,ismode,showdes,printdes)
                        // values(?,?,?,?,?,?,?,?,?)";
                        sql = "insert into workflow_flownode(workflowid,nodeid,nodetype,viewnodeids,ismode,showdes,printdes,viewtypeall,viewdescall,showtype,"
                                + "vtapprove,vtrealize,vtforward,vtpostil,vtrecipient,vtreject,vtsuperintend,vtover,vdcomments,vddeptname,vdoperator,vddate,vdtime,"
                                + "nodetitle,isFormSignature,IsPendingForward,IsWaitForwardOpinion,IsBeForward,IsSubmitedOpinion,IsSubmitForward,formSignatureWidth,"
                                + "formSignatureHeight,IsFreeWorkflow,freewfsetcurnamecn,freewfsetcurnameen,stnull,toexcel,issignmustinput,rejectbackflag,drawbackflag,"
                                + "vsignupload,vsigndoc,vsignworkflow,freewfsetcurnametw,IsBeForwardSubmit,IsBeForwardModify,IsBeForwardPending,IsShowPendingForward,"
                                + "IsShowWaitForwardOpinion,IsShowBeForward,IsShowSubmitedOpinion,IsShowSubmitForward,IsShowBeForwardSubmit,IsShowBeForwardModify,"
                                //+ "IsShowBeForwardPending,isfeedback,isnullnotfeedback,Freefs,vtTakingOpinions,vtHandleForward,vttpostil,vtrpostil,vtintervenor,IsBeForwardTodo,IsShowBeForwardTodo,IsBeForwardAlready,IsAlreadyForward,IsShowAlreadyForward,IsBeForwardSubmitAlready,IsShowBeForwardSubmitAlready,IsBeForwardSubmitNotaries,IsShowBeForwardSubmitNotaries,nodeorder,ishideinput,ishidearea,signfieldids,issubwfAllEnd,subwfscope,subwfdiffscope,issubwfremind,subwfremindtype,subwfremindoperator,subwfremindobject,subwfremindperson,subwffreeforword,IsTakingOpinions,IsHandleForward,vmobilesource,ruleRelationship,pdfprint) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        		+ "IsShowBeForwardPending,isfeedback,isnullnotfeedback,Freefs,vtTakingOpinions,vtHandleForward,vttpostil,vtrpostil,vtintervenor,IsBeForwardTodo,IsShowBeForwardTodo,IsBeForwardAlready,IsAlreadyForward,IsShowAlreadyForward,IsBeForwardSubmitAlready,IsShowBeForwardSubmitAlready,IsBeForwardSubmitNotaries,IsShowBeForwardSubmitNotaries,nodeorder,ishideinput,ishidearea,signfieldids,issubwfAllEnd,subwfscope,subwfdiffscope,issubwfremind,subwfremindtype,subwfremindoperator,subwfremindobject,subwfremindperson,subwffreeforword,IsTakingOpinions,IsHandleForward,vmobilesource,ruleRelationship,pdfprint,useExceptionHandle,exceptionHandleWay,flowToAssignNode,notseeeachother,printflowcomment,subProcessSummary,isRejectRemind,isChangRejectNode,isSelectRejectNode,rejectableNodes,isSubmitDirectNode,batchsubmit,isprintpage,printnum) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, nodeid);
                        statement.setString(3, nodetype);
                        statement.setString(4, "-1");// 节点日志查看都默认为全部查看
                        statement.setString(5, ismode);
                        statement.setString(6, showdes);
                        statement.setString(7, printdes);
                        statement.setString(8, viewtypeall);
                        statement.setString(9, viewdescall);
                        statement.setString(10, showtype);
                        statement.setString(11, vtapprove);
                        statement.setString(12, vtrealize);
                        statement.setString(13, vtforward);
                        statement.setString(14, vtpostil);
                        statement.setString(15, vtrecipient);
                        statement.setString(16, vtreject);
                        statement.setString(17, vtsuperintend);
                        statement.setString(18, vtover);
                        statement.setString(19, vdcomments);
                        statement.setString(20, vddeptname);
                        statement.setString(21, vdoperator);
                        statement.setString(22, vddate);
                        statement.setString(23, vdtime);
                        statement.setString(24, nodetitle);
                        statement.setString(25, isFormSignature);
                        statement.setString(26, IsPendingForward);
                        statement.setString(27, IsWaitForwardOpinion);
                        statement.setString(28, IsBeForward);
                        statement.setString(29, IsSubmitedOpinion);
                        statement.setString(30, IsSubmitForward);
                        statement.setString(31, formSignatureWidth);
                        statement.setString(32, formSignatureHeight);
                        statement.setString(33, IsFreeWorkflow);
                        statement.setString(34, freewfsetcurnamecn);
                        statement.setString(35, freewfsetcurnameen);
                        statement.setString(36, stnull);
                        statement.setString(37, toexcel);
                        statement.setString(38, issignmustinput);
                        statement.setString(39, rejectbackflag);
                        statement.setString(40, drawbackflag);
                        statement.setString(41, vsignupload);
                        statement.setString(42, vsigndoc);
                        statement.setString(43, vsignworkflow);
                        statement.setString(44, freewfsetcurnametw);
                        statement.setString(45, IsBeForwardSubmit);
                        statement.setString(46, IsBeForwardModify);
                        statement.setString(47, IsBeForwardPending);
                        statement.setString(48, IsShowPendingForward);
                        statement.setString(49, IsShowWaitForwardOpinion);
                        statement.setString(50, IsShowBeForward);
                        statement.setString(51, IsShowSubmitedOpinion);
                        statement.setString(52, IsShowSubmitForward);
                        statement.setString(53, IsShowBeForwardSubmit);
                        statement.setString(54, IsShowBeForwardModify);
                        statement.setString(55, IsShowBeForwardPending);
                        statement.setString(56, isfeedback);
                        statement.setString(57, isnullnotfeedback);
                        statement.setString(58, Freefs);
                        statement.setString(59, vtTakingOpinions);
                        statement.setString(60, vtHandleForward);
                        statement.setString(61, vttpostil);
                        statement.setString(62, vtrpostil);
                        statement.setString(63, vtintervenor);
                        statement.setString(64, IsBeForwardTodo);// 节点日志查看都默认为全部查看
                        statement.setString(65, IsShowBeForwardTodo);
                        statement.setString(66, IsBeForwardAlready);
                        // statement.setString(67,IsShowAlreadyForward);
                        statement.setString(67, IsAlreadyForward);
                        statement.setString(68, IsShowAlreadyForward);
                        statement.setString(69, IsBeForwardSubmitAlready);
                        statement.setString(70, IsShowBeForwardSubmitAlready);
                        statement.setString(71, IsBeForwardSubmitNotaries);
                        statement.setString(72, IsShowBeForwardSubmitNotaries);
                        // statement.setString(74,Freefs);
                        statement.setInt(73, nodeorder);
                        statement.setInt(74, ishideinput);
                        statement.setInt(75, ishidearea);
                        statement.setString(76, signfieldids);
                        statement.setString(77, issubwfAllEnd);
                        statement.setString(78, subwfscope);
                        statement.setString(79, subwfdiffscope);
                        statement.setString(80, issubwfremind);
                        statement.setString(81, subwfremindtype);
                        statement.setString(82, subwfremindoperator);
                        statement.setString(83, subwfremindobject);
                        statement.setString(84, subwfremindperson);
                        statement.setString(85, subwffreeforword);
                        /*
                         * statement.setInt(86,allowtransfer);
                         * statement.setInt(87,allowcomment);
                         * statement.setInt(88,allowforward);
                         * statement.setInt(89,submitCommentsOnToDo);
                         * statement.setInt(90,allowForwardOnToDo);
                         * statement.setInt(91,allowForwardOnDone);
                         * statement.setInt(92,allowForwardOnComplete);
                         * statement.setInt(93,allowforwarddone);
                         * statement.setInt(94,allowforwardcomplete);
                         * statement.setInt(95,submitCommentsOnDone);
                         */
                        // statement.setInt(96,submitCommentsOnComplete);
                        // statement.setString(97,tpostil);
                        // statement.setString(98,rpostil);
                        statement.setString(86, IsTakingOpinions);
                        statement.setString(87, IsHandleForward);
                        statement.setString(88, vmobilesource);
                        // statement.setString(102,ruleRelationship_temp);
                        statement.setString(89, ruleRelationship);
                        statement.setString(90, pdfprint);
                        statement.setString(91, useExceptionHandle);
                        statement.setString(92, exceptionHandleWay);
                        statement.setString(93, flowToAssignNode);
                        statement.setString(94, notseeeachother);
                        statement.setString(95, printflowcomment);
                        statement.setString(96, subProcessSummary);
                        statement.setString(97, isRejectRemind);
                        statement.setString(98, isChangRejectNode);
                        statement.setInt(99, isSelectRejectNode);
                        statement.setString(100, rejectableNodes);
                        statement.setString(101, isSubmitDirectNode);
                        statement.setInt(102, batchsubmit);
                        statement.setInt(103, isprintpage);
                        statement.setString(104, printnum);
                        statement.executeUpdate();

                        // 流程版本
                        if ("3".equals(isvalid)) {
                            WorkflowVersion workflowversion = new WorkflowVersion(wfid + "");
                            workflowversion.relationNode(nodeid, oldnodeid);
                        }


                    }	
                    // copy 工作流功能管理表
                    sql = "select * from workflow_function_manage where workflowid=" + templateid + " order by operatortype";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        /*
                         * String typeview=rs.getString("typeview"); String
                         * dataview=rs.getString("dataview"); String
                         * automatism=rs.getString("automatism"); String
                         * manual=rs.getString("manual"); String
                         * transmit=rs.getString("transmit");
                         */
                        String retract = rs.getString("retract");
                        String pigeonhole = rs.getString("pigeonhole");
                        int operatortype = rs.getInt("operatortype");
                        int newopreatortype = -1;
                        if (operatortype > 0) {
                            int nodeidx = oldnodeidlist.indexOf("" + operatortype);
                            if (nodeidx > -1) {
                                newopreatortype = Util.getIntValue((String) nodeidlist.get(nodeidx));
                            }

                        } else if (operatortype == -9) { // 自由流程设置
                            newopreatortype = operatortype;
                        }
                        sql = "insert into workflow_function_manage(workflowid,retract,pigeonhole,operatortype) values(?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setString(2, retract);
                        statement.setString(3, pigeonhole);
                        statement.setInt(4, newopreatortype);
                        statement.executeUpdate();
                    }
                    // end 工作流功能管理表
                    // 节点日志查看范围处理

                    sql = "select * from workflow_logviewnode ,workflow_nodebase  where nodeid=id and (workflow_nodebase.isfreenode is null or workflow_nodebase.isfreenode !='1') and workflowid=" + templateid + " order by id";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String oldnodeid2 = rs.getString("nodeid");
                        oldnodeidlist2.add(oldnodeid2);
                        String viewnodeids2 = rs.getString("viewnodeids");
                        oldnodeloglist2.add(viewnodeids2);

                    }
                    for (int i = 0; i < oldnodeloglist2.size(); i++) {
                        String nodelogs = "";
                        String nodelogids = Util.null2String((String) oldnodeloglist2.get(i));
                        String tmpnodeids = (String) nodeidlist.get(i);
                        if (!nodelogids.trim().equals("-1")) {
                            StringTokenizer templ = new StringTokenizer(nodelogids, ",");
                            while (templ.hasMoreElements()) {
                                int indx = oldnodeidlist2.indexOf(templ.nextToken());
                                if (indx > -1) {
                                    nodelogs += (String) nodeidlist.get(indx) + ",";
                                }
                            }
                            if (nodelogs.length() > 0) {
                                // sql="update workflow_flownode set
                                // viewnodeids='"+nodelogs+"' where
                                // workflowid="+returnValue+" and
                                // nodeid="+tmpnodeids;
                                // rs.executeSql(sql);
                                // sql="update workflow_logviewnode set
                                // viewnodeids='"+nodelogs+"' where
                                // workflowid="+returnValue+" and
                                // nodeid="+tmpnodeids;
                                // rs.executeSql(sql);

                                sql = "insert into workflow_logviewnode(workflowid,viewnodeids,nodeid) values(?,?,?)";
                                statement.setStatementSql(sql);
                                statement.setInt(1, returnValue);
                                statement.setString(2, nodelogs);
                                statement.setInt(3, Util.getIntValue(tmpnodeids, 0));
                                statement.executeUpdate();
                            }
                        }
                    }
                    // 节点信息end

                    // 分组信息start

                    sql = "select * from workflow_groupinfo where workflowid=" + templateid;
                    rs.execute(sql);
                    while (rs.next()) {
                        String groupName = rs.getString("groupname");
                        int direction = rs.getInt("direction");
                        double x = rs.getDouble("x");
                        double y = rs.getDouble("y");
                        double width = rs.getDouble("width");
                        double height = rs.getDouble("height");

                        String istGroupSql = "insert into workflow_groupinfo(workflowid, groupname, direction, x, y, width, height) values (?, ?, ?, ?, ?, ?, ?)";
                        statement.setStatementSql(istGroupSql);
                        // returnValue
                        statement.setInt(1, returnValue);
                        statement.setString(2, groupName);
                        statement.setInt(3, direction);
                        statement.setFloat(4, (float) x);
                        statement.setFloat(5, (float) y);
                        statement.setFloat(6, (float) width);
                        statement.setFloat(7, (float) height);
                        statement.executeUpdate();
                    }
                    // 分组信息End

                    // 出口信息
                    ArrayList oldnodelinks = new ArrayList();
                    ArrayList nodeLinks = new ArrayList();
                    //sql = "select * from workflow_nodelink where wfrequestid is null and EXISTS(select 1 from workflow_nodebase c where workflow_nodelink.nodeid=c.id and (c.IsFreeNode is null or c.IsFreeNode!='1')) and workflowid=" + templateid + " order by id";
                    sql = "select * from workflow_nodelink where wfrequestid is null and EXISTS(select 1 from workflow_nodebase c where workflow_nodelink.nodeid=c.id and (c.IsFreeNode is null or c.IsFreeNode!='1')) and EXISTS(select 1 from workflow_nodebase c where workflow_nodelink.destnodeid=c.id and (c.IsFreeNode is null or c.IsFreeNode!='1'))  and workflowid=" + templateid + " order by id";
                    ConnStatement _statement = null;
                    try {
                        _statement = new ConnStatement();
                        
                        _statement.setStatementSql(sql, false);
                        _statement.executeQuery();
    
                        while (_statement.next()) {
                            String linkid = _statement.getString("id");
                            oldnodelinks.add(linkid);
                            String oldnodeid = _statement.getString("nodeid");
                            String olddestnodeid = _statement.getString("destnodeid");
                            int newnodeid = 0;
                            int newdestnodeid = 0;
                            int index = oldnodeidlist.indexOf(oldnodeid);
                            if (index > -1) {
                                newnodeid = Util.getIntValue((String) nodeidlist.get(index));
                            }
                            index = oldnodeidlist.indexOf(olddestnodeid);
                            if (index > -1) {
                                newdestnodeid = Util.getIntValue((String) nodeidlist.get(index));
                            }
    
                            String condition = "";
                            String conditioncn = "";
                            if (!_statement.getDBType().equals("oracle")) {
                                condition = _statement.getString("condition");
                                conditioncn = _statement.getString("conditioncn");
                            } else {
                                oracle.sql.CLOB theclob = _statement.getClob("condition");
                                String readline = "";
                                StringBuffer clobStrBuff = new StringBuffer("");
                                if (_statement.getClob("condition") != null) {
                                    java.io.BufferedReader clobin = new java.io.BufferedReader(theclob.getCharacterStream());
                                    while ((readline = clobin.readLine()) != null)
                                        clobStrBuff = clobStrBuff.append(readline);
                                    clobin.close();
                                    condition = clobStrBuff.toString();
                                } else {
                                    condition = "";
                                }
    
                                oracle.sql.CLOB theclob2 = _statement.getClob("conditioncn");
                                String readline2 = "";
                                StringBuffer clobStrBuff2 = new StringBuffer("");
                                if (_statement.getClob("conditioncn") != null) {
                                    java.io.BufferedReader clobin2 = new java.io.BufferedReader(theclob2.getCharacterStream());
                                    while ((readline2 = clobin2.readLine()) != null)
                                        clobStrBuff2 = clobStrBuff2.append(readline2);
                                    clobin2.close();
                                    conditioncn = clobStrBuff2.toString();
                                } else {
    
                                    conditioncn = "";
    
                                }
                            }
    
                            String isreject = _statement.getString("isreject");
                            String linkname = _statement.getString("linkname");
                            int directionfrom = _statement.getInt("directionfrom");
                            int directionto = _statement.getInt("directionto");
                            int x1 = _statement.getInt("x1");
                            int y1 = _statement.getInt("y1");
                            int x2 = _statement.getInt("x2");
                            int y2 = _statement.getInt("y2");
                            int x3 = _statement.getInt("x3");
                            int y3 = _statement.getInt("y3");
                            int x4 = _statement.getInt("x4");
                            int y4 = _statement.getInt("y4");
                            int x5 = _statement.getInt("x5");
                            int y5 = _statement.getInt("y5");
                            float nodepasstime = _statement.getFloat("nodepasstime");
                            int nodepasshour = Util.getIntValue(_statement.getString("nodepasshour"), 0);
                            int nodepassminute = Util.getIntValue(_statement.getString("nodepassminute"), 0);
                            String isremind = _statement.getString("isremind");
                            int remindhour = Util.getIntValue(_statement.getString("remindhour"), 0);
                            int remindminute = Util.getIntValue(_statement.getString("remindminute"), 0);
                            String FlowRemind = _statement.getString("FlowRemind");
                            String MsgRemind = _statement.getString("MsgRemind");
                            String MailRemind = _statement.getString("MailRemind");
                            String isnodeoperator = _statement.getString("isnodeoperator");
                            String iscreater = _statement.getString("iscreater");
                            String ismanager = _statement.getString("ismanager");
                            String isother = _statement.getString("isother");
                            String remindobjectids = _statement.getString("remindobjectids");
                            String isautoflow = _statement.getString("isautoflow");
                            String flownextoperator = _statement.getString("flownextoperator");
                            String flowobjectids = _statement.getString("flowobjectids");
                            String isBulidCode = Util.null2String(_statement.getString("isBulidCode"));
                            String ismustpass = _statement.getString("ismustpass");
                            String processoropinion = _statement.getString("processoropinion");
                            String tipsinfo = _statement.getString("tipsinfo");// TD27753
    						String linkorder = _statement.getString("linkorder");// 
    
                            // ---------------------------------------
                            // 新版流程图增加字段 Start
                            // ---------------------------------------
                            String startDirection = _statement.getString("startDirection");
                            String endDirection = _statement.getString("endDirection");
    						if("".equals(startDirection)) startDirection = null;
    						if("".equals(endDirection)) endDirection = null;
                            String points = _statement.getString("points");
                            String newrule = _statement.getString("newrule");
                            String ruleRelationship = _statement.getString("ruleRelationship");
							
							//超时相关
						String strDatefield = Util.null2String(_statement.getString("datefield"));
						String strTimefield = Util.null2String(_statement.getString("timefield"));
						String strChatsRemind = Util.null2String(_statement.getString("ChatsRemind"));
						int strCustomWorkflowid = _statement.getInt("CustomWorkflowid");
						String strFlowobjectreject = Util.null2String(_statement.getString("flowobjectreject"));
						String strFlowobjectsubmit = Util.null2String(_statement.getString("flowobjectsubmit"));
						int strSelectnodepass = _statement.getInt("selectnodepass");
						String strInfoCentreRemind = Util.null2String(_statement.getString("InfoCentreRemind"));
						String strInfoCentreRemind_csh = Util.null2String(_statement.getString("InfoCentreRemind_csh"));
						int strCustomWorkflowid_csh = _statement.getInt("CustomWorkflowid_csh");
    
                            sql = "insert into workflow_nodelink(workflowid,nodeid,destnodeid,isreject,linkname,directionfrom," + "directionto,x1,y1,x2,y2,x3,y3,x4,y4,x5,y5,nodepasstime,nodepasshour,nodepassminute,"
                                    + "isremind,remindhour,remindminute,FlowRemind,MsgRemind,MailRemind,isnodeoperator,iscreater,ismanager," + "isother,remindobjectids,isautoflow,flownextoperator,flowobjectids,isBulidCode,ismustpass,processoropinion,"
                                     + "tipsinfo,condition,conditioncn, startDirection, endDirection, points,newrule,ruleRelationship,linkorder,datefield,timefield,ChatsRemind,CustomWorkflowid,flowobjectreject,flowobjectsubmit,selectnodepass,InfoCentreRemind,InfoCentreRemind_csh,CustomWorkflowid_csh) " + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

								if (statement.getDBType().equals("oracle")) {
                                sql = "insert into workflow_nodelink(workflowid,nodeid,destnodeid,isreject,linkname,directionfrom," + "directionto,x1,y1,x2,y2,x3,y3,x4,y4,x5,y5,nodepasstime,nodepasshour,nodepassminute,"
                                        + "isremind,remindhour,remindminute,FlowRemind,MsgRemind,MailRemind,isnodeoperator,iscreater,ismanager," + "isother,remindobjectids,isautoflow,flownextoperator,flowobjectids,isBulidCode,ismustpass,processoropinion,"
                                         + "tipsinfo,condition,conditioncn, startDirection, endDirection, points,newrule,ruleRelationship,linkorder,datefield,timefield,ChatsRemind,CustomWorkflowid,flowobjectreject,flowobjectsubmit,selectnodepass,InfoCentreRemind,InfoCentreRemind_csh,CustomWorkflowid_csh) " + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,empty_clob(),empty_clob(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

									}
    
                            statement.setStatementSql(sql);
                            statement.setInt(1, returnValue);
                            statement.setInt(2, newnodeid);
                            statement.setInt(3, newdestnodeid);
                            statement.setString(4, isreject);
                            statement.setString(5, linkname);
                            statement.setInt(6, directionfrom);
                            statement.setInt(7, directionto);
                            statement.setInt(8, x1);
                            statement.setInt(9, y1);
                            statement.setInt(10, x2);
                            statement.setInt(11, y2);
                            statement.setInt(12, x3);
                            statement.setInt(13, y3);
                            statement.setInt(14, x4);
                            statement.setInt(15, y4);
                            statement.setInt(16, x5);
                            statement.setInt(17, y5);
                            statement.setFloat(18, nodepasstime);
                            statement.setInt(19, nodepasshour);
                            statement.setInt(20, nodepassminute);
                            statement.setString(21, isremind);
                            statement.setInt(22, remindhour);
                            statement.setInt(23, remindminute);
                            statement.setString(24, FlowRemind);
                            statement.setString(25, MsgRemind);
                            statement.setString(26, MailRemind);
                            statement.setString(27, isnodeoperator);
                            statement.setString(28, iscreater);
                            statement.setString(29, ismanager);
                            statement.setString(30, isother);
                            statement.setString(31, remindobjectids);
                            statement.setString(32, isautoflow);
                            statement.setString(33, flownextoperator);
                            statement.setString(34, flowobjectids);
                            statement.setString(35, isBulidCode);
                            statement.setString(36, ismustpass);
                            statement.setString(37, processoropinion);
                            statement.setString(38, tipsinfo);// TD27753
                            if (!statement.getDBType().equals("oracle")) {
                                statement.setString(39, condition);
                                statement.setString(40, conditioncn);
                                statement.setString(41, startDirection);
                                statement.setString(42, endDirection);
                                statement.setString(43, points);
                                 statement.setString(44, newrule);
                            statement.setString(45, ruleRelationship);
							statement.setString(46, linkorder);
							statement.setString(47, strDatefield);
							statement.setString(48, strTimefield);
							statement.setString(49, strChatsRemind);
							statement.setInt(50, strCustomWorkflowid);
							statement.setString(51, strFlowobjectreject);
							statement.setString(52, strFlowobjectsubmit);
							statement.setInt(53, strSelectnodepass);
							statement.setString(54, strInfoCentreRemind);
							statement.setString(55, strInfoCentreRemind_csh);
							statement.setInt(56, strCustomWorkflowid_csh);
                                statement.executeUpdate();
                            } else {
                                statement.setString(39, startDirection);
                                statement.setString(40, endDirection);
                                statement.setString(41, points);
                                 statement.setString(42, newrule);
                            statement.setString(43, ruleRelationship);
							 statement.setString(44, linkorder);
							 statement.setString(45, strDatefield);
							statement.setString(46, strTimefield);
							statement.setString(47, strChatsRemind);
							statement.setInt(48, strCustomWorkflowid);
							statement.setString(49, strFlowobjectreject);
							statement.setString(50, strFlowobjectsubmit);
							statement.setInt(51, strSelectnodepass);
							statement.setString(52, strInfoCentreRemind);
							statement.setString(53, strInfoCentreRemind_csh);
							statement.setInt(54, strCustomWorkflowid_csh);
    
                                statement.executeUpdate();
                                sql = "select condition,conditioncn from workflow_nodelink where workflowid = " + returnValue + " and destnodeid=" + newdestnodeid + " and nodeid=" + newnodeid + " order by id desc for update";
                                statement.setStatementSql(sql, false);
                                statement.executeQuery();
                                if (statement.next()) {
                                    oracle.sql.CLOB theclob = statement.getClob(1);
                                    oracle.sql.CLOB theclob2 = statement.getClob(2);
    
                                    char[] contentchar = condition.toCharArray();
                                    if (theclob != null) {
                                        java.io.Writer contentwrite = theclob.getCharacterOutputStream();
                                        contentwrite.write(contentchar);
                                        contentwrite.flush();
                                        contentwrite.close();
                                    }
                                    char[] contentchar2 = conditioncn.toCharArray();
                                    if (theclob2 != null) {
                                        java.io.Writer contentwrite2 = theclob2.getCharacterOutputStream();
                                        contentwrite2.write(contentchar2);
                                        contentwrite2.flush();
                                        contentwrite2.close();
                                    }
                                }
                            }
                            int newlinkid = -1;
                            sql = "select max(id) as maxid from workflow_nodelink";
                            statement.setStatementSql(sql);
                            statement.executeQuery();
                            if (statement.next()) {
                                newlinkid = Util.getIntValue(statement.getString("maxid"), -1);
                                nodelinkmap.put(linkid, newlinkid);
                                nodeLinks.add(statement.getString("maxid"));
                                // 写出口日志
                                syslog.resetParameter();
                                syslog.setRelatedId(returnValue);
                                syslog.setRelatedName(linkname);
                                syslog.setOperateType("1");
                                syslog.setOperateDesc("WrokFlowNodePortal_insert");
                                syslog.setOperateItem("88");
                                syslog.setOperateUserid(userid);
                                syslog.setClientAddress(LocIP);
                                syslog.setIstemplate(Util.getIntValue(istemplate));
                                syslog.setSysLogInfo();
                                // 写出口日志End
                            }
    
                            //出口条件 start
                            
                            int oldrule_id = -1;
                            String condit = "";
                            int newrule_Id = -1;
                            int linkrulesrc = -1;
                            int linkformid = -1;
                            if(!"".equals(newrule)){
                                String strnewrules = RuleBusiness.copyRulesByRuleids(newrule, returnValue, Util.getIntValue(linkid), newlinkid, RuleInterface.RULESRC_CK, oldnodeidlist, nodeidlist);
                                if(!"".equals(strnewrules)){
                                    linkrs.executeSql("update workflow_nodelink set newrule='"+strnewrules+"' where id="+newlinkid);
                                }
                            }
                            //出口条件 end
                            
                            // 超时配置 start
                            linkrs.executeSql("delete from workflow_nodelinkOverTime where linkid=" + newlinkid);
                            linkrs.executeSql("select * from workflow_nodelinkOverTime where linkid=" + linkid);
                            while(linkrs.next()) {
                            	int overTimeId = Util.getIntValue(linkrs.getString("id"), 0);
                            	String remindname = Util.null2String(linkrs.getString("remindname"));
                            	String remindtype = Util.null2String(linkrs.getString("remindtype"));
                    			int remindhour_new = Util.getIntValue(linkrs.getString("remindhour"), 0);
                    			int remindminute_new = Util.getIntValue(linkrs.getString("remindminute"), 0);
                    			String repeatremind = Util.null2String(linkrs.getString("repeatremind"));
                    			int repeathour = Util.getIntValue(linkrs.getString("repeathour"), 0);
                    			int repeatminute = Util.getIntValue(linkrs.getString("repeatminute"), 0);
                    			String FlowRemind_new = Util.null2String(linkrs.getString("FlowRemind"));
                    			String MsgRemind_new = Util.null2String(linkrs.getString("MsgRemind"));
                    			String MailRemind_new = Util.null2String(linkrs.getString("MailRemind"));
                    			String ChatsRemind = Util.null2String(linkrs.getString("ChatsRemind"));
                    			String InfoCentreRemind = Util.null2String(linkrs.getString("InfoCentreRemind"));
                    			int CustomWorkflowid = Util.getIntValue(linkrs.getString("CustomWorkflowid"), 0);
                    			String isnodeoperator_new = Util.null2String(linkrs.getString("isnodeoperator"));
                    			String iscreater_new = Util.null2String(linkrs.getString("iscreater"));
                    			String ismanager_new = Util.null2String(linkrs.getString("ismanager"));
                    			String isother_new = Util.null2String(linkrs.getString("isother"));
                    			String remindobjectids_new = Util.null2String(linkrs.getString("remindobjectids"));
                    			
                    			String sql_new = "insert into workflow_nodelinkOverTime (linkid, workflowid, remindname, remindtype, remindhour, remindminute, repeatremind, repeathour, repeatminute, FlowRemind, MsgRemind, MailRemind, ChatsRemind, InfoCentreRemind, CustomWorkflowid, isnodeoperator, iscreater, ismanager, isother, remindobjectids) "
                    				+ " values(" + newlinkid + ", " + returnValue + ", '" + remindname.replace("'", "''") + "', " + remindtype + ", " + remindhour_new + ", " + remindminute_new + ", " + repeatremind + ", " + repeathour + ", " + repeatminute + ", '" + FlowRemind_new + "', '" + MsgRemind_new + "', '" + MailRemind_new + "', '" + ChatsRemind + "', '" + InfoCentreRemind + "', " + CustomWorkflowid
                    				+ ", '" + isnodeoperator_new + "', '" + iscreater_new + "', '" + ismanager_new + "', '" + isother_new + "', '" + remindobjectids_new.replace("'", "''") + "') ";
                    			linkrs1.executeSql(sql_new);
                    			
                    			int noewOverTimeId = 0;
                    			linkrs1.executeSql("select max(id) as maxid from workflow_nodelinkOverTime where linkid=" + newlinkid);
                    			if(linkrs1.next()) {
                    				noewOverTimeId = Util.getIntValue(linkrs1.getString("maxid"), 0);
                    			}
                    			linkrs2.executeSql("select * from workflow_nodelinkOTField where overTimeId=" + overTimeId);
                    			while(linkrs2.next()) {
                    				int toFieldId = Util.getIntValue(linkrs2.getString("toFieldId"), 0);
                    				String toFieldName = Util.null2String(linkrs2.getString("toFieldName"));
                    				int toFieldGroupId = Util.getIntValue(linkrs2.getString("toFieldGroupId"), 0);
                    				int fromFieldId = Util.getIntValue(linkrs2.getString("fromFieldId"), 0);
                    				
                    				String nodelinkOTField_sql = "insert into workflow_nodelinkOTField(overTimeId, toFieldId, toFieldName, toFieldGroupid, fromFieldId) values(" + noewOverTimeId + ", " + toFieldId + ", '" + toFieldName + "', " + toFieldGroupId + ", " + fromFieldId + ") ";
                    				rs.executeSql(nodelinkOTField_sql);
                    			}
                            }
                            // 超时配置 end
                            
                            // 出口条件
                            
                            //sql = "select * from rule_base where formid=" + formid + " and linkid=" + linkid + "order by id";
                            //ConnStatement statement2 = new ConnStatement();
                            //statement2.setStatementSql(sql, false);
                            //statement2.executeQuery();
                            /*while (statement2.next()) {
                                String condit = "";
    
                                if (!statement2.getDBType().equals("oracle")) {
                                    condit = statement2.getString("condit");
                                    // conditioncn=_statement.getString("conditioncn");
                                } else {
                                    oracle.sql.CLOB theclob3 = statement2.getClob("condit");
                                    String readline3 = "";
                                    StringBuffer clobStrBuff3 = new StringBuffer("");
                                    if (statement2.getClob("condit") != null) {
                                        java.io.BufferedReader clobin = new java.io.BufferedReader(theclob3.getCharacterStream());
                                        while ((readline3 = clobin.readLine()) != null)
                                            clobStrBuff3 = clobStrBuff3.append(readline3);
                                        clobin.close();
                                        condit = clobStrBuff3.toString();
                                        // System.out.println("---2307---condit----"+condit);
                                    } else {
                                        condit = "";
                                    }
    
                                }
    
                                oldrule_id = statement2.getInt("id");
                                //if (!statement.getDBType().equals("oracle")) {
                                    sql = "insert into rule_base(formid,linkid,rulesrc,isbill,rulename,ruledesc,condit) values(?,?,?,?,?,?,?)";
                                //} else {
                                    //sql = "insert into rule_base(formid,linkid,rulesrc,isbill,rulename,ruledesc,condit) values(?,?,?,?,?,?,empty_clob())";
                                //}
                                statement.setStatementSql(sql);
                                statement.setInt(1, formid);
                                statement.setInt(2, newlinkid);
                                statement.setInt(3, statement2.getInt("rulesrc"));
                                statement.setInt(4, statement2.getInt("isbill"));
                                statement.setString(5, statement2.getString("rulename"));
                                statement.setString(6, statement2.getString("ruledesc"));
                                //if (!statement.getDBType().equals("oracle")) {
                                statement.setString(7, statement2.getString("condit"));
                                statement.executeUpdate();*/
                                /*} else {
                                    statement.executeUpdate();
                                    sql = "select condit from rule_base where linkid = " + newlinkid + " and formid=" + formid + " order by id desc for update";
                                    // System.out.println("---2335---sql----"+sql);
                                    statement.setStatementSql(sql, false);
                                    statement.executeQuery();
                                    if (statement.next()) {
                                        oracle.sql.CLOB theclob3 = statement.getClob(1);
    
                                        char[] contentchar3 = condit.toCharArray();
                                        if (theclob3 != null) {
                                            java.io.Writer contentwrite3 = theclob3.getCharacterOutputStream();
                                            contentwrite3.write(contentchar3);
                                            contentwrite3.flush();
                                            contentwrite3.close();
                                            statement.close();
                                        }
    
                                    }
    
                                }*/
                                // System.out.println("-2119=====oldrule_id---"+oldrule_id);
    
                                /*int newrule_Id = -1;
    
                                RecordSet rs3 = new RecordSet();*/
                                /*
                                 * rs3 .executeSql("select max(id) as maxId from
                                 * rule_base"); if (rs3.next()) { newrule_Id =
                                 * Util.getIntValue(rs3 .getString("maxId"), -1);
                                 * System.out.println("-newrule_Id---"+newrule_Id); }
                                 */
                                /*sql = "select max(id) as maxid from rule_base";
                                statement.setStatementSql(sql);
                                statement.executeQuery();
                                if (statement.next()) {
                                    newrule_Id = statement.getInt("maxid");
                                }
    
                                // System.out.println("--2-=--newrule_Id---"+newrule_Id);
                                Map ruleMap = new HashMap();
                                //Map ruleexpbaseMap = new HashMap();
                                //Map ruleexpionsMap = new HashMap();
                                ruleMap.put(oldrule_id, newrule_Id);
                                Iterator it = ruleMap.entrySet().iterator();
                               /* while (it.hasNext()) {
                                 Map.Entry entry = (Map.Entry) it.next();
                                 String oldruleid = entry.getKey().toString();
                                 int newruleid = Util.getIntValue(entry.getValue().toString());
                                 System.out.println("key=" + oldruleid + " value=" + newruleid);
                                 int result = 0;
                                 RecordSet rsexpbase = new RecordSet();
                                 rsexpbase.executeSql("select max(id) as id from rule_expressionbase");
                                 if (rsexpbase.next()) {
                                     result = Util.getIntValue(rsexpbase.getString("id"), 0);
                                 }
                                 result = result + 1;
                                 sql = "select * from rule_expressionbase where ruleid = "+ oldruleid;
                                 rsexpbase.executeSql(sql);
                                 while(rsexpbase.next()){
                                     int id = rsexpbase.getInt("id");
                                     int ruleid = rsexpbase.getInt("ruleid");
                                     int datafield = rsexpbase.getInt("datafield");
                                     String datafieldtext = rsexpbase.getString("datafieldtext");
                                     int compareoption1 = rsexpbase.getInt("compareoption1");
                                     int compareoption2 = rsexpbase.getInt("compareoption2");
                                     int htmltype = rsexpbase.getInt("htmltype");
                                     int typehrm = rsexpbase.getInt("typehrm");
                                     String fieldtype = rsexpbase.getString("fieldtype");
                                     int valuetype = rsexpbase.getInt("valuetype");
                                     int paramtype = rsexpbase.getInt("paramtype");
                                     String elementvalue1 = rsexpbase.getString("elementvalue1");
                                     String elementlabel1 = rsexpbase.getString("elementlabel1");
                                     String elementvalue2 = rsexpbase.getString("elementvalue2");
                                     String dbtype = rsexpbase.getString("dbtype");
                                     ruleexpbaseMap.put(id, result);
                                     RecordSet rsexpbasein = new RecordSet();
                                     rsexpbasein.executeSql("insert into rule_expressionbase(id,ruleid,datafield,datafieldtext,compareoption1,compareoption2,htmltype,typehrm,fieldtype,valuetype,paramtype,elementvalue1,elementlabel1,elementvalue2,dbtype) values (" + result + "," + newruleid
                                             + ","+datafield+ ",'"+datafieldtext+ "',"+compareoption1+ ","+compareoption2+ ","+htmltype+ ","+typehrm+ ",'"+fieldtype+ "',"+valuetype+ ","+paramtype+ ",'"+elementvalue1+ "','"+elementlabel1+ "','"+elementvalue2+ "','"+dbtype+"')");
                                     
                                 }
                                 Iterator it2 = ruleexpbaseMap.entrySet().iterator();
                                 while (it2.hasNext()) {
                                     Map.Entry entry2 = (Map.Entry) it2.next();
                                     int oldexpbaesid = Util.getIntValue(entry2.getKey().toString());
                                     int newexpbaesid = Util.getIntValue(entry2.getValue().toString());
                                     RecordSet rsexps = new RecordSet();
                                     int result2 = 0;
                                     rsexps.executeSql("select max(id) as id from rule_expressions");
                                     if (rsexps.next()) {
                                         result2 = Util.getIntValue(rsexps.getString("id"), 0);
                                     }
                                     result2 = result2 + 1;
                                     List oldexpids = new ArrayList();
                                     sql = "select * from rule_expressions where ruleid = "+ oldruleid + " and expbaseid = " +oldexpbaesid;
                                     rsexps.executeSql(sql);
                                     while(rsexps.next()){
                                         int id = rsexps.getInt("id");
                                         int ruleid = rsexps.getInt("ruleid");
                                         int relation = rsexps.getInt("datafield");
                                         String expids = Util.null2String(rsexps.getString("expids"));
                                         int expbaseid = rsexps.getInt("expbaseid");
                                         oldexpids.add(expbaseid);
                                         ruleexpionsMap.put(id, result2);
                                         RecordSet rsexpsin = new RecordSet();
                                         rsexpsin.executeSql("insert into rule_expressions(id,ruleid,relation,expids,expbaseid) values (" + result2 + "," + newruleid + ","+relation+ ",'"+expids+"',"+expbaseid+")");
                                         
                                     }
                                     Iterator it3 = ruleexpionsMap.entrySet().iterator();
                                     while (it3.hasNext()) {
                                         Map.Entry entry3 = (Map.Entry) it3.next();
                                         int oldexpionsid = Util.getIntValue(entry3.getKey().toString());
                                         int newexpionsid = Util.getIntValue(entry3.getValue().toString());
                                         RecordSet rsexpions = new RecordSet();
                                         int result3 = 0;
                                         rsexpions.executeSql("select max(id) as id from rule_expressions");
                                         if (rsexpions.next()) {
                                             result3 = Util.getIntValue(rsexpions.getString("id"), 0);
                                         }
                                         result3 = result3 + 1;
                                         sql = "select * from rule_expressions where ruleid = "+ oldruleid + " and expids is not null";
                                         rsexpions.executeSql(sql);
                                         while(rsexpions.next()){
                                             int id = rsexpions.getInt("id");
                                             int ruleid = rsexpions.getInt("ruleid");
                                             int relation = rsexpions.getInt("datafield");
                                             String expids = rsexpions.getString("expids");
                                             int expbaseid = rsexpions.getInt("expbaseid");
                                             String newexpids = "";
                                             for(int o=0;o<oldexpids.size();o++){
                                                 String value = Util.null2String(oldexpids.get(o));
                                                 if(expids.indexOf(value)>-1){
                                                     if("".equals(newexpids)){
                                                         newexpids = value; 
                                                     }else{
                                                         newexpids = ","+value;  
                                                     }
                                                     
                                                 }
                                             }
                                            // String newexpids = "";
                                            // ruleexpionsMap.put(id, result2);
                                             RecordSet rsexpsin = new RecordSet();
                                             rsexpsin.executeSql("insert into rule_expressions(id,ruleid,relation,expids,expbaseid) values (" + result3 + "," + newruleid + ","+relation+ ",'"+newexpids+ "',"+expbaseid+")");
                                             
                                         }
                                 
    
                                    }
    
                                }
    
                            }
    
                            }*/
    
                            /*RecordSet rs33 = new RecordSet();
                            // System.out.println("--23333333-=--newrule_Id---"+newrule_Id);
                            sql = "insert into rule_mapitem(ruleid,linkid,rulesrc,rulevarid,formfieldid,rowidenty) select ruleid ," + newlinkid + ",rulesrc,rulevarid,formfieldid,rowidenty from rule_mapitem where linkid = " + linkid;
                            // System.out.println("-2221-sql--===="+sql);
                            rs33.execute(sql);
    									   	sql = "insert into rule_maplist(wfid,ruleid,linkid,isused,rulesrc,nm,rowidenty) select "
    											+ returnValue + ",ruleid ,"+ newlinkid +",isused,rulesrc,nm,rowidenty from rule_maplist where nm != 0 and rulesrc!='-1' and wfid = " + templateid + " and linkid = " + linkid ;
                            // System.out.println("-2149-sql--===="+sql);
                            rs33.execute(sql);*/
    
                        }
                        
                        /*
                         * // rule_base for (int i = 0; i < oldnodelinks.size();
                         * i++) { int ruleoldlinkid = Util .getIntValue((String)
                         * oldnodelinks.get(i));
                         * System.out.println("-ruleoldlinkid--"+ruleoldlinkid); int
                         * rulenewlinkid = Util .getIntValue((String)
                         * nodeLinks.get(i));
                         * System.out.println("-rulenewlinkid--"+rulenewlinkid); sql =
                         * "select * from rule_base where formid=" + formid + " and
                         * linkid=" + ruleoldlinkid + "order by id"; ConnStatement
                         * statement2=new ConnStatement();
                         * statement2.setStatementSql(sql, false);
                         * statement2.executeQuery(); while (statement2.next()) {
                         * String condit="";
                         * 
                         * if(!statement2.getDBType().equals("oracle")){
                         * condit=statement2.getString("condit");
                         * //conditioncn=_statement.getString("conditioncn"); }else{
                         * oracle.sql.CLOB theclob3 = statement2.getClob("condit");
                         * String readline3 = ""; StringBuffer clobStrBuff3 = new
                         * StringBuffer(""); if(statement2.getClob("condit")!=null){
                         * java.io.BufferedReader clobin = new
                         * java.io.BufferedReader(theclob3.getCharacterStream());
                         * while ((readline3 = clobin.readLine()) != null)
                         * clobStrBuff3 = clobStrBuff3.append(readline3);
                         * clobin.close() ; condit = clobStrBuff3.toString(); }else{
                         * condit=""; }
                         *  }
                         * 
                         * int oldrule_id = statement2.getInt("id");
                         * if(!statement.getDBType().equals("oracle")){ sql =
                         * "insert into
                         * rule_base(formid,linkid,rulesrc,isbill,rulename,ruledesc,condit)
                         * values(?,?,?,?,?,?,?)"; }else{ sql = "insert into
                         * rule_base(formid,linkid,rulesrc,isbill,rulename,ruledesc)
                         * values(?,?,?,?,?,?)"; } statement.setStatementSql(sql);
                         * statement.setInt(1, formid); statement.setInt(2,
                         * rulenewlinkid); statement.setInt(3,
                         * statement2.getInt("rulesrc")); statement.setInt(4,
                         * statement2.getInt("isbill")); statement.setString(5,
                         * statement2.getString("rulename")); statement.setString(6,
                         * statement2.getString("ruledesc"));
                         * if(!statement.getDBType().equals("oracle")){
                         * statement.setString(7, statement2.getString("condit"));
                         * statement.executeUpdate(); }else{
                         * statement.executeUpdate(); sql = "select condit from
                         * rule_base where linkid = " +rulenewlinkid +" and formid=" +
                         * formid + " order by id desc for update";
                         * statement.setStatementSql(sql, false);
                         * statement.executeQuery(); if(statement.next()){
                         * oracle.sql.CLOB theclob3 = statement.getClob(1);
                         * 
                         * char[] contentchar3 = condit.toCharArray();
                         * if(theclob3!=null){ java.io.Writer contentwrite3 =
                         * theclob3.getCharacterOutputStream();
                         * contentwrite3.write(contentchar3); contentwrite3.flush();
                         * contentwrite3.close(); }
                         *  }
                         *  } } int newrule_Id = -1; RecordSet rs3 = new
                         * RecordSet(); rs3 .executeSql("select max(id) as maxId
                         * from rule_base"); if (rs3.next()) { newrule_Id =
                         * Util.getIntValue(rs3 .getString("maxId"), -1);
                         * System.out.println("-newrule_Id---"+newrule_Id); }
                         * System.out.println("--2-=--newrule_Id---"+newrule_Id); if
                         * (newrule_Id > 0) {
                         * 
                         * //sql = "select * from rule_maplist where wfid = " +
                         * templateid + " and linkid = " + ruleoldlinkid sql =
                         * "insert into
                         * rule_maplist(wfid,ruleid,linkid,isused,rulesrc,nm,rowidenty)
                         * select " + returnValue + ","+ newrule_Id + ","+
                         * rulenewlinkid +",isused,rulesrc,nm,rowidenty from
                         * rule_maplist where nm = 0 and wfid = " + templateid + "
                         * and linkid = " + ruleoldlinkid ;
                         * System.out.println("-2135-sql--===="+sql);
                         * rs3.execute(sql);
                         *  }
                         * 
                         * 
                         * RecordSet rs33 = new RecordSet(); //
                         * System.out.println("--23333333-=--newrule_Id---"+newrule_Id);
                         * sql = "insert into
                         * rule_maplist(wfid,ruleid,linkid,isused,rulesrc,nm,rowidenty)
                         * select " + returnValue + ",ruleid ,"+ rulenewlinkid
                         * +",isused,rulesrc,nm,rowidenty from rule_maplist where nm !=
                         * 0 and wfid = " + templateid + " and linkid = " +
                         * ruleoldlinkid ;
                         * System.out.println("-2149-sql--===="+sql);
                         * rs33.execute(sql); }
                         */
    
                        
                        
                        
                        
                    } catch (Exception e9) {
                        e9.printStackTrace();
                    } finally {
                        if (_statement != null) {
                            _statement.close();
                        }
                    }
                    // 出口信息end
                    // 节点属性中自定义名称start
                    sql = "select * from workflow_CustFieldName where workflowId =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String oldnodeid = rs.getString("nodeid");
                        int newnodeid = 0;
                        int index = oldnodeidlist.indexOf(oldnodeid);
                        if (index > -1) {
                            newnodeid = Util.getIntValue((String) nodeidlist.get(index));
                        }
                        int Languageid = Util.getIntValue(rs.getString("Languageid"), 7);
                        String fieldname = rs.getString("fieldname");
                        String CustFieldName = rs.getString("CustFieldName");
                        sql = "insert into workflow_CustFieldName(workflowid,nodeid,Languageid,fieldname,CustFieldName) " + "values(?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, newnodeid);
                        statement.setInt(3, Languageid);
                        statement.setString(4, fieldname);
                        statement.setString(5, CustFieldName);
                        statement.executeUpdate();
                    }
                    // 节点属性中自定义名称end

                    // 显示联动中自定义名称start
                    sql = "select * from workflow_viewattrlinkage where workflowId =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String oldnodeid = rs.getString("nodeid");
                        int newnodeid = 0;
                        int index = oldnodeidlist.indexOf(oldnodeid);
                        if (index > -1) {
                            newnodeid = Util.getIntValue((String) nodeidlist.get(index));
                        }
                        String selectfieldid = rs.getString("selectfieldid");
                        String selectfieldvalue = rs.getString("selectfieldvalue");
                        String changefieldids = rs.getString("changefieldids");
                        String viewattr = rs.getString("viewattr");
                        sql = "insert into workflow_viewattrlinkage (workflowid,nodeid,selectfieldid,selectfieldvalue,changefieldids,viewattr) " + "values(?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, newnodeid);
                        statement.setString(3, selectfieldid);
                        statement.setString(4, selectfieldvalue);
                        statement.setString(5, changefieldids);
                        statement.setString(6, viewattr);
                        statement.executeUpdate();
                    }
                    // 显示联动中自定义名称end

                    // 字段联动中自定义名称start
                    sql = "select * from Workflow_DataInput_entry where workflowId =" + templateid + " order by id";
                    // System.out.println("-2234-sql---=================>>>>"+sql);
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String entryID = rs.getString("id");
                        // System.out.println("-2328-entryID---=================>>>>"+entryID);
                        String TriggerFieldName = rs.getString("TriggerFieldName");
                        String type = rs.getString("type");
                        String triggerName = rs.getString("triggerName");
                        String detailindex = rs.getString("detailindex");
                        sql = "insert into Workflow_DataInput_entry (workflowid,TriggerFieldName,type,triggerName,detailindex) " + "values(?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setString(2, TriggerFieldName);
                        statement.setString(3, type);
                        statement.setString(4, triggerName);
                        statement.setString(5, detailindex);
                        statement.executeUpdate();

                        int newentryID = -1;

                        // int newDataInputID = -1;
                        RecordSet rs7 = new RecordSet();
                        /*
                         * rs7.executeSql("select max(id) as maxId from
                         * Workflow_DataInput_entry"); if (rs7.next()) {
                         * newentryID = Util.getIntValue(rs7.getString("maxId"),
                         * -1); }
                         */

                        sql = "select max(id) as maxid from Workflow_DataInput_entry";
                        statement.setStatementSql(sql);
                        statement.executeQuery();
                        if (statement.next()) {
                            newentryID = statement.getInt("maxid");
                        }
                        // System.out.println("-2245-newentryID---=================>>>>"+newentryID);
                        if (newentryID > 0) {
                            sql = "select * from Workflow_DataInput_main where entryID = " + entryID + " order by id ";
                            // System.out.println("-2392-sql---=================>>>>"+sql);
                            rs7.executeSql(sql);
                            while (rs7.next()) {
                                String DataInputID = rs7.getString("id");
                                String WhereClause = rs7.getString("WhereClause");
                                int IsCycle = rs7.getInt("IsCycle");
                                int OrderID = rs7.getInt("OrderID");
                                String datasourcename = rs7.getString("datasourcename");
                                // System.out.println("-2239-DataInputID---=================>>>>"+DataInputID);

                                sql = "insert into Workflow_DataInput_main (entryID,WhereClause,IsCycle,OrderID,datasourcename) " + "values(?,?,?,?,?)";
                                statement.setStatementSql(sql);
                                statement.setInt(1, newentryID);
                                statement.setString(2, WhereClause);
                                statement.setInt(3, IsCycle);
                                statement.setInt(4, OrderID);
                                statement.setString(5, datasourcename);
                                statement.executeUpdate();

                                int newDataInputID = -1;
                                RecordSet rs6 = new RecordSet();
                                /*
                                 * rs6.executeSql("select max(id) as maxId from
                                 * Workflow_DataInput_main"); if (rs6.next()) {
                                 * newDataInputID =
                                 * Util.getIntValue(rs6.getString("maxId"), -1); }
                                 */
                                sql = "select max(id) as maxid from Workflow_DataInput_main";
                                statement.setStatementSql(sql);
                                statement.executeQuery();
                                if (statement.next()) {
                                    newDataInputID = statement.getInt("maxid");
                                }
                                if (newDataInputID > 0) {
                                    // System.out.println("-2461-newDataInputID---=================>>>>"+newDataInputID);
                                    sql = "select * from Workflow_DataInput_table where DataInputID = " + DataInputID + " order by id ";
                                    // System.out.println("-2463-sql---=================>>>>"+sql);
                                    rs6.executeSql(sql);
                                    if (rs6.next()) {
                                        // sql = "select * from rule_maplist
                                        // where
                                        // wfid = " + templateid + " and linkid
                                        // = "
                                        // + ruleoldlinkid
                                        RecordSet rs61 = new RecordSet();
                                        sql = "insert into Workflow_DataInput_table(DataInputID,TableName,Alias,FormId) select " + newDataInputID + ",TableName,Alias,FormId from Workflow_DataInput_table where DataInputID = " + DataInputID;
                                        // System.out.println("-2471-sql---=================>>>>"+sql);
                                        rs61.execute(sql);

                                        //获取最新Workflow_DataInput_table表id
                                        //获取最新Workflow_DataInput_table表id
                                        String wdoldtableid = "";
                                        String wdnewtableid = "";
                                        String wdolddatainputid="";
                                        String wdoldtablename="";
                                        String wdnewtablename="";
                                        //获取旧版本对应的tableid/datainputid/tablename
                                        RecordSet rs71 = new RecordSet();
                                        sql = "select id,datainputid,tablename from Workflow_DataInput_table where datainputid = "+DataInputID;
                                        rs71.execute(sql);
                                        while (rs71.next()) {
                                            // 循环分别插入对应的表记录，避免只取最后一个表的情况
                                            wdoldtablename = rs71.getString("tablename");
                                            wdoldtableid = rs71.getString("id");
                                            // 查询新插入的tableid
                                            sql = "select id,tablename from Workflow_DataInput_table where datainputid = " + newDataInputID;
                                            RecordSet rs81 = new RecordSet();
                                            rs81.execute(sql);
                                            while (rs81.next()) {
                                                wdnewtableid = rs81.getString("id");
                                                wdnewtablename = rs81.getString("tablename");
                                                if (wdoldtablename.trim().equals(wdnewtablename.trim())) {
                                                    sql = "insert into Workflow_DataInput_field(DataInputID,TableID,Type,DBFieldName,PageFieldName,pagefieldindex) select " + newDataInputID + "," + wdnewtableid
                                                            + ",Type,DBFieldName,PageFieldName,pagefieldindex from Workflow_DataInput_field where DataInputID = " + DataInputID + "and tableid = " + wdoldtableid;
                                                    rs61.execute(sql);
                                                }
                                            }

                                        }
                                    }

                                }
                            }
                        }

                    }
                    // 字段联动中自定义名称end

                    // 流程计划中自定义名称start
                    sql = "select * from workflow_createplan where wfid =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int oldplanid = Util.getIntValue(rs.getString("id"));
                        String oldnodeid = rs.getString("nodeid");
                        int newnodeid = 0;
                        int index = oldnodeidlist.indexOf(oldnodeid);
                        // System.out.println("----index---2483-==="+index);
                        if (index > -1) {
                            newnodeid = Util.getIntValue((String) nodeidlist.get(index));
                        }
                        int changetime = Util.getIntValue(rs.getString("changetime"));
                        int plantypeid = Util.getIntValue(rs.getString("plantypeid"));
                        int creatertype = Util.getIntValue(rs.getString("creatertype"));
                        int wffieldid = Util.getIntValue(rs.getString("wffieldid"));
                        int remindType = Util.getIntValue(rs.getString("remindType"));
                        int remindBeforeStart = Util.getIntValue(rs.getString("remindBeforeStart"));
                        int remindDateBeforeStart = Util.getIntValue(rs.getString("remindDateBeforeStart"));
                        int remindTimeBeforeStart = Util.getIntValue(rs.getString("remindTimeBeforeStart"));
                        int remindBeforeEnd = Util.getIntValue(rs.getString("remindBeforeEnd"));
                        int remindDateBeforeEnd = Util.getIntValue(rs.getString("remindDateBeforeEnd"));
                        int remindTimeBeforeEnd = Util.getIntValue(rs.getString("remindTimeBeforeEnd"));
                        int changemode = Util.getIntValue(rs.getString("changemode"));

                        sql = "insert into workflow_createplan (wfid,nodeid,changetime,plantypeid,creatertype,wffieldid,remindType,remindBeforeStart,remindDateBeforeStart,remindTimeBeforeStart,remindBeforeEnd,remindDateBeforeEnd,remindTimeBeforeEnd,changemode) "
                                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, newnodeid);
                        statement.setInt(3, changetime);
                        statement.setInt(4, plantypeid);
                        statement.setInt(5, creatertype);
                        statement.setInt(6, wffieldid);
                        statement.setInt(7, remindType);
                        statement.setInt(8, remindBeforeStart);
                        statement.setInt(9, remindDateBeforeStart);
                        statement.setInt(10, remindTimeBeforeStart);
                        statement.setInt(11, remindBeforeEnd);
                        statement.setInt(12, remindDateBeforeEnd);
                        statement.setInt(13, remindTimeBeforeEnd);
                        statement.setInt(14, changemode);
                        statement.executeUpdate();

                        int newplanid = -1;
                        RecordSet rs5 = new RecordSet();
                        /*
                         * rs5 .executeSql("select max(id) as maxId from
                         * workflow_createplan"); if (rs5.next()) { newplanid =
                         * Util.getIntValue(rs5 .getString("maxId"), -1); }
                         */
                        sql = "select max(id) as maxid from workflow_createplan";
                        statement.setStatementSql(sql);
                        statement.executeQuery();
                        if (statement.next()) {
                            newplanid = statement.getInt("maxid");
                        }

                        if (newplanid > 0) {

                            sql = "insert into workflow_createplandetail(createplanid,wffieldid,isdetail,planfieldname,groupid) select " + newplanid + ",wffieldid,isdetail,planfieldname,groupid from workflow_createplandetail where createplanid=" + oldplanid;
                            rs5.execute(sql);

                            sql = "insert into workflow_createplangroup(createplanid,groupid,isused) select " + newplanid + ",groupid,isused from workflow_createplangroup where createplanid=" + oldplanid;
                            rs5.execute(sql);

                            sql = "select * from workflow_createplandetail where wffieldid <> -1 and createplanid = " + oldplanid;
                            // System.out.println("-2540--workflow_createplandetail--sql---"+sql);
                            rs5.execute(sql);
                            while (rs5.next()) {
                                RecordSet rs6 = new RecordSet();
                                int cp_newnodeid = 0;
                                String cp_wffieldid = Util.null2String(rs5.getString("wffieldid"));
                                String wffieldids = cp_wffieldid.substring(1, cp_wffieldid.length());

                                int oldwffieldids = Util.getIntValue(wffieldids);
                                wffieldids = "" + (oldwffieldids * (1) - 10);
                                // System.out.println("-cp_wffieldid---==="+cp_wffieldid);
                                // System.out.println("-wffieldids---==="+wffieldids);
                                int index2 = oldnodeidlist.indexOf(wffieldids);
                                // System.out.println("----index2---2551-==="+index2);
                                if (index2 > -1) {
                                    cp_newnodeid = Util.getIntValue((String) nodeidlist.get(index2));
                                }
                                // System.out.println("-cp_newnodeid---===="+cp_newnodeid);
                                if (cp_newnodeid > 0) {
                                    int cp_newnodeid2 = Util.getIntValue((Util.null2String(cp_newnodeid)), -1);
                                    cp_newnodeid2 = cp_newnodeid2 * (-1) - 10;
                                    // System.out.println("-cp_newnodeid2---==="+cp_newnodeid2);
                                    sql = "update workflow_createplandetail set wffieldid = " + cp_newnodeid2 + " where wffieldid = " + cp_wffieldid + " and createplanid = " + newplanid;
                                    rs6.execute(sql);
                                }

                            }
                        }

                    }

                    // 计划end

                    // 浏览数据定义 start
                    sql = "select * from workflow_browdef where workflowId =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {

                        int fieldid = rs.getInt("fieldid");
                        int viewtype = rs.getInt("viewtype");
                        int fieldtype = rs.getInt("fieldtype");
                        String title = rs.getString("title");
                        sql = "insert into workflow_browdef (workflowid,fieldid,viewtype,fieldtype,title) " + "values(?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, fieldid);
                        statement.setInt(3, viewtype);
                        statement.setInt(4, fieldtype);
                        statement.setString(5, title);
                        statement.executeUpdate();
                    }
                    // 浏览数据定义 end

                    // 浏览数据定义明细 start
                    sql = "select * from workflow_browdef_field where workflowId =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int configid = rs.getInt("configid");
                        int fieldid = rs.getInt("fieldid");
                        int viewtype = rs.getInt("viewtype");
                        String showorder = rs.getString("showorder");
                        String hide = rs.getString("hide");
                        String readonly = rs.getString("readonly");
                        String canselectvalues = rs.getString("canselectvalues");
                        String valuetype = rs.getString("valuetype");
                        String value = rs.getString("value");
                        sql = "insert into workflow_browdef_field (workflowid,fieldid,viewtype,configid,showorder,hide,readonly,canselectvalues,valuetype,value) " + "values(?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, fieldid);
                        statement.setInt(3, viewtype);
                        statement.setInt(4, configid);
                        statement.setString(5, showorder);
                        statement.setString(6, hide);
                        statement.setString(7, readonly);
                        statement.setString(8, canselectvalues);
                        statement.setString(9, valuetype);
                        statement.setString(10, value);
                        statement.executeUpdate();
                    }
                    // 浏览数据定义明细 end

                    // 浏览数据定义流程 start
                    sql = "select * from workflow_rquestBrowseFunction where workflowId =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String fieldid = rs.getString("fieldid");
                        String fieldtype = rs.getString("fieldtype");
                        String searchname = rs.getString("searchname");
                        String Showorder = rs.getString("Showorder");
                        String showopen = rs.getString("showopen");
                        String searchvalue = rs.getString("searchvalue");
                        String requestbs = rs.getString("requestbs");
                        String requestname = rs.getString("requestname");
                        String workflowtype_rq = rs.getString("workflowtype");
                        String Processnumber = rs.getString("Processnumber");
                        String createtype = rs.getString("createtype");
                        String createtypeid = rs.getString("createtypeid");
                        String xgxmidopen = rs.getString("xgxmidopen");
                        String xgkhidopen = rs.getString("xgkhidopen");
                        String gdtypeopen = rs.getString("gdtypeopen");
                        String jsqjtypeopen = rs.getString("jsqjtypeopen");
                        String createdepttype = rs.getString("createdepttype");
                        String createsubidopen = rs.getString("createsubidopen");
                        String createdateopen = rs.getString("createdateopen");
                        // String
                        // createsubidcreatesubidshoworder=rs.getString("createsubidcreatesubidshoworder");
                        String createdatetypeshoworder = rs.getString("createdatetypeshoworder");
                        String xgxmidshoworder = rs.getString("xgxmidshoworder");
                        String xgkhidshoworder = rs.getString("xgkhidshoworder");
                        String gdtypeshoworder = rs.getString("gdtypeshoworder");
                        String jsqjtypeshoworder = rs.getString("jsqjtypeshoworder");
                        String department = rs.getString("department");
                        String createsubtype = rs.getString("createsubtype");
                        String createsubid = rs.getString("createsubid");
                        String createdatetype = rs.getString("createdatetype");
                        String createdatestart = rs.getString("createdatestart");
                        String xgxmid = rs.getString("xgxmid");
                        String xgkhid = rs.getString("xgkhid");
                        String gdtype = rs.getString("gdtype");
                        String jsqjtype = rs.getString("jsqjtype");
                        String requestnameopen = rs.getString("requestnameopen");
                        String workflowtypeopen = rs.getString("workflowtypeopen");
                        String Processnumberopen = rs.getString("Processnumberopen");
                        String createtypeidopen = rs.getString("createtypeidopen");
                        String createdeptidopen = rs.getString("createdeptidopen");
                        String createdateend = rs.getString("createdateend");
                        String requestnameshoworder = rs.getString("requestnameshoworder");
                        String workflowtypeshoworder = rs.getString("workflowtypeshoworder");
                        String Processnumbershoworder = rs.getString("Processnumbershoworder");
                        String createtypeidshoworder = rs.getString("createtypeidshoworder");
                        String departmentshoworder = rs.getString("departmentshoworder");
                        String createsubidshoworder = rs.getString("createsubidshoworder");
                        String cjrfbshoworder = rs.getString("cjrfbshoworder");
                        String jsqjtype_readonly = rs.getString("jsqjtype_readonly");
                        String gdtype_readonly = rs.getString("gdtype_readonly");
                        String xgkhid_readonly = rs.getString("xgkhid_readonly");
                        String xgxmid_readonly = rs.getString("xgxmid_readonly");
                        String createdate_readonly = rs.getString("createdate_readonly");
                        String createsubid_readonly = rs.getString("createsubid_readonly");
                        String createdeptid_readonly = rs.getString("createdeptid_readonly");
                        String createtypeid_readonly = rs.getString("createtypeid_readonly");
                        String Processnumber_readonly = rs.getString("Processnumber_readonly");
                        String workflowtype_readonly = rs.getString("workflowtype_readonly");
                        String requestname_readonly = rs.getString("requestname_readonly");
                        String xgxmtype = rs.getString("xgxmtype");
                        String xgkhtype = rs.getString("xgkhtype");
                        String createdatefieldid = rs.getString("createdatefieldid");

                        sql = "insert into workflow_rquestBrowseFunction (workflowid,fieldid,fieldtype,searchname,Showorder,showopen,searchvalue,requestbs,requestname,workflowtype,Processnumber,createtype,createtypeid,xgxmidopen,xgkhidopen,gdtypeopen,jsqjtypeopen,createdepttype,createsubidopen,createdateopen,createdatetypeshoworder,xgxmidshoworder,xgkhidshoworder,gdtypeshoworder,jsqjtypeshoworder,department,createsubtype,createsubid,createdatetype,createdatestart,xgxmid,xgkhid,gdtype,jsqjtype,requestnameopen,workflowtypeopen,Processnumberopen,createtypeidopen,createdeptidopen,createdateend,requestnameshoworder,workflowtypeshoworder,Processnumbershoworder,createtypeidshoworder,departmentshoworder,createsubidshoworder,cjrfbshoworder,jsqjtype_readonly,gdtype_readonly,xgkhid_readonly,xgxmid_readonly,createdate_readonly,createsubid_readonly,createdeptid_readonly,createtypeid_readonly,Processnumber_readonly,workflowtype_readonly,requestname_readonly,xgxmtype,xgkhtype,createdatefieldid) "
                                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setString(1, Util.null2String(returnValue));
                        statement.setString(2, fieldid);
                        statement.setString(3, fieldtype);
                        statement.setString(4, searchname);
                        statement.setString(5, Showorder);
                        statement.setString(6, showopen);
                        statement.setString(7, searchvalue);
                        statement.setString(8, requestbs);
                        statement.setString(9, requestname);
                        statement.setString(10, workflowtype_rq);
                        statement.setString(11, Processnumber);
                        statement.setString(12, createtype);
                        statement.setString(13, createtypeid);
                        statement.setString(14, xgxmidopen);
                        statement.setString(15, xgkhidopen);
                        statement.setString(16, gdtypeopen);
                        statement.setString(17, jsqjtypeopen);
                        statement.setString(18, createdepttype);
                        statement.setString(19, createsubidopen);
                        statement.setString(20, createdateopen);
                        // statement.setString(21,createsubidcreatesubidshoworder);
                        statement.setString(21, createdatetypeshoworder);
                        statement.setString(22, xgxmidshoworder);
                        statement.setString(23, xgkhidshoworder);
                        statement.setString(24, gdtypeshoworder);
                        statement.setString(25, jsqjtypeshoworder);
                        statement.setString(26, department);
                        statement.setString(27, createsubtype);
                        statement.setString(28, createsubid);
                        statement.setString(29, createdatetype);
                        statement.setString(30, createdatestart);
                        statement.setString(31, xgxmid);
                        statement.setString(32, xgkhid);
                        statement.setString(33, gdtype);
                        statement.setString(34, jsqjtype);
                        statement.setString(35, requestnameopen);
                        statement.setString(36, workflowtypeopen);
                        statement.setString(37, Processnumberopen);
                        statement.setString(38, createtypeidopen);
                        statement.setString(39, createdeptidopen);
                        statement.setString(40, createdateend);
                        statement.setString(41, requestnameshoworder);
                        statement.setString(42, workflowtypeshoworder);
                        statement.setString(43, Processnumbershoworder);
                        statement.setString(44, createtypeidshoworder);
                        statement.setString(45, departmentshoworder);
                        statement.setString(46, createsubidshoworder);
                        statement.setString(47, cjrfbshoworder);
                        statement.setString(48, jsqjtype_readonly);
                        statement.setString(49, gdtype_readonly);
                        statement.setString(50, xgkhid_readonly);
                        statement.setString(51, xgxmid_readonly);
                        statement.setString(52, createdate_readonly);
                        statement.setString(53, createsubid_readonly);
                        statement.setString(54, createdeptid_readonly);
                        statement.setString(55, createtypeid_readonly);
                        statement.setString(56, Processnumber_readonly);
                        statement.setString(57, workflowtype_readonly);
                        statement.setString(58, requestname_readonly);
                        statement.setString(59, xgxmtype);
                        statement.setString(60, xgkhtype);
                        statement.setString(61, createdatefieldid);

                        statement.executeUpdate();
                    }
                    // 浏览数据定义流程 end

                    // 自定义报表 start	----不做处理
              /*      sql = "  select * from Workflow_Report where reportwfid = '" + templateid + "'";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int rp_id = rs.getInt("id");
                        String reportname = rs.getString("reportname");
                        int reporttype = rs.getInt("reporttype");
                        int formId = rs.getInt("formId");
                        String isbillreport = rs.getString("isbill");
                        String isShowOnReportOutput = rs.getString("isShowOnReportOutput");
                        int subCompanyId = rs.getInt("subCompanyId");
                        sql = "insert into Workflow_Report (reportwfid,reportname,reporttype,formId,isbill,isShowOnReportOutput,subCompanyId) " + "values(?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setString(1, Util.null2String(returnValue));
                        statement.setString(2, reportname);
                        statement.setInt(3, reporttype);
                        statement.setInt(4, formId);
                        statement.setString(5, isbillreport);
                        statement.setString(6, isShowOnReportOutput);
                        statement.setInt(7, subCompanyId);
                        statement.executeUpdate();

                        int reportId = -1;
                        RecordSet rs0 = new RecordSet();
                       
                        sql = "select max(id) as maxid from Workflow_Report";
                        statement.setStatementSql(sql);
                        statement.executeQuery();
                        if (statement.next()) {
                            reportId = statement.getInt("maxid");
                        }

                        if (reportId > 0) {
                            // System.out.println("-2814-reportId--"+reportId);
                            sql = "insert into Workflow_ReportDspField(reportId,fieldid,dsporder,isstat,dborder,dbordertype,compositororder,fieldwidth,reportcondition,httype,htdetailtype,valuefour,valueone,valuethree,valuetwo) select " + reportId
                                    + ",fieldid,dsporder,isstat,dborder,dbordertype,compositororder,fieldwidth,reportcondition,httype,htdetailtype,valuefour,valueone,valuethree,valuetwo from Workflow_ReportDspField where reportid=" + rp_id;
                            // System.out.println("-2814----reportId---sql--"+sql);
                            rs0.execute(sql);

                            sql = "insert into WorkflowReportShare(reportId,sharetype,rolelevel,sharelevel,foralluser,crmid,mutidepartmentid,allowlook,seclevel2,userid,departmentid,subcompanyid,roleid,seclevel) select " + reportId
                                    + ",sharetype,rolelevel,sharelevel,foralluser,crmid,mutidepartmentid,allowlook,seclevel2,userid,departmentid,subcompanyid,roleid,seclevel from WorkflowReportShare where reportid=" + rp_id;
                            rs0.execute(sql);
                        }
                    }  */
                    // 自定义报表 end
					

                    // 附加操作
                    sql = "select * from workflow_addinoperate where workflowid=" + templateid + " order by id";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int isnode = rs.getInt("isnode");
                        int objid = rs.getInt("objid");
                        int newobjid = 0;
                        int index = -1;
                        if (isnode == 0) {
                            index = oldnodelinks.indexOf("" + objid);
                            if (index > -1) {
                                newobjid = Util.getIntValue((String) nodeLinks.get(index));
                            }
                        } else {
                            index = oldnodeidlist.indexOf("" + objid);
                            if (index > -1) {
                                newobjid = Util.getIntValue((String) nodeidlist.get(index));
                            }
                        }
                        String fid = Util.null2String(rs.getString("fieldid"));
                        int fieldid = rs.getInt("fieldid");
                        int fieldop1id = rs.getInt("fieldop1id");
                        int fieldop2id = rs.getInt("fieldop2id");
                        int operation = rs.getInt("operation");
                        String isnewsap = Util.getIntValue(rs.getString("isnewsap"), 0) + "";
                        String customervalue = rs.getString("customervalue");
                        int rules = rs.getInt("rules");
                        int type = rs.getInt("type");
                        String ispreadd = rs.getString("ispreadd");
                        sql = "insert into workflow_addinoperate(workflowid,isnode,objid,fieldid,fieldop1id,fieldop2id,operation,customervalue,rules,type,ispreadd,isnewsap) values(?,?,?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, isnode);
                        statement.setInt(3, newobjid);
                        if (fid.equals("")) {// 出口的action不出现在附加条件中 by alan
                                                // for td:10043
                            statement.setString(4, null);
                        } else {
                            statement.setInt(4, fieldid);
                        }
                        statement.setInt(5, fieldop1id);
                        statement.setInt(6, fieldop2id);
                        statement.setInt(7, operation);
                        statement.setString(8, customervalue);
                        statement.setInt(9, rules);
                        statement.setInt(10, type);
                        statement.setString(11, ispreadd);
                        statement.setString(12, isnewsap);
                        statement.executeUpdate();
                    }
                    // 附加操作end

                    // 节点接口 start
                    sql = "select * from workflowactionset where workflowid=" + templateid + " order by id";
                    // boolean isoracle =
                    // (statement.getDBType()).equals("oracle");
                    rs.executeSql(sql);
                    // TD14872
                    // ArrayList oldnodeidListTmp = new ArrayList();
                    // ArrayList newnodeidListTmp = new ArrayList();
                    while (rs.next()) {
                        String actionname = rs.getString("actionname");
                        // int workflowid=rs.getInt("nodeid");
                        int oldactionnodeid = rs.getInt("nodeid");
                        int newnacodeid = 0;
                        int index = oldnodeidlist.indexOf("" + oldactionnodeid);
                        if (index > -1) {
                            newnacodeid = Util.getIntValue((String) nodeidlist.get(index));
                        }
                        int linkindex = -1;
                        int oldacnodelinkid = rs.getInt("nodelinkid");
                        int newnodelinkid = 0;
                        linkindex = oldnodelinks.indexOf("" + oldacnodelinkid);
                        if (linkindex > -1) {
                            newnodelinkid = Util.getIntValue((String) nodeLinks.get(linkindex));
                        }
                        // System.out.println("-2243---newnacodeid---==="+newnacodeid);
                        // System.out.println("-2243---newnodelinkid---==="+newnodelinkid);
                        int ispreoperator = rs.getInt("ispreoperator");
                        int actionorder = rs.getInt("actionorder");
                        String interfaceid = rs.getString("interfaceid");
                        int interfacetype = rs.getInt("interfacetype");
                        String typename = rs.getString("typename");
                        int isused = rs.getInt("isused");

                        sql = "insert into workflowactionset(workflowid,actionname,nodeid,nodelinkid,ispreoperator,actionorder,interfaceid,interfacetype,typename,isused) values(?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setString(2, actionname);
                        statement.setInt(3, newnacodeid);
                        statement.setInt(4, newnodelinkid);
                        statement.setInt(5, ispreoperator);
                        statement.setInt(6, actionorder);
                        statement.setString(7, interfaceid);
                        statement.setInt(8, interfacetype);
                        statement.setString(9, typename);
                        statement.setInt(10, isused);
                        statement.executeUpdate();

                        sql = "select * from actionsetting where actionname = " + interfaceid;
                    }

                    // 节点字段（模板模式）
                    sql = "select * from workflow_nodemode where workflowid=" + templateid + " order by id";
                    boolean isoracle = (statement.getDBType()).equals("oracle");
                    statement.setStatementSql(sql);
                    statement.executeQuery();
                    // TD14872
                    ArrayList oldnodeidListTmp = new ArrayList();
                    ArrayList newnodeidListTmp = new ArrayList();
                    
                    
                    while (statement.next()) {
                        int modeformid = statement.getInt("formid");
                        int oldnodeid = statement.getInt("nodeid");
                        int newnodeid = 0;
                        int index = oldnodeidlist.indexOf("" + oldnodeid);
                        if (index > -1) {
                            newnodeid = Util.getIntValue((String) nodeidlist.get(index));
                        }
                        String isprint = statement.getString("isprint");
                        String modename = statement.getString("modename");
                        String modedesc = "";
                        ConnStatement statement1 = null;
                        try {
                            statement1 = new ConnStatement();
                            
                            if (isoracle) {
                                CLOB theclob = statement.getClob("MODEDESC");
                                String readline = "";
                                StringBuffer clobStrBuff = new StringBuffer("");
                                BufferedReader clobin = new BufferedReader(theclob.getCharacterStream());
                                while ((readline = clobin.readLine()) != null)
                                    clobStrBuff = clobStrBuff.append(readline);
                                clobin.close();
                                modedesc = clobStrBuff.toString();
                                sql = "insert into workflow_nodemode(formid,nodeid,isprint,modedesc,workflowid,modename) values(?,?,?,empty_clob(),?,?)";
                                statement1.setStatementSql(sql);
                                statement1.setInt(1, modeformid);
                                statement1.setInt(2, newnodeid);
                                statement1.setString(3, isprint);
                                statement1.setInt(4, returnValue);
                                statement1.setString(5, modename);
                                statement1.executeUpdate();
                                sql = "select modedesc from workflow_nodemode where formid = " + modeformid + " and nodeid=" + newnodeid + " and isprint='" + isprint + "' and workflowid=" + returnValue + " order by id desc";
                                statement1.setStatementSql(sql, false);
                                statement1.executeQuery();
                                if (statement1.next()) {
                                    CLOB theclob1 = statement1.getClob(1);
                                    char[] contentchar = modedesc.toCharArray();
                                    Writer contentwrite = theclob1.getCharacterOutputStream();
                                    contentwrite.write(contentchar);
                                    contentwrite.flush();
                                    contentwrite.close();
                                    statement1.close();
                                }
                            } else {
                                modedesc = statement.getString("modedesc");
                                sql = "insert into workflow_nodemode(formid,nodeid,isprint,modedesc,workflowid,modename) values(?,?,?,?,?,?)";
                                statement1.setStatementSql(sql);
                                statement1.setInt(1, modeformid);
                                statement1.setInt(2, newnodeid);
                                statement1.setString(3, isprint);
                                statement1.setString(4, modedesc);
                                statement1.setInt(5, returnValue);
                                statement1.setString(6, modename);
                                statement1.executeUpdate();
                            }
                        } catch (Exception e9) {
                            e9.printStackTrace();
                            this.writeLog(e9);
                        } finally {
                            if (statement1 != null) {
                                statement1.close();
                            }
                        }
                        // sql = "delete from workflow_modeview where formid=" +
                        // formid + " and nodeid=" + oldnodeid+" and
                        // isbill="+Util.getIntValue(isbill,0);
                        // rs.executeSql(sql);
                        /**
                         * TD14872 此处处理移至外部 sql = "select * from
                         * workflow_modeview where formid=" + formid + " and
                         * nodeid=" + oldnodeid+" and
                         * isbill="+Util.getIntValue(isbill,0);
                         * rs.executeSql(sql); while (rs.next()) { sql = "insert
                         * into
                         * workflow_modeview(formid,nodeid,isbill,fieldid,isview,isedit,ismandatory)
                         * values(?,?,?,?,?,?,?)";
                         * statement1.setStatementSql(sql); statement1.setInt(1,
                         * formid); statement1.setInt(2, newnodeid);
                         * statement1.setInt(3, Util.getIntValue(isbill,0));
                         * statement1.setInt(4, rs.getInt("fieldid"));
                         * statement1.setString(5, rs.getString("isview"));
                         * statement1.setString(6, rs.getString("isedit"));
                         * statement1.setString(7, rs.getString("ismandatory"));
                         * statement1.executeUpdate(); }
                         */
                        // 对于打印模板不添加至workflow_modeview
                        if ("0".equals(isprint)) {
                            oldnodeidListTmp.add("" + oldnodeid);
                            newnodeidListTmp.add("" + newnodeid);
                        }
                    }
                    for (int i = 0; i < oldnodeidListTmp.size(); i++) {
                        int oldnodeid = Util.getIntValue((String) oldnodeidListTmp.get(i));
                        int newnodeid = Util.getIntValue((String) newnodeidListTmp.get(i));
                        sql = "select formid,nodeid,isbill,fieldid,isview,isedit,ismandatory from workflow_modeview where formid=" + formid + " and nodeid=" + oldnodeid + " and isbill=" + Util.getIntValue(isbill, 0) + " and nodeid <> " + newnodeid
                                + " group by formid,nodeid,isbill,fieldid,isview,isedit,ismandatory";
                        rs.executeSql(sql);
                        while (rs.next()) {
                            sql = "insert into workflow_modeview(formid,nodeid,isbill,fieldid,isview,isedit,ismandatory) values(?,?,?,?,?,?,?)";
                            statement.setStatementSql(sql);
                            statement.setInt(1, formid);
                            statement.setInt(2, newnodeid);
                            statement.setInt(3, Util.getIntValue(isbill, 0));
                            statement.setInt(4, rs.getInt("fieldid"));
                            statement.setString(5, rs.getString("isview"));
                            statement.setString(6, rs.getString("isedit"));
                            statement.setString(7, rs.getString("ismandatory"));
                            statement.executeUpdate();
                        }
                    }
                    // 节点字段（模板模式）end
                    // 节点字段 Html模式 Start
                    WFNodeFieldManager wFNodeFieldManager = new WFNodeFieldManager();
                    
                    for (int i = 0; i < oldnodeidlist.size(); i++) {
                        String oldnodeid = (String) oldnodeidlist.get(i);
                        int nodeid = Util.getIntValue((String) nodeidlist.get(i), 0);
                        sql = "insert into workflow_nodefieldattr (fieldid, formid, isbill, nodeid, attrcontent, caltype, othertype, transtype,datasourceid) select fieldid, formid, isbill, " + nodeid + ", attrcontent, caltype, othertype, transtype,datasourceid from workflow_nodefieldattr where nodeid=" + oldnodeid;
                        rs.execute(sql);
                        this.copyHtmlLayout(Integer.parseInt(oldnodeid), nodeid, returnValue, oldnodeidlist, nodeidlist, wFNodeFieldManager);
                    }
                    // 节点字段 Html模式 End
                    // 节点操作组and节点字段（一般模式）
                    for (int i = 0; i < oldnodeidlist.size(); i++) {
                        String oldnodeid = (String) oldnodeidlist.get(i);
                        String newnodeid = (String) nodeidlist.get(i);
                        //流程节点子流程数据汇总设置
                        String dissumsql = " insert into Workflow_DistributionSummary(mainwfid,mainformid,mainfieldid,mainfieldname,maindetailnum, "+
                        			" nodeid,subwfid,subformid,subfieldid,subfieldname,fieldhtmltype,type,subtype,iscreatedoc) "+
                        			" ( select "+Util.null2String(returnValue)+",mainformid,mainfieldid,mainfieldname,maindetailnum, "+
                        			" "+newnodeid+",subwfid,subformid,subfieldid,subfieldname,fieldhtmltype,type,subtype,iscreatedoc from Workflow_DistributionSummary "+
                        			" where mainwfid= "+templateid+" and nodeid="+oldnodeid+" ) ";
                        rs.executeSql(dissumsql);
                        
                        // 节点操作组
                        sql = "select * from workflow_nodegroup where nodeid=" + oldnodeid + " order by id";
                        rs.executeSql(sql);
                        while (rs.next()) {
                            String groupid = rs.getString("id");
                            String groupname = rs.getString("groupname");
                            int canview = rs.getInt("canview");
                            sql = "select max(id) as maxid from workflow_nodegroup";
                            statement.setStatementSql(sql);
                            statement.executeQuery();
                            int newgourpid = 1;
                            if (statement.next()) {
                                newgourpid = statement.getInt("maxid");
                            }
                            newgourpid++;

                            RecordSet rs1 = new RecordSet();
                            
                            // sql="insert into
                            // workflow_nodegroup(id,nodeid,groupname,canview)
                            // values(?,?,?,?)";
                            sql = "insert into workflow_nodegroup(id,nodeid,groupname,canview) values(" + newgourpid + "," + newnodeid + ",'" + groupname + "'," + canview + ")";
                            rs1.executeSql(sql);
                            
                            
                            // statement.setStatementSql(sql);
                            // statement.setInt(1,newgourpid);
                            // statement.setInt(2,Util.getIntValue(newnodeid));
                            // statement.setString(3,groupname);
                            // statement.setInt(4,canview);
                            // statement.executeUpdate();
                            /*sql = "insert into workflow_groupdetail (groupid,type,objid,level_n,level2_n,signorder,conditions,conditioncn,orders,IsCoadjutant,signtype,issyscoadjutant,issubmitdesc,ispending,isforward,ismodify,coadjutants,coadjutantcn,deptField,subcompanyField,virtualid,ruleRelationship) select "
                                    + newgourpid
                                    + ",type,objid,level_n,level2_n,signorder,conditions,conditioncn,orders,IsCoadjutant,signtype,issyscoadjutant,issubmitdesc,ispending,isforward,ismodify,coadjutants,coadjutantcn,deptField,subcompanyField,virtualid,ruleRelationship from workflow_groupdetail where groupid="
                                    + groupid;
                            // RecordSet rs1=new RecordSet();
                            rs1.executeSql(sql);*/
                            //////
                            String gdconditions = "";
                            RecordSet rsgroupdetail = new RecordSet();
                            sql = "select id,groupid,type,objid,level_n,level2_n,signorder,conditions,conditioncn,orders,IsCoadjutant,signtype,issyscoadjutant,issubmitdesc,ispending,isforward,ismodify,coadjutants,coadjutantcn,deptField,subcompanyField,virtualid,ruleRelationship,bhxj,jobobj,jobfield from workflow_groupdetail where groupid= " + groupid+" order by id";
                            rsgroupdetail.executeSql(sql);
                            while(rsgroupdetail.next()){
                            	int batcholdgdetailid = rsgroupdetail.getInt("id");
                            	int batchgroupid = rsgroupdetail.getInt("groupid");
                            	int type = rsgroupdetail.getInt("type");
                            	int objid = rsgroupdetail.getInt("objid");
                            	int level_n = rsgroupdetail.getInt("level_n");
                            	int level2_n = rsgroupdetail.getInt("level2_n");
                            	String signorder = rsgroupdetail.getString("signorder");
                            	gdconditions = rsgroupdetail.getString("conditions");
                            	String conditioncn = rsgroupdetail.getString("conditioncn");
                            	conditioncn = conditioncn.replace("'", "''");
                            	String orders = rsgroupdetail.getString("orders");
                            	String IsCoadjutant = rsgroupdetail.getString("IsCoadjutant");
                            	String signtype = rsgroupdetail.getString("signtype");
                            	String issyscoadjutant = rsgroupdetail.getString("issyscoadjutant");
                            	String issubmitdesc = rsgroupdetail.getString("issubmitdesc");
                            	String ispending = rsgroupdetail.getString("ispending");
                            	String isforward = rsgroupdetail.getString("isforward");
                            	String ismodify = rsgroupdetail.getString("ismodify");
                            	String coadjutants = rsgroupdetail.getString("coadjutants");
                            	String coadjutantcn = rsgroupdetail.getString("coadjutantcn");
                            	String deptField = rsgroupdetail.getString("deptField");
                            	String subcompanyField = rsgroupdetail.getString("subcompanyField");
                            	String virtualid = rsgroupdetail.getString("virtualid");
                            	String ruleRelationship = rsgroupdetail.getString("ruleRelationship");
                            	int bhxj = Util.getIntValue(rsgroupdetail.getString("bhxj"),0);
                            	String jobobj = rsgroupdetail.getString("jobobj");
                            	String jobfield = rsgroupdetail.getString("jobfield");
                            	RecordSet gdetailinsertrs = new RecordSet();
                            	sql = "insert into workflow_groupdetail (groupid,type,objid,level_n,level2_n,signorder,conditions,conditioncn,orders,IsCoadjutant,signtype,issyscoadjutant,issubmitdesc,ispending,isforward,ismodify,coadjutants,coadjutantcn,deptField,subcompanyField,virtualid,ruleRelationship,bhxj,jobobj,jobfield) values( "
                            			 + newgourpid+","+type+","+objid+","+level_n+","+level2_n+",'"+signorder+"','"+gdconditions+"','"+conditioncn+"','"+orders+"','"+IsCoadjutant+"','"+signtype+"','"+issyscoadjutant+"','"+issubmitdesc+"','"+ispending+"','"+isforward+"','"+ismodify+"','"+coadjutants+"','"+coadjutantcn+"','"+deptField+"','"+subcompanyField+"','"+virtualid+"','"+ruleRelationship+"',"+bhxj+",'"+jobobj+"','"+jobfield+"' )" ;
                            	gdetailinsertrs.executeSql(sql);
	                            //批次条件保存--start
                            	int batchgroupdetailid = -1;
                            	sql = "select max(id) as maxid from workflow_groupdetail";
                            	RecordSet gdetailrs = new RecordSet();
                            	gdetailrs.executeSql(sql);
                                if (gdetailrs.next()) {
                                	batchgroupdetailid = Util.getIntValue(gdetailrs.getString("maxid"), 0);
                                }
                               
                                if(type==3){//人力资源类型特殊控制
                                	 RecordSet HrmOpers = new RecordSet();
                                	HrmOpers.executeSql("insert into Workflow_HrmOperator(groupid,groupdetailid,objid,orders) select "+newgourpid+","+batchgroupdetailid+",objid,orders  from Workflow_HrmOperator where groupdetailid='"+batcholdgdetailid+"' ");
                                }
                                
	                            int batchrulesrc = -1;
	                            int batchformid = -1;
	                            int batchlinkid = -1;
	                            //rs1.executeSql("select id,conditions from workflow_groupdetail where groupid = " + newgourpid);
	                            //while (rs1.next()) {
                            	//batchgroupdetailid = Util.getIntValue(rs1.getString("id"),-1);
                            	//gdconditions = Util.null2String(rs1.getString("conditions"));
                            	if(!"".equals(gdconditions) && gdconditions.indexOf("(") == -1){
                                    String newconditions = RuleBusiness.copyRulesByRuleids(gdconditions, returnValue, batcholdgdetailid, batchgroupdetailid, RuleInterface.RULESRC_PC, oldnodeidlist, nodeidlist);
                                    if(!"".equals(newconditions)){
                                        String groupdetailsql = "update workflow_groupdetail set conditions = ? where id = " + batchgroupdetailid;
                                        RecordSet gdrs_2 = new RecordSet();
                                        gdrs_2.executeUpdate(groupdetailsql, newconditions);
                                    }
                            	}
	                            //}
	                            //批次条件保存--end
                            }
                            
                            // Myq 修改 2008.3.10 开始
                            // 复制操作组时,上面的rs1.executeSql(sql)是将原来流程的信息除了groupid字段完全复制,
                            // 如果条件中含有节点操作者(节点操作者本人或节点操作者经理)条件,
                            // 保存在objid字段中的值是原流程中的节点id，
                            // 应该保存当前匹配的节点id，所以做此修改
                            // 因为在匹配当前流程节点时用到了节点名称为条件与原流程比较，
                            // 所以如果原流程中节点名称有相同的将会出错。
                            sql = "select id,objid from workflow_groupdetail where (type=40 or type=41) and groupid=" + newgourpid + " order by id";
                            rs1.executeSql(sql);
                            while (rs1.next()) {
                                int tempid = rs1.getInt("id");
                                int tempobjid = rs1.getInt("objid");
                                //String conditions = Util.null2String(rs1.getString("conditions"));
                                int newobjid = 0;
                                RecordSet rs2 = new RecordSet();
                                rs2.executeSql("select nodename from workflow_nodebase where (workflow_nodebase.isfreenode is null or workflow_nodebase.isfreenode !='1') and id=" + tempobjid);
                                if (rs2.next()) {
                                    String tempnodename = rs2.getString("nodename");
                                    for (int j = 0; j < nodeidlist.size(); j++) {
                                        rs2.executeSql("select id from workflow_nodebase  where  (workflow_nodebase.isfreenode is null or workflow_nodebase.isfreenode !='1') and nodename='" + tempnodename + "' and id=" + (String) nodeidlist.get(j));
                                        if (rs2.next()) {
                                            newobjid = rs2.getInt("id");
                                        }
                                    }
                                }
                                rs2.executeSql("update workflow_groupdetail set objid=" + newobjid + " where id=" + tempid);
                            }
                            // Myq 修改 2008.3.10 结束
                            // 批次 引用
                            ArrayList groupidlist2 = new ArrayList();
                            ArrayList newgroupidlist2 = new ArrayList();
                            sql = "select * from workflow_groupdetail where groupid=" + newgourpid + " order by id";
                            rs1.executeSql(sql);
                            while (rs1.next()) {
                                int newgtid2 = rs1.getInt("id");
                                // System.out.println("-3490-newgtid2-==="+newgtid2);
                                newgroupidlist2.add("" + newgtid2);
                            }
                            sql = "select id,objid from workflow_groupdetail where groupid=" + groupid + " order by id";
                            // System.out.println("-2772-sql-==="+sql);
                            rs1.executeSql(sql);
                            while (rs1.next()) {
                                int oldgtid2 = rs1.getInt("id");
                                groupidlist2.add("" + oldgtid2);
                            }
                            // 矩阵
                            ArrayList groupidlist = new ArrayList();
                            ArrayList newgroupidlist = new ArrayList();
                            sql = "select id,objid from workflow_groupdetail where  type=99 and groupid=" + newgourpid + " order by id";
                            // System.out.println("-2762-sql-==="+sql);
                            rs1.executeSql(sql);
                            while (rs1.next()) {
                                int newgtid = rs1.getInt("id");
                                // System.out.println("-2762-newgtid-==="+newgtid);
                                newgroupidlist.add("" + newgtid);
                            }

                            sql = "select id,objid from workflow_groupdetail where  type=99 and groupid=" + groupid + " order by id";
                            // System.out.println("-2772-sql-==="+sql);
                            rs1.executeSql(sql);
                            while (rs1.next()) {
                                int oldgtid = rs1.getInt("id");
                                groupidlist.add("" + oldgtid);
                            }
                            sql = "select id,objid from workflow_groupdetail where  type=99 and groupid=" + groupid + " order by id";
                            // System.out.println("-2780-sql-==="+sql);
                            rs1.executeSql(sql);
                            while (rs1.next()) {
                                int oldgtid2 = rs1.getInt("id");
                                int gtindex = groupidlist.indexOf("" + oldgtid2);
                                // System.out.println("-2778-gtindex-==="+gtindex);
                                // System.out.println("-2778-oldgtid-==="+oldgtid2);
                                int newgtid = -1;
                                if (gtindex > -1) {
                                    newgtid = Util.getIntValue((String) newgroupidlist.get(gtindex));
                                }
                                // System.out.println("-2783-newgtid-==="+newgtid);
                                if (newgtid > 0) {
                                    sql = "insert into workflow_groupdetail_matrix (groupdetailid,matrix,value_field) select " + newgtid + ",matrix,value_field from workflow_groupdetail_matrix where groupdetailid=" + oldgtid2;
                                    rs1.executeSql(sql);

                                    sql = "insert into workflow_matrixdetail (groupdetailid,condition_field,workflow_field) select " + newgtid + ",condition_field,workflow_field from workflow_matrixdetail where groupdetailid=" + oldgtid2;
                                    rs1.executeSql(sql);

                                }

                            }

                            /*
                             * while(rs1.next()){ int type=rs1.getInt("type");
                             * int objid=rs1.getInt("objid"); int
                             * level_n=rs1.getInt("level_n"); int
                             * level2_n=rs1.getInt("level2_n"); int
                             * signorder=rs1.getInt("signorder"); int
                             * order=rs1.getInt("orders");
                             * 
                             * sql="insert into
                             * workflow_groupdetail(groupid,type,objid,level_n,level2_n,signorder)
                             * values(?,?,?,?,?,?)";
                             * statement.setStatementSql(sql);
                             * statement.setInt(1,newgourpid);
                             * statement.setInt(2,type);
                             * statement.setInt(3,objid);
                             * statement.setInt(4,level_n);
                             * statement.setInt(5,level2_n);
                             * statement.setInt(6,signorder);
                             * statement.executeUpdate(); }
                             */
                            RecordSet rs2 = new RecordSet();
                            String nodetype = "";
                            rs2.executeProc("workflow_NodeType_Select", "" + returnValue + Util.getSeparator() + newnodeid);
                            if (rs2.next()) {
                                nodetype = rs2.getString("nodetype");
                            }
                            if (nodetype.equals("0")) {
                                RequestCheckUser cuser = new RequestCheckUser();
                                cuser.resetParameter();
                                cuser.setWorkflowid(returnValue);
                                cuser.setNodeid(Util.getIntValue(newnodeid));
                                cuser.updateCreateList(newgourpid);
                            }
                        }
                        // 节点操作组end
                        // 节点字段（一般模式）
                        sql = "select * from workflow_nodeform where nodeid=" + oldnodeid;
                        rs.executeSql(sql);
                        while (rs.next()) {
                            int fieldid = rs.getInt("fieldid");
                            String isview = rs.getString("isview");
                            String isedit = rs.getString("isedit");
                            String ismandatory = rs.getString("ismandatory");
                            int orderid_tmp = Util.getIntValue(rs.getString("orderid"), 0);
                            sql = "insert into workflow_nodeform(nodeid,fieldid,isview,isedit,ismandatory,orderid) values(?,?,?,?,?,?)";
                            statement.setStatementSql(sql);
                            statement.setInt(1, Util.getIntValue(newnodeid));
                            statement.setInt(2, fieldid);
                            statement.setString(3, isview);
                            statement.setString(4, isedit);
                            statement.setString(5, ismandatory);
                            statement.setInt(6, orderid_tmp);
                            statement.executeUpdate();
                        }
                        // 节点字段（一般模式）end
                        // 明细字段4个属性 Start TD10080
                        sql = "select groupid,isadd,isedit,isdelete,ishidenull,isdefault,isneed,isopensapmul,defaultrows,isprintserial,allowscroll from workflow_NodeFormGroup where nodeid=" + oldnodeid;
                        rs.executeSql(sql);
                        while (rs.next()) {
                            int groupid_tmp = Util.getIntValue(rs.getString("groupid"), 0);
                            int isadd_tmp = Util.getIntValue(rs.getString("isadd"), 0);
                            int isedit_tmp = Util.getIntValue(rs.getString("isedit"), 0);
                            int isdelete_tmp = Util.getIntValue(rs.getString("isdelete"), 0);
                            int ishidenull_tmp = Util.getIntValue(rs.getString("ishidenull"), 0);
                            int isdefault_tmp = Util.getIntValue(rs.getString("isdefault"), 0);
                            int isneed_tmp = Util.getIntValue(rs.getString("isneed"), 0);
                            int isopensapmul_tmp = Util.getIntValue(rs.getString("isopensapmul"), 0);
                            int defaultrows_tmp = Util.getIntValue(rs.getString("defaultrows"), 0);
                            int isprintserial_tmp = Util.getIntValue(rs.getString("isprintserial"), 0);
                            int allowscroll_tmp = Util.getIntValue(rs.getString("allowscroll"), 0);
                            
                            sql = "insert into workflow_NodeFormGroup(nodeid,groupid,isadd,isedit,isdelete,ishidenull,isdefault,isneed,isopensapmul,defaultrows,isprintserial,allowscroll) values(?,?,?,?,?,?,?,?,?,?,?,?)";
                            statement.setStatementSql(sql);
                            statement.setInt(1, Util.getIntValue(newnodeid));
                            statement.setInt(2, groupid_tmp);
                            statement.setString(3, "" + isadd_tmp);
                            statement.setString(4, "" + isedit_tmp);
                            statement.setString(5, "" + isdelete_tmp);
                            statement.setString(6, "" + ishidenull_tmp);
                            statement.setString(7, "" + isdefault_tmp);
                            statement.setString(8, "" + isneed_tmp);
                            statement.setString(9, "" + isopensapmul_tmp);
                            statement.setString(10, "" + defaultrows_tmp);
                            statement.setString(11, "" + isprintserial_tmp);
                            statement.setString(12, "" + allowscroll_tmp);
                            statement.executeUpdate();
                        }
                        // 明细字段4个属性 End

                        // 自定义菜单start
                        sql = "select * from workflow_nodecustomrcmenu where wfid=" + templateid + " and nodeid=" + oldnodeid;
                        rs.executeSql(sql);
                        while (rs.next()) {
                            sql = "insert into workflow_nodecustomrcmenu("
                                    + "wfid,nodeid,submitName7,submitName8,submitName9,forwardName7,forwardName8,forwardName9,"
                                    + "saveName7,saveName8,saveName9,rejectName7,rejectName8,rejectName9,forsubName7,forsubName8,forsubName9,"
                                    + "ccsubName7,ccsubName8,ccsubName9,newWFName7,newWFName8,newWFName9,newSMSName7,newSMSName8,newSMSName9,"
                                    + "haswfrm,hassmsrm,workflowid,customMessage,fieldid,"
                                    + "subnobackName7,subnobackName8,subnobackName9,subbackName7,subbackName8,subbackName9,"
                                    + "forsubnobackName7,forsubnobackName8,forsubnobackName9,forsubbackName7,forsubbackName8,forsubbackName9,"
                                    + "ccsubnobackName7,ccsubnobackName8,ccsubnobackName9,ccsubbackName7,ccsubbackName8,ccsubbackName9,"
                                    + "hasfornoback,hasforback,hasccnoback,hasccback,hasnoback,hasback,usecustomsender,hasovertime,"
                                    + "newOverTimeName7,newOverTimeName8,newOverTimeName9,hasforhandback,hasforhandnoback,hastakingOpinionsback,hastakingOpinionsnoback,"
                                    + "forhandName7,forhandName8,forhandName9,forhandnobackName7,forhandnobackName8,forhandnobackName9,forhandbackName7,forhandbackName8,forhandbackName9,takingOpName7,takingOpName8,takingOpName9,takingOpinionsName7,takingOpinionsName8,takingOpinionsName9,takingOpinionsnobackName7,takingOpinionsnobackName8,takingOpinionsnobackName9,takingOpinionsbackName7,takingOpinionsbackName8,takingOpinionsbackName9,newCHATSName7,newCHATSName8,newCHATSName9,haschats,customChats,chatsfieldid,isshowinwflog"
                                    + ",subbackCtrl,forhandbackCtrl,forsubbackCtrl,ccsubbackCtrl,takingOpinionsbackCtrl,isSubmitDirect,submitDirectName7,submitDirectName8,submitDirectName9"
                                    + ") " + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            statement.setStatementSql(sql);
                            statement.setInt(1, returnValue);
                            statement.setInt(2, Util.getIntValue(newnodeid));
                            statement.setString(3, rs.getString("submitName7"));
                            statement.setString(4, rs.getString("submitName8"));
                            statement.setString(5, rs.getString("submitName9"));
                            statement.setString(6, rs.getString("forwardName7"));
                            statement.setString(7, rs.getString("forwardName8"));
                            statement.setString(8, rs.getString("forwardName9"));
                            statement.setString(9, rs.getString("saveName7"));
                            statement.setString(10, rs.getString("saveName8"));
                            statement.setString(11, rs.getString("saveName9"));
                            statement.setString(12, rs.getString("rejectName7"));
                            statement.setString(13, rs.getString("rejectName8"));
                            statement.setString(14, rs.getString("rejectName9"));
                            statement.setString(15, rs.getString("forsubName7"));
                            statement.setString(16, rs.getString("forsubName8"));
                            statement.setString(17, rs.getString("forsubName9"));
                            statement.setString(18, rs.getString("ccsubName7"));
                            statement.setString(19, rs.getString("ccsubName8"));
                            statement.setString(20, rs.getString("ccsubName9"));
                            statement.setString(21, rs.getString("newWFName7"));
                            statement.setString(22, rs.getString("newWFName8"));
                            statement.setString(23, rs.getString("newWFName9"));
                            statement.setString(24, rs.getString("newSMSName7"));
                            statement.setString(25, rs.getString("newSMSName8"));
                            statement.setString(26, rs.getString("newSMSName9"));
                            statement.setString(27, rs.getString("haswfrm"));
                            statement.setString(28, rs.getString("hassmsrm"));
                            statement.setString(29, rs.getString("workflowid"));
                            statement.setString(30, rs.getString("customMessage"));
                            statement.setString(31, rs.getString("fieldid"));
                            statement.setString(32, rs.getString("subnobackName7"));
                            statement.setString(33, rs.getString("subnobackName8"));
                            statement.setString(34, rs.getString("subnobackName9"));
                            statement.setString(35, rs.getString("subbackName7"));
                            statement.setString(36, rs.getString("subbackName8"));
                            statement.setString(37, rs.getString("subbackName9"));
                            statement.setString(38, rs.getString("forsubnobackName7"));
                            statement.setString(39, rs.getString("forsubnobackName8"));
                            statement.setString(40, rs.getString("forsubnobackName9"));
                            statement.setString(41, rs.getString("forsubbackName7"));
                            statement.setString(42, rs.getString("forsubbackName8"));
                            statement.setString(43, rs.getString("forsubbackName9"));
                            statement.setString(44, rs.getString("ccsubnobackName7"));
                            statement.setString(45, rs.getString("ccsubnobackName8"));
                            statement.setString(46, rs.getString("ccsubnobackName9"));
                            statement.setString(47, rs.getString("ccsubbackName7"));
                            statement.setString(48, rs.getString("ccsubbackName8"));
                            statement.setString(49, rs.getString("ccsubbackName9"));
                            statement.setString(50, rs.getString("hasfornoback"));
                            statement.setString(51, rs.getString("hasforback"));
                            statement.setString(52, rs.getString("hasccnoback"));
                            statement.setString(53, rs.getString("hasccback"));
                            statement.setString(54, rs.getString("hasnoback"));
                            statement.setString(55, rs.getString("hasback"));
                            statement.setString(56, rs.getString("usecustomsender"));
                            statement.setString(57, rs.getString("hasovertime"));
                            statement.setString(58, rs.getString("newOverTimeName7"));
                            statement.setString(59, rs.getString("newOverTimeName8"));
                            statement.setString(60, rs.getString("newOverTimeName9"));
                            statement.setString(61, rs.getString("hasforhandback"));
                            statement.setString(62, rs.getString("hasforhandnoback"));
                            statement.setString(63, rs.getString("hastakingOpinionsback"));
                            statement.setString(64, rs.getString("hastakingOpinionsnoback"));
                            statement.setString(65, rs.getString("forhandName7"));
                            statement.setString(66, rs.getString("forhandName8"));
                            statement.setString(67, rs.getString("forhandName9"));
                            statement.setString(68, rs.getString("forhandnobackName7"));
                            statement.setString(69, rs.getString("forhandnobackName8"));
                            statement.setString(70, rs.getString("forhandnobackName9"));
                            statement.setString(71, rs.getString("forhandbackName7"));
                            statement.setString(72, rs.getString("forhandbackName8"));
                            statement.setString(73, rs.getString("forhandbackName9"));
                            statement.setString(74, rs.getString("takingOpName7"));
                            statement.setString(75, rs.getString("takingOpName8"));
                            statement.setString(76, rs.getString("takingOpName9"));
                            statement.setString(77, rs.getString("takingOpinionsName7"));
                            statement.setString(78, rs.getString("takingOpinionsName8"));
                            statement.setString(79, rs.getString("takingOpinionsName9"));
                            statement.setString(80, rs.getString("takingOpinionsnobackName7"));
                            statement.setString(81, rs.getString("takingOpinionsnobackName8"));
                            statement.setString(82, rs.getString("takingOpinionsnobackName9"));
                            statement.setString(83, rs.getString("takingOpinionsbackName7"));
                            statement.setString(84, rs.getString("takingOpinionsbackName8"));
                            statement.setString(85, rs.getString("takingOpinionsbackName9"));
                            statement.setString(86, rs.getString("newCHATSName7"));
                            statement.setString(87, rs.getString("newCHATSName8"));
                            statement.setString(88, rs.getString("newCHATSName9"));
                            statement.setString(89, rs.getString("haschats"));
                            statement.setString(90, rs.getString("customChats"));
                            statement.setInt(91, Util.getIntValue(rs.getString("chatsfieldid")));
                            statement.setString(92, rs.getString("isshowinwflog"));
                            statement.setInt(93, rs.getInt("subbackCtrl"));
							statement.setInt(94, rs.getInt("forhandbackCtrl"));
							statement.setInt(95, rs.getInt("forsubbackCtrl"));
							statement.setInt(96, rs.getInt("ccsubbackCtrl"));
							statement.setInt(97, rs.getInt("takingOpinionsbackCtrl"));
							statement.setString(98, rs.getString("isSubmitDirect"));
							statement.setString(99, rs.getString("submitDirectName7"));
							statement.setString(100, rs.getString("submitDirectName8"));
							statement.setString(101, rs.getString("submitDirectName9"));

                            statement.executeUpdate();
                        }
                        // 自定义菜单end
                        
                        // 新建菜单 start
                        rs.executeSql("delete from workflow_nodeCustomNewMenu where wfid=" + returnValue + " and nodeid=" + newnodeid);
                        rs.executeSql("select * from workflow_nodeCustomNewMenu where wfid=" + templateid + " and nodeid=" + oldnodeid);
                        while(rs.next()) {
                        	int menuType = Util.getIntValue(rs.getString("menuType"));
                        	int enable = Util.getIntValue(rs.getString("enable"), 0);
                        	String newName7 = Util.null2String(rs.getString("newName7"));
                        	String newName8 = Util.null2String(rs.getString("newName8"));
                        	String newName9 = Util.null2String(rs.getString("newName9"));
                        	int workflowid = Util.getIntValue(rs.getString("workflowid"), 0);
                        	String customMessage = Util.null2String(rs.getString("customMessage"));
                        	int fieldid = Util.getIntValue(rs.getString("fieldid"), 0);
                        	String usecustomsender = Util.null2String(rs.getString("usecustomsender"));
                			
                			String sql_new = "insert into workflow_nodeCustomNewMenu(wfid, nodeid, menuType, enable, newName7, newName8, newName9, workflowid, customMessage, fieldid, usecustomsender) "
                				+ " values(" + returnValue + ", " + newnodeid + ", " + menuType + ", " + enable + ", '" + Util.toHtml100(newName7) + "', '" + Util.toHtml100(newName8) + "', '" + Util.toHtml100(newName9) + "', " + workflowid + ", '" + Util.toHtml100(customMessage) + "', " + fieldid + ", '" + Util.toHtml100(usecustomsender) + "') ";
                			rs.executeSql(sql_new);
                        }
                        // 新建菜单 end
                    }
                    // 节点操作组and节点字段（一般模式）end

                    // 流程计划复制 start TD27753
                    // 查询原流程计划
                    sql = "select * from WorkFlowPlanSet where flowid=" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        String status = rs.getString("status");
                        String frequencyt = rs.getString("frequencyt");
                        String dateType = rs.getString("dateType");
                        int dateSum = rs.getInt("dateSum");
                        String alertType = rs.getString("alertType");
                        String timeSet = rs.getString("timeSet");
                        // 新增新流程计划
                        sql = "insert into WorkFlowPlanSet (status,frequencyt,dateType,dateSum,alertType,flowId,timeSet) values (?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setString(1, status);
                        statement.setString(2, frequencyt);
                        statement.setString(3, dateType);
                        statement.setInt(4, dateSum);
                        statement.setString(5, alertType);
                        statement.setInt(6, returnValue);
                        statement.setString(7, timeSet);
                        rs.executeSql(sql);
                        statement.executeUpdate();
                    }
                    // 流程计划复制 end

                    //标题字段复制 start
                    sql = "select * from Workflow_SetTitle where workflowId =" + templateid;
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int maxst = 0;
                        RecordSet rsst = new RecordSet();
                        /* rsst.executeSql("select max(id) as id from Workflow_SetTitle");
                        if (rsst.next()) {
                        		 maxst = Util.getIntValue(rsst.getString("id"), 0);
                        				 }*/
                        sql = "select max(id) as maxid from Workflow_SetTitle";
                        statement.setStatementSql(sql);
                        statement.executeQuery();
                        if (statement.next()) {
                            maxst = statement.getInt("maxid");
                        }

                        maxst = maxst + 1;
                        String xh = rs.getString("xh");
                        String fieldtype = rs.getString("fieldtype");
                        String fieldvalue = rs.getString("fieldvalue");
                        String fieldlevle = rs.getString("fieldlevle");
                        String fieldname = rs.getString("fieldname");
                        String fieldzx = rs.getString("fieldzx");
                        String trrowid = rs.getString("trrowid");
                        String txtUserUse = rs.getString("txtUserUse");
                        String showhtml = rs.getString("showhtml");
                        sql = "insert into Workflow_SetTitle (workflowid,xh,fieldtype,fieldvalue,fieldlevle,fieldname,fieldzx,trrowid,txtUserUse,showhtml) " + "values(?,?,?,?,?,?,?,?,?,?)";
                        if (statement.getDBType().equals("oracle")) {
                            sql = "insert into Workflow_SetTitle (workflowid,xh,fieldtype,fieldvalue,fieldlevle,fieldname,fieldzx,trrowid,txtUserUse,showhtml,id) " + "values(?,?,?,?,?,?,?,?,?,?,?)";
                        }
                        statement.setStatementSql(sql);
                        statement.setString(1, Util.null2String(returnValue));
                        statement.setString(2, xh);
                        statement.setString(3, fieldtype);
                        statement.setString(4, fieldvalue);
                        statement.setString(5, fieldlevle);
                        statement.setString(6, fieldname);
                        statement.setString(7, fieldzx);
                        statement.setString(8, trrowid);
                        statement.setString(9, txtUserUse);
                        statement.setString(10, showhtml);
                        if (statement.getDBType().equals("oracle")) {
                            statement.setInt(11, maxst);
                        }
                        statement.executeUpdate();
                    }
                    //标题字段复制end

                    // 标题字段复制
                    rs.execute("insert into workflow_titleSet select " + returnValue + ",fieldId,null from workflow_titleSet where flowId=" + templateid);
                    // 督办设置
                    sql = "select * from workflow_urgerdetail where workflowid=" + templateid + " order by id";
                    rs.executeSql(sql);
                    while (rs.next()) {
                        int utype = rs.getInt("utype");
                        int objid = rs.getInt("objid");
                        int level_n = rs.getInt("level_n");
                        int level2_n = rs.getInt("level2_n");
                        String conditions = rs.getString("conditions");
                        String conditioncn = rs.getString("conditioncn");
                        String jobobj = rs.getString("jobobj");
                        String jobfield = rs.getString("jobfield");
                        sql = "Insert into workflow_urgerdetail(workflowid,utype,objid,level_n,level2_n,conditions,conditioncn,jobobj,jobfield) values(?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, utype);
                        statement.setInt(3, objid);
                        statement.setInt(4, level_n);
                        statement.setInt(5, level2_n);
                        statement.setString(6, conditions);
                        statement.setString(7, conditioncn);
                        statement.setString(8, jobobj);
                        statement.setString(9, jobfield);
                        statement.executeUpdate();
                    }
                    // 督办设置 end
                    // 高级设置复制
                    // 流程创建文档
                    rs.executeSql("select * from workflow_createdoc where workflowId=" + templateid + " order by id");
                    while (rs.next()) {

                        String useTempletNode = Util.null2String((String) map.get(rs.getString("useTempletNode")));
                        String printNodes = Util.null2String((String) map.get(rs.getString("printNodes")));
                        String signatureNodes = Util.null2String((String) map.get(rs.getString("signatureNodes")));
                        String documentTitleField = rs.getString("documentTitleField");
                        String isCompellentMark = rs.getString("isCompellentMark");
                        String isCancelCheck = rs.getString("isCancelCheck");
                        String isWorkflowDraft = rs.getString("isWorkflowDraft");
                        String extfile2doc = rs.getString("extfile2doc");
                        String isHideTheTraces = rs.getString("isHideTheTraces");
                        String newTextNodes = rs.getString("newTextNodes");
                        String defaultDocType = rs.getString("defaultDocType");

                        String status = rs.getString("status");
                        String flowCodeField = rs.getString("flowCodeField");
                        String flowDocField = rs.getString("flowDocField");
                        String flowDocCatField = rs.getString("flowDocCatField");
                        String defaultView = rs.getString("defaultView");
						String openTextDefaultNode = rs.getString("openTextDefaultNode");
						String cleanCopyNodes = rs.getString("cleanCopyNodes");
						String isTextInForm = Util.null2String(rs.getString("isTextInForm")); 
                        sql = "INSERT INTO workflow_createdoc(workflowId, status, flowCodeField, flowDocField, flowDocCatField, defaultView,useTempletNode,printNodes,signatureNodes,documentTitleField,isCompellentMark,isCancelCheck,isWorkflowDraft,extfile2doc,isHideTheTraces,newTextNodes,defaultDocType,openTextDefaultNode,cleanCopyNodes,isTextInForm) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";						
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setString(2, status);
                        statement.setInt(3, Util.getIntValue(flowCodeField));
                        statement.setInt(4, Util.getIntValue(flowDocField));
                        statement.setInt(5, Util.getIntValue(flowDocCatField));
                        statement.setString(6, defaultView);

                        statement.setString(7, useTempletNode);
                        statement.setString(8, printNodes);
                        statement.setString(9, signatureNodes);
                        statement.setString(10, documentTitleField);
                        statement.setString(11, isCompellentMark);
                        statement.setString(12, isCancelCheck);
                        statement.setString(13, isWorkflowDraft);
                        statement.setString(14, extfile2doc);
                        statement.setString(15, isHideTheTraces);
                        statement.setString(16, newTextNodes);
                        statement.setString(17, defaultDocType);
						statement.setString(18, convertOpenTextNodesInfo(nodeIdMap ,openTextDefaultNode));
						statement.setString(19, convertOpenTextNodesInfo(nodeIdMap ,cleanCopyNodes));
						statement.setString(20, isTextInForm);
                        statement.executeUpdate();
                    }
                    
                    //正文转PDF
                    rs.executeSql("select * from workflow_texttopdfconfig where workflowid=" + templateid);
                    while (rs.next()) {
                    	int topdfnodeid = rs.getInt("topdfnodeid");
                    	int pdfsavesecid = rs.getInt("pdfsavesecid");
                    	String catalogtype2 = rs.getString("catalogtype2");
                    	int selectcatalog2 = rs.getInt("selectcatalog2");
                    	int pdfdocstatus = rs.getInt("pdfdocstatus");
                    	int pdffieldid = rs.getInt("pdffieldid");
                    	int decryptpdfsavesecid = rs.getInt("decryptpdfsavesecid");
                    	String decryptcatalogtype2 = rs.getString("decryptcatalogtype2");
                    	int decryptselectcatalog2 = rs.getInt("decryptselectcatalog2");
                    	int decryptpdfdocstatus = rs.getInt("decryptpdfdocstatus");
                    	int decryptpdffieldid = rs.getInt("decryptpdffieldid");
                    	int operationtype = rs.getInt("operationtype");
                    	String checktype = rs.getString("checktype");
                    	int filetopdffile = rs.getInt("filetopdffile");
                    	int filetopdf = rs.getInt("filetopdf");
                    	int filemaxsize = rs.getInt("filemaxsize");
                    	
                    	
                        sql = "INSERT INTO workflow_texttopdfconfig(workflowId, topdfnodeid, pdfsavesecid,"
                        		+ " catalogtype2, selectcatalog2, pdfdocstatus,pdffieldid,decryptpdfsavesecid,"
                        		+ "decryptcatalogtype2,decryptselectcatalog2,decryptpdfdocstatus,decryptpdffieldid,"
                        		+ "operationtype,checktype,filetopdffile,filetopdf,filemaxsize) VALUES(?,?,?,?,?,?,?"
                        		+ ",?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        //修改存为新版后，正文转PDF不生效的问题
                        statement.setInt(2, Util.getIntValue(convertOpenTextNodesInfo(nodeIdMap ,topdfnodeid + "")));
                        statement.setInt(3, pdfsavesecid);
                        statement.setString(4, catalogtype2);
                        statement.setInt(5, selectcatalog2);
                        statement.setInt(6, pdfdocstatus);
                        statement.setInt(7, pdffieldid);
                        statement.setInt(8, decryptpdfsavesecid);
                        statement.setString(9, decryptcatalogtype2);
                        statement.setInt(10, decryptselectcatalog2);
                        statement.setInt(11, decryptpdfdocstatus);
                        statement.setInt(12, decryptpdffieldid);
                        statement.setInt(13, operationtype);
                        statement.setString(14, checktype);
                        statement.setInt(15, filetopdffile);
                        statement.setInt(16, filetopdf);
                        statement.setInt(17, filemaxsize);
                        statement.executeUpdate();
                    }

                    // 默认显示模板
                    rs.executeSql("select * from workflow_docshow where flowId=" + templateid);
                    while (rs.next()) {
                        String selectItemId = rs.getString("selectItemId");
                        String secCategoryID = rs.getString("secCategoryID");
                        String dateShowType = rs.getString("dateShowType");
                        String docMouldID = rs.getString("docMouldID");
                        String modulId = rs.getString("modulId");
                        String fieldId = rs.getString("fieldId");
                        String isdefault = rs.getString("isdefault");
                        sql = "INSERT INTO workflow_docshow(flowId, selectItemId, secCategoryID, docMouldID, modulId, fieldId,dateShowType,isdefault) VALUES(?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, Util.getIntValue(selectItemId));
                        statement.setString(3, secCategoryID);
                        statement.setInt(4, Util.getIntValue(docMouldID));
                        statement.setInt(5, Util.getIntValue(modulId));
                        statement.setInt(6, Util.getIntValue(fieldId));
                        statement.setInt(7, Util.getIntValue(dateShowType));
                        statement.setString(8, isdefault);
                        statement.executeUpdate();
                    }
                    
                    // 默认编辑模板
                    rs.executeSql("select * from workflow_docshowedit where flowId=" + templateid);
                    while (rs.next()) {
                        String selectItemId = rs.getString("selectItemId");
                        String secCategoryID = rs.getString("secCategoryID");
                        String dateShowType = rs.getString("dateShowType");
                        String docMouldID = rs.getString("docMouldID");
                        String modulId = rs.getString("modulId");
                        String fieldId = rs.getString("fieldId");
                        String isdefault = rs.getString("isdefault");
                        sql = "INSERT INTO workflow_docshowedit(flowId, selectItemId, secCategoryID, docMouldID, modulId, fieldId,dateShowType,isdefault) VALUES(?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, Util.getIntValue(selectItemId));
                        statement.setString(3, secCategoryID);
                        statement.setInt(4, Util.getIntValue(docMouldID));
                        statement.setInt(5, Util.getIntValue(modulId));
                        statement.setInt(6, Util.getIntValue(fieldId));
                        statement.setInt(7, Util.getIntValue(dateShowType));
                        statement.setString(8, isdefault);
                        statement.executeUpdate();
                    }
                    
                    //编辑模板
                    rs.executeSql("select * from workflow_mould where workflowid=" + templateid);
                    while(rs.next()){
                    	String mouldid = rs.getString("mouldid");
                    	String mouldType = rs.getString("mouldType");
                    	String visible = rs.getString("visible");
                    	String seccategory = rs.getString("seccategory");
                    	sql = "INSERT INTO workflow_mould(workflowid,mouldid,mouldType,visible,seccategory) VALUES(?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, Util.getIntValue(mouldid));
                        statement.setInt(3, Util.getIntValue(mouldType));
                        statement.setInt(4, Util.getIntValue(visible));
                        statement.setInt(5, Util.getIntValue(seccategory));
                        statement.executeUpdate();
                    }
                    
                    // 文档属性页设置
                    rs.execute("select id from Workflow_DocProp where workflowid=" + templateid);
                    while (rs.next()) {
                        int dp_id = Util.getIntValue(rs.getString("id"), 0);
                        RecordSet rs00 = new RecordSet();
                        if (dp_id > 0) {
                            sql = "insert into Workflow_DocProp(workflowId,selectItemId,secCategoryId,objId,objType) select " + returnValue + ", selectItemId,secCategoryId,objId,objType from Workflow_DocProp where id=" + dp_id;
                            writeLog(sql);
                            rs00.execute(sql);
                            int docPropId = -1;
                            /*rs00
                            		.executeSql("select max(id) as maxId from Workflow_DocProp");
                            if (rs00.next()){
                            	docPropId = Util.getIntValue(rs00
                            			.getString("maxId"), -1);
                            }	*/
                            sql = "select max(id) as maxid from Workflow_DocProp";
                            statement.setStatementSql(sql);
                            statement.executeQuery();
                            if (statement.next()) {
                                docPropId = statement.getInt("maxid");
                            }

                            if (docPropId > 0) {
                                sql = "insert into Workflow_DocPropDetail(docPropId,docPropFieldId,workflowFieldId) select " + docPropId + ",docPropFieldId, workflowFieldId from Workflow_DocPropDetail where docPropId=" + dp_id;
                                rs00.execute(sql);
                            }

                        }
                    }
                    // 流程创建文档 end

                    // 创建任务
                    rs.executeSql("select * from workflow_createtask where wfid=" + templateid + " order by id");
                    while (rs.next()) {
                        int ctid = rs.getInt("id");
                        //String subWorkflowId = rs.getString("subWorkflowId");
                        int ctNodeId = rs.getInt("nodeid");

                        int index = oldnodeidlist.indexOf(ctNodeId);
                        if (index != -1)
                            ctNodeId = Util.getIntValue((String) nodeidlist.get(index));
                        int changetime = rs.getInt("changetime");
                        int taskid = rs.getInt("taskid");
                        int creatertype = rs.getInt("creatertype");
                        int wffieldid = rs.getInt("wffieldid");
                        int changemode = rs.getInt("changemode");

                        sql = "insert into workflow_createtask(wfid,nodeid,changetime,taskid,creatertype,wffieldid,changemode) values(?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, returnValue);
                        statement.setInt(2, ctNodeId);
                        statement.setInt(3, changetime);
                        statement.setInt(4, taskid);
                        statement.setInt(5, creatertype);
                        statement.setInt(6, wffieldid);
                        statement.setInt(7, changemode);
                        statement.executeUpdate();
                    }

                    //子流程对应关系保存
                    Map<String,String> subwfMap = new HashMap<String,String>();
                    Map<String,String> triDiffwfMap = new HashMap<String,String>();
                    // 触发子流程
                    rs.executeSql("select * from Workflow_SubwfSet where mainWorkflowId=" + templateid + " order by id");
                    while (rs.next()) {
                        int id = rs.getInt("id");

                        String subWorkflowId = rs.getString("subWorkflowId");
                        String triggerNodeId = rs.getString("triggerNodeId");
                        String triggerType = rs.getString("triggerType");
                        int index = oldnodeidlist.indexOf(triggerNodeId);
                        if (index != -1)
                            triggerNodeId = (String) nodeidlist.get(index);
                        String triggerTime = rs.getString("triggerTime");
                        String subwfCreatorType = rs.getString("subwfCreatorType");
                        String subwfcreatorfieldid = rs.getString("subwfcreatorfieldid");
                        String isread = rs.getString("isread");
                        int workflowSubwfSetId = 0;
                        RecordSet rs1 = new RecordSet();
                        rs1.executeProc("SequenceIndex_SelectNextID", "Workflow_SubwfSetId");
                        if (rs1.next()) {
                            workflowSubwfSetId = rs1.getInt(1);
                        }
                        subwfMap.put(id+"", workflowSubwfSetId+"");
                        String mainWorkflowId = rs.getString("mainWorkflowId");
                        String TriggerOperation = rs.getString("TriggerOperation");
                        int triggerSource = Util.getIntValue(rs.getString("triggerSource"));
                        String triggerSourceType = rs.getString("triggerSourceType");
                        int triggerSourceOrder = Util.getIntValue(rs.getString("triggersourceorder"));
                        String triggerCondition = rs.getString("triggerCondition");
                        String isreadNodes = rs.getString("isreadNodes");
                        String isreadMainwf = rs.getString("isreadMainwf");
                        String isreadMainWfNodes = rs.getString("isreadMainWfNodes");
                        String isreadParallelwf = rs.getString("isreadParallelwf");
                        String isreadParallelwfNodes = rs.getString("isreadParallelwfNodes");
                        String enable = rs.getString("enable");
                        String isStopCreaterNode = rs.getString("isStopCreaterNode");

                        String condition = Util.null2String(rs.getString("condition"));
                        String conditioncn = rs.getString("conditioncn");
                        String ruleRelationship = rs.getString("ruleRelationship");
                        
                        if (!"".equals(condition)) {
                            condition = RuleBusiness.copyRulesByRuleids(condition, returnValue, id, workflowSubwfSetId, 7, oldnodeidlist, nodeidlist);
                        }
                        
                        sql = "insert into Workflow_SubwfSet(id,subWorkflowId,triggerNodeId,triggerType,triggerTime,subwfCreatorType,subwfcreatorfieldid,isread,mainWorkflowId,TriggerOperation,triggerSource,triggerSourceType,triggerSourceOrder,triggerCondition,isreadNodes,isreadMainwf,isreadMainWfNodes,isreadParallelwf,isreadParallelwfNodes,enable,isStopCreaterNode, condition, conditioncn, ruleRelationship) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, workflowSubwfSetId);
                        statement.setInt(2, Util.getIntValue(subWorkflowId));
                        statement.setInt(3, Util.getIntValue(triggerNodeId));
                        statement.setString(4, triggerType);
                        statement.setString(5, triggerTime);
                        statement.setString(6, subwfCreatorType);
                        statement.setInt(7, Util.getIntValue(subwfcreatorfieldid));
                        statement.setString(8, isread);
                        statement.setString(9, Util.null2String(returnValue));
                        statement.setString(10, TriggerOperation);
                        statement.setInt(11, triggerSource);
                        statement.setString(12, triggerSourceType);
                        statement.setInt(13, triggerSourceOrder);
                        statement.setString(14, triggerCondition);
                        statement.setString(15, isreadNodes);
                        statement.setString(16, isreadMainwf);
                        statement.setString(17, isreadMainWfNodes);
                        statement.setString(18, isreadParallelwf);
                        statement.setString(19, isreadParallelwfNodes);
                        statement.setString(20, enable);
                        statement.setString(21, isStopCreaterNode);
                        statement.setString(22, condition);
                        statement.setString(23, conditioncn);
                        statement.setString(24, ruleRelationship);
                        statement.executeUpdate();
                        rs1.executeSql("select * from Workflow_SubwfSetdetail where subwfSetId=" + id + " order by id");
                        while (rs1.next()) {
                            String subWorkflowFieldId = rs1.getString("subWorkflowFieldId");
                            String mainWorkflowFieldId = rs1.getString("mainWorkflowFieldId");
                            String ifSplitField = rs1.getString("ifSplitField");
                            // add by liaodong for qc61523 in 2013-11-12 start
                            String isDetail = rs1.getString("isdetail");
                            // add by liaodong for qc61523 in 2013-11-12 start
                            // isdetail
                            //后续补充 
                            String isCreateDocAgain = rs1.getString("isCreateDocAgain");
                            String isCreateAttachmentAgain = rs1.getString("isCreateAttachmentAgain");
                            String isCreateForAnyone = rs1.getString("isCreateForAnyone");

                            sql = "insert into Workflow_SubwfSetDetail(subwfSetId,subWorkflowFieldId,mainWorkflowFieldId,ifSplitField,isdetail,isCreateDocAgain,isCreateAttachmentAgain,isCreateForAnyone)  values(?,?,?,?,?,?,?,?)";
                            statement.setStatementSql(sql);
                            statement.setInt(1, workflowSubwfSetId);
                            statement.setInt(2, Util.getIntValue(subWorkflowFieldId));
                            statement.setInt(3, Util.getIntValue(mainWorkflowFieldId));
                            statement.setString(4, ifSplitField);
                            statement.setString(5, isDetail); // add by
                            // liaodong for
                            // qc61523 in
                            // 2013-11-12
                            // start
                            statement.setString(6, isCreateDocAgain);
                            statement.setString(7, isCreateAttachmentAgain);
                            statement.setString(8, isCreateForAnyone);
                            statement.executeUpdate();
                        }
                    }
                    // add by liaodong for qc61523 in 2013-11-12 start
                    rs.executeSql("select * from Workflow_TriDiffWfDiffField where mainWorkflowId = " + templateid + " order by id");
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String triggerNodeId = rs.getString("triggerNodeId");
                        String triggerTime = rs.getString("triggerTime");
                        String fieldId = rs.getString("fieldId");
                        String triggerType = rs.getString("triggerType");
                        String TriggerOperation = rs.getString("TriggerOperation");
                        //后续
                        String enable = rs.getString("enable");
                        String triggerSourceType = rs.getString("triggerSourceType");
                        String triggerSourceOrder = rs.getString("triggerSourceOrder");
                        String triggerCondition = rs.getString("triggerCondition");
                        int triggerSource = Util.getIntValue(rs.getString("triggerSource"));
                        
                        String condition = Util.null2String(rs.getString("condition"));
                        String conditioncn = rs.getString("conditioncn");
                        String ruleRelationship = rs.getString("ruleRelationship");
                        sql = " insert into Workflow_TriDiffWfDiffField(mainWorkflowId,triggerNodeId,triggerTime,fieldId,triggerType,TriggerOperation,enable,triggerSourceType,triggerSourceOrder,triggerCondition,triggerSource) values (?,?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setString(1, returnValue + "");
                        statement.setString(2, (String) map.get(triggerNodeId));
                        statement.setString(3, triggerTime);
                        statement.setString(4, fieldId);
                        statement.setString(5, triggerType);
                        statement.setString(6, TriggerOperation);
                        statement.setString(7, enable);
                        statement.setString(8, triggerSourceType);
                        statement.setString(9, triggerSourceOrder);
                        statement.setString(10, triggerCondition);
                        statement.setInt(11, triggerSource);
                        statement.executeUpdate();
                        RecordSet rs01 = new RecordSet();
                        String maxid = "";
                        /*rs01
                        		.executeSql("select max(id) from Workflow_TriDiffWfDiffField");
                        
                        if (rs01.next()) {
                        	maxid = rs01.getString(1);
                        }	*/
                        sql = "select max(id) as maxid from Workflow_TriDiffWfDiffField";
                        statement.setStatementSql(sql);
                        statement.executeQuery();
                        if (statement.next()) {
                            maxid = statement.getString("maxid");
                        }
                        
                        if (!"".equals(condition)) {
                            condition = RuleBusiness.copyRulesByRuleids(condition, returnValue, Util.getIntValue(id), Util.getIntValue(maxid), 8, oldnodeidlist, nodeidlist);
                        }
                        
                        sql = " update Workflow_TriDiffWfDiffField set condition=?, conditioncn=?, ruleRelationship=? where id=" + maxid;
                        statement.setStatementSql(sql);
                        statement.setString(1, condition);
                        statement.setString(2, conditioncn);
                        statement.setString(3, ruleRelationship);
                        statement.executeUpdate();
                        
                        triDiffwfMap.put(id, maxid);
                        RecordSet rs1 = new RecordSet();
                        rs1.executeSql("select  * from Workflow_TriDiffWfSubWf  where triDiffWfDiffFieldId=" + id);
                        while (rs1.next()) {
                            String maxTriDiffFId = rs1.getString("id");
                            String subWorkflowId = rs1.getString("subWorkflowId");
                            String subwfCreatorType = rs1.getString("subwfCreatorType");
                            String subwfCreatorFieldId = rs1.getString("subwfCreatorFieldId");
                            String isRead = rs1.getString("isRead");
                            String fieldValue = rs1.getString("fieldValue");
                            //tiany 后续
                            String isreadNodes = rs1.getString("isreadNodes");
                            String isreadMainwf = rs1.getString("isreadMainwf");
                            String isreadMainWfNodes = rs1.getString("isreadMainWfNodes");
                            String isreadParallelwf = rs1.getString("isreadParallelwf");
                            String isreadParallelwfNodes = rs1.getString("isreadParallelwfNodes");
                            String ifSplitField = rs1.getString("ifSplitField");
                            String isStopCreaterNode = rs1.getString("isStopCreaterNode");
                            sql = " insert into  Workflow_TriDiffWfSubWf(triDiffWfDiffFieldId,subWorkflowId,subwfCreatorType,subwfCreatorFieldId,isRead,fieldValue,isreadNodes,isreadMainwf,isreadMainWfNodes,isreadParallelwf,isreadParallelwfNodes,ifSplitField,isStopCreaterNode) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            statement.setStatementSql(sql);
                            statement.setString(1, maxid);
                            statement.setString(2, subWorkflowId);
                            statement.setString(3, subwfCreatorType);
                            statement.setString(4, subwfCreatorFieldId);
                            statement.setString(5, isRead);
                            statement.setString(6, fieldValue);
                            statement.setString(7, isreadNodes);
                            statement.setString(8, isreadMainwf);
                            statement.setString(9, isreadMainWfNodes);
                            statement.setString(10, isreadParallelwf);
                            statement.setString(11, isreadParallelwfNodes);
                            statement.setString(12, ifSplitField);
                            statement.setString(13, isStopCreaterNode);
                            statement.executeUpdate();
                            RecordSet rs3 = new RecordSet();
                            String maxTriDiffId = "";
                            /*rs3
                            		.executeSql("select max(id) from  Workflow_TriDiffWfSubWf ");
                            
                            if (rs3.next()) {
                            	maxTriDiffId = rs3.getString(1);
                            }
                             */
                            sql = "select max(id) as maxid from Workflow_TriDiffWfSubWf";
                            statement.setStatementSql(sql);
                            statement.executeQuery();
                            if (statement.next()) {
                                maxTriDiffId = statement.getString("maxid");
                            }

                            RecordSet rs2 = new RecordSet();
                            rs2.executeSql(" select triDiffWfSubWfId,subWorkflowFieldId,mainWorkflowFieldId,isDetail,isCreateDocAgain,ifSplitField from Workflow_TriDiffWfSubWfField  where  triDiffWfSubWfId=" + maxTriDiffFId);
                            while (rs2.next()) {
                                String subWorkflowFieldId = rs2.getString("subWorkflowFieldId");
                                String mainWorkflowFieldId = rs2.getString("mainWorkflowFieldId");
                                String isDetail = rs2.getString("isDetail");
                                String isCreateDocAgain = rs2.getString("isCreateDocAgain");
                                String ifSplitField2 = rs2.getString("ifSplitField");
                                String isCreateAttachmentAgain = rs2.getString("isCreateAttachmentAgain");
                                sql = " insert into  Workflow_TriDiffWfSubWfField(triDiffWfSubWfId,subWorkflowFieldId,mainWorkflowFieldId,isDetail,isCreateDocAgain,ifSplitField,isCreateAttachmentAgain) values (?,?,?,?,?,?,?)";
                                statement.setStatementSql(sql);
                                statement.setString(1, maxTriDiffId);
                                statement.setString(2, subWorkflowFieldId);
                                statement.setString(3, mainWorkflowFieldId);
                                statement.setString(4, isDetail);
                                statement.setString(5, isCreateDocAgain);
                                statement.setString(6, ifSplitField2);
                                statement.setString(7, isCreateAttachmentAgain);
                                statement.executeUpdate();
                            }
                        }
                    }
                    // end
                    // 触发子流程 end
                    
                    //保存workflow_flownode中subwfscope、subwfdiffscope
                    String upnodeid = "";
                    String upsubwfscope = "";
                    String upsubwfdiffscope = "";
                    String flowsql = " select * from workflow_flownode where workflowid= " + returnValue;
                    rs.executeSql(flowsql);
                    while (rs.next()) {
                    	upnodeid = rs.getString("nodeid");
                    	upsubwfscope = Util.null2String(rs.getString("subwfscope"));
                    	upsubwfdiffscope = Util.null2String(rs.getString("subwfdiffscope"));
                    	String rejectableNodes = Util.null2String(rs.getString("rejectableNodes")).trim();
                    	if(!"".equals(rejectableNodes)) {
                    		String rejectableNodes_up = "";
                    		String[] rejectableNodeArr = rejectableNodes.split(",");
                    		for(int i = 0; i < rejectableNodeArr.length; i++) {
                    			String rejectableNode = Util.null2String(map.get(rejectableNodeArr[i])).trim();
                    			if(!"".equals(rejectableNode)) {
                    				rejectableNodes_up += "," + rejectableNode;
                    			}
                    		}
                    		if(!"".equals(rejectableNodes_up)) {
                    			rejectableNodes_up = rejectableNodes_up.substring(1);
                    		}
                    		rejectableNodes = rejectableNodes_up;
                    	}
                    	if(!"".equals(upsubwfscope)){
                    		if(upsubwfscope.indexOf(",") > -1){
            					String allconditions = "";
            					String [] array = Util.TokenizerString2(upsubwfscope, ",");
            					for(int r=0;r<array.length;r++){
            						if("".equals(allconditions)){
            							allconditions = (String) subwfMap.get(array[r]);
            						}else{
            							allconditions += ","+(String) subwfMap.get(array[r]);
            						}
            					}
            					upsubwfscope = allconditions;
            				}else{
            					upsubwfscope = (String) subwfMap.get(upsubwfscope);
            				}
                    	}
                    	if(!"".equals(upsubwfdiffscope)){
                    		if(upsubwfdiffscope.indexOf(",") > -1){
            					String allconditions = "";
            					String [] array = Util.TokenizerString2(upsubwfdiffscope, ",");
            					for(int r=0;r<array.length;r++){
            						if("".equals(allconditions)){
            							allconditions = (String) triDiffwfMap.get(array[r]);
            						}else{
            							allconditions += ","+(String) triDiffwfMap.get(array[r]);
            						}
            					}
            					upsubwfdiffscope = allconditions;
            				}else{
            					upsubwfdiffscope = (String) triDiffwfMap.get(upsubwfdiffscope);
            				}
                    	}
                    	//123
                    	RecordSet rs1 = new RecordSet();
                    	rs1.executeSql("update workflow_flownode set subwfscope='"+upsubwfscope+"' , subwfdiffscope='"+upsubwfdiffscope+"',rejectableNodes='" + rejectableNodes + "' where workflowid = " + returnValue + " and nodeid = " + upnodeid);
                    }
                    //end
                    
                    //流程编号 tiany
                    rs.executeSql("select * from workflow_code where flowId=" + templateid);

                    while (rs.next()) {
                        int codeformId = rs.getInt("formId");
                        int codeFieldId = rs.getInt("codeFieldId");
                        String isUse = rs.getString("isUse");
                        String currentCode = rs.getString("currentCode");
                        String isBill = rs.getString("isBill");
                        String workflowSeqAlone = rs.getString("workflowSeqAlone");
                        String dateSeqAlone = rs.getString("dateSeqAlone");
                        String dateSeqSelect = rs.getString("dateSeqSelect");
                        String fieldSequenceAlone = rs.getString("fieldSequenceAlone");
                        String struSeqAlone = rs.getString("struSeqAlone");
                        String struSeqSelect = rs.getString("struSeqSelect");
                        String correspondField = rs.getString("correspondField");
                        String correspondDate = rs.getString("correspondDate");
                        /*RecordSet rs1 = new RecordSet();
                        rs1.executeProc("SequenceIndex_SelectNextID",
                        		"Workflow_SubwfSetId");
                        if (rs1.next()) {
                        	workflowSubwfSetId = rs1.getInt(1);
                        }*/

                        sql = "insert into workflow_code(formId,flowId,codeFieldId,isUse,currentCode,isBill,workflowSeqAlone,dateSeqAlone,dateSeqSelect,fieldSequenceAlone,struSeqAlone,struSeqSelect,correspondDate) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, codeformId);
                        statement.setString(2, Util.null2String(returnValue));
                        statement.setInt(3, codeFieldId);
                        statement.setString(4, isUse);
                        statement.setString(5, currentCode);
                        statement.setString(6, isBill);
                        statement.setString(7, workflowSeqAlone);
                        statement.setString(8, dateSeqAlone);
                        statement.setString(9, dateSeqSelect);
                        statement.setString(10, fieldSequenceAlone);
                        statement.setString(11, struSeqAlone);
                        statement.setString(12, struSeqSelect);
                        statement.setString(13, correspondDate);
                        statement.executeUpdate();
                        
                        /*
                         RecordSet rs2 = new RecordSet();
                         rs2.executeSql("select * from workflow_codeSeq where formid ="
                         + formId + "isbill = "+ isBill);
                         while(rs2.next()){
                         int departmentId = rs1.getInt("departmentId");
                         int yearId = rs1.getInt("yearId");
                         String showType = rs1.getString("showType");
                         String codeValue = rs1.getString("codeValue");
                         String codeOrder = rs1.getString("codeOrder");
                         String isBill2 = rs1.getString("isBill");
                         String concreteField = rs1
                         .getString("concreteField");
                         String enablecode = rs1
                         .getString("enablecode");
                         sql = "insert into workflow_codeRegulate(formId,workflowid,showId,showType,codeValue,codeOrder,isBill,concreteField,enablecode) values(?,?,?,?,?,?,?,?,?)";
                         statement.setStatementSql(sql);
                         statement.setInt(1, formid2);
                         statement.setString(2, Util.null2String(returnValue));
                         statement.setInt(3, showId);
                         statement.setString(4, showType);
                         statement.setString(5, codeValue);
                         statement.setString(6, codeOrder);
                         statement.setString(7, isBill2);
                         statement.setString(8, concreteField);
                         statement.setString(9, enablecode);
                         statement.executeUpdate();		
                        
                         }*/

                    }
                    RecordSet rs1 = new RecordSet();
                    rs1.executeSql("select * from workflow_codeRegulate where workflowId =" + templateid);
                    while (rs1.next()) {
                        int formid2 = rs1.getInt("formId");
                        int showId = rs1.getInt("showId");
                        String showType = rs1.getString("showType");
                        String codeValue = rs1.getString("codeValue");
                        String codeOrder = rs1.getString("codeOrder");
                        String isBill2 = rs1.getString("isBill");
                        String concreteField = rs1.getString("concreteField");
                        String enablecode = rs1.getString("enablecode");
                        sql = "insert into workflow_codeRegulate(formId,workflowid,showId,showType,codeValue,codeOrder,isBill,concreteField,enablecode) values(?,?,?,?,?,?,?,?,?)";
                        statement.setStatementSql(sql);
                        statement.setInt(1, formid2);
                        statement.setString(2, Util.null2String(returnValue));
                        statement.setInt(3, showId);
                        statement.setString(4, showType);
                        statement.setString(5, codeValue);
                        statement.setString(6, codeOrder);
                        statement.setString(7, isBill2);
                        statement.setString(8, concreteField);
                        statement.setString(9, enablecode);
                        statement.executeUpdate();

                    }
                    
                    //流程编号 代字设置：选择框
                    sql =  "insert into workflow_shortNameSetting(workflowid,formid,isbill,fieldid,fieldvalue,shortnamesetting) select "+Util.null2String(returnValue)+",formid,isbill,fieldid,fieldvalue,shortnamesetting from workflow_shortNameSetting where workflowid = "+ templateid;
                    statement.setStatementSql(sql);
                    statement.executeUpdate();
                    
                    //流程编号 代字设置 部门
                    statement.setStatementSql("insert into workflow_deptAbbr(workflowid,formid,isbill,fieldid,fieldvalue,abbr,enabledeptcode) select "+Util.null2String(returnValue)+",formid,isbill,fieldid,fieldvalue,abbr,enabledeptcode from workflow_deptAbbr where workflowid = "+ templateid);
                    statement.executeUpdate();
                    
                    //流程编号 代字设置 分部
                    statement.setStatementSql("insert into workflow_subComAbbr(workflowid,formid,isbill,fieldid,fieldvalue,abbr,enablesubcode) select "+Util.null2String(returnValue)+",formid,isbill,fieldid,fieldvalue,abbr,enablesubcode from workflow_subComAbbr where workflowid = "+ templateid);
                    statement.executeUpdate();

                    // end
                    // 流程编号 end

                    //流程存为文档。

                    rs.execute("select * from WorkflowToDocProp where workflowid=" + templateid);
                    //	System.out.println("=====--sql------select * from WorkflowToDocProp where workflowid="+ templateid);
                    while (rs.next()) {
                        int wdp_id = Util.getIntValue(rs.getString("id"), 0);
                        //System.out.println("----wdp_id----------sql------====="+ wdp_id);
                        RecordSet rs0 = new RecordSet();
                        if (wdp_id > 0) {
                            sql = "insert into WorkflowToDocProp(workflowId,secCategoryId) select " + returnValue + ",secCategoryId from WorkflowToDocProp where id=" + wdp_id;
                            writeLog(sql);
                            rs0.execute(sql);
                            int wdocPropId = -1;
                            /*rs0
                            		.executeSql("select max(id) as maxId from WorkflowToDocProp");
                            if (rs0.next()) {
                            	wdocPropId = Util.getIntValue(rs0
                            			.getString("maxId"), -1);
                            			}*/
                            sql = "select max(id) as maxid from WorkflowToDocProp";
                            statement.setStatementSql(sql);
                            statement.executeQuery();
                            if (statement.next()) {
                                wdocPropId = statement.getInt("maxid");
                            }

                            if (wdocPropId > 0) {
                                sql = "insert into WorkflowToDocPropDetail(docPropId,docPropFieldId,workflowFieldId) select " + wdocPropId + ",docPropFieldId, workflowFieldId from WorkflowToDocPropDetail where docPropId=" + wdp_id;
                                rs0.execute(sql);
                            }

                        }
                    }
                    // 流程存为文档。 end

                }
            } finally {
                statement.close();
            }
            if (rs.getDBType().equalsIgnoreCase("ORACLE")) {
                sql = "select nodeid from workflow_flownode where nodetype='0' and workflowid=" + returnValue;
                writeLog("sql = " + sql);
                rs.execute(sql);
                while (rs.next()) {
                    int newnodeid = Util.getIntValue(rs.getString(1), 0);
                    RequestCheckUser cuser = new RequestCheckUser();
                    cuser.resetParameter();
                    cuser.setWorkflowid(returnValue);
                    cuser.setNodeid(newnodeid);
                    cuser.updateCreateList(0);
                }
            }
        }
        /*
         * mcw
         * 当另存一个新版本的流程时需要将流程转数据的相关配置也同时复制一份
         * */
        WorkflowToMode workflowtoMode = new WorkflowToMode();
        workflowtoMode.copyWorkflowToModeSet(templateid, returnValue, map, nodelinkmap);
        //当另存一个新版本的流程时需要将用车流程设置的相关配置也同时复制一份
        CarInfoManager carInfoManager=new CarInfoManager();
        carInfoManager.copyCarWrokflowSet(templateid, returnValue);
        return returnValue;
    }

	/**
	 * 保存流程的基本信息，如果是更新同时修改具体请求中的相关数据
	 * 
	 * @return 流程id
	 */
	public int setWfInfo() throws Exception {
	    checkSubCompanyId2();
		int returnValue = 0;
		// 微信提醒修改START(QC:98106) 增加3个参数值
        String insert_wf = "insert into workflow_base(workflowname,workflowdesc,workflowtype,formid,isbill,iscust,helpdocid,isvalid,needmark,messageType,multiSubmit,defaultName,docCategory,docPath,subcompanyid,mailMessageType,docRightByOperator,isTemplate,Templateid,catelogType,selectedCateLog,docRightByHrmResource,needaffirmance,isremarks,isannexUpload,annexdoccategory,isShowOnReportInput,isShowChart,orderbytype,isModifyLog,wfdocpath,wfdocowner,ShowDelButtonByReject,showUploadTab,isSignDoc,showDocTab,isSignWorkflow,showWorkflowTab,candelacc,isforwardrights,isimportwf,isrejectremind,ischangrejectnode,wfdocownertype,wfdocownerfieldid,newdocpath,issignview,isselectrejectnode,isImportDetail,specialApproval,Frequency,Cycle,forbidAttDownload,nosynfields,isneeddelacc,SAPSource,smsAlertsType,isSaveCheckForm,archiveNoMsgAlert,archiveNoMailAlert,forwardReceiveDef,fieldNotImport,dsporder,isfree,chatsType,chatsAlertType,notRemindifArchived,isshared,isoverrb,isoveriv,custompage,isAutoApprove,isAutoCommit,IsAutoRemark,hrmResourceShow,importReadOnlyField,submittype) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		// 微信提醒修改END(QC:98106)
		ConnStatement statement = null;
		statement = new ConnStatement();

		try {
			if (this.action.equalsIgnoreCase("addwf")) {
				statement.setStatementSql(insert_wf);
				statement.setString(1, this.wfname);
				statement.setString(2, this.wfdes);
				statement.setInt(3, this.typeid);
				statement.setInt(4, this.formid);
				statement.setString(5, this.isbill);
				statement.setString(6, this.iscust);
				statement.setInt(7, this.helpdocid);
				statement.setString(8, this.isvalid);
				statement.setString(9, this.needmark);
				statement.setString(10, this.messageType);
				statement.setString(11, this.multiSubmit);
				statement.setString(12, this.defaultName);
				statement.setString(13, this.docCategory);
				statement.setString(14, this.docPath);
				statement.setInt(15, this.subCompanyId2);
				statement.setString(16, this.mailMessageType);// added by xwj
																// for td2965
																// 20051101
				statement.setString(17, this.docRightByOperator);
				statement.setString(18, this.IsTemplate);
				statement.setInt(19, this.Templateid);
				statement.setInt(20, this.catelogType);
				statement.setInt(21, this.selectedCateLog);
				statement.setInt(22, this.docRightByHrmResource);
				statement.setString(23, this.isaffirmance);
				statement.setString(24, this.isremak);
				statement.setString(25, this.isannexUpload);
				statement.setString(26, this.annexdocCategory);
				statement.setString(27, this.isShowOnReportInput);
				statement.setString(28, this.isShowChart);
				statement.setString(29, this.orderbytype);
				statement.setString(30, this.isModifyLog);
				statement.setString(31, this.wfdocpath);
				statement.setString(32, this.wfdocowner);
				statement.setString(33, this.ShowDelButtonByReject);
				statement.setString(34, this.showUploadTab);
				statement.setString(35, this.isSignDoc);
				statement.setString(36, this.showDocTab);
				statement.setString(37, this.isSignWorkflow);
				statement.setString(38, this.showWorkflowTab);
				statement.setString(39, this.candelacc);
				statement.setString(40, this.isforwardrights);
				statement.setString(41, this.isimportwf);
				statement.setString(42, this.isrejectremind);
				statement.setString(43, this.ischangrejectnode);
				statement.setInt(44, Util.getIntValue(this.wfdocownertype, 0));
				statement.setInt(45, Util
						.getIntValue(this.wfdocownerfieldid, 0));
				statement.setString(46, newdocpath);
				statement.setInt(47, Util.getIntValue(this.issignview, 0));
				// td30785
				statement.setString(48, this.isselectrejectnode);
				statement.setString(49, this.isImportDetail);
				statement.setString(50, this.specialApproval);
				statement.setString(51, this.Frequency);
				statement.setString(52, this.Cycle);
				statement.setString(53, this.forbidAttDownload);
				statement.setString(54, this.nosynfields);
				statement.setString(55, this.isneeddelacc);
				statement.setString(56, this.SAPSource);
				statement.setString(57, this.smsAlertsType);
				statement.setString(58, this.isSaveCheckForm);
				statement.setString(59, this.archiveNoMsgAlert);
				statement.setString(60, this.archiveNoMailAlert);
				statement.setString(61, this.isForwardReceiveDef);
				statement.setString(62, this.fieldNotImport);
				statement.setInt(63, this.dsporder);
				statement.setString(64, this.isFree);

				// 微信提醒START(QC:98106)
				if (this.chatsType.equals("1")) {
					statement.setString(65, this.chatsType);
					statement.setString(66, this.chatsAlertType);
					statement.setString(67, this.notRemindifArchived);
				} else {
					statement.setString(65, this.chatsType);
					statement.setString(66, "0");
					statement.setString(67, "0");
				}
				statement.setString(68, this.isshared);
				statement.setString(69, "0");
				statement.setString(70, "0");
				statement.setString(71, this.custompage);
				statement.setString(72, this.isAutoApprove);
				statement.setString(73, this.isAutoCommit);
				statement.setString(74, this.isAutoRemark);
                statement.setInt(75, this.hrmResourceShow);
                statement.setString(76, this.importReadOnlyField);
                statement.setInt(77, submittype);
				// 微信提醒END(QC:98106)
				statement.executeUpdate();

				String select_wf = "select max(id) as maxid from workflow_base";
				statement.setStatementSql(select_wf);
				statement.executeQuery();
				if (statement.next()) {
					returnValue = statement.getInt("maxid");
				}
			} else if (this.action.equalsIgnoreCase("editwf")) {
                String update_wf = "update workflow_base set workflowname=?,workflowdesc=?,workflowtype=?,formid=?,isbill=?,iscust=?,helpdocid=?,isvalid=? , needmark=? ,messageType=? ,multiSubmit=? ,defaultName=? ,docCategory=? ,docPath=?,subcompanyid=?,mailMessageType=?,docRightByOperator=?,catelogType=?,selectedCateLog=?,docRightByHrmResource=?,needaffirmance=?,isremarks=?,isannexUpload=?,annexdoccategory=?,isShowOnReportInput=?, isShowChart=?, orderbytype=?, isModifyLog=?, wfdocpath=?,wfdocowner=?,ShowDelButtonByReject=?, showUploadTab=?, isSignDoc=?, showDocTab=?,isSignWorkflow=?,showWorkflowTab=?,candelacc=?,isforwardrights=?,isimportwf=?,isrejectremind=?,ischangrejectnode=?,wfdocownertype=?,wfdocownerfieldid=? ,newdocpath=? ,issignview=?,isselectrejectnode=? ,isImportDetail=? ,specialApproval=?,Frequency=?,Cycle=?,forbidAttDownload=?,nosynfields=?,isneeddelacc=?,SAPSource=?,smsAlertsType=?,isSaveCheckForm=?,archiveNoMsgAlert=?,archiveNoMailAlert=?,forwardReceiveDef=?,fieldNotImport=?,dsporder=?,chatsType=?,chatsAlertType=?,notRemindifArchived=?,isshared=?,isAutoApprove=?,isAutoCommit=?,isAutoRemark=?,hrmResourceShow=?,importReadOnlyField=?,submittype = ? where id=?";
				statement.setStatementSql(update_wf);
				statement.setString(1, this.wfname);
				statement.setString(2, this.wfdes);
				statement.setInt(3, this.typeid);
				statement.setInt(4, this.formid);
				statement.setString(5, this.isbill);
				statement.setString(6, this.iscust);
				statement.setInt(7, this.helpdocid);
				statement.setString(8, this.isvalid);
				statement.setString(9, this.needmark);
				statement.setString(10, this.messageType);
				statement.setString(11, this.multiSubmit);
				statement.setString(12, this.defaultName);
				statement.setString(13, this.docCategory);
				statement.setString(14, this.docPath);
				statement.setInt(15, this.subCompanyId2);
				statement.setString(16, this.mailMessageType);// added by xwj
																// for td2965
																// 20051101
				statement.setString(17, this.docRightByOperator);
				statement.setInt(18, this.catelogType);
				statement.setInt(19, this.selectedCateLog);
				statement.setInt(20, this.docRightByHrmResource);
				statement.setString(21, this.isaffirmance);
				statement.setString(22, this.isremak);
				statement.setString(23, this.isannexUpload);
				statement.setString(24, this.annexdocCategory);
				statement.setString(25, this.isShowOnReportInput);
				// statement.setInt(26, this.wfid);
				statement.setString(26, this.isShowChart);
				statement.setString(27, orderbytype);
				statement.setString(28, this.isModifyLog);
				statement.setString(29, this.wfdocpath);
				statement.setString(30, this.wfdocowner);
				statement.setString(31, this.ShowDelButtonByReject);
				statement.setString(32, this.showUploadTab);
				statement.setString(33, this.isSignDoc);
				statement.setString(34, this.showDocTab);
				statement.setString(35, this.isSignWorkflow);
				statement.setString(36, this.showWorkflowTab);
				statement.setString(37, this.candelacc);
				statement.setString(38, this.isforwardrights);
				statement.setString(39, this.isimportwf);
				statement.setString(40, this.isrejectremind);
				statement.setString(41, this.ischangrejectnode);
				statement.setInt(42, Util.getIntValue(this.wfdocownertype, 0));
				statement.setInt(43, Util
						.getIntValue(this.wfdocownerfieldid, 0));
				statement.setString(44, newdocpath);
				statement.setInt(45, Util.getIntValue(this.issignview, 0));
				statement.setString(46, this.isselectrejectnode);
				statement.setString(47, this.isImportDetail);

				statement.setString(48, this.specialApproval);
				statement.setString(49, this.Frequency);
				statement.setString(50, this.Cycle);
				statement.setString(51, this.forbidAttDownload);
				statement.setString(52, this.nosynfields);
				statement.setString(53, this.isneeddelacc);
				statement.setString(54, this.SAPSource);
				statement.setString(55, this.smsAlertsType);
				statement.setString(56, this.isSaveCheckForm);
				statement.setString(57, this.archiveNoMsgAlert);
				statement.setString(58, this.archiveNoMailAlert);
				statement.setString(59, this.isForwardReceiveDef);
				statement.setString(60, this.fieldNotImport);
				statement.setInt(61, this.dsporder);
				// 微信提醒START(QC:98106)
				if (this.chatsType.equals("1")) {
					statement.setString(62, this.chatsType);
					statement.setString(63, this.chatsAlertType);
					statement.setString(64, this.notRemindifArchived);
				} else {
					statement.setString(62, this.chatsType);
					statement.setString(63, "0");
					statement.setString(64, "0");
				}
				statement.setString(65, this.isshared);
				//statement.setString(66, this.isoverrb);
				//statement.setString(67, this.isoveriv);
				//statement.setString(66, this.custompage);
				statement.setString(66, this.isAutoApprove);
				statement.setString(67, this.isAutoCommit);
				statement.setString(68, this.isAutoRemark);
                statement.setInt(69, this.hrmResourceShow);
                statement.setString(70, this.importReadOnlyField);
                statement.setInt(71, submittype);
                statement.setInt(72, this.wfid);
				// 微信提醒END(QC:98106)
				statement.executeUpdate();

				if (oldtypeid != typeid) {
					statement
							.setStatementSql(" update workflow_CurrentOperator set workflowtype = ? where workflowid = ? ");
					statement.setInt(1, this.typeid);
					statement.setInt(2, this.wfid);
					statement.executeUpdate();
				}

				returnValue = this.wfid;
			}
		} catch (Exception e) {
			writeLog(e);
			throw e;
		} finally {
			try {
				statement.close();
			} catch (Exception ex) {
			}
		}

		if (this.action.equalsIgnoreCase("editwf")) {
			// 同步所有版本流程名称
		    WorkflowVersion.updateAllVersionName(this.wfid + "", wfname,this.typeid,this.subCompanyId2);
		}
		if (this.action.equalsIgnoreCase("editwf")) {
	        //根据流程id插入督办权限表
	        WFUrgerManager wFUrgerManager = new WFUrgerManager();
	        wFUrgerManager.insertUrgerByWfid(wfid);
		}
        
		return returnValue;
	}
	
	private void checkSubCompanyId2(){
	    if(this.subCompanyId2 == -1){
	        this.writeLog(">>>>>>>>>>>>>>>>>>>>>>>>>>111111111111111>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>wfid:"+this.wfid+",action:"+this.action+",subcompanyid:"+this.subCompanyId2);
	        RecordSet rs = new RecordSet();
	        rs.executeQuery("select subcompanyid from workflow_base where id= ?",this.wfid);
	        if(rs.next()){
	            this.setSubCompanyId2(rs.getInt(1));
	        }
	        this.writeLog(">>>>>>>>>>>>>>>>>>>>>>>>>>222222222222222>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>wfid:"+this.wfid+",action:"+this.action+",subcompanyid:"+this.subCompanyId2);
	    }
	}

	/**
	 * 门户相关信息 workflow_groupdetail中type类型为20～25为门户操作组信息
	 */
	public void clearWFCRM() throws Exception {
		String sql = "select nodeid from workflow_flownode where workflowid="
				+ this.wfid;
		RecordSet rs = new RecordSet();
		rs.executeSql(sql);
		while (rs.next()) {
			String nodeid = rs.getString("nodeid");
			RecordSet rs1 = new RecordSet();
			sql = "select id from workflow_nodegroup where nodeid=" + nodeid;
			rs1.executeSql(sql);
			while (rs1.next()) {
				int groupid = rs1.getInt("id");
				RecordSet rs2 = new RecordSet();
				sql = "delete from workflow_groupdetail where type>19 and type<26 and groupid="
						+ groupid;
				rs2.executeSql(sql);
				String nodetype = "";
				rs2.executeProc("workflow_NodeType_Select", "" + this.wfid
						+ Util.getSeparator() + nodeid);
				if (rs2.next()) {
					nodetype = rs2.getString("nodetype");
				}
				if (nodetype.equals("0")) {
					RequestCheckUser cuser = new RequestCheckUser();
					cuser.resetParameter();
					cuser.setWorkflowid(this.wfid);
					cuser.setNodeid(Util.getIntValue(nodeid));
					cuser.updateCreateList(groupid);
				}
				sql = "select count(*) from workflow_groupdetail where groupid="
						+ groupid;
				rs2.executeSql(sql);
				int detailnum = 0;
				if (rs2.next()) {
					detailnum = rs2.getInt(1);
				}
				if (detailnum < 1) {
					sql = "delete from workflow_nodegroup where id=" + groupid
							+ " and nodeid=" + nodeid;
					//rs2.executeSql(sql);
				}
			}
		}
	}

	/**
	 * 清除附加操作中关联的字段信息、字段显示信息及模板信息、出口条件信息,操作组字段权限type=5~16,31~35,38,42~48
	 */
	public void clearWFFormInfo() throws Exception {
		RecordSet rs = new RecordSet();
		// 清除出口条件信息
		String sql = "update workflow_nodelink set condition='',conditioncn='',newrule='',rulerelationship='' where workflowid="
				+ this.wfid;
//		ConnStatement statement = new ConnStatement();
		if (rs.getDBType().equals("oracle"))
			sql = "update workflow_nodelink set condition=empty_clob(),conditioncn=empty_clob(),newrule='',rulerelationship='' where workflowid="
					+ this.wfid;
		rs.executeSql(sql);
//		if (statement != null)
//			statement.close();
		// 清楚规则关联
		sql = "delete rule_maplist where wfid=" + this.wfid;
		rs.executeSql(sql);
		// 清除出口条件信息end
		// 清除附加操作中关联的字段信息
		sql = "delete from workflow_addinoperate where (fieldid>0 or fieldop1id>0 or fieldop2id>0) and workflowid="
				+ this.wfid;
		rs.executeSql(sql);
		// 清除附加操作中关联的字段信息end
		// 清除字段显示信息(模板信息)
		sql = "delete from workflow_nodemode where workflowid=" + this.wfid;
		rs.executeSql(sql);
		// 清除流程标题字段
		sql = "delete from workflow_TitleSet where flowId =" + this.wfid;
		rs.executeSql(sql);
		// 清除流程编号字段
		sql = "delete from  workflow_code  where flowId=" + this.wfid;
		rs.executeSql(sql);
		// 清除督办信息
		sql = "delete from  workflow_urgerdetail  where workflowid="
				+ this.wfid;
		rs.executeSql(sql);
		// 清除字段显示信息(模板信息)end
		sql = "select nodeid from workflow_flownode where workflowid="
				+ this.wfid;
		rs.executeSql(sql);
		while (rs.next()) {
			String nodeid = rs.getString("nodeid");
			RecordSet rs1 = new RecordSet();
			// 清除操作组字段权限
			sql = "select id from workflow_nodegroup where nodeid=" + nodeid;
			rs1.executeSql(sql);
			while (rs1.next()) {
				int groupid = rs1.getInt("id");
				RecordSet rs2 = new RecordSet();
				sql = "delete from workflow_groupdetail where (((type>4 and type<17) or (type>30 and type<36) or (type>41 and type<49) or type=38) or conditions is not null) and groupid="
						+ groupid;
				rs2.executeSql(sql);
				String nodetype = "";
				rs2.executeProc("workflow_NodeType_Select", "" + this.wfid
						+ Util.getSeparator() + nodeid);
				if (rs2.next()) {
					nodetype = rs2.getString("nodetype");
				}
				if (nodetype.equals("0")) {
					RequestCheckUser cuser = new RequestCheckUser();
					cuser.resetParameter();
					cuser.setWorkflowid(this.wfid);
					cuser.setNodeid(Util.getIntValue(nodeid));
					cuser.updateCreateList(groupid);
				}
				sql = "select count(*) from workflow_groupdetail where groupid="
						+ groupid;
				rs2.executeSql(sql);
				int detailnum = 0;
				if (rs2.next()) {
					detailnum = rs2.getInt(1);
				}
				if (detailnum < 1) {
					sql = "delete from workflow_nodegroup where id=" + groupid
							+ " and nodeid=" + nodeid;
					rs2.executeSql(sql);
				}
			}
			// 清除操作组字段权限end
			// 清除字段显示信息(一般信息)
			sql = "delete from workflow_nodeform where nodeid=" + nodeid;
			rs1.executeSql(sql);
			// 清除字段显示信息(一般信息)end
			// 清除字段显示信息(图形化表单)
			sql = "delete from workflow_modeview where nodeid=" + nodeid;
			rs1.executeSql(sql);
			// 清除字段显示信息(图形化表单)end
			// 清空Html模式相关信息Start
			sql = "delete from workflow_nodefieldattr where nodeid=" + nodeid;
			rs1.execute(sql);
			sql = "delete from workflow_nodehtmllayout where nodeid=" + nodeid;
			rs1.execute(sql);
			// 清空Html模式相关信息End
			// 插入新的表单节点信息
			addWFFieldInfoByNode(nodeid);
			// 插入新的表单节点信息end
		}
	}

	/**
	 * 增加新表单字段信息
	 */
	public void addWFFieldInfo() throws Exception {
		String sql = "select nodeid from workflow_flownode where workflowid="
				+ this.wfid;
		RecordSet rs = new RecordSet();
		rs.executeSql(sql);
		while (rs.next()) {
			String nodeid = rs.getString("nodeid");
			// 插入新的表单节点信息
			addWFFieldInfoByNode(nodeid);
			// 插入新的表单节点信息end
		}
	}

	/**
	 * 增加新表单某节点字段信息
	 * 
	 * @param nodeid
	 *            节点ID
	 */
	public void addWFFieldInfoByNode(String nodeid) throws Exception {
		if (!this.isbill.equals("3") && this.formid > 0) {
			String sql = "";
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			if (this.isbill.equals("0")) {
				// 获得表单字段
				sql = "select fieldid from workflow_formfield where formid="
						+ this.formid + " order by isdetail,fieldorder";
				rs.executeSql(sql);
				while (rs.next()) {
					String fieldid = rs.getString("fieldid");
					sql = "insert into workflow_nodeform(nodeid,fieldid,isview,isedit,ismandatory) values("
							+ nodeid + "," + fieldid + ",'1','0','0')";
					rs1.executeSql(sql);
				}
			} else {
				// 获得单据字段
				sql = "select id from workflow_billfield where billid="
						+ this.formid
						+ " order by viewtype,detailtable,dsporder";
				rs.executeSql(sql);
				while (rs.next()) {
					String fieldid = rs.getString("id");
					sql = "insert into workflow_nodeform(nodeid,fieldid,isview,isedit,ismandatory) values("
							+ nodeid + "," + fieldid + ",'1','0','0')";
					rs1.executeSql(sql);
				}
			}
		}
	}

	/**
	 * 得到是否已经创建工作流
	 * 
	 * @return 返回 是否已经创建工作流
	 */
	public int getIsused() {
		return isused;
	}

	/**
	 * 设置是否已经创建工作流
	 * 
	 * @param isused
	 *            是否已经创建工作流
	 */
	public void setIsused(int isused) {
		this.isused = isused;
	}

	/**
	 * 得到流程文档目录
	 * 
	 * @return 返回 流程文档目录
	 */
	public String getDocCategory() {
		return docCategory;
	}

	/**
	 * 得到是否允许签字意见上传附件
	 * 
	 * @return 返回 是否允许签字意见上传附件
	 */
	public String getIsAnnexUpload() {
		return isannexUpload;
	}

	/**
	 * 得到是否允许创建人删除附件
	 * 
	 * @return 返回 是否允许创建人删除附件
	 */
	public String getCanDelAcc() {
		return candelacc;
	}

	/**
	 * 得到流程签字意见附件文档目录
	 * 
	 * @return 返回 流程签字意见附件文档目录
	 */
	public String getAnnexDocCategory() {
		return annexdocCategory;
	}

	/**
	 * 设置流程文档目录用
	 * 
	 * @param docCategory
	 *            流程文档目录
	 */
	public void setDocCategory(String docCategory) {
		this.docCategory = docCategory;
	}

	/**
	 * 设置是否允许创建人删除附件
	 * 
	 * @param candelacc
	 *            是否允许创建人删除附件
	 */
	public void setCanDelAcc(String candelacc) {
		this.candelacc = candelacc;
	}

	/**
	 * 设置是否允许签字意见上传附件
	 * 
	 * @param IsAnnexUpload
	 *            是否允许签字意见上传附件
	 */
	public void setIsAnnexUpload(String IsAnnexUpload) {
		this.isannexUpload = IsAnnexUpload;
	}

	/**
	 * 设置流程签字意见附件文档目录用
	 * 
	 * @param docCategory
	 *            流程签字意见附件文档目录
	 */
	public void setAnnexDocCategory(String docCategory) {
		this.annexdocCategory = docCategory;
	}

	/**
	 * 得到流程文档路径
	 * 
	 * @return 返回 流程文档路径
	 */
	public String getDocPath() {
		return docPath;
	}

	/**
	 * 设置流程文档路径
	 * 
	 * @param docPath
	 *            流程文档路径
	 */
	public void setDocPath(String docPath) {
		this.docPath = docPath;
	}

	/**
	 * 设置分部ID
	 * 
	 * @param id
	 *            分部ID
	 */
	public void setSubCompanyId2(int id) {
		this.subCompanyId2 = id;
	}

	/**
	 * 得到分部ID
	 * 
	 * @return 分部ID
	 */
	public int getSubCompanyId2() {
		return this.subCompanyId2;
	}

	/**
	 * 得到是否跟随文档关联人赋权
	 * 
	 * @return 是否跟随文档关联人赋权
	 */
	public String getDocRightByOperator() {
		return docRightByOperator;
	}

	/**
	 * 设置是否跟随文档关联人赋权
	 * 
	 * @param docRightByOperator
	 *            是否跟随文档关联人赋权
	 */
	public void setDocRightByOperator(String docRightByOperator) {
		this.docRightByOperator = docRightByOperator;
	}

	/**
	 * 得到流程树
	 * 
	 * @param subcompay
	 *            分部
	 * @param isTemplate
	 *            是否流程模板
	 * @return 流程树字符串
	 */
	public String getWrokflowTree(int subcompay, String isTemplate) {
		String trees = "";
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		String template = "";
		String sql = "";
		String subCompanys = subcompay + "";
		try {
			SubCompanyComInfo companyinfo = new SubCompanyComInfo();
			ArrayList sublist = new ArrayList();
			ArrayList subcompanylist = companyinfo.getSubCompanyLists(subcompay
					+ "", sublist);

			for (int i = 0; i < sublist.size(); i++) {
				subCompanys += "," + subcompanylist.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isTemplate.equals("1")) {
			template = " and istemplate='1' ";
		} else {
			template = " and (istemplate is null or istemplate<>'1') ";
		}
		rs
				.executeSql("select id,typename from workflow_type order by dsporder");
		while (rs.next()) {
			int typeid = Util.getIntValue(rs.getString("id"));
			String typename = rs.getString("typename");
			trees += "['<img src=\\\"/LeftMenu/ThemeXP/folder2_wev8.gif\\\">','"
					+ typename
					+ "','/workflow/workflow/managewf.jsp?isTemplate="
					+ isTemplate
					+ "&typeid="
					+ typeid
					+ "&subCompanyId="
					+ subcompay + "','wfmainFrame','" + typename + "'";
			sql = "select id,workflowname from workflow_base where subcompanyid in("
					+ subCompanys
					+ ")"
					+ template
					+ " and workflowtype="
					+ typeid;
			rs1.executeSql(sql);
			while (rs1.next()) {
				trees += ",['<img src=\\\"/LeftMenu/ThemeXP/page_wev8.gif\\\">','"
						+ rs1.getString("workflowname")
						+ "','/workflow/workflow/addwf.jsp?src=editwf&wfid="
						+ rs1.getString("id")
						+ "&isTemplate="
						+ isTemplate
						+ "','wfmainFrame','"
						+ rs1.getString("workflowname")
						+ "']";
			}
			trees += "],";
		}
		if (trees.endsWith(",")) {
			return trees.substring(0, trees.length() - 1);
		} else {
			return trees;
		}
	}

	/**
	 * 得到限制重复提交功能是否启用
	 * 
	 * @return 限制重复提交功能是否启用判断
	 */
	public int getIsUse() {
		return isUse;
	}

	/**
	 * 设置限制重复提交功能是否启用
	 * 
	 * @param isUse
	 *            限制重复提交功能是否启用
	 */
	public void setIsUse(int isUse) {
		this.isUse = isUse;
	}

	/**
	 * 得到时间类型
	 * 
	 * @return 时间类型
	 */
	public int getTtype() {
		return Ttype;
	}

	/**
	 * 设置时间类型
	 * 
	 * @param ttype
	 *            时间类型
	 */
	public void setTtype(int ttype) {
		Ttype = ttype;
	}

	/**
	 * 得到用户类型
	 * 
	 * @return 用户类型
	 */
	public int getUtype() {
		return Utype;
	}

	/**
	 * 设置用户类型
	 * 
	 * @param utype
	 *            用户类型
	 */
	public void setUtype(int utype) {
		Utype = utype;
	}

	/**
	 * 得到附件上传目录类型 0：固定目录 1：选择目录
	 * 
	 * @return 附件上传目录类型 0：固定目录 1：选择目录
	 */
	public int getCatelogType() {
		return catelogType;
	}

	/**
	 * 设置附件上传目录类型 0：固定目录 1：选择目录
	 * 
	 * @param catelogType
	 *            附件上传目录类型 0：固定目录 1：选择目录
	 */
	public void setCatelogType(int catelogType) {
		this.catelogType = catelogType;
	}

	/**
	 * 得到所选择目录的对应的id
	 * 
	 * @return 所选择目录的对应的id
	 */
	public int getSelectedCateLog() {
		return selectedCateLog;
	}

	/**
	 * 设置所选择目录的对应的id
	 * 
	 * @param selectedCateLog
	 *            所选择目录的对应的id
	 */
	public void setSelectedCateLog(int selectedCateLog) {
		this.selectedCateLog = selectedCateLog;
	}

	/**
	 * 得到是否按人力资源字段附权的值.
	 * 
	 * @return docRightByHrmResource
	 */
	public int getDocRightByHrmResource() {
		return docRightByHrmResource;
	}

	/**
	 * 设置是否按人力资源字段附权的值.
	 * 
	 * @param docRightByHrmResource
	 */
	public void setDocRightByHrmResource(int docRightByHrmResource) {
		this.docRightByHrmResource = docRightByHrmResource;
	}

    /**
     * 获取人力资源条件显示是否显示安全级别
     * @return
     */
    public int getHrmResourceShow() {
        return hrmResourceShow;
    }

    /**
     * 设置人力资源条件显示是否显示安全级别
     * @param hrmResourceShow
     */
    public void setHrmResourceShow(int hrmResourceShow) {
        this.hrmResourceShow = hrmResourceShow;
    }
    
    /**
     * 得到标题字段id.
	 * 
	 * @return titleFieldId
	 */
	public int getTitleFieldId() {
		return titleFieldId;
	}

	/**
	 * 设置标题字段id.
	 * 
	 * @param titleFieldId
	 */
	public void setTitleFieldId(int titleFieldId) {
		this.titleFieldId = titleFieldId;
	}

	/**
	 * 得到主题词字段id.
	 * 
	 * @return keywordFieldId
	 */
	public int getKeywordFieldId() {
		return keywordFieldId;
	}

	/**
	 * 设置主题词字段id.
	 * 
	 * @param keywordFieldId
	 */
	public void setKeywordFieldId(int keywordFieldId) {
		this.keywordFieldId = keywordFieldId;
	}

	public String getIsShowChart() {
		return isShowChart;
	}

	public void setIsShowChart(String isShowChart) {
		this.isShowChart = isShowChart;
	}

	public String getOrderbytype() {
		return orderbytype;
	}

	public void setOrderbytype(String orderbytype) {
		this.orderbytype = orderbytype;
	}

	public String getWfdocpath() {
		return wfdocpath;
	}

	public void setWfdocpath(String wfdocpath) {
		this.wfdocpath = wfdocpath;
	}

	public String getWfdocownertype() {
		return wfdocownertype;
	}

	public void setWfdocownertype(String wfdocownertype) {
		this.wfdocownertype = wfdocownertype;
	}

	public String getWfdocownerfieldid() {
		return wfdocownerfieldid;
	}

	public void setWfdocownerfieldid(String wfdocownerfieldid) {
		this.wfdocownerfieldid = wfdocownerfieldid;
	}

	public String getCycle() {
		return Cycle;
	}

	public void setCycle(String cycle) {
		Cycle = cycle;
	}

	public String getFrequency() {
		return Frequency;
	}

	public void setFrequency(String frequency) {
		Frequency = frequency;
	}

	/**
	 * 得到是否禁止流程附件批量下载的值
	 * 
	 * @return forbidAttDownload
	 */
	public String getForbidAttDownload() {
		return forbidAttDownload;
	}

	/**
	 * 设置是否禁止流程附件批量下载
	 * 
	 * @param forbidAttDownload
	 *            是否禁止流程附件批量下载
	 */
	public void setForbidAttDownload(String forbidAttDownload) {
		this.forbidAttDownload = forbidAttDownload;
	}

	public String getIsneeddelacc() {
		return isneeddelacc;
	}

	public void setIsneeddelacc(String isneeddelacc) {
		this.isneeddelacc = isneeddelacc;
	}

	public String getSAPSource() {
		return SAPSource;
	}

	public void setSAPSource(String source) {
		SAPSource = source;
	}

	public String getIsForwardReceiveDef() {
		return isForwardReceiveDef;
	}

	public void setIsForwardReceiveDef(String isForwardReceiveDef) {
		this.isForwardReceiveDef = isForwardReceiveDef;
	}

	// 微信提醒START(QC:98106)
	public String getChatsType() {
		return chatsType;
	}

	public void setChatsType(String chatsType) {
		this.chatsType = chatsType;
	}

	public String getChatsAlertType() {
		return chatsAlertType;
	}

	public void setChatsAlertType(String chatsAlertType) {
		this.chatsAlertType = chatsAlertType;
	}

	public String getNotRemindifArchived() {
		return notRemindifArchived;
	}

	public void setNotRemindifArchived(String notRemindifArchived) {
		this.notRemindifArchived = notRemindifArchived;
	}

	// 微信提醒END(QC:98106)

	private void copyHtmlLayout(int startnodeid, int newnodeid, int newwfid, ArrayList oldnodeidlist, ArrayList nodeidlist, WFNodeFieldManager wFNodeFieldManager) {
		ConnStatement statement = null;
		ConnStatement statement1 = null;
		try {
		    statement = new ConnStatement();
		    statement1 = new ConnStatement();
			String sql = "select datajson,pluginjson,scripts,workflowid,formid,isbill,type,layoutname,syspath,cssfile,htmlparsescheme,version,operuser,isactive from workflow_nodehtmllayout where nodeid="
					+ startnodeid + " order by id";
			statement1.setStatementSql(sql);
			statement1.executeQuery();
			String layoutname_tmp = "", syspath_tmp = "", cssfile_tmp = "", operuser_tmp = "";
			String datajson = "", pluginjson = "", scripts = "";
			int formid_tmp, isbill_tmp, type_tmp, version_tmp, htmlparsescheme_tmp,isactive_tmp;
			// 显示模板及打印模板
			while (statement1.next()) {
				formid_tmp = Util.getIntValue(statement1.getString("formid"), 0);
				isbill_tmp = Util.getIntValue(statement1.getString("isbill"), 0);
				type_tmp = Util.getIntValue(statement1.getString("type"), 0);
				layoutname_tmp = Util.null2String(statement1.getString("layoutname"));
				cssfile_tmp = Util.null2String(statement1.getString("cssfile"));
				htmlparsescheme_tmp = Util.getIntValue(statement1.getString("htmlparsescheme"), 0);
				version_tmp = Util.getIntValue(statement1.getString("version"),0);
				operuser_tmp = Util.null2String(statement1.getString("operuser"));
				datajson = statement1.getString("datajson");
				pluginjson = statement1.getString("pluginjson");
				scripts = statement1.getString("scripts");
				isactive_tmp = Util.getIntValue(statement1.getString("isactive"),0);
				if(version_tmp == 0 || version_tmp == 1){
					syspath_tmp = Util.null2String(statement1.getString("syspath"));
					syspath_tmp = wFNodeFieldManager.copyAndChangeHtmlFile(syspath_tmp, oldnodeidlist, nodeidlist, newnodeid, newwfid, type_tmp);
				}else if(version_tmp == 2 && !"".equals(datajson)){
					syspath_tmp = "";
					//处理datajson，模板内节点信息需替换
					for (int i = 0; i < oldnodeidlist.size(); i++) {
	                    String _oldnodeid = Util.null2String(oldnodeidlist.get(i));
	                    String _newnodeid = Util.null2String(nodeidlist.get(i));
	                    datajson = datajson.replaceAll("\"etype\":\"4\",\"field\":\""+_oldnodeid+"\"", "\"etype\":\"4\",\"field\":\""+_newnodeid+"\"");
						datajson = datajson.replaceAll("\"etype\":\"5\",\"field\":\""+_oldnodeid+"\"", "\"etype\":\"5\",\"field\":\""+_newnodeid+"\"");
					}
				}

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentDate = formatter.format(new Date());
				// 插入
				sql = "insert into workflow_nodehtmllayout(workflowid,formid,isbill,nodeid,type,layoutname,syspath,cssfile,htmlParseScheme,version,operuser,opertime,datajson,pluginjson,scripts,isactive) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				statement.setStatementSql(sql);
				statement.setInt(1, newwfid);
				statement.setInt(2, formid_tmp);
				statement.setInt(3, isbill_tmp);
				statement.setInt(4, newnodeid);
				statement.setInt(5, type_tmp);
				statement.setString(6, layoutname_tmp);
				statement.setString(7, syspath_tmp);
				statement.setString(8, cssfile_tmp);
				statement.setInt(9, htmlparsescheme_tmp);
				statement.setInt(10, version_tmp);
				statement.setString(11, operuser_tmp);
				statement.setString(12, currentDate);
				statement.setString(13, datajson);
				statement.setString(14, pluginjson);
				statement.setString(15, scripts);
				statement.setInt(16, isactive_tmp);
				statement.executeUpdate();
			}
		} catch (Exception e) {
			writeLog(e);
		} finally {
			try {
				statement.close();
				statement1.close();
			} catch (Exception e) {
				writeLog(e);
			}
		}
	


	}

	public void save(RecordSet rs,ConnStatement statement,String sql,String tablename,int returnValue) throws Exception {
		rs.executeSql(sql);
		//RecordSet rs2 = new RecordSet();
		//rs2.executeSql("select * from ")
		String[] cns=rs.getColumnName();
	    int[] cts = rs.getColumnType();
	   
	    while(rs.next()){
			 String sql1 = "insert into "+tablename+" ( ";
	    String sql2 = " ) values (";
				String oldnodeid = rs.getString("nodeid");
						//oldnodeidlist.add(oldnodeid);
						String nodename = rs.getString("nodename");
						String isstart = rs.getString("isstart");
						String isreject = rs.getString("isreject");
						String isreopen = rs.getString("isreopen");
						String isend = rs.getString("isend");
						int drawxpos = rs.getInt("drawxpos");
						int drawypos = rs.getInt("drawypos");
						int totalgroups = rs.getInt("totalgroups");
						String nodetype = rs.getString("nodetype");
						// int isFutureViewer=rs.getInt("isFutureViewer");
						// int isHistoryViewer=rs.getInt("isHistoryViewer");
						String viewnodeids = rs.getString("viewnodeids");
						//oldnodeloglist.add(viewnodeids);
						String ismode = rs.getString("ismode");
						String showdes = rs.getString("showdes");
						String printdes = rs.getString("printdes");
						String Freefs = rs.getString("Freefs");
						String nodeattribute = rs.getString("nodeattribute");
						int passnum = rs.getInt("passnum");
						String viewtypeall = rs.getString("viewtypeall");
						String viewdescall = rs.getString("viewdescall");
						String showtype = rs.getString("showtype");
						String vtapprove = rs.getString("vtapprove");
						String vtrealize = rs.getString("vtrealize");
						String vtforward = rs.getString("vtforward");
						String vtpostil = rs.getString("vtpostil");
						String vtHandleForward = rs
								.getString("vtHandleForward"); // 转办
						String vtTakingOpinions = rs
								.getString("vtTakingOpinions"); // 征求意见
						String vttpostil = rs.getString("vttpostil");
						String vtrecipient = rs.getString("vtrecipient");
						String vtrpostil = rs.getString("vtrpostil");
						String vtreject = rs.getString("vtreject");
						String vtsuperintend = rs.getString("vtsuperintend");
						String vtover = rs.getString("vtover");
						String vdcomments = rs.getString("vdcomments");
						String vddeptname = rs.getString("vddeptname");
						String vdoperator = rs.getString("vdoperator");
						String vddate = rs.getString("vddate");
						String vdtime = rs.getString("vdtime");
						String nodetitle = rs.getString("nodetitle");
						String isFormSignature = rs
								.getString("isFormSignature");
						String IsPendingForward = rs
								.getString("IsPendingForward");
						String IsWaitForwardOpinion = rs
								.getString("IsWaitForwardOpinion");
						String IsBeForward = rs.getString("IsBeForward");
						String IsSubmitedOpinion = rs
								.getString("IsSubmitedOpinion");
						String IsSubmitForward = rs
								.getString("IsSubmitForward");
						String formSignatureWidth = rs
								.getString("formSignatureWidth");
						String formSignatureHeight = rs
								.getString("formSignatureHeight");
						String IsFreeWorkflow = rs.getString("IsFreeWorkflow");
						String IsFreeNode = rs.getString("IsFreeNode");
						String freewfsetcurnamecn = rs
								.getString("freewfsetcurnamecn");
						String freewfsetcurnameen = rs
								.getString("freewfsetcurnameen");
						String freewfsetcurnametw = rs
								.getString("freewfsetcurnametw");
						String stnull = rs.getString("stnull");
						String toexcel = rs.getString("toexcel");
						String issignmustinput = rs
								.getString("issignmustinput");
						String isfeedback = rs.getString("isfeedback");
						String isnullnotfeedback = rs
								.getString("isnullnotfeedback");
						String rejectbackflag = rs.getString("rejectbackflag");
						String drawbackflag = rs.getString("drawbackflag");
						String vsignupload = rs.getString("vsignupload");
						String vsigndoc = rs.getString("vsigndoc");
						String vsignworkflow = rs.getString("vsignworkflow");
						String IsBeForwardSubmit = rs
								.getString("IsBeForwardSubmit");
						String IsBeForwardModify = rs
								.getString("IsBeForwardModify");
						String IsBeForwardPending = rs
								.getString("IsBeForwardPending");
						String IsShowPendingForward = rs
								.getString("IsShowPendingForward");
						String IsShowWaitForwardOpinion = rs
								.getString("IsShowWaitForwardOpinion");
						String IsShowBeForward = rs
								.getString("IsShowBeForward");
						String IsShowSubmitedOpinion = rs
								.getString("IsShowSubmitedOpinion");
						String IsShowSubmitForward = rs
								.getString("IsShowSubmitForward");
						String IsShowBeForwardSubmit = rs
								.getString("IsShowBeForwardSubmit");
						String IsShowBeForwardModify = rs
								.getString("IsShowBeForwardModify");
						String IsShowBeForwardPending = rs
								.getString("IsShowBeForwardPending");

					/*	 sql="insert into workflow_nodebase(nodename,isstart,isreject,isreopen,isend,drawxpos,drawypos,totalgroups,nodeattribute,passnum,IsFreeNode) values(?,?,?,?,?,?,?,?,?,?,?)";
						 statement.setStatementSql(sql);
						 statement.setString(1,nodename);
						 statement.setString(2,isstart);
						 statement.setString(3,isreject);
						 statement.setString(4,isreopen);
						 statement.setString(5,isend);
						 statement.setInt(6,drawxpos);
						 statement.setInt(7,drawypos);
						 statement.setInt(8,totalgroups);
						 statement.setString(9,nodeattribute);
						 statement.setInt(10,passnum);
						 statement.setString(11,IsFreeNode);
						 statement.executeUpdate();
						 sql = "select max(id) as maxid from workflow_nodebase";
						 statement.setStatementSql(sql);
						 statement.executeQuery();
*/
						WFFreeFlowManager wfffmanager = new WFFreeFlowManager();
						int nodeid = wfffmanager.getNodeNewId(nodename,
								drawxpos, drawypos, passnum, isstart, isreject,
								isreopen, isend, totalgroups, nodeattribute,
								IsFreeNode);
	    	for(int i=0;i<cns.length;i++){
	    		if(!"id".equals(cns[i])){
	    		sql1+= cns[i]+",";
	    		if("workflowid".equals(cns[i])){
	    			sql2 += returnValue+",";
	    		}else if("nodeid".equals(cns[i])){
	    			sql2 += nodeid+",";
	    		}else{
	    		sql2 += "'"+rs.getString(cns[i])+"',";
	    		}
				}
	    	}
	    	if(sql1.endsWith(",")){
	    		sql1 = sql1.substring(0,sql1.length()-1);
	    	}
	    	if(sql2.endsWith(",")){
	    		sql2 = sql2.substring(0,sql2.length()-1);
	    	}
	    		sql2 += ")";
	    		//System.out.println("----sql1+sql2---==="+sql1+sql2);

	    			rs.executeSql(sql1+sql2);
	    }
	}



		public void save2(RecordSet rs,String sql,String tablename,int returnValue) {
		rs.executeSql(sql);
		//RecordSet rs2 = new RecordSet();
		//rs2.executeSql("select * from ")
		String[] cns=rs.getColumnName();
	    int[] cts = rs.getColumnType();
	   
	    while(rs.next()){
			 String sql1 = "insert into "+tablename+" ( ";
	    String sql2 = " ) values (";
				
	    	for(int i=0;i<cns.length;i++){
	    		if(!"id".equals(cns[i])){
	    		sql1+= cns[i]+",";
	    		if("workflowid".equals(cns[i])){
	    			sql2 += returnValue+",";
	    		}else{
	    		sql2 += "'"+rs.getString(cns[i])+"',";
	    		}
				}
	    	}
	    	if(sql1.endsWith(",")){
	    		sql1 = sql1.substring(0,sql1.length()-1);
	    	}
	    	if(sql2.endsWith(",")){
	    		sql2 = sql2.substring(0,sql2.length()-1);
	    	}
	    		sql2 += ")";
	    		//System.out.println("----sql1+sql2---==="+sql1+sql2);

	    			rs.executeSql(sql1+sql2);
	    }
	}

	private String convertOpenTextNodesInfo(Map<String,String> nodeIdMap,String sourceNodeIds) {
		if(null == sourceNodeIds || "".equals(sourceNodeIds.trim())) {
			return "";
		}
		StringBuffer bufNodeIds = new StringBuffer();
		String[] oldNodeIds = sourceNodeIds.split(",");
		for(int i = 0; i < oldNodeIds.length; i++) {
			bufNodeIds.append(nodeIdMap.get(oldNodeIds[i]));
			if(i != oldNodeIds.length - 1) {
				bufNodeIds.append(",");
			}
		}
		return bufNodeIds.toString();
	}
	
	
	/**
     * 判断当为选择目录时附件上传目录是否设置完全
     * @param workflowid 流程ID
     * @return 是否设置完全
     */
    public static boolean hasUsedType(int workflowid) {
        boolean isuse = true;
        StringBuffer sb = new StringBuffer().append(
                "select a.* from workflow_selectitem a ")
                .append(" left join workflow_base b ")
                .append(" on a.fieldid = b.selectedCateLog ")
                .append(" where (a.docPath is null or a.docCategory is null ")
                .append(" or a.docPath='' or a.docCategory='') ")
                .append(" and a.isAccordToSubCom='0'")
                .append(" and b.id=").append(workflowid);
        RecordSet rs = new RecordSet();
        rs.executeSql(sb.toString());
        if (rs.next()) {
            isuse = false;
        }
        return isuse;
    }
}