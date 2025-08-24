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
        addItem(ModItems.SHOP_INVENTORY_PEN, "店铺库存笔");
        addItem(ModItems.SHOP_PATH_PEN, "店铺路径笔");

        // 信息
        add("message.foodieshop.diner_blueprint_pen.bind_success", "成功绑定店铺位置: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.switch_mode", "切换到 %s 模式");
        add("message.foodieshop.diner_blueprint_pen.set_area_pos2", "设置店铺区域位置2: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.set_area_pos1", "设置店铺区域位置1: %d, %d, %d");
        add("setup_mode.foodieshop.shop_area", "店铺区域");
        add("setup_mode.foodieshop.seat", "座位");
        add("setup_mode.foodieshop.table", "桌子");
        add("setup_mode.foodieshop.path", "路径");
        add("mode.foodieshop.inventory", "库存");
        add("mode.foodieshop.delivery_box", "收货箱");
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
        add("message.foodieshop.diner_blueprint_pen.area_invalid_cashier_not_in", "区域设置无效: 收银台必须在店铺区域内。位置2已重置。");
        add("message.foodieshop.table_exists_at_pos", "该位置已存在桌子！");
        add("message.foodieshop.seat_exists_at_pos", "该位置已存在座位！");
        add("message.foodieshop.pos_already_occupied", "该位置已被占用！");
        add("message.foodieshop.diner_blueprint_pen.seat_bound_to_table", "已将座位 [%d, %d, %d] 绑定到桌子 [%d, %d, %d]");
        add("message.foodieshop.diner_blueprint_pen.tables_combined", "已将桌子 [%d, %d, %d] 与桌子 [%d, %d, %d] 合并");
        add("message.foodieshop.diner_blueprint_pen.selected", "已选中方块: %d, %d, %d");
        add("message.foodieshop.diner_blueprint_pen.selection_cancelled", "选择已取消");
        add("message.foodieshop.diner_blueprint_pen.wrong_type_for_selection", "无法在当前模式下选择此类型的方块");
        add("message.foodieshop.diner_blueprint_pen.seat_not_adjacent_to_table", "绑定失败：座位必须与桌子相邻");
        add("message.foodieshop.diner_blueprint_pen.tables_not_adjacent", "组合失败：桌子必须相邻");
        add("message.foodieshop.shop_inventory_pen.mode_switched", "切换到 %s 模式");
        add("message.foodieshop.shop_inventory_pen.bound_to_cashier_desk", "已绑定到收银台: %s");
        add("message.foodieshop.shop_inventory_pen.not_bound", "请先右键点击一个收银台来绑定");
        add("message.foodieshop.shop_inventory_pen.cashier_desk_not_found", "绑定的收银台未找到");
        add("message.foodieshop.shop_inventory_pen.inventory_pos_added", "库存位置已添加: %s");
        add("message.foodieshop.shop_inventory_pen.inventory_pos_removed", "库存位置已移除: %s");
        add("message.foodieshop.shop_inventory_pen.delivery_box_pos_added", "收货箱位置已添加: %s");
        add("message.foodieshop.shop_inventory_pen.delivery_box_pos_removed", "收货箱位置已移除: %s");
        add("message.foodieshop.shop_inventory_pen.pos_selected", "已选择位置: %s, 再次右键点击以移除");
        add("message.foodieshop.cannot_edit", "你没有权限编辑这家商店");

        // GUI
        add("gui.foodieshop.shop_config.tab.general", "基础设置");
        add("gui.foodieshop.shop_config.tab.layout", "区域与布局");
        add("gui.foodieshop.shop_config.tab.menu_inventory", "菜单与库存");
        add("gui.foodieshop.shop_config.tab.save", "保存与验证");
        add("gui.foodieshop.shop_config.stock", "库存");
        add("gui.foodieshop.close", "关闭");
        add("gui.foodieshop.save", "保存");
        add("gui.foodieshop.shop_config.owner_name", "店主名称:");
        add("gui.foodieshop.shop_config.shop_name", "店铺名称:");
        add("gui.foodieshop.shop_config.shop_location", "店铺位置:");
        add("gui.foodieshop.shop_config.reset_layout", "重置布局");
        add("gui.foodieshop.shop_config.validate", "验证配置");
        add("gui.foodieshop.shop_config.validation_results", "验证结果:");
        add("gui.foodieshop.shop_config.logs", "操作日志:");
        add("gui.foodieshop.shop_config.reset_layout.confirm.title", "确认重置布局");
        add("gui.foodieshop.shop_config.reset_layout.confirm.message", "你确定要重置所有布局设置吗？此操作无法撤销。");

        // GUI - 图例
        add("gui.foodieshop.shop_config.legend.table", "桌子");
        add("gui.foodieshop.shop_config.legend.seat", "座位");
        add("gui.foodieshop.shop_config.legend.inventory", "库存");
        add("gui.foodieshop.shop_config.legend.delivery_box", "收货箱");
        add("gui.foodieshop.shop_config.legend.cashier_desk", "收银台");
        add("gui.foodieshop.shop_config.legend.waypoint", "路径点");
        add("gui.foodieshop.shop_config.legend.entry", "入口");
        add("gui.foodieshop.shop_config.legend.exit", "出口");

        // 配置
        add("foodieshop.configuration.title", "FoodieShop 配置");
        add("foodieshop.configuration.canModifyTradingConfig", "是否允许修改交易配置");
        add("foodieshop.configuration.section.foodieshop.trading.server.toml", "FoodieShop 服务器配置");
        add("foodieshop.configuration.section.foodieshop.trading.server.toml.title", "FoodieShop 服务器配置");
        add("foodieshop.configuration.canModifyTradingConfig.tooltip", "是否允许修改交易配置");
        add("foodieshop.config.title", "FoodieShop 交易配置");
        add("foodieshop.config.reload", "重新加载");
        add("foodieshop.config.reload_trading", "重新加载交易配置");
        add("foodieshop.config.open_folder", "打开配置文件夹");
        add("foodieshop.config.reloaded", "交易配置已重新加载！");
        add("foodieshop.config.trading_setting.title", "交易设置");
        add("foodieshop.config.trading_setting.button", "交易设置...");
        add("foodieshop.config.trading_setting.no_world.title", "需要加载世界");
        add("foodieshop.config.trading_setting.no_world.message", "请先进入一个世界，然后再进行配置。");
        add("foodieshop.config.trading_setting.save_disabled.title", "保存已禁用");
        add("foodieshop.config.trading_setting.save_disabled.message", "服务器配置不允许修改交易配置。");

        // 配置-交易配置
        add("foodieshop.config.trading_setting.button.global_settings", "全局设置");
        add("foodieshop.config.trading_setting.button.sellable_items", "可售物品");
        add("foodieshop.config.trading_setting.button.currency_items", "货币物品");
        add("foodieshop.config.trading_setting.button.fixed_trades", "固定交易");
        add("foodieshop.config.trading_setting.button.mod_settings", "模组设置");
        add("foodieshop.config.trading_setting.search_placeholder", "搜索...");
        add("foodieshop.config.trading_setting.add_button", "添加");
        add("foodieshop.config.trading_setting.settings_button", "设置");
        add("foodieshop.config.save", "保存");

        // 配置-交易配置-弹窗
        add("foodieshop.config.trading_setting.popup.add_item_title", "添加新条目");
        add("foodieshop.config.trading_setting.popup.item_mode", "物品添加");
        add("foodieshop.config.trading_setting.popup.mod_mode", "模组添加");
        add("foodieshop.config.trading_setting.popup.item_id", "物品ID");
        add("foodieshop.config.trading_setting.popup.price", "价格");
        add("foodieshop.config.trading_setting.popup.mod_id", "模组ID");
    }

}
