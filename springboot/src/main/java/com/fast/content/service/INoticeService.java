package com.fast.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.content.domain.Notice;

/**
 * 公告 业务接口
 */
public interface INoticeService {

    /**
     * 分页查询公告列表
     * @param page 分页对象
     * @param notice 查询参数
     * @return 分页结果
     */
    Page<Notice> selectNoticeList(Page<Notice> page, Notice notice);

    /**
     * 新增公告
     * @param notice 表单参数
     * @return 是否新增成功
     */
    int insertNotice(Notice notice);

    /**
     * 修改公告
     * @param notice 表单参数
     * @return 是否修改成功
     */
    int updateNotice(Notice notice);

    /**
     * 删除公告
     * @param noticeIds 公告ID数组
     * @return 是否删除成功
     */
    int deleteNoticeByNoticeIds(Long[] noticeIds);
}
