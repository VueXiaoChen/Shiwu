package com.fast.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.message.domain.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户留言消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    /**
     * 分页查询消息列表（关联查询收发双方用户名和物品标题）
     * @param page 分页对象
     * @param message 查询参数
     * @return 分页结果
     */
    Page<Message> selectMessageList(@Param("page") Page<Message> page, @Param("message") Message message);

    /**
     * 通过消息ID查询消息信息（关联查询收发双方用户名和物品标题）
     * @param messageId 消息ID
     * @return 消息对象信息
     */
    Message selectMessageByMessageId(Long messageId);

    /**
     * 分页查询全站会话列表（管理端对话记录用）
     * 每个"物品+用户对"组合只取最新一条留言, 顺带统计该会话的留言总数和未读数
     * @param page 分页对象
     * @param keyword 关键词（模糊匹配双方用户名/物品标题/最新留言内容）
     * @return 分页结果（按最新留言时间倒序）
     */
    Page<Message> selectConversationList(@Param("page") Page<Message> page, @Param("keyword") String keyword);

    /**
     * 查询我的会话列表（小程序消息中心用）
     * 每个"物品+对方"组合只取最新一条留言, 顺带统计对方发给我的未读数
     * @param userId 当前登录用户ID
     * @return 会话列表（按最新留言时间倒序）
     */
    List<Message> selectMyConversationList(@Param("userId") Long userId);

    /**
     * 查询我和对方围绕某件物品的完整对话（小程序对话页用）
     * @param userId 当前登录用户ID
     * @param peerUserId 对方用户ID
     * @param itemId 关联物品ID
     * @return 消息列表（按发送时间正序, 老的在上新的在下）
     */
    List<Message> selectConversation(@Param("userId") Long userId,
                                     @Param("peerUserId") Long peerUserId,
                                     @Param("itemId") Long itemId);
}
