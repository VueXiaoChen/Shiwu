package com.fast.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.content.domain.Notice;
import com.fast.content.mapper.NoticeMapper;
import com.fast.content.service.INoticeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 公告 业务处理类
 */
@Service
public class NoticeServiceImpl implements INoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    /**
     * 分页查询公告列表
     * @param page 分页对象
     * @param notice 查询参数
     * @return 分页结果
     */
    @Override
    public Page<Notice> selectNoticeList(Page<Notice> page, Notice notice) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        //标题模糊查询
        wrapper.like(StringUtils.isNotBlank(notice.getTitle()), Notice::getTitle, notice.getTitle());
        //最新的公告排在前面
        wrapper.orderByDesc(Notice::getCreateTime);
        return noticeMapper.selectPage(page, wrapper);
    }

    /**
     * 新增公告
     * @param notice 表单参数
     * @return 是否新增成功
     */
    @Override
    public int insertNotice(Notice notice) {
        return noticeMapper.insert(notice);
    }

    /**
     * 修改公告
     * @param notice 表单参数
     * @return 是否修改成功
     */
    @Override
    public int updateNotice(Notice notice) {
        return noticeMapper.updateById(notice);
    }

    /**
     * 删除公告
     * @param noticeIds 公告ID数组
     * @return 是否删除成功
     */
    @Override
    public int deleteNoticeByNoticeIds(Long[] noticeIds) {
        return noticeMapper.deleteBatchIds(Arrays.asList(noticeIds));
    }
}
