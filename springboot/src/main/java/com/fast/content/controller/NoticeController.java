package com.fast.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.content.domain.Notice;
import com.fast.content.mapper.NoticeMapper;
import com.fast.content.service.INoticeService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 公告信息
 */
@RestController
@RequestMapping("/content/notice")
public class NoticeController extends BaseController {
    @Resource
    private INoticeService noticeService;

    @Resource
    private NoticeMapper noticeMapper;

    /**
     * 查询公告列表
     */
    @GetMapping("/selectNoticeList")
    public TableDataInfo selectNoticeList(Notice notice) {
        Page<Notice> page = startPage();
        page = noticeService.selectNoticeList(page, notice);
        return getDataTable(page);
    }

    /**
     * 根据公告ID查询公告信息
     */
    @GetMapping("/selectNoticeByNoticeId/{noticeId}")
    public AjaxResult selectNoticeByNoticeId(@PathVariable Long noticeId) {
        return success(noticeMapper.selectById(noticeId));
    }

    /**
     * 新增公告
     */
    @PostMapping("/insertNotice")
    public AjaxResult insertNotice(@RequestBody Notice notice) {
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改公告
     */
    @PutMapping("/updateNotice")
    public AjaxResult updateNotice(@RequestBody Notice notice) {
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/deleteNoticeByNoticeIds/{noticeIds}")
    public AjaxResult deleteNoticeByNoticeIds(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByNoticeIds(noticeIds));
    }
}
