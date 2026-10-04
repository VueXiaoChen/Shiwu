package com.fast.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.message.domain.Message;
import com.fast.message.mapper.MessageMapper;
import com.fast.message.service.IMessageService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户留言消息
 * 管理端: 管理员查看/清理用户之间的留言, 不参与发消息
 * 小程序端: 用户围绕某件物品和对方留言沟通（会话列表/对话记录/发送）
 */
@RestController
@RequestMapping("/message/message")
public class MessageController extends BaseController {
    @Resource
    private IMessageService messageService;

    @Resource
    private MessageMapper messageMapper;

    /**
     * 查询消息列表
     * 支持 fromUserName/toUserName/content/isRead/itemId 筛选
     */
    @GetMapping("/selectMessageList")
    public TableDataInfo selectMessageList(Message message) {
        Page<Message> page = startPage();
        page = messageService.selectMessageList(page, message);
        return getDataTable(page);
    }

    /**
     * 根据消息ID查询消息信息
     */
    @GetMapping("/selectMessageByMessageId/{messageId}")
    public AjaxResult selectMessageByMessageId(@PathVariable Long messageId) {
        return success(messageMapper.selectMessageByMessageId(messageId));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/deleteMessageByMessageIds/{messageIds}")
    public AjaxResult deleteMessageByMessageIds(@PathVariable Long[] messageIds) {
        return toAjax(messageService.deleteMessageByMessageIds(messageIds));
    }

    /**
     * 全站会话列表（管理端对话记录左侧栏）
     * 每个"物品+用户对"组合返回最新一条留言, 附带留言总数和未读数
     */
    @GetMapping("/selectConversationList")
    public TableDataInfo selectConversationList(String keyword) {
        Page<Message> page = startPage();
        page = messageService.selectConversationList(page, keyword);
        return getDataTable(page);
    }

    /**
     * 某个会话的完整对话记录（管理端对话记录右侧栏）
     * 管理员只是旁观, 不标记已读, 直接复用小程序对话查询（收发双方是对称的）
     */
    @GetMapping("/selectConversationDetail")
    public AjaxResult selectConversationDetail(Long itemId, Long userAId, Long userBId) {
        if (itemId == null || userAId == null || userBId == null) {
            return error("参数不完整");
        }
        return success(messageMapper.selectConversation(userAId, userBId, itemId));
    }

    /**
     * 我的会话列表（小程序消息中心）
     * 每个"物品+对方"组合返回最新一条留言, 附带未读数
     */
    @GetMapping("/selectMyConversationList")
    public AjaxResult selectMyConversationList() {
        return success(messageService.selectMyConversationList(SecurityUtils.getUserId()));
    }

    /**
     * 我和对方围绕某件物品的完整对话（小程序对话页）
     * 打开会话时顺带把对方发给我的未读消息标记为已读
     */
    @GetMapping("/selectConversation")
    public AjaxResult selectConversation(Long itemId, Long peerUserId) {
        if (itemId == null || peerUserId == null) {
            return error("参数不完整");
        }
        return success(messageService.selectConversation(SecurityUtils.getUserId(), peerUserId, itemId));
    }

    /**
     * 发送一条留言（小程序对话页）
     * 发送人就是当前登录用户, 前端只用传 itemId、toUserId、content
     */
    @PostMapping("/sendMessage")
    public AjaxResult sendMessage(@RequestBody Message message) {
        if (message.getItemId() == null || message.getToUserId() == null
                || message.getContent() == null || message.getContent().trim().isEmpty()) {
            return error("留言内容不能为空");
        }

        Long userId = SecurityUtils.getUserId();
        //自己给自己留言没有意义, 拦一道
        if (userId.equals(message.getToUserId())) {
            return error("不能给自己留言");
        }

        message.setFromUserId(userId);
        return toAjax(messageService.sendMessage(message));
    }
}
