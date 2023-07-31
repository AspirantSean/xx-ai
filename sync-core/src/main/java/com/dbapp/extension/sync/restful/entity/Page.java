package com.dbapp.extension.sync.restful.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static com.dbapp.extension.sync.restful.RestfulUtils.PAGE_NUM_PARAM;
import static com.dbapp.extension.sync.restful.RestfulUtils.PAGE_SIZE_PARAM;

/**
 * @ClassName Page
 * @Description 分页参数
 * @Author joker.tong
 * @Date 2021/2/20 15:21
 * @Version 1.0
 **/
public class Page {
    //页码
    @JsonProperty(PAGE_NUM_PARAM)
    private final long num;
    //每页记录数
    @JsonProperty(PAGE_SIZE_PARAM)
    private final long size;
    //查询偏移量
    @JsonIgnore
    private final long offset;

    public Page(long num, long size) {
        this.num = num;
        this.size = size;
        this.offset = (this.num - 1) * this.size;
    }

    public Page(long num, long size, long offset) {
        this.num = num;
        this.size = size;
        this.offset = offset;
    }

    public static Page create(long offset, long limit) {
        long num = offset / limit + 1;
        return new Page(num, limit, offset);
    }

    public long getNum() {
        return num;
    }

    public long getSize() {
        return size;
    }

    public long getOffset() {
        return this.offset;
    }

    @JsonCreator
    public static Page of(@JsonProperty(PAGE_NUM_PARAM) long num, @JsonProperty(PAGE_SIZE_PARAM) long size) {
        Page page = new Page(num, size);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getAttribute(PAGE_NUM_PARAM, RequestAttributes.SCOPE_REQUEST) == null) {
            attributes.setAttribute(PAGE_NUM_PARAM, page, RequestAttributes.SCOPE_REQUEST);
        }
        return page;
    }

    public Page nextPage() {
        if (getSize() == 0) {
            return Page.create(getOffset() + getSize(), 100);
        }
        return Page.create(getOffset() + getSize(), getSize());
    }

    @Override
    public String toString() {
        return "page:" + num + ":" + size;
    }
}
