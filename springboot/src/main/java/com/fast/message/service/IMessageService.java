package com.fast.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.message.domain.Message;

import java.util.List;

/**
 * 用户留言消息 业务接口
 */
public interface IMessageService {

    /**
     * 分页查询消息列表
     * @param page 分页对象
     * @param message 查询参数
     * @return 分页结果
     */
    Page<Message> selectMessageList(Page<Message> page, Message message);

    /**
     * 删除消息
     * @param messageIds 消息ID数组
     * @return 是否删除成功
     */
    int deleteMessageByMessageIds(Long[] messageIds);

    /**
     * 分页查询全站会话列表（管理端对话记录用）
     * @param page 分页对象
     * @param keyword 关键词（模糊匹配双方用户名/物品标题/最新留言内容）
     * @return 分页结果, 每个"物品+用户对"组合一条最新留言, 附带留言总数和未读数
     */
    Page<Message> selectConversationList(Page<Message> page, String keyword);

    /**
     * 查询我的会话列表（小程序消息中心用）
     * @param userId 当前登录用户ID
     * @return 会话列表, 每个"物品+对方"组合一条最新留言, 附带未读数
     */
    List<Message> selectMyConversationList(Long userId);

    /**
     * 查询我和对方围绕某件物品的完整对话（小程序对话页用）
     * 打开会话时顺带把对方发给我的未读消息标记为已读
     * @param userId 当前登录用户ID
     * @param peerUserId 对方用户ID
     * @param itemId 关联物品ID
     * @return 消息列表（按发送时间正序）
     */
    List<Message> selectConversation(Long userId, Long peerUserId, Long itemId);

    /**
     * 发送一条留言
     * @param message 消息对象（fromUserId 已由 Controller 填好）
     * @return 是否发送成功
     */
    int sendMessage(Message message);
}
