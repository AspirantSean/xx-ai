package com.dbapp.extension.sync.restful.response;


import com.dbapp.extension.sync.restful.entity.Page;
import com.dbapp.extension.sync.restful.entity.Sort;
import com.dbapp.extension.sync.restful.entity.SuccessCode;
import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.dbapp.extension.sync.restful.RestfulUtils.SORT_PARAM;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * @ClassName DataResponse
 * @Description restful数据返回格式
 * @Author joker.tong
 * @Date 2021/2/21 9:47
 * @Version 1.0
 **/
public class DataResponse<T> extends ApiResponse<T> {
    @JsonIgnore
    private SuccessCode code;
    //分页
    @JsonInclude(NON_NULL)
    @JsonUnwrapped
    private Page page;

    //排序参数
    @JsonInclude(NON_NULL)
    @JsonProperty(SORT_PARAM)
    private String orderBy;

    //记录总数
    @JsonInclude(NON_NULL)
    private Long total;

    //返回数据
    private T data;

    //附加属性
    private Map<String, Object> props;

    public SuccessCode getCode() {
        return code;
    }

    public void setCode(SuccessCode code) {
        this.code = code;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }

    @JsonAnySetter
    public void addProp(String key, Object value) {
        if (this.props == null) {
            this.props = new HashMap<>();
        }
        this.props.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getProps() {
        return this.props;
    }

    public static class DataBuilder<T> {
        private DataResponse<T> dataRes;

        public DataBuilder(SuccessCode code, T data) {
            this.dataRes = new DataResponse<>();
            this.dataRes.setCode(code);
            this.dataRes.setData(data);
        }

        public DataBuilder<T> data(T data) {
            dataRes.setData(data);
            return this;
        }

        public DataBuilder<T> page(Page page) {
            dataRes.setPage(page);
            return this;
        }

        public DataBuilder<T> sort(Sort sort) {
            dataRes.setOrderBy(sort.getParam());
            return this;
        }

        public DataBuilder<T> sort(String sort) {
            dataRes.setOrderBy(sort);
            return this;
        }

        public DataBuilder<T> total(Long total) {
            dataRes.setTotal(total);
            return this;
        }

        public DataBuilder<T> props(Map<String, Object> props) {
            dataRes.setProps(props);
            return this;
        }

        public DataBuilder<T> prop(String key, Object value) {
            dataRes.addProp(key, value);
            return this;
        }

        public <V extends T> DataResponse<V> build() {
            return (DataResponse<V>) dataRes;
        }
    }
}
