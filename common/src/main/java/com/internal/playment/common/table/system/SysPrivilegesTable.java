package com.internal.playment.common.table.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description  
 * @Author  monkey
 * @Date 2019-11-13 
 */

@TableName ( "1_sys_privileges_table" )
@Data
public class SysPrivilegesTable implements Serializable {
   
	private Long id;//权限主键
	private String name;//权限名称
	private String description;//权限描述
	private Long parentId;//父级名称
	private String stateName;//页面菜单：声明/描述
	private String iconFont;//页面菜单：图标class
	@TableField(exist = false)
	private Boolean available;//是否可用
	private Date createTime;//创建时间
	private Date updateTime;//修改时间
	private Integer status;//是否可用
	@TableField(exist = false)
	private List<Long> ids;
	// 子菜单
	@TableField(exist = false)
	private List<SysPrivilegesTable> submenu;

}
