package com.yizhi.student.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yizhi.common.annotation.Log;
import com.yizhi.common.controller.BaseController;
import com.yizhi.common.utils.*;
import com.yizhi.student.domain.ClassDO;
import com.yizhi.student.domain.CollegeDO;
import com.yizhi.student.service.ClassService;
import com.yizhi.student.service.CollegeService;
import com.yizhi.student.service.MajorService;
import com.yizhi.system.domain.UserDO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.yizhi.student.domain.StudentInfoDO;
import com.yizhi.student.service.StudentInfoService;

import javax.servlet.http.HttpServletRequest;

/**
 * 生基础信息表
 */
 
@Controller
@RequestMapping("/student/studentInfo")
public class StudentInfoController {

	
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private StudentInfoService studentInfoService;
    //
	@Log("学生信息保存")
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("student:studentInfo:add")
	public R save(StudentInfoDO studentInfoDO) throws ParseException {
		// 指定日期格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		// 获取当前时间
		LocalDateTime now = LocalDateTime.now();
		// 格式化日期
		String formatDateTime = now.format(formatter);
		// 将格式化后的日期字符串转化为日期
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formatDateTime);
		// 设置时间
		studentInfoDO.setAddTime(date);
		studentInfoDO.setEditTime(date);
		studentInfoDO.setEditUserid(Math.toIntExact(ShiroUtils.getUserId()));
		studentInfoDO.setAddUserid(Math.toIntExact(ShiroUtils.getUserId()));
		studentInfoService.save(studentInfoDO);
		return R.ok();
	}

	// 获取 RedisTemplate 对象

	/**
	 * 可分页 查询
	 */
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("student:studentInfo:studentInfo")
	public PageUtils list(@RequestParam Map<String, Object> params){
		params.put("tocollege",params.get("tocollegeId"));
		params.put("tomajor",params.get("tomajorId"));
		params.put("studentName",params.get("name"));
		params.remove("tocollegeId");
		params.remove("tomajorId");
		params.remove("name");
		if (params.get("sort")!=null) {
			params.put("sort",BeanHump.camelToUnderline(params.get("sort").toString()));
		}
		Query query = new Query(params);
		List<StudentInfoDO> list = studentInfoService.list(query);
		int count = studentInfoService.count(query);
		PageUtils pageUtils = new PageUtils(list,count,query.getCurrPage(), query.getCurrPage());
		//查询列表数据
		return pageUtils;
	}

	/**
	 * 修改
	 */
	@Log("学生基础信息表修改")
	@ResponseBody
	@PostMapping("/update")
	@RequiresPermissions("student:studentInfo:edit")
	public R update(StudentInfoDO studentInfo) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String formatDateTime = now.format(formatter);
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formatDateTime);
		studentInfo.setEditTime(date);
		studentInfo.setEditUserid(Math.toIntExact(ShiroUtils.getUserId()));
		studentInfoService.update(studentInfo);
		return R.ok();
	}

	/**
	 * 删除
	 */
	@Log("学生基础信息表删除")
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("student:studentInfo:remove")
	public R remove( Integer id){
		studentInfoService.remove(id);
		return R.ok();
	}
	
	/**
	 * 批量删除
	 */
	@Log("学生基础信息表批量删除")
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("student:studentInfo:batchRemove")
	public R remove(@RequestParam("ids[]") Integer[] ids){
		studentInfoService.batchRemove(ids);
		return R.ok();
	}


	//前后端不分离 客户端 -> 控制器-> 定位视图
	/**
	 * 学生管理 点击Tab标签 forward页面
	 */
	@GetMapping()
	@RequiresPermissions("student:studentInfo:studentInfo")
	String StudentInfo(){
		return "student/studentInfo/studentInfo";
	}

	/**
	 * 更新功能 弹出View定位
	 */
	@GetMapping("/edit/{id}")
	@RequiresPermissions("student:studentInfo:edit")
	String edit(@PathVariable("id") Integer id,Model model){
		StudentInfoDO studentInfo = studentInfoService.get(id);
		model.addAttribute("studentInfo", studentInfo);
		return "student/studentInfo/edit";
	}

	/**
	 * 学生管理 添加学生弹出 View
	 */
	@GetMapping("/add")
	@RequiresPermissions("student:studentInfo:add")
	String add(){
	    return "student/studentInfo/add";
	}
	
}//end class
