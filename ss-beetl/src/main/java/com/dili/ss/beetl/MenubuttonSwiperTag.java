package com.dili.ss.beetl;

import com.dili.ss.util.AopTargetUtils;
import com.dili.ss.util.POJOUtils;
import com.dili.ss.util.SpringUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.tag.Tag;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 菜单按钮自定义标签
 * Created by asiamaster on 2017/7/11 0011.
 */
@Component("menubuttonSwiper")
public class MenubuttonSwiperTag extends Tag {
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String TAB = "    ";

	//标签自定义属性
	private final String ID_FIELD = "_idField";
	private final String TEXT_FIELD = "_textField";
	private final String PARENT_ID_FIELD = "_parentIdField";
	private final String ICON_CLS_FIELD = "_iconClsField";
	private final String DISABLED_FIELD = "_disabledField";
	private final String SERVICE = "_service";
	private final String METHOD = "_method";
	private final String QUERYPARAMS = "_queryParams";
	private final String DIV_ID = "_divId";
	private final String CONTAINER_ID = "_containerId";
	private final String MENU_WIDTH = "_menuWidth";
	private final String MENU_HEIGHT = "_menuHeight";
	private final String PANEL_ALIGN = "_panelAlign";

	//标签默认值
	private final String ID_FIELD_DEFAULT = "id";
	private final String TEXT_FIELD_DEFAULT = "text";
	private final String PARENT_ID_FIELD_DEFAULT = "parentId";
	private final String ICON_CLS_FIELD_DEFAULT = "iconCls";
	private final String DISABLED_FIELD_DEFAULT = "disabled";
	private final String DIV_ID_DEFAULT = "_menubuttonDiv";
	private final String CONTAINER_ID_DEFAULT = "swiperContainer";
	private final String MENU_WIDTH_DEFAULT = "80";
	private final String MENU_HEIGHT_DEFAULT = "30";
	private final String PANEL_ALIGN_DEFAULT = "left";

	//easyui menubutton和menu属性，用于在menubutton标签上使用
	private final String ALIGN = "align";
	private final String MIN_WIDTH = "minWidth";
	private final String ITEM_WIDTH = "itemWidth";
	private final String ITEM_HEIGHT = "itemHeight";
	private final String DURATION = "duration";
	private final String HIDE_ON_UNHOVER = "hideOnUnhover";
	private final String INLINE = "inline";
	private final String FIT = "fit";
	private final String NOLINE = "noline";
	private final String ON_CLICK = "onClick";
	private final String PLAIN = "plain";
	private final String MENU_ALIGN = "menuAlign";
	private final String HAS_DOWN_ARROW = "hasDownArrow";

	//easyui菜单项属性对应，用于在菜单数据列表中使用
	private final String ICON_CLS = "iconCls";
	private final String DISABLED = "disabled";

