package mczme.foodieshop.data.language;

import mczme.foodieshop.FoodieShop;
import mczme.foodieshop.registry.ModBlocks;
import mczme.foodieshop.registry.ModEntityTypes;
import mczme.foodieshop.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ZH_CN extends LanguageProvider {

    public ZH_CN(PackOutput output) {
        super(output, FoodieShop.MODID, "zh_cn");
        
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + FoodieShop.MODID + ".foodieshop", "食客店铺");

        // 实体
        addEntityType(ModEntityTypes.FOODIE, "食客");

        // 方块
        addBlock(ModBlocks.CASHIER_DESK_BLOCK, "收银台");

        // 物品
        addItem(ModItems.DINER_BLUEPRINT_PEN, "店铺蓝图笔");
    
        // 信息
        add("message.foodieshop.diner_blueprint_pen.bind_success", "成功绑定店铺位置: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.switch_mode", "切换到 %s 模式");
        add("message.foodieshop.diner_blueprint_pen.set_area_pos2", "设置店铺区域位置2: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.set_area_pos1", "设置店铺区域位置1: %d, %d, %d");
        add("setup_mode.foodieshop.shop_area", "店铺区域");
        add("setup_mode.foodieshop.seat", "座位");
        add("setup_mode.foodieshop.table", "桌子");
        add("setup_mode.foodieshop.path", "路径");
        add("message.foodieshop.diner_blueprint_pen.add_seat", "添加座位: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.add_table", "添加桌子: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.remove_seat", "移除座位: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.remove_table", "移除桌子: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.path_node_added", "路径节点已添加: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.path_node_removed", "路径节点已移除: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.path_connection_start", "已选择路径连接起点: %d, %d, %d. 请点击另一节点以连接, 或再次点击以删除该节点.");
        add("message.foodieshop.diner_blueprint_pen.path_connection_cancelled", "路径连接已取消");
        add("message.foodieshop.diner_blueprint_pen.path_edge_created", "路径连接已创建: 从 [%d, %d, %d] 到 [%d, %d, %d]");
        add("message.foodieshop.diner_blueprint_pen.path_edge_removed", "路径连接已移除: 从 [%d, %d, %d] 到 [%d, %d, %d]");
        add("message.foodieshop.shop_area_not_set", "请先设置店铺范围！");
        add("message.foodieshop.pos_not_in_area", "操作位置不在店铺范围内！");

        // GUI
        add("gui.foodieshop.shop_config.tab.general", "基础设置");
        add("gui.foodieshop.shop_config.tab.layout", "区域与布局");
        add("gui.foodieshop.shop_config.tab.save", "保存与验证");
        add("gui.foodieshop.close", "关闭");
        add("gui.foodieshop.save", "保存");
        add("gui.foodieshop.shop_config.select_container", "选择容器");
        add("gui.foodieshop.shop_config.clear_link", "清除");
        add("gui.foodieshop.shop_config.owner_uuid", "店主UUID:");
        add("gui.foodieshop.shop_config.cashier_pos", "收银台坐标:");
        add("gui.foodieshop.shop_config.menu_container", "菜单库存容器:");
        add("gui.foodieshop.shop_config.cash_box_container", "收银箱容器:");
        add("gui.foodieshop.shop_config.redefine_area", "重新定义范围");
        add("gui.foodieshop.shop_config.preview_area", "预览范围");
        add("gui.foodieshop.shop_config.shop_area", "店铺边界:");
        add("gui.foodieshop.shop_config.pos1", "点1: %s");
        add("gui.foodieshop.shop_config.pos2", "点2: %s");
        add("gui.foodieshop.shop_config.edit_seats", "编辑座位");
        add("gui.foodieshop.shop_config.edit_tables", "编辑桌子");
        add("gui.foodieshop.shop_config.edit_waypoints", "编辑路径点");
        add("gui.foodieshop.shop_config.seats", "座位数: %d");
        add("gui.foodieshop.shop_config.tables", "桌子数: %d");
        add("gui.foodieshop.shop_config.waypoints", "路径点数: %d");
        add("gui.foodieshop.shop_config.validate", "验证配置");
        add("gui.foodieshop.shop_config.validation_results", "验证结果:");
        add("gui.foodieshop.shop_config.logs", "操作日志:");
    }

}
