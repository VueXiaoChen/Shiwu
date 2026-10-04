package com.fast.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fast.system.constants.RoleIdConstants;
import com.fast.system.domain.Menu;
import com.fast.system.domain.TreeSelect;
import com.fast.system.domain.User;
import com.fast.system.domain.vo.MetaVo;
import com.fast.system.domain.vo.RouterVo;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.mapper.UserMapper;
import com.fast.system.service.IMenuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层
 */
@Service
public class MenuServiceImpl implements IMenuService {
    @Resource
    private MenuMapper menuMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 查询菜单列表
     *
     * @param menu 查询参数
     * @return 菜单列表数据
     */
    @Override
    public List<Menu> selectMenuList(Menu menu, Long userId) {
        //根据用户ID查询对应的角色ID
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getRoleId).eq(User::getUserId, userId));
        Long roleId = u != null ? u.getRoleId() : null;

        //管理员显示所有菜单信息
        if (roleId.equals(RoleIdConstants.ADMIN_ROLE_ID)) {
            return menuMapper.selectMenuListByUserId(menu);
        } else {
            menu.setUserId(userId);
            return menuMapper.selectMenuListByUserId(menu);
        }
    }

    /**
     * 新增菜单
     *
     * @param menu 表单参数
     * @return 是否新增成功
     */
    @Override
    public int insertMenu(Menu menu) {
        return menuMapper.insert(menu);
    }

    /**
     * 修改菜单
     *
     * @param menu 表单参数
     * @return 是否修改成功
     */
    @Override
    public int updateMenu(Menu menu) {
        return menuMapper.updateById(menu);
    }

    /**
     * 根据角色ID查询对应菜单树
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        return menuMapper.selectMenuListByRoleId(roleId);
    }

    /**
     * 构建前端所需要的下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<Menu> menus) {
        //步骤1: 先将平铺的菜单列表转换为树形结构的Menu列表
        List<Menu> menuTrees = buildMenuTree(menus);

        //步骤2: 将树形Menu对象转换为TreeSelect对象
        return menuTrees.stream()
                .map(TreeSelect::new)
                .collect(Collectors.toList());
    }

    /**
     * 构建前端所需要的树结构
     */
    public List<Menu> buildMenuTree(List<Menu> menus) {
        //先去除重复的菜单项, 以menuId为键
        LinkedHashMap<Long, Menu> uniqueMenusMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            //如果菜单ID已存在, 则跳过, 只保留第一次出现的
            if (!uniqueMenusMap.containsKey(menu.getMenuId())) {
                uniqueMenusMap.put(menu.getMenuId(), menu);
            }
        }
        //转换为去重后的列表
        ArrayList<Menu> uniqueMenus = new ArrayList<>(uniqueMenusMap.values());
        //存放最终构建好的树形菜单列表
        ArrayList<Menu> returnList = new ArrayList<>();
        //将菜单列表转换为map, 快速查找
        HashMap<Long, Menu> menuMap = new HashMap<>();
        for (Menu menu : uniqueMenus) {
            menuMap.put(menu.getMenuId(), menu);
            //确保每个菜单都有初始化的children列表
            if (menu.getChildren() == null) {
                menu.setChildren(new ArrayList<>());
            } else {
                //清空现有的children, 避免重复添加
                menu.getChildren().clear();
            }
        }
        //查找顶级菜单(父ID为0或者null的菜单)
        HashSet<Long> addedTopMenuIds = new HashSet<>();
        for (Menu menu : uniqueMenus) {
            if (menu.getParentId() == 0 || menu.getParentId() == null) {
                //这是一个顶级菜单, 如果还没有添加过, 则加入到结果列表
                if (!addedTopMenuIds.contains(menu.getMenuId())) {
                    returnList.add(menu);
                    addedTopMenuIds.add(menu.getParentId());
                }
            } else {
                //不是顶级菜单, 将其添加到父菜单的子菜单列表中
                Menu parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.getChildren().add(menu);
                } else {
                    //如果没有找到父菜单, 暂时作为顶级菜单处理
                    if (!addedTopMenuIds.contains(menu.getMenuId())) {
                        returnList.add(menu);
                        addedTopMenuIds.add(menu.getMenuId());
                    }
                }
            }
        }

        return returnList;
    }

    /**
     * 根据用户ID查询该用户的菜单树并且构建成前端需要的路由格式
     *
     * @param userId 用户ID
     * @return 路由列表
     */
    @Override
    public List<RouterVo> selectMenuTreeRouterByUserId(Long userId) {
        //创建菜单查询条件对象
        Menu menu = new Menu();
        List<Menu> menus;

        //1.判断用户身份, 获取对应平铺的菜单列表数据
        //根据用户ID查询对应的角色ID
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getRoleId).eq(User::getUserId, userId));
        Long roleId = u != null ? u.getRoleId() : null;

        //管理员显示所有菜单信息
        if (roleId.equals(RoleIdConstants.ADMIN_ROLE_ID)) {
            menus = menuMapper.selectMenuListByUserId(menu);
        } else {
            menu.setUserId(userId);
            menus = menuMapper.selectMenuListByUserId(menu);
        }

        //2. 把菜单列表变成map, 方便根据ID快速查找
        HashMap<Long, Menu> menuMap = new HashMap<>();
        for (Menu m : menus) {
            menuMap.put(m.getMenuId(), m); //key=菜单ID, value=菜单对象
        }

        //3.构建树形结构(父子关系)
        List<Menu> rootMenus = new ArrayList<>(); //存储所有的根节点菜单
        for (Menu m : menus) {
            //清空children列表, 避免数据婚恋
            m.setChildren(new ArrayList<>());
            //判断是否是根节点菜单(父ID=0)
            if (m.getParentId() == 0) {
                //根节点: 直接诶添加到根菜单列表
                rootMenus.add(m);
            } else {
                //不是根节点: 找到它的父菜单, 把自己加入到父菜单的children中
                Menu parent = menuMap.get(m.getParentId());
                if (parent != null) {
                    parent.getChildren().add(m);
                }
            }
        }

        //4.把构建好的树形菜单转换成前端需要的格式路由
        return buildMenus(rootMenus);
    }

    /**
     * 构建前端需要的路由格式
     */
    private List<RouterVo> buildMenus(List<Menu> menus) {
        //创建一个空的LinkedList来存放所有转换好的路由
        LinkedList<RouterVo> routers = new LinkedList<>();

        //开始处理菜单列表, 使用for循环遍历每一个菜单项
        for (Menu menu : menus) {
            //1.创建一个新的前端路由对象
            RouterVo router = new RouterVo();

            //2.设置路由的名称(name属性)
            String routerName = menu.getPath();
            if (menu.getParentId() == 0 && menu.getMenuId() != null) {
                //对于顶级菜单, 确保名称唯一, 添加菜单ID后缀
                routerName = menu.getPath() + '_' + menu.getMenuId();
            }
            router.setName(routerName);

            //3.设置路由的访问路径(path属性)
            //先获取菜单在数据库中的原始字符串路径, 比如"system/user/index"
            String routerPath = menu.getPath();

            //情况1: 如果这个菜单是一级目录(顶级目录)
            if (menu.getParentId().intValue() == 0 && menu.getMenuType().equals("M")) {
                //一级目录的路径需要以斜杠开头
                //转换前: "system" -> 转换后: "/system"
                routerPath = "/" + menu.getPath();
            }

            //情况2: 如果这个菜单是一级菜单(不是目录, 是具体的页面)
            else if (menu.getParentId().intValue() == 0 && menu.getMenuType().equals("C")) {
                //一级菜单的路径设置为根路径
                routerPath = "/";
            }

            //情况3: 其他情况(二级菜单或者以下的菜单)
            router.setPath(routerPath);

            //4.设置路由对应的组件(component属性)
            //情况1: 如果菜单配置了组件, 并且不是特殊情况(不是一级C类型的菜单)
            String component = "Layout";
            if (menu.getComponent() != null && !menu.getComponent().isEmpty()
                    && !(menu.getParentId().intValue() == 0 && "C".equals(menu.getMenuType()))) {
                //使用菜单配置的组件路径
                component = menu.getComponent();
            }

            //情况2: 如果菜单没有配置组件, 但是它是二级或者以上的目录
            else if ((menu.getComponent() == null || menu.getComponent().isEmpty())
                    && (menu.getParentId().intValue() != 0 && "M".equals(menu.getMenuType()))) {
                component = "ParentView";
            }

            //情况3: 其他情况(包括一级目录 或者没有组件的一级菜单等等情况)
            router.setComponent(component);

            //5.设置路由的元数据(meta属性)
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));

            //6.处理子菜单(递归处理)
            List<Menu> cMenus = menu.getChildren();

            //情况1: 如果当前菜单有子菜单, 并且它是目录(M)
            if (!cMenus.isEmpty() && "M".equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setChildren(buildMenus(cMenus));
            }

            //情况2: 特殊情况处理 - 一级菜单(C类型)
            else if (menu.getParentId().intValue() == 0 && "C".equals(menu.getMenuType())) {
                //1.清空当前路由的meta
                router.setMeta(null);
                //2.创建一个子路由列表
                ArrayList<RouterVo> childrenList = new ArrayList<RouterVo>();
                //3.创建一个子路由对象
                RouterVo children = new RouterVo();
                //4.设置子路由的各个属性
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(menu.getPath());
                //5.创建子路由的meta
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                //6.将子路由添加到列表中
                childrenList.add(children);
                //7.将子路由列表设置到父路由上
                router.setChildren(childrenList);
            }

            //情况3: 普通页面菜单(没有子菜单的C类型菜单)
            routers.add(router);
        }
        //返回所有转换好的路由
        return routers;
    }

}
