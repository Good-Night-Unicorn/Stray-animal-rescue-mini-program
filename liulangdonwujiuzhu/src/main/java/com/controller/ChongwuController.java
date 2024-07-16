
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
 * 寻宠
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/chongwu")
public class ChongwuController {
    private static final Logger logger = LoggerFactory.getLogger(ChongwuController.class);

    private static final String TABLE_NAME = "chongwu";

    @Autowired
    private ChongwuService chongwuService;


    @Autowired
    private TokenService tokenService;

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
    private DongwuYuyueService dongwuYuyueService;//求助报名
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
        params.put("chongwuDeleteStart",1);params.put("chongwuDeleteEnd",1);
        CommonUtil.checkMap(params);
        PageUtils page = chongwuService.queryPage(params);

        //字典表数据转换
        List<ChongwuView> list =(List<ChongwuView>)page.getList();
        for(ChongwuView c:list){
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
        ChongwuEntity chongwu = chongwuService.selectById(id);
        if(chongwu !=null){
            //entity转view
            ChongwuView view = new ChongwuView();
            BeanUtils.copyProperties( chongwu , view );//把实体数据重构到view中
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
    public R save(@RequestBody ChongwuEntity chongwu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,chongwu:{}",this.getClass().getName(),chongwu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<ChongwuEntity> queryWrapper = new EntityWrapper<ChongwuEntity>()
            .eq("chongwu_name", chongwu.getChongwuName())
            .eq("chongwu_address", chongwu.getChongwuAddress())
            .eq("zan_number", chongwu.getZanNumber())
            .eq("cai_number", chongwu.getCaiNumber())
            .eq("chongwu_types", chongwu.getChongwuTypes())
            .eq("chongwu_delete", 1)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ChongwuEntity chongwuEntity = chongwuService.selectOne(queryWrapper);
        if(chongwuEntity==null){
            chongwu.setZanNumber(1);
            chongwu.setCaiNumber(1);
            chongwu.setChongwuClicknum(1);
            chongwu.setChongwuDelete(1);
            chongwu.setInsertTime(new Date());
            chongwu.setCreateTime(new Date());
            chongwuService.insert(chongwu);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ChongwuEntity chongwu, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,chongwu:{}",this.getClass().getName(),chongwu.toString());
        ChongwuEntity oldChongwuEntity = chongwuService.selectById(chongwu.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        if("".equals(chongwu.getChongwuPhoto()) || "null".equals(chongwu.getChongwuPhoto())){
                chongwu.setChongwuPhoto(null);
        }
        if("".equals(chongwu.getChongwuContent()) || "null".equals(chongwu.getChongwuContent())){
                chongwu.setChongwuContent(null);
        }

            chongwuService.updateById(chongwu);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<ChongwuEntity> oldChongwuList =chongwuService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        ArrayList<ChongwuEntity> list = new ArrayList<>();
        for(Integer id:ids){
            ChongwuEntity chongwuEntity = new ChongwuEntity();
            chongwuEntity.setId(id);
            chongwuEntity.setChongwuDelete(2);
            list.add(chongwuEntity);
        }
        if(list != null && list.size() >0){
            chongwuService.updateBatchById(list);
        }

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
            List<ChongwuEntity> chongwuList = new ArrayList<>();//上传的东西
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
                            ChongwuEntity chongwuEntity = new ChongwuEntity();
//                            chongwuEntity.setChongwuName(data.get(0));                    //寻宠标题 要改的
//                            chongwuEntity.setChongwuPhoto("");//详情和图片
//                            chongwuEntity.setChongwuAddress(data.get(0));                    //位置 要改的
//                            chongwuEntity.setZanNumber(Integer.valueOf(data.get(0)));   //赞 要改的
//                            chongwuEntity.setCaiNumber(Integer.valueOf(data.get(0)));   //踩 要改的
//                            chongwuEntity.setChongwuTypes(Integer.valueOf(data.get(0)));   //寻宠类型 要改的
//                            chongwuEntity.setChongwuClicknum(Integer.valueOf(data.get(0)));   //寻宠热度 要改的
//                            chongwuEntity.setChongwuContent("");//详情和图片
//                            chongwuEntity.setChongwuDelete(1);//逻辑删除字段
//                            chongwuEntity.setInsertTime(date);//时间
//                            chongwuEntity.setCreateTime(date);//时间
                            chongwuList.add(chongwuEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        chongwuService.insertBatch(chongwuList);
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
    * 个性推荐
    */
    @IgnoreAuth
    @RequestMapping("/gexingtuijian")
    public R gexingtuijian(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("gexingtuijian方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        CommonUtil.checkMap(params);
        List<ChongwuView> returnChongwuViewList = new ArrayList<>();

        //查看收藏
        Map<String, Object> params1 = new HashMap<>(params);params1.put("sort","id");params1.put("yonghuId",request.getSession().getAttribute("userId"));
        params1.put("shangxiaTypes",1);
        params1.put("chongwuYesnoTypes",2);
        PageUtils pageUtils = chongwuCollectionService.queryPage(params1);
        List<ChongwuCollectionView> collectionViewsList =(List<ChongwuCollectionView>)pageUtils.getList();
        Map<Integer,Integer> typeMap=new HashMap<>();//购买的类型list
        for(ChongwuCollectionView collectionView:collectionViewsList){
            Integer chongwuTypes = collectionView.getChongwuTypes();
            if(typeMap.containsKey(chongwuTypes)){
                typeMap.put(chongwuTypes,typeMap.get(chongwuTypes)+1);
            }else{
                typeMap.put(chongwuTypes,1);
            }
        }
        List<Integer> typeList = new ArrayList<>();//排序后的有序的类型 按最多到最少
        typeMap.entrySet().stream().sorted((o1, o2) -> o2.getValue() - o1.getValue()).forEach(e -> typeList.add(e.getKey()));//排序
        Integer limit = Integer.valueOf(String.valueOf(params.get("limit")));
        for(Integer type:typeList){
            Map<String, Object> params2 = new HashMap<>(params);params2.put("chongwuTypes",type);
            params2.put("shangxiaTypes",1);
            params2.put("chongwuYesnoTypes",2);
            PageUtils pageUtils1 = chongwuService.queryPage(params2);
            List<ChongwuView> chongwuViewList =(List<ChongwuView>)pageUtils1.getList();
            returnChongwuViewList.addAll(chongwuViewList);
            if(returnChongwuViewList.size()>= limit) break;//返回的推荐数量大于要的数量 跳出循环
        }
        params.put("shangxiaTypes",1);
        params.put("chongwuYesnoTypes",2);
        //正常查询出来商品,用于补全推荐缺少的数据
        PageUtils page = chongwuService.queryPage(params);
        if(returnChongwuViewList.size()<limit){//返回数量还是小于要求数量
            int toAddNum = limit - returnChongwuViewList.size();//要添加的数量
            List<ChongwuView> chongwuViewList =(List<ChongwuView>)page.getList();
            for(ChongwuView chongwuView:chongwuViewList){
                Boolean addFlag = true;
                for(ChongwuView returnChongwuView:returnChongwuViewList){
                    if(returnChongwuView.getId().intValue() ==chongwuView.getId().intValue()) addFlag=false;//返回的数据中已存在此商品
                }
                if(addFlag){
                    toAddNum=toAddNum-1;
                    returnChongwuViewList.add(chongwuView);
                    if(toAddNum==0) break;//够数量了
                }
            }
        }else {
            returnChongwuViewList = returnChongwuViewList.subList(0, limit);
        }

        for(ChongwuView c:returnChongwuViewList)
            dictionaryService.dictionaryConvert(c, request);
        page.setList(returnChongwuViewList);
        return R.ok().put("data", page);
    }

    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = chongwuService.queryPage(params);

        //字典表数据转换
        List<ChongwuView> list =(List<ChongwuView>)page.getList();
        for(ChongwuView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Integer id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ChongwuEntity chongwu = chongwuService.selectById(id);
            if(chongwu !=null){

                //点击数量加1
                chongwu.setChongwuClicknum(chongwu.getChongwuClicknum()+1);
                chongwuService.updateById(chongwu);

                //entity转view
                ChongwuView view = new ChongwuView();
                BeanUtils.copyProperties( chongwu , view );//把实体数据重构到view中

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
    public R add(@RequestBody ChongwuEntity chongwu, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,chongwu:{}",this.getClass().getName(),chongwu.toString());
        Wrapper<ChongwuEntity> queryWrapper = new EntityWrapper<ChongwuEntity>()
            .eq("chongwu_name", chongwu.getChongwuName())
            .eq("chongwu_address", chongwu.getChongwuAddress())
            .eq("zan_number", chongwu.getZanNumber())
            .eq("cai_number", chongwu.getCaiNumber())
            .eq("chongwu_types", chongwu.getChongwuTypes())
            .eq("chongwu_clicknum", chongwu.getChongwuClicknum())
            .eq("chongwu_delete", chongwu.getChongwuDelete())
//            .notIn("chongwu_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ChongwuEntity chongwuEntity = chongwuService.selectOne(queryWrapper);
        if(chongwuEntity==null){
                chongwu.setZanNumber(1);
                chongwu.setCaiNumber(1);
            chongwu.setChongwuClicknum(1);
            chongwu.setChongwuDelete(1);
            chongwu.setInsertTime(new Date());
            chongwu.setCreateTime(new Date());
        chongwuService.insert(chongwu);

            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}

