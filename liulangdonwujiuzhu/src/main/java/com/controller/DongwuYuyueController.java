
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 求助报名
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/dongwuYuyue")
public class DongwuYuyueController {
    private static final Logger logger = LoggerFactory.getLogger(DongwuYuyueController.class);

    private static final String TABLE_NAME = "dongwuYuyue";

    @Autowired
    private DongwuYuyueService dongwuYuyueService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private ChongwuService chongwuService;//寻宠
    @Autowired
    private ChongwuCollectionService chongwuCollectionService;//寻宠收藏
    @Autowired
    private ChongwuLiuyanService chongwuLiuyanService;//寻宠留言
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private DongwuService dongwuService;//动物求助
    @Autowired
    private DongwuCollectionService dongwuCollectionService;//求助收藏
    @Autowired
    private DongwuLiuyanService dongwuLiuyanService;//求助留言
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private NewsService newsService;//公告通知
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = dongwuYuyueService.queryPage(params);

        //字典表数据转换
        List<DongwuYuyueView> list =(List<DongwuYuyueView>)page.getList();
        for(DongwuYuyueView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        DongwuYuyueEntity dongwuYuyue = dongwuYuyueService.selectById(id);
        if(dongwuYuyue !=null){
            //entity转view
            DongwuYuyueView view = new DongwuYuyueView();
            BeanUtils.copyProperties( dongwuYuyue , view );//把实体数据重构到view中
            //级联表 动物求助
            //级联表
            DongwuEntity dongwu = dongwuService.selectById(dongwuYuyue.getDongwuId());
            if(dongwu != null){
            BeanUtils.copyProperties( dongwu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setDongwuId(dongwu.getId());
            }
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(dongwuYuyue.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody DongwuYuyueEntity dongwuYuyue, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,dongwuYuyue:{}",this.getClass().getName(),dongwuYuyue.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            dongwuYuyue.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<DongwuYuyueEntity> queryWrapper = new EntityWrapper<DongwuYuyueEntity>()
            .eq("dongwu_id", dongwuYuyue.getDongwuId())
            .eq("yonghu_id", dongwuYuyue.getYonghuId())
            .in("dongwu_yuyue_yesno_types", new Integer[]{1,2})
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        DongwuYuyueEntity dongwuYuyueEntity = dongwuYuyueService.selectOne(queryWrapper);
        if(dongwuYuyueEntity==null){
            dongwuYuyue.setDongwuYuyueYesnoTypes(1);
            dongwuYuyue.setInsertTime(new Date());
            dongwuYuyue.setCreateTime(new Date());
            dongwuYuyueService.insert(dongwuYuyue);
            return R.ok();
        }else {
            if(dongwuYuyueEntity.getDongwuYuyueYesnoTypes()==1)
                return R.error(511,"有相同的待审核的数据");
            else if(dongwuYuyueEntity.getDongwuYuyueYesnoTypes()==2)
                return R.error(511,"有相同的审核通过的数据");
            else
                return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody DongwuYuyueEntity dongwuYuyue, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,dongwuYuyue:{}",this.getClass().getName(),dongwuYuyue.toString());
        DongwuYuyueEntity oldDongwuYuyueEntity = dongwuYuyueService.selectById(dongwuYuyue.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            dongwuYuyue.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        if("".equals(dongwuYuyue.getDongwuYuyueText()) || "null".equals(dongwuYuyue.getDongwuYuyueText())){
                dongwuYuyue.setDongwuYuyueText(null);
        }
        if("".equals(dongwuYuyue.getDongwuYuyueYesnoText()) || "null".equals(dongwuYuyue.getDongwuYuyueYesnoText())){
                dongwuYuyue.setDongwuYuyueYesnoText(null);
        }

            dongwuYuyueService.updateById(dongwuYuyue);//根据id更新
            return R.ok();
    }


    /**
    * 审核
    */
    @RequestMapping("/shenhe")
    public R shenhe(@RequestBody DongwuYuyueEntity dongwuYuyueEntity, HttpServletRequest request){
        logger.debug("shenhe方法:,,Controller:{},,dongwuYuyueEntity:{}",this.getClass().getName(),dongwuYuyueEntity.toString());

        DongwuYuyueEntity oldDongwuYuyue = dongwuYuyueService.selectById(dongwuYuyueEntity.getId());//查询原先数据

        if(dongwuYuyueEntity.getDongwuYuyueYesnoTypes() == 2){//通过
            DongwuEntity dongwuEntity = new DongwuEntity();
            dongwuEntity.setId(oldDongwuYuyue.getDongwuId());
            dongwuEntity.setQiuzhuzhuangtaiTypes(2);
            dongwuService.updateById(dongwuEntity);
        }
        dongwuYuyueService.updateById(dongwuYuyueEntity);//审核

        return R.ok();
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<DongwuYuyueEntity> oldDongwuYuyueList =dongwuYuyueService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        dongwuYuyueService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<DongwuYuyueEntity> dongwuYuyueList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            DongwuYuyueEntity dongwuYuyueEntity = new DongwuYuyueEntity();
//                            dongwuYuyueEntity.setDongwuId(Integer.valueOf(data.get(0)));   //求助 要改的
//                            dongwuYuyueEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            dongwuYuyueEntity.setDongwuYuyueText(data.get(0));                    //领养缘由 要改的
//                            dongwuYuyueEntity.setDongwuYuyueYesnoTypes(Integer.valueOf(data.get(0)));   //审核状态 要改的
//                            dongwuYuyueEntity.setDongwuYuyueYesnoText(data.get(0));                    //审核回复 要改的
//                            dongwuYuyueEntity.setInsertTime(date);//时间
//                            dongwuYuyueEntity.setCreateTime(date);//时间
                            dongwuYuyueList.add(dongwuYuyueEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        dongwuYuyueService.insertBatch(dongwuYuyueList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = dongwuYuyueService.queryPage(params);

        //字典表数据转换
        List<DongwuYuyueView> list =(List<DongwuYuyueView>)page.getList();
        for(DongwuYuyueView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Integer id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        DongwuYuyueEntity dongwuYuyue = dongwuYuyueService.selectById(id);
            if(dongwuYuyue !=null){


                //entity转view
                DongwuYuyueView view = new DongwuYuyueView();
                BeanUtils.copyProperties( dongwuYuyue , view );//把实体数据重构到view中

                //级联表
                    DongwuEntity dongwu = dongwuService.selectById(dongwuYuyue.getDongwuId());
                if(dongwu != null){
                    BeanUtils.copyProperties( dongwu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setDongwuId(dongwu.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(dongwuYuyue.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody DongwuYuyueEntity dongwuYuyue, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,dongwuYuyue:{}",this.getClass().getName(),dongwuYuyue.toString());
        Wrapper<DongwuYuyueEntity> queryWrapper = new EntityWrapper<DongwuYuyueEntity>()
            .eq("dongwu_id", dongwuYuyue.getDongwuId())
            .eq("yonghu_id", dongwuYuyue.getYonghuId())
            .eq("dongwu_yuyue_text", dongwuYuyue.getDongwuYuyueText())
            .in("dongwu_yuyue_yesno_types", new Integer[]{1,2})
            .eq("dongwu_yuyue_yesno_text", dongwuYuyue.getDongwuYuyueYesnoText())
//            .notIn("dongwu_yuyue_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        DongwuYuyueEntity dongwuYuyueEntity = dongwuYuyueService.selectOne(queryWrapper);
        if(dongwuYuyueEntity==null){
            dongwuYuyue.setDongwuYuyueYesnoTypes(1);
            dongwuYuyue.setInsertTime(new Date());
            dongwuYuyue.setCreateTime(new Date());
        dongwuYuyueService.insert(dongwuYuyue);

            return R.ok();
        }else {
            if(dongwuYuyueEntity.getDongwuYuyueYesnoTypes()==1)
                return R.error(511,"有相同的待审核的数据");
            else if(dongwuYuyueEntity.getDongwuYuyueYesnoTypes()==2)
                return R.error(511,"有相同的审核通过的数据");
            else
                return R.error(511,"表中有相同数据");
        }
    }

}

