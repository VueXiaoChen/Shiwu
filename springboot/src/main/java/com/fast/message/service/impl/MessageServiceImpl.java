package com.fast.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.message.domain.Message;
import com.fast.message.mapper.MessageMapper;
import com.fast.message.service.IMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 用户留言消息 业务处理类
 */
@Service
public class MessageServiceImpl implements IMessageService {
    @Resource
    private MessageMapper messageMapper;

    /**
     * 分页查询消息列表
     * @param page 分页对象
     * @param message 查询参数
     * @return 分页结果
     */
    @Override
    public Page<Message> selectMessageList(Page<Message> page, Message message) {
        //筛选和联表都写在XML里, 这里直接把参数递过去就行
        return messageMapper.selectMessageList(page, message);
    }

    /**
     * 删除消息
     * @param messageIds 消息ID数组
     * @return 是否删除成功
     */
    @Override
    public int deleteMessageByMessageIds(Long[] messageIds) {
        return messageMapper.deleteBatchIds(Arrays.asList(messageIds));
    }

    /**
     * 分页查询全站会话列表（管理端对话记录用）
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Override
    public Page<Message> selectConversationList(Page<Message> page, String keyword) {
        //分组取最新一条+统计总数/未读数的活都在XML的SQL里干了
        return messageMapper.selectConversationList(page, keyword);
    }

    /**
     * 查询我的会话列表
     * @param userId 当前登录用户ID
     * @return 会话列表
     */
    @Override
    public List<Message> selectMyConversationList(Long userId) {
        //分组取最新+数未读的活都在XML的SQL里干了
        return messageMapper.selectMyConversationList(userId);
    }

    /**
     * 查询我和对方围绕某件物品的完整对话, 顺带把对方发来的标成已读
     * @param userId 当前登录用户ID
     * @param peerUserId 对方用户ID
     * @param itemId 关联物品ID
     * @return 消息列表
     */
    @Override
    public List<Message> selectConversation(Long userId, Long peerUserId, Long itemId) {
        //既然都点进来看了, 对方发给我的这些消息自然就算已读了
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getItemId, itemId)
                .eq(Message::getFromUserId, peerUserId)
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);
        messageMapper.update(null, wrapper);

        return messageMapper.selectConversation(userId, peerUserId, itemId);
    }

    /**
     * 发送一条留言
     * @param message 消息对象
     * @return 是否发送成功
     */
    @Override
    public int sendMessage(Message message) {
        //补全默认值: 新消息肯定是未读的, 时间用服务器时间
        message.setContent(message.getContent().trim());
        message.setIsRead(0);
        message.setCreateTime(new Date());
        return messageMapper.insert(message);
    }
}
