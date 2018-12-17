package weaver.workflow.exceldesign;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import weaver.hrm.User;
import weaver.hrm.resource.ResourceComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.datainput.DynamicDataInput;
import weaver.workflow.request.RequestPreAddinoperateManager;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.workflow.workflow.WfLinkageInfo;
import weaver.workflow.field.DetailFieldComInfo;
import weaver.workflow.field.FieldTypeComInfo;
import weaver.workflow.field.HtmlElement;
import weaver.workflow.html.WFLayoutToHtml;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author liuzy 2016-02-19
 * 将表单设计器生成的Html模板解析成具体DOM结构
 */
public class ParseLayoutToHtml extends BaseBean{
	private HttpServletRequest request;
	private User user;
	private int requestid;
	private int workflowid;
	private int nodeid;
	private int isbill;
	private int formid;
	private int pageSize = 0; // 每页打印的明细行数
    private boolean isSplitPrint = false; // 是否分页打印明细
    private int startIndex = 0; // 明细打印分页起始行索引,从0开始，0表示第一条
    private int pageNum = 1; // 当前页号
    
	public ParseLayoutToHtml(HttpServletRequest request, User user){
		this.request = request;
		this.user = user;
		this.requestid = Util.getIntValue(request.getParameter("requestid"), 0);
		this.workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
		this.nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
		this.isbill = Util.getIntValue(request.getParameter("isbill"), 0);
		this.formid = Util.getIntValue(request.getParameter("formid"), 0);
		this.pageSize = 0;
		this.isSplitPrint = false;
		this.startIndex = 0;
		this.pageNum = 1;
	}