	@Override
	public void render() {
		try {
//			BodyContent content = getBodyContent(); // 标签体内容，暂存
			Map<String, Object> argsMap = (Map)this.args[1];
			String service = (String) argsMap.get(SERVICE);
			String method = (String) argsMap.get(METHOD);
			String queryParams = argsMap.get(QUERYPARAMS) == null ? null : String.valueOf(argsMap.get(QUERYPARAMS));
//			service和method参数必填
			if(StringUtils.isBlank(service) || StringUtils.isBlank(method)) {
				return;
			}
			Object serviceObj = AopTargetUtils.getTarget(SpringUtil.getBean(service));
			List list = (List)serviceObj.getClass().getMethod(method, String.class).invoke(serviceObj, queryParams);
			if(null == list || list.isEmpty()){
				return;
			}
			writeMenu(list, writeMenubutton(list));
		} catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成菜单按钮
	 * @param list
	 * @return
	 */
	private List writeMenubutton(List list){
		List rootList = null;
		boolean isMap = false;
		Map<String, String> argsMap = (Map)this.args[1];
		//获取对应字段名称，默认为常量值
		String textField = argsMap.get(TEXT_FIELD) == null ? TEXT_FIELD_DEFAULT : argsMap.get(TEXT_FIELD).toString();
		String idField = argsMap.get(ID_FIELD) == null ? ID_FIELD_DEFAULT : argsMap.get(ID_FIELD).toString();
		String parentIdField = argsMap.get(PARENT_ID_FIELD) == null ? PARENT_ID_FIELD_DEFAULT : argsMap.get(PARENT_ID_FIELD).toString();
		String iconClsField = argsMap.get(ICON_CLS_FIELD) == null ? ICON_CLS_FIELD_DEFAULT : argsMap.get(ICON_CLS_FIELD).toString();
		String disabledField = argsMap.get(DISABLED_FIELD) == null ? DISABLED_FIELD_DEFAULT : argsMap.get(DISABLED_FIELD).toString();
		String divId = argsMap.get(DIV_ID) == null ? DIV_ID_DEFAULT : argsMap.get(DIV_ID).toString();
		//swiper用的容器id
		String containerId = argsMap.get(CONTAINER_ID) == null ? CONTAINER_ID_DEFAULT : argsMap.get(CONTAINER_ID).toString();
		String menuWidth = argsMap.get(MENU_WIDTH) == null ? MENU_WIDTH_DEFAULT : argsMap.get(MENU_WIDTH).toString();
		String menuHeight = argsMap.get(MENU_HEIGHT) == null ? MENU_HEIGHT_DEFAULT : argsMap.get(MENU_HEIGHT).toString();
		String panelAlign = argsMap.get(PANEL_ALIGN) == null ? PANEL_ALIGN_DEFAULT : argsMap.get(PANEL_ALIGN).toString();
		if(Map.class.isAssignableFrom(list.get(0).getClass())){
			rootList = getMapRoots(list, parentIdField);
			isMap = true;
		}else{
			rootList = getBeanRoots(list, parentIdField);
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<div id=\""+divId+"\" class=\"easyui-panel swiper-container\" align=\""+panelAlign+"\" style=\"padding:1px;\">"+ LINE_SEPARATOR);
		stringBuilder.append("<div id=\""+containerId+"\" >");
		stringBuilder.append("<div class=\"swiper-wrapper\">");
		for(Object root : rootList) {
			//校验text和id必须有
			if(getData(root, textField, isMap) == null || getData(root, idField, isMap) == null) {
				continue;
			}
			String id = getData(root, idField, isMap);
			String iconCls = getData(root, iconClsField, isMap);
			String disabled = getData(root, disabledField, isMap);
			stringBuilder.append("<div class=\"swiper-slide\">");
			//没有子的根节点的class为easyui-linkbutton
			if(!hasChild(list, id, isMap, parentIdField)){
				stringBuilder.append("<a id=\"menubutton_" + id + "\" href=\"#\" class=\"easyui-linkbutton\"  data-options=\"width:"+menuWidth+", height:"+menuHeight+", blankKey:''");
			}else{
				stringBuilder.append("<a id=\"menubutton_" + id + "\" href=\"#\" class=\"easyui-menubutton\"  data-options=\"width:"+menuWidth+", height:"+menuHeight+", menu:'#menu_" + id + "'");
			}
			//添加menubutton属性
			if (argsMap.containsKey(PLAIN)) {
				stringBuilder.append(", plain:" + argsMap.get(PLAIN));
			}
			if (argsMap.containsKey(MENU_ALIGN)) {
				stringBuilder.append(", menuAlign:'" + argsMap.get(MENU_ALIGN) + "'");
			}
			if (argsMap.containsKey(DURATION)) {
				stringBuilder.append(", duration:" + argsMap.get(DURATION) );
			}
			if (argsMap.containsKey(HAS_DOWN_ARROW)) {
				stringBuilder.append(", hasDownArrow:" + argsMap.get(HAS_DOWN_ARROW) );
			}
			if(argsMap.containsKey(ON_CLICK)){
				stringBuilder.append(", onClick:"+argsMap.get(ON_CLICK));
			}
			if (StringUtils.isNotBlank(iconCls)) {
				stringBuilder.append(", iconCls:'" + iconCls + "'");
			}
			if (StringUtils.isNotBlank(disabled)) {
				stringBuilder.append(", disabled:" + disabled);
			}
			stringBuilder.append("," + getDataOptions(root, Map.class.isAssignableFrom(root.getClass())));
			stringBuilder.append("\">" + getData(root, textField, isMap) + "</a>"+ LINE_SEPARATOR);
			stringBuilder.append("</div>");
		}
		stringBuilder.append("</div>"+ LINE_SEPARATOR);//end of swiper-wrapper
		stringBuilder.append("</div>");//end of swiper-container
		//导航按钮
		stringBuilder.append("<div class=\"swiper-button-prev swiper-button-black\"></div>");
		stringBuilder.append("<div class=\"swiper-button-next swiper-button-black\"></div>");
		stringBuilder.append("</div>"+ LINE_SEPARATOR);
		try {
			ctx.byteWriter.writeString(stringBuilder.toString() );
		} catch (IOException e) {
			e.printStackTrace();
		}
		//从list中删除rootList，优化性能
		removeListElement(list, rootList);
		return rootList;
	}

	/**
	 * 生成菜单
	 * @param list
	 * @param rootList
	 * @throws IOException
	 */
	private void writeMenu(List list, List rootList) throws IOException {
		//排除只有根节点没有子节点的情况
		if(list.isEmpty()) {
			return;
		}
		Map<String, String> argsMap = (Map)this.args[1];
		//获取对应字段名称，默认为常量值
		String textField = argsMap.get(TEXT_FIELD) == null ? TEXT_FIELD_DEFAULT : argsMap.get(TEXT_FIELD).toString();
		String idField = argsMap.get(ID_FIELD) == null ? ID_FIELD_DEFAULT : argsMap.get(ID_FIELD).toString();
		String parentIdField = argsMap.get(PARENT_ID_FIELD) == null ? PARENT_ID_FIELD_DEFAULT : argsMap.get(PARENT_ID_FIELD).toString();
		String iconClsField = argsMap.get(ICON_CLS_FIELD) == null ? ICON_CLS_FIELD_DEFAULT : argsMap.get(ICON_CLS_FIELD).toString();
		String disabledField = argsMap.get(DISABLED_FIELD) == null ? DISABLED_FIELD_DEFAULT : argsMap.get(DISABLED_FIELD).toString();
		StringBuilder stringBuilder = new StringBuilder();
		boolean isMap = false;
		if(Map.class.isAssignableFrom(list.get(0).getClass())){
			isMap = true;
		}
		for(Object root : rootList){
			String rootId = getData(root, idField, isMap).toString();
			//如果根节点下面没有子节点则不生成menu
			if(!hasChild(list, rootId, isMap, parentIdField)){
				continue;
			}
			//构建菜单, zIndex:110000是默认值，暂且写死
			if(argsMap.containsKey(ITEM_WIDTH)){
				stringBuilder.append("<div id=\"menu_" + rootId + "\" style=\"width:"+argsMap.get(ITEM_WIDTH)+"px;\" data-options=\"zIndex:'110000' ");
			}else {
				stringBuilder.append("<div id=\"menu_" + rootId + "\" data-options=\"zIndex:'110000' ");
			}
			//添加所有menu公用属性
			if(argsMap.containsKey(ALIGN)) {
				stringBuilder.append(", align:'" + argsMap.get(ALIGN) + "'");
			}
			if(argsMap.containsKey(MIN_WIDTH)){
				stringBuilder.append(", minWidth:"+argsMap.get(MIN_WIDTH));
			}
			if(argsMap.containsKey(ITEM_HEIGHT)){
				stringBuilder.append(", itemHeight:"+argsMap.get(ITEM_HEIGHT));
			}
			if(argsMap.containsKey(DURATION)){
				stringBuilder.append(", duration:"+argsMap.get(DURATION));
			}
			if(argsMap.containsKey(HIDE_ON_UNHOVER)){
				stringBuilder.append(", hideOnUnhover:"+argsMap.get(HIDE_ON_UNHOVER));
			}
			if(argsMap.containsKey(INLINE)){
				stringBuilder.append(", inline:"+argsMap.get(INLINE));
			}
			if(argsMap.containsKey(FIT)){
				stringBuilder.append(", fit:"+argsMap.get(FIT));
			}
			if(argsMap.containsKey(NOLINE)){
				stringBuilder.append(", noline:"+argsMap.get(NOLINE));
			}
			if(argsMap.containsKey(ON_CLICK)){
				stringBuilder.append(", onClick:"+argsMap.get(ON_CLICK));
			}
			stringBuilder.append("\">"+ LINE_SEPARATOR);

			Iterator it =list.listIterator();
			//构建子菜单
			while(it.hasNext()) {
				Object row = it.next();
				//校验text和id必须有，没有就略过，从list中移除掉
				if (getData(row, textField, isMap) == null || getData(row, idField, isMap) == null) {
					it.remove();
					continue;
				}
				Object parentId = getData(row, parentIdField, isMap);
				//parentId有可能为空，所以在右侧
				if (rootId.equals(parentId)) {
					appendMenuItem(stringBuilder, list, parentId, isMap, textField, idField, parentIdField, iconClsField, disabledField,  "", argsMap);
				}

			}
			stringBuilder.append("</div>"+ LINE_SEPARATOR);
		}
		ctx.byteWriter.writeString(stringBuilder.toString());
	}

	/**
	 * 递归添加子菜单
	 * @param stringBuilder
	 * @param list
	 * @param parentId
	 * @param isMap
	 * @param textField
	 * @param idField
	 * @param parentIdField
	 * @param tab
	 */
	public void appendMenuItem(StringBuilder stringBuilder, List list, Object parentId, boolean isMap, String textField, String idField, String parentIdField, String iconClsField, String disabledField, String tab, Map<String, String> argsMap){
		Iterator it =list.listIterator();
		//构建子菜单
		while(it.hasNext()) {
			Object row = it.next();
			Object currentParentId = getData(row, parentIdField, isMap);
			Object id = getData(row, idField, isMap);
			//在叶节点完成后将id设置为空，这里跳过以提升性能(因为即使是Iterator，在递归中也会有报ConcurrentModificationException)
			if(id == null){
				continue;
			}
			String iconCls = getData(row, iconClsField, isMap);
			String disabled = getData(row, disabledField, isMap);
			//currentParentId有可能为空，所以在右侧
			if (parentId.equals(currentParentId)) {
				//选拼接<div>开始标签MenuItem属性和内容
				if(argsMap.containsKey(ITEM_WIDTH)){
					//-3个像素，避免菜单之间的间隔
					Integer itemWidth = Integer.parseInt(argsMap.get(ITEM_WIDTH)) - 3;
					stringBuilder.append(tab).append(TAB).append("<div parentId=\""+parentId+"\" style=\"width:" + itemWidth + "px;\" id=\""+id+"\" data-options=\"blankKey:''");
				}else {
					stringBuilder.append(tab).append(TAB).append("<div parentId=\""+parentId+"\" id=\""+id+"\" data-options=\"blankKey:''");
				}
				stringBuilder.append("," + getDataOptions(row, isMap));
				if (iconCls != null) {
					stringBuilder.append(", iconCls:'" + iconCls + "'");
				}
				if (disabled != null) {
					stringBuilder.append(", disabled:" + disabled);
				}
				stringBuilder.append(" \">").append(LINE_SEPARATOR);
				stringBuilder.append(tab).append(TAB).append(TAB).append(getData(row, textField, isMap)).append(LINE_SEPARATOR);
				//如果有子节点，就拼接子的<div>开始，然后再递归
				if (hasChild(list, id, isMap, parentIdField)) {
					if(argsMap.containsKey(ITEM_WIDTH)){
						stringBuilder.append(tab).append(TAB).append(TAB).append("<div style=\"width:" + argsMap.get(ITEM_WIDTH) + "px;\">").append(LINE_SEPARATOR);
					}else{
						stringBuilder.append(tab).append(TAB).append(TAB).append("<div>").append(LINE_SEPARATOR);
					}

					appendMenuItem(stringBuilder, list, id, isMap, textField, idField, parentIdField, iconClsField, disabledField, tab + TAB + TAB, argsMap);
					//递归子节点完了关闭</div>
					stringBuilder.append(tab).append(TAB).append(TAB).append("</div>").append(LINE_SEPARATOR);
					stringBuilder.append(tab).append(TAB).append("</div>").append(LINE_SEPARATOR);
					setData(row, "id", null , isMap);
				} else {
					//如果没有子标签就直接关闭</div>
					stringBuilder.append(tab).append(TAB).append("</div>").append(LINE_SEPARATOR);
					//设置id为空，在下次进入时判断为空不处理，以提升性能(因为即使是Iterator，在递归中也会有报ConcurrentModificationException)
					setData(row, idField, null, isMap);
				}
			}
		}
	}

	/**
	 * List深拷贝
	 * @param src
	 * @return
	 */
	private List deepCopyList(List src){
		List<String> dest = new ArrayList<String>(Arrays.asList(new String[src.size()]));
		Collections.copy(dest, src);
		return dest;
	}

	/**
	 * 判断该节点下是否有子节点, id不允许为空，id为空返回false
	 * @param list
	 * @param id
	 * @param isMap
	 * @param parentIdField
	 * @return
	 */
	private boolean hasChild(List list, Object id, boolean isMap, String parentIdField){
		if(id == null){
			return false;
		}
		for(Object row : list){
			if(id.equals(getData(row, parentIdField, isMap))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据key从Map或Bean中获取值
	 * @param row
	 * @param key
	 * @param isMap
	 * @return
	 */
	private String getData(Object row, String key, Boolean isMap){
		Object data = isMap ? ((Map)row).get(key) : POJOUtils.getProperty(row, key);
		return data == null ? null : data.toString();
	}

	/**
	 * 根据对象获取data-options串
	 * @param row
	 * @param isMap
	 * @return
	 */
	private String getDataOptions(Object row, Boolean isMap){
		StringBuilder stringBuilder = new StringBuilder();
		if(isMap){
			Map map = (Map) row;
			map.forEach((key, value) ->{
				if(value != null) {
					stringBuilder.append(", "+key+":'"+value.toString()+"'");
				}
			});
			return stringBuilder.substring(1, stringBuilder.length());
		}else{
			Method[] methods = row.getClass().getMethods();
			for(Method method : methods){
				//get方法，且不能有参数
				if(POJOUtils.isGetMethod(method) && method.getParameters().length == 0){
					String fieldName = POJOUtils.getBeanField(method);
					try {
						Object value = getObjectStringValue(method.invoke(row));
						if(value != null){
							stringBuilder.append(", "+fieldName+":'"+value+"'");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return stringBuilder.substring(1, stringBuilder.length());
		}
	}

	/**
	 * 获取Object对象的String值，主要处理时间和日期类型
	 * @param obj
	 * @return
	 */
	private String getObjectStringValue (Object obj){
		if(obj == null) {
			return null;
		}
		if(obj instanceof Instant){
			//输出yyyy-MM-dd HH:mm:ss格式字符串
			return DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(((Instant)obj));
		}
		if(obj instanceof LocalDateTime){
			//输出yyyy-MM-dd HH:mm:ss格式字符串
			return ((LocalDateTime)obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(obj instanceof Date){
			return sdf.format((Date)obj);
		}
		return String.valueOf(obj);
	}

	/**
	 * 从Map或Bean中根据key设置value
	 * @param row
	 * @param key
	 * @param value
	 * @param isMap
	 */
	private void setData(Object row, String key, Object value,  Boolean isMap){
		if(isMap){
			((Map)row).put(key, value);
		}else{
			POJOUtils.setProperty(row, key, value);
		}
	}

	/**
	 * 获取Map类型的根节点
	 * @param list
	 * @param parentIdField
	 * @return
	 */
	private List getMapRoots(List<Map> list, String parentIdField){
		List<Map> rootLists = Lists.newArrayList();
		for(Map map : list){
			Object parentId = getData(map, parentIdField, true);
			if(parentId == null || "".equals(parentId) || "-1".equals(parentId)){
				rootLists.add(map);
			}
		}
		return rootLists;
	}

	/**
	 * 获取Bean类型的根节点
	 * @param list
	 * @param parentIdField
	 * @return
	 */
	private List getBeanRoots(List list, String parentIdField){
		List rootLists = Lists.newArrayList();
		for(Object bean : list){
			Object parentId = getData(bean, parentIdField, false);
			if(parentId == null || "".equals(parentId) || "-1".equals(parentId)){
				rootLists.add(bean);
			}
		}
		return rootLists;
	}

	/**
	 * 删除source中的dest集合
	 * @param source
	 * @param dest
	 */
	private static void removeListElement(List source, List dest){
		Iterator i = source.listIterator();
		while(i.hasNext()){
			Object o = i.next();
			for(Object s : dest){
				if(o.equals(s)){
					i.remove();
				}
			}
		}
	}

	/**
	 * 删除source中的dest集合
	 * @param source
	 * @param dest
	 */
	private static void removeListElement(List source, Object ... dest){
		Iterator i = source.listIterator();
		while(i.hasNext()){
			Object o = i.next();
			for(Object s : dest){
				if(o.equals(s)){
					i.remove();
				}
			}
		}
	}
}