	/**
	 * 表单设计器模板内高级明细解析成Html
	 */
	public HashMap<String,String> transSeniorDetail(String wfformhtml, Hashtable otherPara_hs, Map fieldMap) throws Exception{
		RecordSet rs = new RecordSet();
		RecordSet rs_record = new RecordSet();
		StringBuilder jsStr = new StringBuilder();
		StringBuilder hiddenStr = new StringBuilder();
		StringBuilder readyJsStr = new StringBuilder();
		StringBuilder needCheckStr = new StringBuilder();
		FieldTypeComInfo fieldTypeComInfo = new FieldTypeComInfo();
		ResourceComInfo resourceComInfo = new ResourceComInfo();
		int isprint = Util.getIntValue(request.getParameter("isprint"), 0);
		int nodetype = Util.getIntValue(request.getParameter("nodetype"), 0);
		int isviewonly = Util.getIntValue((String) otherPara_hs.get("isviewonly"), 0);
	    int mustNoEdit = Util.getIntValue((String) otherPara_hs.get("mustNoEdit"), 0);
	    int iscreate = Util.getIntValue((String) otherPara_hs.get("iscreate"), 0);
		HttpSession session = (HttpSession) request.getSession(false);
		int _intervenorright = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "intervenorright"), 0);
		int isaffirmancebody = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "isaffirmance"), 0);// 是否需要提交确认
		int reEditbody = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "reEdit"), 0);// 是否需要提交确认
		parseDetailExtendParams(otherPara_hs, session);			//扩充otherPara_hs内容
		String trrigerdetailfield = otherPara_hs.get("trrigerdetailfield")+"";	//扩充otherPara_hs之后取
		
		//获取明细字段信息
		Hashtable detailFieldid_hs = new Hashtable();	// 表单的所有明细表字段
		Hashtable fieldname_hs = new Hashtable();		// 表字段在数据库的字段名字
		Hashtable fieldlabel_hs = new Hashtable();		// 字段显示名
		Hashtable fieldhtmltype_hs = new Hashtable(); 	// 字段的htmltype队列
		Hashtable fieldtype_hs = new Hashtable(); 		// 字段的type队列
		Hashtable fielddbtype_hs = new Hashtable(); 	// 字段的数据库字段类型队列
		Hashtable isview_hs = new Hashtable();			// 是否显示
		Hashtable isedit_hs = new Hashtable();			// 是否可编辑
		Hashtable ismand_hs = new Hashtable();			// 是否必填
		buildDetailFieldInfo(detailFieldid_hs, fieldname_hs, fieldlabel_hs,
				fieldhtmltype_hs, fieldtype_hs, fielddbtype_hs, isview_hs, isedit_hs, ismand_hs, otherPara_hs);
		otherPara_hs.put("detailFieldid_hs", detailFieldid_hs);		//SelectElement用到
		otherPara_hs.put("languageId", ""+ user.getLanguage());
	      
	    // 获取节点前附加操作
	    Hashtable inoperatefield_hs = new Hashtable();
	    Hashtable fieldvalue_hs = new Hashtable();		// 节点前附加操作的值
	    buildPreOperInfo(inoperatefield_hs, fieldvalue_hs);

	    //获取行列规则信息
	    ArrayList rowCalAry = new ArrayList();
	    ArrayList colCalAry = new ArrayList();
	    ArrayList mainCalAry = new ArrayList();
	    buildCalRuleInfo(rowCalAry, colCalAry, mainCalAry);
		ParseCalculateRule parseCalculateRule = new ParseCalculateRule();
		String formcalrule = parseCalculateRule.parseRuleGroupByDetail(isbill, formid);
		readyJsStr.append("\t calOperate.initCalRuleCfg('"+formcalrule+"');\n");
		boolean PageLoadTriRowRule=Prop.getPropValue("FormCalaulate","PageLoadTriRowRule").equalsIgnoreCase("1");
		
		//取单据对应table信息
		String billtablename = "";
		ArrayList<String> billDetailTable = new ArrayList<String>();
		if(isbill == 1){
			rs.executeSql("select tablename from workflow_bill where id="+formid);
	        if(rs.next())
	        	billtablename = rs.getString("tablename");
	        rs.executeSql("select tablename from Workflow_billdetailtable where billid="+formid+" order by orderid");
	        while(rs.next()){
	        	billDetailTable.add(rs.getString("tablename"));
	        }
		}
		int groupTotalCount = billDetailTable.size(); // 明细表个数
	    int size = 1; // 总页数
	    boolean hasDate = false; // 判断第二个明细表开始的明细表是否有数据
	    int currentGorup = 0; // 记录当前循环到第几个明细表
		//循环模板内明细表
		String inputStr_tmp = "";
		HtmlElement object = null;
		while(true){
			int begIndex = wfformhtml.indexOf(ParseExcelLayout.BEGMARK);
			int endIndex = wfformhtml.indexOf(ParseExcelLayout.ENDMARK);
			if(begIndex == -1 || endIndex == -1 || begIndex >= endIndex)
				break;
			String frontcontent = wfformhtml.substring(0, begIndex);
			String behindcontent = wfformhtml.substring(endIndex+ParseExcelLayout.ENDMARK.length());
			String detailcontent = wfformhtml.substring(begIndex+ParseExcelLayout.BEGMARK.length(), endIndex);
			String detailHtml = "";
			try{
				Document doc = Jsoup.parse(detailcontent.toString(), "UTF-8");
				Element oTable = doc.getElementsByClass("excelDetailTable").first();
				int groupid = Util.getIntValue(oTable.id().replace("oTable", ""), 0);
				String groupName = groupid+"";
				if(isbill == 1)
					groupName = billDetailTable.get(groupid);
				//明细配置项
				WFNodeDtlFieldManager wFNodeDtlFieldManager = new WFNodeDtlFieldManager();
		        wFNodeDtlFieldManager.resetParameter();
		        wFNodeDtlFieldManager.setNodeid(nodeid);
		        wFNodeDtlFieldManager.setGroupid(groupid);
		        wFNodeDtlFieldManager.selectWfNodeDtlField();
		        String dtladd = wFNodeDtlFieldManager.getIsadd();
		        String dtldelete = wFNodeDtlFieldManager.getIsdelete();
		        String dtledit = wFNodeDtlFieldManager.getIsedit();
		        String isprintnulldetail = wFNodeDtlFieldManager.getIshide();
		        String dtldefault = wFNodeDtlFieldManager.getIsdefault();
		        int defaultrow =  wFNodeDtlFieldManager.getDefaultrows();
		        String dtlneed = wFNodeDtlFieldManager.getIsneed();
		        //String delprintserial = wFNodeDtlFieldManager.getIsprintserial();
		       
		        //是否隐藏明细区域
		        boolean shouldHidden = false;
		        if(_intervenorright == 1){
		        	shouldHidden = true;
		        }else{
			        if(isprint == 1 && !"1".equals(isprintnulldetail))
			        	shouldHidden = judgeShouldHiddenDetail(billtablename, groupName);
		        }
	            currentGorup = groupid;
	            if(groupid > 0 && !hasDate) {
	                if(!shouldHidden) {
	                    hasDate = true;
	                }
		        }
		        if(shouldHidden){
		        	int index_lasttr = frontcontent.lastIndexOf("<tr ");
		        	String content1 = frontcontent.substring(0, index_lasttr);
	        		String content2 = frontcontent.substring(index_lasttr);
	        		String content3 = content2.substring(content2.indexOf(">")+1);
	        		frontcontent = content1 +"<tr style=\"display:none\">" +content3;
		        	wfformhtml = frontcontent + "" + behindcontent;
		        	continue;
		        }
		        //取已有明细记录
				if(requestid > 0)	
					buildDetailRecordCollection(rs_record, billtablename, groupName, groupid);
		        //控制按钮显示
		        boolean needBuildAddJs = true;
		        boolean needBuildDelJs = true;
		        if(isviewonly == 1 || mustNoEdit == 1 || !"1".equals(dtladd)){
		        	doc.select("button[id=$addbutton"+groupid+"$]").remove();
		        	needBuildAddJs = false;
		        }
		        if(isviewonly == 1 || mustNoEdit == 1){
		        	doc.select("button[id=$delbutton"+groupid+"$]").remove();
		        	needBuildDelJs = false;
		        }
		        if(isviewonly == 1 || mustNoEdit == 1){
		        	doc.select("button[id=$sapmulbutton"+groupid+"$]").remove();
		        }
		        //默认新增明细,需生成addRow方法
		        boolean needDefaultAddRow = false;
		        if(dtldefault.equals("1") && isviewonly == 0 && rs_record.getCounts() < 1 && !(isaffirmancebody == 1 && reEditbody == 0 && nodetype==0)) {
		        	needDefaultAddRow = true;
		        	needBuildAddJs = true;
		        }
		        //解析全选按钮
		        if(doc.select("input[name=detailSpecialMark][value=20]").size() == 1){
					String checkAllStr = "<input type=\"checkbox\" notbeauty=\"true\" "+(needBuildDelJs?"":"disabled")+" name=\"check_all_record\" onclick=\"detailOperate.checkAllFun("+groupid+");\" title=\""+SystemEnv.getHtmlLabelName(556, user.getLanguage())+"\" />";
					doc.select("input[name=detailSpecialMark][value=20]").first().after(checkAllStr).remove();
		        }
				//解析明细doc取数据行、合计行
				Elements dataElms = doc.select("tr[_target=datarow]");		//模板数据行
				boolean hasCheckSingle = dataElms.select("input[name=detailSpecialMark][value=21]").size() == 1;
				boolean hasSerialNum = dataElms.select("input[name=detailSpecialMark][value=22]").size() == 1;
				dataElms.select("td").first().append("<div class=\"detailRowHideArea\"></div>");		//生成隐藏DIV存隐藏对象
				//循环明细字段，生成已有明细数据行存Map、生成AddRow所需Html、JS等
				String submitdtlid = "";
				LinkedHashMap<String,Elements> detailRecordMap = new LinkedHashMap<String,Elements>();		//有序Map
				String addRowHtmlStr = "", addRowjsStr = "", addNeedCheckFields = "", addInitDetailFields = "";
				if(needBuildAddJs){
					String rowIndexMark = "~^~rowindex~^~";
					String nextSerialMark = "~^~nextindex~^~";
					Elements addRowElms = dataElms.clone();
					addRowElms.select("tr").attr("_rowindex", rowIndexMark);
					parseElmsTDAttrs(addRowElms, rowIndexMark);
					//处理TD上相关参数
					
					//DOM结构时处理check框及序号
					String checkStr = "<input type=\"checkbox\" notbeauty=\"true\" name=\"check_node_"+groupid+"\" value=\""+rowIndexMark+"\" />";
					checkStr += "<input type=\"hidden\" name=\"dtl_id_"+groupid+"_"+rowIndexMark+"\" value=\"\" />";
					if(hasCheckSingle){
						Element checkElm = addRowElms.select("input[name=detailSpecialMark][value=21]").first();
						checkElm.after(checkStr);
						checkElm.remove();
					}else{
						addRowElms.select("div.detailRowHideArea").first().append(checkStr);
					}
					if(hasSerialNum){
						Element serialElm = addRowElms.select("input[name=detailSpecialMark][value=22]").first();
						serialElm.after("<span name=\"detailIndexSpan"+groupid+"\">"+nextSerialMark+"</span>");
						serialElm.remove();
					}
					addRowHtmlStr = addRowElms.toString();
					addRowHtmlStr = addRowHtmlStr.replace("\"", "\\\"");
					addRowHtmlStr = addRowHtmlStr.replace(rowIndexMark, "\"+rowindex+\"");
					addRowHtmlStr = addRowHtmlStr.replace(nextSerialMark, "\"+(curindex+1)+\"");
				}
				ArrayList fieldidList = (ArrayList) detailFieldid_hs.get("detailfieldList_" + groupid);
				for(int i=0; i<fieldidList.size(); i++){
					int fieldid_tmp = Util.getIntValue((String) fieldidList.get(i), 0);
					String fieldname_tmp = Util.null2String((String) fieldname_hs.get("fieldname"+ fieldid_tmp));
					int fieldhtmltype_tmp = Util.getIntValue((String) fieldhtmltype_hs.get("fieldhtmltype"+ fieldid_tmp), 0);
					int type_tmp = Util.getIntValue((String) fieldtype_hs.get("fieldtype" + fieldid_tmp), 0);
					String fielddbtype_tmp = Util.null2String((String) fielddbtype_hs.get("fielddbtype" + fieldid_tmp));
					int isview_tmp = Util.getIntValue((String) isview_hs.get("isview" + fieldid_tmp), 0);
					int isedit_tmp = Util.getIntValue((String) isedit_hs.get("isedit" + fieldid_tmp), 0);
					int ismand_tmp = Util.getIntValue((String) ismand_hs.get("ismand" + fieldid_tmp), 0);
					String fieldlabel_tmp = Util.null2String((String) fieldlabel_hs.get("fieldlabel" + fieldid_tmp));
					if(mustNoEdit == 1){
						isedit_tmp = 0;
						ismand_tmp = 0;
					}
					//不同字段类型需添加相应参数到otherPara_hs，特别是明细字段，尽量避免在HtmlElement重复查询SQL
					buildFieldOtherPara_hs(otherPara_hs, true, groupid, fieldid_tmp, fieldhtmltype_tmp, type_tmp, fielddbtype_tmp);
					otherPara_hs.put("fielddbtype", fielddbtype_tmp);
					int fieldlength_tmp = 0;
		            //增加多行文本的检查
					if(fieldhtmltype_tmp == 1 && type_tmp == 1 ||(fieldhtmltype_tmp == 2 && type_tmp == 1)){		//单文本中的文本
						if((fielddbtype_tmp.toLowerCase()).indexOf("varchar") > -1)
							fieldlength_tmp = Util.getIntValue(fielddbtype_tmp.substring(fielddbtype_tmp.indexOf("(")+1, fielddbtype_tmp.length()-1));
					}
					//表单设计器相关参数解析
					Element fieldInputElm = null;
					boolean existField = dataElms.select("input[id=$field"+fieldid_tmp+"$]").size() > 0;
					if(existField){
						fieldInputElm = dataElms.select("input[id=$field"+fieldid_tmp+"$]").first();
						if(otherPara_hs.containsKey("_format"))		otherPara_hs.remove("_format");
						if(fieldInputElm.hasAttr("_format")){
							String _format = fieldInputElm.attr("_format");
							if(_format.startsWith("${") && _format.endsWith("}$"))
								otherPara_hs.put("_format", _format.substring(2, _format.length()-2));
						}
						if(otherPara_hs.containsKey("_financial"))	otherPara_hs.remove("_financial");
						if(fieldInputElm.hasAttr("_financialfield")){
							String _financial = fieldInputElm.attr("_financialfield");
							if(_financial.startsWith("$[") && _financial.endsWith("]$"))
								otherPara_hs.put("_financial", _financial.substring(2, _financial.length()-2));
						}
						if(otherPara_hs.containsKey("_formula"))	otherPara_hs.remove("_formula");
						if(fieldInputElm.hasAttr("_formulafield_"))
							otherPara_hs.put("_formula", "y");
					}
					//循环已有明细记录存Map
					int derecorderindex = 0;
					String detailRecordId = "";
					String fieldvalue_tmp = "";
					Elements curDataElms = null;
					rs_record.beforFirst();
					int pageCount = 0; // 记录当前页记录数
	                if(isSplitPrint && groupid == 0) { // 如果分页打印明细行，设置分页起始记录（只分页第一个明细）
	                    int totalCount = rs_record.getCounts(); // 总记录数
	                    size = (totalCount % pageSize) == 0 ? (totalCount / pageSize) : ((totalCount / pageSize) + 1); // 总页数
	                    otherPara_hs.put("totalCount", totalCount);
	                    otherPara_hs.put("size", size);
	                    if(startIndex != 0) {
	                        rs_record.absolute(startIndex - 1);
	                        derecorderindex = startIndex;
	                    }
	                }
					int recordIndex=0;
					while(rs_record.next()){
						if(recordIndex > 1000)	//限制最多解析1000条
							break;
						recordIndex ++;
						detailRecordId = rs_record.getString("id");
						fieldvalue_tmp = Util.null2String(rs_record.getString(fieldname_tmp));
						curDataElms = null;
						if(i == 0){					//第一次循环clone数据行、解析check框及序号
							curDataElms = dataElms.clone();
							detailRecordMap.put(detailRecordId, curDataElms);
							
							submitdtlid += ","+derecorderindex;
							curDataElms.select("tr").attr("_rowindex", derecorderindex+"");
							parseElmsTDAttrs(curDataElms, derecorderindex+"");
							String checkStr = "<input type=\"checkbox\" notbeauty=\"true\" name=\"check_node_"+groupid+"\" value=\""+derecorderindex+"\" ";
							if(isviewonly == 1 || !"1".equals(dtldelete))
								checkStr += " disabled ";
							checkStr += "/><input type=\"hidden\" name=\"dtl_id_"+groupid+"_"+derecorderindex+"\" value=\""+detailRecordId+"\" />";
							if(hasCheckSingle){
								Element checkElm = curDataElms.select("input[name=detailSpecialMark][value=21]").first();
								checkElm.after(checkStr);
								checkElm.remove();
							}else{
								curDataElms.select("div.detailRowHideArea").first().append(checkStr);
							}
							if(hasSerialNum){
								Element serialElm = curDataElms.select("input[name=detailSpecialMark][value=22]").first();
								serialElm.after("<span name=\"detailIndexSpan"+groupid+"\">"+(derecorderindex+1)+"</span>");
								serialElm.remove();
							}
						}else{
							curDataElms = detailRecordMap.get(detailRecordId);
						}
						if(existField){
							if(ismand_tmp == 1 && fieldhtmltype_tmp != 4 && "1".equals(dtledit))
								needCheckStr.append(",field"+fieldid_tmp+"_"+derecorderindex);
							//使用select方式定位input，无需循环tr
							fieldInputElm = curDataElms.select("input[id=$field"+fieldid_tmp+"$]").first();
							try{
								int isedit_value = isedit_tmp;
								if (!"1".equals(dtledit))
									isedit_value = 0;
								otherPara_hs.put("derecorderindex", ""+ derecorderindex);
								otherPara_hs.put("detailNumber", ""+ (groupid + 1));		//明细表序号,从1开始
								otherPara_hs.put("detailRecordId", ""+ detailRecordId);		//明细记录主键
								
								object = (HtmlElement) Class.forName(fieldTypeComInfo.getClassname(""+ fieldhtmltype_tmp)).newInstance();
								Hashtable ret_hs = object.getHtmlElementString(fieldid_tmp, fieldname_tmp, type_tmp, fieldlabel_tmp, fieldlength_tmp, 
										1, groupid, fieldvalue_tmp, isviewonly, 1, isedit_value, ismand_tmp, user, otherPara_hs);
								inputStr_tmp = Util.null2String((String) ret_hs.get("inputStr"));
								// 为明细browser添加初始化js
								if(fieldhtmltype_tmp == 3 && !"".equals(Util.null2String((String) ret_hs.get("detailinitjs")))){
									inputStr_tmp += "\n"+Util.null2String((String) ret_hs.get("detailinitjs"));
								}
								inputStr_tmp = this.replaceSpecialChar(inputStr_tmp);
								fieldInputElm.after(inputStr_tmp);		//替换Html串
								jsStr.append(Util.null2String((String) ret_hs.get("jsStr"))).append("\n");
								hiddenStr.append(Util.null2String((String) ret_hs.get("hiddenElementStr"))).append("\n");
							}catch(Exception e){
								writeLog(e);
							}
							fieldInputElm.remove();
						}else{
							curDataElms.select("div.detailRowHideArea").first().append("<input type=\"hidden\" id=\"field"+fieldid_tmp+"_"+derecorderindex+"\" name=\"field"+fieldid_tmp+"_"+derecorderindex+"\" value=\""+fieldvalue_tmp+"\" />");
						}
						derecorderindex++;
						pageCount++;
                        if(isSplitPrint && groupid== 0) {
                            if(pageCount == pageSize) { // 只取当前设置的明细条数
                                break;
                            }
                        }
					}
					if(!needBuildAddJs)
						continue;
					//解析生成addRowHtmlStr，采用substring方式，jsoup会导致单引号/双引号转译偏差
					int findex = addRowHtmlStr.indexOf("$field"+fieldid_tmp+"$");
					if(findex > -1){		//模板存在该字段
						if(ismand_tmp == 1 && fieldhtmltype_tmp != 4)
							addNeedCheckFields += ",field"+fieldid_tmp+"_\"+rowindex+\"";
						if(trrigerdetailfield.indexOf("field"+fieldid_tmp) >= 0)
							addInitDetailFields += "field"+fieldid_tmp+"_\"+rowindex+\",";
						try{
							fieldvalue_tmp = new WFLayoutToHtml().getFieldValueTmp(fieldid_tmp, fieldhtmltype_tmp, type_tmp,
									inoperatefield_hs, fieldvalue_hs, fieldMap, resourceComInfo, otherPara_hs.get("prjid")+"", 
									otherPara_hs.get("docid")+"", otherPara_hs.get("dt_beagenter")+"", otherPara_hs.get("hrmid")+"", 
									Util.getIntValue(otherPara_hs.get("body_isagent")+""), Util.getIntValue(otherPara_hs.get("agenttype")+""), 
									otherPara_hs.get("crmid")+"", otherPara_hs.get("reqid")+"");
							otherPara_hs.put("derecorderindex", "\"+rowindex+\"");
							otherPara_hs.put("detailNumber", ""+(groupid + 1));		//明细表序号,从1开始
							otherPara_hs.put("detailRecordId", "0");				//新增行主键传入0
							
							object = (HtmlElement) Class.forName(fieldTypeComInfo.getClassname(""+ fieldhtmltype_tmp)).newInstance();
							Hashtable ret_hs = object.getHtmlElementString(fieldid_tmp, fieldname_tmp, type_tmp, fieldlabel_tmp, fieldlength_tmp, 
									1, groupid, fieldvalue_tmp, 0, 1, isedit_tmp, ismand_tmp, user, otherPara_hs);
							//替换addRowHtmlStr内容
							String frontstr = addRowHtmlStr.substring(0, findex);
							String behindstr = addRowHtmlStr.substring(findex);
							frontstr = frontstr.substring(0, frontstr.lastIndexOf("<"));
							behindstr = behindstr.substring(behindstr.indexOf(">")+1);
							addRowHtmlStr = frontstr + Util.null2String((String) ret_hs.get("inputStr")) + behindstr;
							
							addRowjsStr += Util.null2String((String)ret_hs.get("jsStr"))+ "\n"; 	//类似SQL联动JS操作，新增行触发，理论应放在addRowjsStr中
							addRowjsStr += Util.null2String((String)ret_hs.get("addRowjsStr"))+ "\n";
							addRowjsStr += Util.null2String((String)ret_hs.get("detailbrowaddjs"))+ "\n";	// browser初始化js
							//hiddenStr.append(Util.null2String((String) ret_hs.get("hiddenElementStr"))).append("\n");
						}catch(Exception e){
							writeLog(e);
						}
					}else{
						
					}
				}
				//模板解析合计行
				for(Object colCalObj : colCalAry){
					String sumFieldid = Util.null2String(colCalObj+"").replace("detailfield_", "").trim();
					if(doc.select("input[id=$sumfield"+sumFieldid+"$]").size() > 0){
						Element sumFieldElm = doc.select("input[id=$sumfield"+sumFieldid+"$]").first();
						sumFieldElm.after("<input type=\"hidden\" id=\"sumvalue"+sumFieldid+"\" name=\"sumvalue"+sumFieldid+"\" />")
							.after("<span id=\"sum"+sumFieldid+"\"></span>");
						sumFieldElm.remove();
					}
				}
				//模板拼接已有数据行
				StringBuffer sb = new StringBuffer();
				for(Map.Entry<String, Elements> entry : detailRecordMap.entrySet()){
					sb.append(entry.getValue().toString());
				}
				doc.select("tr[_target=datarow]").last().after(sb.toString());		//多行取最后一个再after
				dataElms.remove();
				if(needBuildAddJs){		//生成addRow方法
					addRowHtmlStr = addRowHtmlStr.replace("\n", "");
					//addRowHtmlStr = StringEscapeUtils.escapeJavaScript(addRowHtmlStr);
					StringBuilder addRowJs = new StringBuilder();
					addRowJs.append("function addRow"+groupid+"(groupid){").append("\n");
					addRowJs.append("\t var rowindex = parseInt($G(\"indexnum"+groupid+"\").value);").append("\n");
					addRowJs.append("\t var curindex = parseInt($G(\"nodesnum"+groupid+"\").value);").append("\n");
					addRowJs.append("\t var addRowHtmlStr = \""+addRowHtmlStr+"\";").append("\n");
					addRowJs.append("\t //操作主体放JS文件中").append("\n");
					addRowJs.append("\t detailOperate.addRowOperDom(groupid, addRowHtmlStr);").append("\n");
					addRowJs.append("\t $G(\"needcheck\").value += \""+addNeedCheckFields+"\"").append("\n");
					addRowJs.append("\t try{").append("\n");
					addRowJs.append("\t\t").append(addRowjsStr.replace("\n\n", "")).append("\n");
					addRowJs.append("\t }catch(e){}").append("\n");
					addRowJs.append("\t var initDetailFields = \""+addInitDetailFields+"\";").append("\n");
					addRowJs.append("\t detailOperate.addRowExecFun(groupid, initDetailFields);").append("\n");
					addRowJs.append("}").append("\n");
					jsStr.append(addRowJs);
				}
				if(needBuildDelJs){		//拼接deleteRow方法
					StringBuilder delRowJs = new StringBuilder();
					delRowJs.append("function deleteRow"+groupid+"(groupid, isfromsap){").append("\n");
					delRowJs.append("\t //操作主体放JS文件中").append("\n");
					delRowJs.append("\t detailOperate.delRowFun(groupid, isfromsap);").append("\n");
					delRowJs.append("}").append("\n");
					jsStr.append(delRowJs);
				}
				if(doc.select("button[id=$sapmulbutton"+groupid+"$]").size() > 0){		//拼接SAP方法
					jsStr.append(getAddSAPJsStr(groupid, groupName));
				}
				if(needDefaultAddRow){		//拼接新增默认空明细方法
					readyJsStr.append("\t try{ addRow"+groupid+"('"+groupid+"');}catch(e){}").append("\n");
					for(int r=0; r<defaultrow-1; r++){
						readyJsStr.append("\t try{ addRow"+groupid+"('"+groupid+"');}catch(e){}").append("\n");
					}
				}
				if(iscreate == 0){		//非新建页面打印计算行列规则(合计)
					if(PageLoadTriRowRule)
						readyJsStr.append("\t calOperate.calRowRule_allRow("+groupid+");").append("\n");
					readyJsStr.append("\t calSum("+groupid+");").append("\n");
				}
				//生成隐藏域
				hiddenStr.append("<input type=\"hidden\" id=\"rowneed"+groupid+"\" name=\"rowneed"+groupid+"\" value=\""+dtlneed+"\" />").append("\n");
				hiddenStr.append("<input type=\"hidden\" id=\"nodesnum"+groupid+"\" name=\"nodesnum"+groupid+"\" value=\""+rs_record.getCounts()+"\" />").append("\n");
		        hiddenStr.append("<input type=\"hidden\" id=\"indexnum"+groupid+"\" name=\"indexnum"+groupid+"\" value=\""+rs_record.getCounts()+"\" />").append("\n");
		        if(submitdtlid.startsWith(","))	submitdtlid = submitdtlid.substring(1);
		        hiddenStr.append("<input type=\"hidden\" id=\"submitdtlid"+groupid+"\" name=\"submitdtlid"+groupid+"\" value=\""+submitdtlid+"\" />").append("\n");
		        hiddenStr.append("<input type=\"hidden\" id=\"deldtlid"+groupid+"\" name=\"deldtlid"+groupid+"\" value=\"\" />").append("\n");
				detailHtml = doc.body().html();
				detailHtml = this.restoreSpecialCharAfterJsoup(detailHtml);
			}catch(Exception e){
				writeLog("ParseLayoutToHtml TransSeniorDetail Error:" + e);
			}
			wfformhtml = frontcontent + detailHtml + behindcontent;
		}
		//生成行列规则JS
		jsStr.append("function calSum(groupid){").append("\n");
		jsStr.append("\t //实现主体放在JS中").append("\n");
		jsStr.append("\t return calOperate.calSumFun(groupid);").append("\n");
		jsStr.append("}").append("\n");
		//页面加载执行JS
		if(!hasDate && this.pageNum > size) { // 如果后续明细表没有数据，不显示分页
            wfformhtml = "";
        }
		jsStr.append("jQuery(document).ready(function(){").append("\n");
		jsStr.append(readyJsStr).append("\n");
		jsStr.append("});").append("\n");
		HashMap<String,String> retmap = new HashMap<String,String>();
		retmap.put("wfformhtml", wfformhtml);
		retmap.put("jsStr", jsStr.toString());
		retmap.put("hiddenStr", hiddenStr.toString());
		retmap.put("needCheckStr", needCheckStr.toString());
		return retmap;
	}
	
	/**
	 * 解析TD上参数(自定义属性、公式相关参数)
	 */
	private void parseElmsTDAttrs(Elements dataElms, String rowmark){
		Elements tdElms = dataElms.select("td");
		for(Element tdElm : tdElms){
			if(tdElm.hasAttr("_attrid")){
				String _attrid = tdElm.attr("_attrid");
				tdElm.removeAttr("_attrid");
				if(_attrid.startsWith("$[") && _attrid.endsWith("]$"))
					tdElm.attr("id", _attrid.substring(2, _attrid.length()-2)+"_"+rowmark);
			}
			if(tdElm.hasAttr("_attrname")){
				String _attrname = tdElm.attr("_attrname");
				tdElm.removeAttr("_attrname");
				if(_attrname.startsWith("$[") && _attrname.endsWith("]$"))
					tdElm.attr("name", _attrname.substring(2, _attrname.length()-2));
			}
			tdElm.removeAttr("_fieldclass");
			if(tdElm.hasAttr("_cellattr")){
				String _cellattr = tdElm.attr("_cellattr");
				if(_cellattr.startsWith("$[") && _cellattr.endsWith("]$"))
					tdElm.attr("_cellattr", _cellattr.substring(2, _cellattr.length()-2)+"_"+rowmark);
			}
			if(tdElm.hasAttr("_fieldid")){
				String _fieldid = tdElm.attr("_fieldid");
				if(_fieldid.startsWith("$[") && _fieldid.endsWith("]$"))
					tdElm.attr("_fieldid", _fieldid.substring(2, _fieldid.length()-2)+"_"+rowmark);
			}
			if(tdElm.hasAttr("_formula")){
				String _formula = tdElm.attr("_formula");
				if(_formula.startsWith("$[") && _formula.endsWith("]$"))
					tdElm.attr("_formula", _formula.substring(2, _formula.length()-2));
			}
		}
	}
	
	/**
	 * 解析明细时增加相应参数到otherPara_hs(新、老明细解析共用)
	 */
	public void parseDetailExtendParams(Hashtable otherPara_hs, HttpSession session){
		String prjid = Util.null2String(request.getParameter("prjid"));
		String reqid = Util.null2String(request.getParameter("reqid"));
		String docid = Util.null2String(request.getParameter("docid"));
		String hrmid = Util.null2String(request.getParameter("hrmid"));
		String crmid = Util.null2String(request.getParameter("crmid"));
		if ("".equals(hrmid) && "1".equals(this.user.getLogintype())) {
			hrmid = "" + this.user.getUID();
		} else if ("".equals(crmid) && "2".equals(this.user.getLogintype())) {
			crmid = "" + this.user.getUID();
		}
		otherPara_hs.put("prjid", prjid);
		otherPara_hs.put("docid", docid);
		otherPara_hs.put("hrmid", hrmid);
		otherPara_hs.put("crmid", crmid);
		otherPara_hs.put("reqid", reqid);
		// 流程代理信息
		RecordSet rs = new RecordSet();
		int agenttype = -1; // 流程创建代理预设
		String dt_beagenter = "" + user.getUID();
		rs.executeSql("select agentorbyagentid,agenttype from workflow_currentoperator where usertype=0 and isremark='0' and requestid="
						+ requestid+ " and userid="+ user.getUID()+ " and nodeid=" + nodeid + " order by id desc");
		if (rs.next()) { // 获得被代理人
			int tembeagenter = rs.getInt(1);
			agenttype = rs.getInt(2);
			if (tembeagenter > 0) {
				dt_beagenter = "" + tembeagenter;
			}
		}
		
		int body_isagent = Util.getIntValue((String)session.getAttribute(workflowid + "isagent" + user.getUID()), 0);
		if (body_isagent == 1) {
			dt_beagenter = ""+ Util.getIntValue((String)session.getAttribute(workflowid+ "beagenter" + user.getUID()), 0);
		}
		

	      int requestid = Util.getIntValue(request.getParameter("requestid"), 0);
	      if(requestid > 0){

	          int desrequestid = Util.getIntValue(request.getParameter("desrequestid"), 0);
	          int billid = Util.getIntValue(request.getParameter("billid"), 0);
	          int workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
	          if(workflowid==0) workflowid=Util.getIntValue(request.getParameter("wfid"), 0);
	          int nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
	          int beagenter_dt = 0;
	          int beagenter = 0;
	          rs.executeSql("select agentorbyagentid from workflow_currentoperator where usertype=0 and isremark='0' and agenttype=2 and requestid=" + requestid  + " and nodeid=" + nodeid + " order by id desc");
	          if(rs.next()){
	           int tembeagenter = rs.getInt(1);
	              if (tembeagenter > 0) {
	              beagenter_dt = tembeagenter;
	              beagenter =tembeagenter;
	              }
	          }
	          rs.executeSql("select agenttype from workflow_agent where  isCreateAgenter=1 and workflowid ="+ workflowid + "and beagenterId="+ beagenter_dt +" order by agenttype desc");
	          if(rs.next()){
	          int body_isagent_dt = rs.getInt(1);
	          body_isagent = body_isagent_dt;
	          new weaver.general.BaseBean().writeLog("----37---body_isagent-->>"+body_isagent+"-------");
	          }
		}
		otherPara_hs.put("dt_beagenter", dt_beagenter);
		otherPara_hs.put("body_isagent", body_isagent + "");
		otherPara_hs.put("agenttype", agenttype + "");
		//字段联动信息
		DynamicDataInput ddidetail = new DynamicDataInput(workflowid + "");
		String trrigerdetailfield = ddidetail.GetEntryTriggerDetailFieldName();
		otherPara_hs.put("trrigerdetailfield", trrigerdetailfield);
		//显示属性联动信息
		WfLinkageInfo wfLinkageInfo = new WfLinkageInfo();
		ArrayList seldefieldsadd = wfLinkageInfo.getSelectField(workflowid, nodeid, 1);
		ArrayList changedefieldsadd = wfLinkageInfo.getChangeField(workflowid, nodeid, 1);
		otherPara_hs.put("seldefieldsadd", seldefieldsadd);
		otherPara_hs.put("changedefieldsadd", changedefieldsadd);
	}
	
	/**
	 * 生成明细字段信息(新、老明细解析共用)
	 */
	public void buildDetailFieldInfo(Hashtable detailFieldid_hs, Hashtable fieldname_hs, Hashtable fieldlabel_hs,
			Hashtable fieldhtmltype_hs, Hashtable fieldtype_hs, Hashtable fielddbtype_hs, 
			Hashtable isview_hs, Hashtable isedit_hs, Hashtable ismand_hs,  Hashtable otherPara_hs){
		DetailFieldComInfo detailFieldComInfo = new DetailFieldComInfo();
		Hashtable billGroupid_hs = new Hashtable();
		String sql = "";
		RecordSet rs_tmp = new RecordSet();
		if (isbill == 0) {
			sql = "select nf.*,fl.fieldlable,ff.groupid,'' as fieldname from workflow_nodeform nf "
					+ " left join workflow_formfield ff on nf.fieldid=ff.fieldid and ff.formid=" + formid
					+ " left join workflow_fieldlable fl on fl.fieldid=nf.fieldid and fl.formid=" + formid
					+ " and fl.langurageid="+ user.getLanguage()
					+ " where nf.nodeid="+nodeid+" and ff.isdetail='1' order by nf.orderid, ff.fieldorder";
		} else if (isbill == 1) {
			//把单据的明细字段表先明映射为groupid
			int cx = 0;
			sql = "select tablename from Workflow_billdetailtable where billid="+formid+" order by orderid";
			rs_tmp.execute(sql);
			while (rs_tmp.next()) {
				String tablename_tmp = Util.null2String(rs_tmp.getString("tablename"));
				if (!"".equals(tablename_tmp)) {
					billGroupid_hs.put(tablename_tmp, "" + cx);
					cx++;
				}
			}
			// 单据的明细字段信息先拿出来，具体分组以后再查数据库
			sql = "select nf.*,bf.fieldlabel as fieldlable,detailtable as groupid,bf.fieldname,bf.fieldhtmltype,bf.type,bf.fielddbtype,bf.imgheight,bf.imgwidth from workflow_nodeform nf "
					+ " left join workflow_billfield bf on nf.fieldid=bf.id and bf.billid=" + formid
					+ " where nf.nodeid="+nodeid+" and bf.viewtype=1 order by nf.orderid, bf.dsporder";
		}
		rs_tmp.execute(sql);
		while (rs_tmp.next()) {
			int fieldid_tmp = Util.getIntValue(rs_tmp.getString("fieldid"), 0);
			int isview_tmp = Util.getIntValue(rs_tmp.getString("isview"), 0);
			int isedit_tmp = Util.getIntValue(rs_tmp.getString("isedit"), 0);
			int ismand_tmp = Util.getIntValue(rs_tmp.getString("ismandatory"), 0);
			String fieldname = Util.null2String(rs_tmp.getString("fieldname"));
			if (isbill == 0) {
				fieldname = detailFieldComInfo.getFieldname("" + fieldid_tmp);
			}
			String labelName_tmp = "";
			if (isbill == 0) {
				labelName_tmp = Util.null2String(rs_tmp.getString("fieldlable"));
			} else {
				int labelid_tmp = Util.getIntValue(rs_tmp.getString("fieldlable"));
				labelName_tmp = SystemEnv.getHtmlLabelName(labelid_tmp, user.getLanguage());
			}
			labelName_tmp = Util.toScreenForWorkflow(labelName_tmp);
			String groupid_tmp = "";
			if (isbill == 0) {
				groupid_tmp = ""+ Util.getIntValue(rs_tmp.getString("groupid"), 0);
			} else {
				String groupname_tmp = ""+ Util.null2String(rs_tmp.getString("groupid"));
				groupid_tmp = ""+ Util.getIntValue((String) billGroupid_hs.get(groupname_tmp), -1);
			}
			ArrayList detailFieldidList = (ArrayList) detailFieldid_hs.get("detailfieldList_" + groupid_tmp);
			if (detailFieldidList == null) {
				detailFieldidList = new ArrayList();
			}
			detailFieldidList.add("" + fieldid_tmp);
			detailFieldid_hs.put("detailfieldList_" + groupid_tmp, detailFieldidList);
			int htmltype_tmp = 0;
			int type_tmp = 0;
			String dbtype_tmp = "";
			if (isbill == 0) {
				htmltype_tmp = Util.getIntValue(detailFieldComInfo.getFieldhtmltype("" + fieldid_tmp), 0);
				type_tmp = Util.getIntValue(detailFieldComInfo.getFieldType(""+ fieldid_tmp), 0);
				dbtype_tmp = Util.null2String(detailFieldComInfo.getFielddbtype("" + fieldid_tmp));
			} else {
				htmltype_tmp = Util.getIntValue(rs_tmp.getString("fieldhtmltype"), 0);
				type_tmp = Util.getIntValue(rs_tmp.getString("type"), 0);
				dbtype_tmp = Util.null2String(rs_tmp.getString("fielddbtype"));
			}
			fieldname_hs.put("fieldname" + fieldid_tmp, fieldname);
			fieldlabel_hs.put("fieldlabel" + fieldid_tmp, labelName_tmp);
			fieldhtmltype_hs.put("fieldhtmltype" + fieldid_tmp, ""+ htmltype_tmp);
			fieldtype_hs.put("fieldtype" + fieldid_tmp, "" + type_tmp);
			fielddbtype_hs.put("fielddbtype" + fieldid_tmp, dbtype_tmp);
			isview_hs.put("isview" + fieldid_tmp, "" + isview_tmp);
			isedit_hs.put("isedit" + fieldid_tmp, "" + isedit_tmp);
			ismand_hs.put("ismand" + fieldid_tmp, "" + ismand_tmp);
			if (htmltype_tmp == 6) {
				if (isbill == 0) {
					otherPara_hs.put("fieldimgwidth" + fieldid_tmp, ""+ detailFieldComInfo.getImgWidth("" + fieldid_tmp));
					otherPara_hs.put("fieldimgheight" + fieldid_tmp, ""+ detailFieldComInfo.getImgHeight("" + fieldid_tmp));
				} else {
					otherPara_hs.put("fieldimgwidth" + fieldid_tmp, ""+ Util.getIntValue(rs_tmp.getString("imgwidth"), 0));
					otherPara_hs.put("fieldimgheight" + fieldid_tmp, ""+ Util.getIntValue(rs_tmp.getString("imgheight"), 0));
				}
			}
		}
	}
	
	/**
	 * 生成节点前附加操作信息(新、老明细解析共用)
	 */
	public void buildPreOperInfo(Hashtable inoperatefield_hs, Hashtable fieldvalue_hs) {
		RequestPreAddinoperateManager requestPreAddM = new RequestPreAddinoperateManager();
		requestPreAddM.setCreater(user.getUID());
		requestPreAddM.setOptor(user.getUID());
		requestPreAddM.setWorkflowid(workflowid);
		requestPreAddM.setNodeid(nodeid);
		requestPreAddM.setRequestid(requestid);
		Hashtable getPreAddRule_hs = requestPreAddM.getPreAddRule();
		inoperatefield_hs.putAll((Hashtable) getPreAddRule_hs.get("inoperatefield_hs"));
		fieldvalue_hs.putAll((Hashtable) getPreAddRule_hs.get("inoperatevalue_hs"));
	}
	
	/**
	 * 生成行列规则信息(新、老明细解析共用)
	 */
	public void buildCalRuleInfo(ArrayList rowCalAry, ArrayList colCalAry, ArrayList mainCalAry) {
		RecordSet rs = new RecordSet();
		String rowCalItemStr1 = "", colCalItemStr1 = "", mainCalStr1 = "";
		rs.executeProc("Workflow_formdetailinfo_Sel", formid + "");
		while (rs.next()) {
			rowCalItemStr1 = Util.null2String(rs.getString("rowCalStr"));
			colCalItemStr1 = Util.null2String(rs.getString("colCalStr"));
			mainCalStr1 = Util.null2String(rs.getString("mainCalStr"));
		}
		StringTokenizer stk = new StringTokenizer(rowCalItemStr1, ";");
		while (stk.hasMoreTokens()) {
			rowCalAry.add(stk.nextToken());
		}
		stk = new StringTokenizer(colCalItemStr1, ";");
		while (stk.hasMoreTokens()) {
			colCalAry.add(stk.nextToken());
		}
		stk = new StringTokenizer(mainCalStr1, ";");
		while (stk.hasMoreTokens()) {
			mainCalAry.add(stk.nextToken());
		}
	}
	
	/**
	 * 判断打印是否隐藏空明细(新、老明细解析共用)
	 */
	public boolean judgeShouldHiddenDetail(String billtablename, String groupName_tmp) {
		RecordSet rs = new RecordSet();
		boolean shouldHidden = false;
		if (isbill == 1) {
			if ((billtablename.indexOf("formtable_main_") == 0 || billtablename.indexOf("uf_") == 0)
					&& (groupName_tmp.indexOf("formtable_main_") == 0 || groupName_tmp.indexOf("uf_") == 0)) {// 新表单
				rs.executeSql("select b.* from " + billtablename + " a,"+ groupName_tmp+ " b where a.id=b.mainid and a.requestid ="+ requestid + " order by b.id");
			} else if (Util.getIntValue("" + formid) < 0) { // 数据中心模块创建的明细报表
				rs.executeSql("select b.* from " + billtablename + " a,"+ groupName_tmp+ " b where a.id=b.mainid and a.requestid ="+ requestid + " order by b.inputid");
			} else if(billtablename.equals("bill_hrmfinance")){//费用报销单 QC29615
			    rs.executeSql("select b.* from "+billtablename+" a,"+groupName_tmp+" b where a.id=b.expenseid and a.requestid ="+requestid+" order by b.id");
            } else {
				rs.executeSql("select b.* from " + billtablename + " a,"+ groupName_tmp+ " b where a.id=b.mainid and a.requestid ="+ requestid);
			}
			if(rs.getCounts() <= 0)
				shouldHidden = true;
		} else {
			rs.executeSql("select * from Workflow_formdetail where requestid ="+ requestid+ " and groupId="+ groupName_tmp+ " order by id");
			if(rs.getCounts() <= 0)
				shouldHidden = true;
		}
		return shouldHidden;
	}
	
	/**
	 * 获取已有明细记录(新、老明细解析共用)
	 */
	public void buildDetailRecordCollection(RecordSet rs_record, String billtablename, String groupName_tmp, int groupid_tmp){
		if (isbill == 0) {
			rs_record.executeSql("select * from Workflow_formdetail where requestid ="+ requestid+ "  and groupId="+ groupid_tmp+ " order by id");
		} else {
			if ((billtablename.indexOf("formtable_main_") == 0 || billtablename.indexOf("uf_") == 0)
					&& (groupName_tmp.indexOf("formtable_main_") == 0 || groupName_tmp.indexOf("uf_") == 0)) {// 新表单
				rs_record.executeSql("select b.* from " + billtablename+ " a," + groupName_tmp+ " b where a.id=b.mainid and a.requestid ="+ requestid + " order by b.id");
			} else if (billtablename.equals("bill_hrmfinance")) {// 费用报销单
				rs_record.executeSql("select b.* from " + billtablename+ " a," + groupName_tmp+ " b where a.id=b.expenseid and a.requestid ="+ requestid + " order by b.id");
			} else if (formid < 0) { // 数据中心模块创建的明细报表
				rs_record.executeSql("select b.* from " + billtablename+ " a," + groupName_tmp+ " b where a.id=b.mainid and a.requestid ="+ requestid + " order by b.inputid");
			} else {
				rs_record.executeSql("select b.* from " + billtablename+ " a," + groupName_tmp+ " b where a.id=b.mainid and a.requestid ="+ requestid);
			}
		}
	}
	
	/**
	 * 获取明细SAP按钮JS(新、老明细解析共用)
	 */
	public String getAddSAPJsStr(int groupid, String groupName){
		StringBuffer addSAPJsSb = new StringBuffer();
		//------------------zzl生成多选浏览按钮脚本-------------------------------------
        addSAPJsSb.append("\n").append("function addSapRow" + groupid + "(groupid){").append("\n");
        RecordSet rs_sap = new RecordSet();
		rs_sap.execute("select browsermark from sap_multiBrowser where mxformid='"+formid+"' and mxformname='"+groupName+"'");
		if(rs_sap.next()){
			String browsermark=rs_sap.getString("browsermark");
			//addSAPJsSb.append("addRow"+groupid_tmp+"(groupid);");
			//http://localhost:8081/systeminfo/BrowserMain.jsp?url=/integration/sapSingleBrowser.jsp?type=browser.181|11182_1
			//打开sap多选浏览按钮
			addSAPJsSb.append("var browsermark ='"+browsermark+"';").append("\n");
			addSAPJsSb.append("var urls='/systeminfo/BrowserMain.jsp?url=/integration/sapSingleBrowser.jsp?type="+browsermark+"|'+groupid;").append("\n");
			addSAPJsSb.append("var dialog = new window.top.Dialog();").append("\n");
			addSAPJsSb.append("dialog.currentWindow = window;");
			addSAPJsSb.append("dialog.URL = urls;").append("\n");
			addSAPJsSb.append("dialog.Title = 'SAP';").append("\n");
			addSAPJsSb.append("dialog.Width = 550 ;").append("\n");
			addSAPJsSb.append("dialog.Height = 600;").append("\n");
			addSAPJsSb.append("dialog.show();").append("\n");
			//addSAPJsSb.append("window.showModalDialog(\"/systeminfo/BrowserMain.jsp?url=/integration/sapSingleBrowser.jsp?type="+browsermark+"|\"+groupid, window, \"dialogWidth=550px;dialogHeight=550px\");");
		}else{
			addSAPJsSb.append("top.Dialog.alert('"+SystemEnv.getHtmlLabelName(84117,Util.getIntValue(""+user.getLanguage(),7))+"')").append("\n");
		}
        addSAPJsSb.append("\n").append("}");
        addSAPJsSb.append("\n");
        //------------------zzl生成多选浏览按钮脚本-------------------------------------
        return addSAPJsSb.toString();
	}
	
	/**
	 * 不同字段类型需添加相应参数到otherPara_hs，特别是明细字段，尽量避免在HtmlElement重复查询SQL(适用于主表字段、明细表字段)
	 */
	public void buildFieldOtherPara_hs(Hashtable otherPara_hs, boolean isDetail,int groupid, int fieldid, int fieldhtmltype, int type, String fielddbtype){
		String formFieldTable = isDetail ? "workflow_formdictdetail" : "workflow_formdict";
		RecordSet rs = new RecordSet();
		otherPara_hs.remove("decimaldigits_t");
		otherPara_hs.remove("fieldheight");
		otherPara_hs.remove("childfieldid_tmp");
		otherPara_hs.remove("firstPfieldid_tmp");
		otherPara_hs.remove("hasPfield");
		otherPara_hs.remove("selectitemrs");
		if(fieldhtmltype == 1 && (type == 3 || type == 5)){		//文本框
			int decimaldigits_t = 2;
			if(type == 3){
				int digitsIndex = fielddbtype.indexOf(",");
				if(digitsIndex > -1)
					decimaldigits_t = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1), 2);
			}else if(type == 5){
				if(isbill == 0)
					rs.executeSql("select qfws from "+formFieldTable+" where id="+fieldid);
				else
					rs.executeSql("select qfws from workflow_billfield where id="+fieldid);
				if(rs.next())
					decimaldigits_t = Util.getIntValue(rs.getString("qfws"), 2);
			}
			if(decimaldigits_t < 0)
				decimaldigits_t = 2;
			otherPara_hs.put("decimaldigits_t", decimaldigits_t);
		}else if(fieldhtmltype == 2){				//多行文本框
			if(isbill == 0)
				rs.execute("select textheight from "+formFieldTable+" where id="+ fieldid);
			else
				rs.execute("select textheight from workflow_billfield where id="+ fieldid+ " and billid="+ formid);
			if(rs.next()){
				int fieldheight_tmp = Util.getIntValue(rs.getString("textheight"), 4);
				otherPara_hs.put("fieldheight", ""+fieldheight_tmp);
			}
		}else if(fieldhtmltype == 5){		//选择框
			ArrayList fieldList = new ArrayList();
			if(isDetail){
				if(otherPara_hs.containsKey("detailFieldid_hs")){
					Hashtable detailFieldid_hs = (Hashtable)otherPara_hs.get("detailFieldid_hs");
					if(detailFieldid_hs != null)
						fieldList = (ArrayList)detailFieldid_hs.get("detailfieldList_"+groupid);
				}
			}else{
				if(otherPara_hs.containsKey("fieldidList"))
					fieldList = (ArrayList)otherPara_hs.get("fieldidList");
			}
			int childfieldid_tmp = 0;
			int firstPfieldid_tmp = 0;
			boolean hasPfield = false;
			if(isbill == 0)
				rs.execute("select childfieldid from "+formFieldTable+" where id="+fieldid);
			else
				rs.execute("select childfieldid from workflow_billfield where id="+fieldid);
			if(rs.next())
				childfieldid_tmp = Util.getIntValue(rs.getString("childfieldid"), 0);
			if(isbill == 0)
				rs.execute("select id from "+formFieldTable+" where childfieldid="+fieldid);
			else
				rs.execute("select id from workflow_billfield where childfieldid="+fieldid);
			while(rs.next()){
				firstPfieldid_tmp = Util.getIntValue(rs.getString("id"), 0);
				if(fieldList.contains(""+firstPfieldid_tmp)){
					hasPfield = true;
					break;
				}
			}
			otherPara_hs.put("childfieldid_tmp", childfieldid_tmp);
			otherPara_hs.put("firstPfieldid_tmp", firstPfieldid_tmp);
			otherPara_hs.put("hasPfield", hasPfield);
			
			char flag = Util.getSeparator();
			int isviewonly = Util.getIntValue((String) otherPara_hs.get("isviewonly"), 0);
			RecordSet selectitemrs = new RecordSet();
			//在使用的时候进行判断， 因为每个字段的编辑状态不一样， 在表单可编辑状态、字段只读时，也要显示出封存选项
			selectitemrs.executeProc("workflow_SelectItemSelectByid", ""+fieldid+flag+isbill);
			otherPara_hs.put("selectitemrs", selectitemrs);
		}
	}
	
	/**
	 * jsoup操作dom(after/append)前替换特殊字符，解决\n经过.after成空格问题
	 */
	private String replaceSpecialChar(String str){
		str = str.replace("\n", "~~^~~breakline~~^~~");
        str = str.replace("²", "~~^~~u00b2~~^~~");
        str = str.replace("³", "~~^~~u00b3~~^~~");
		return str;
	}
	
	/**
	 * jsoup操作完后，特殊字符转译成原因字符
	 */
	private String restoreSpecialCharAfterJsoup(String str){
		str = str.replace("~~^~~breakline~~^~~", "\n");
        str = str.replace("~~^~~u00b2~~^~~","²");
        str = str.replace("~~^~~u00b3~~^~~","³");
		return str;
	}
	
	public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public void setIsSplitPrint(boolean isSplitPrint) {
        this.isSplitPrint = isSplitPrint;
    }
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
	
	
}
