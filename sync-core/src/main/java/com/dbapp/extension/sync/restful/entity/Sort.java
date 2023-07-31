package com.dbapp.extension.sync.restful.entity;

import cn.hutool.core.util.StrUtil;
import com.dbapp.extension.sync.restful.RestfulUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

/**
 * @ClassName Sort
 * @Description 排序参数
 * @Author joker.tong
 * @Date 2021/2/20 15:26
 * @Version 1.0
 **/
public class Sort {
    //接口传入的排序参数字符串
    private final String param;
    //解析后的排序参数对象
    private final List<Order> orders;

    public Sort(String param, List<Order> orders) {
        this.param = param;
        this.orders = orders;
    }

    public static Sort toMe(String orderBy) {
        Sort sortable;
        if (StringUtils.isEmpty(orderBy)) {
            sortable = new Sort(orderBy, EMPTY_LIST);
        } else {
            List<Order> orders = Arrays.stream(orderBy.split(RestfulUtils.ORDER_DELIMITER))
                    .filter(StringUtils::isNotBlank)
                    .map(sort -> {
                        sort = sort.trim();
                        String[] sortArr = sort.split(RestfulUtils.DIRECTION_DELIMITER);
                        if (sortArr.length == 1) {
                            return new Order(sortArr[0], RestfulUtils.DEFAULT_DIRECTION);
                        }
                        return new Order(sortArr[0], Direction.fromString(sortArr[1]));
                    }).collect(Collectors.toList());
            sortable = new Sort(orderBy, Collections.unmodifiableList(orders));
        }
        return sortable;
    }

    public String getParam() {
        return param;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public static class Order {
        //排序字段
        private final String property;
        //排序类型，正序或倒序
        private final Direction direction;

        public Order(String property, Direction direction) {
            this.property = property;
            this.direction = direction;
        }

        public String getProperty() {
            return property;
        }

        public Direction getDirection() {
            return direction;
        }
    }

    public enum Direction {
        ASC,
        DESC;

        public static Direction fromString(String value) {
            try {
                return valueOf(value.toUpperCase(Locale.US));
            } catch (Exception var2) {
                throw new IllegalArgumentException(String.format("Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), var2);
            }
        }
    }

    @Override
    public String toString() {
        String template = "{} {}";
        return "sort" + orders.stream()
                .map(item -> StrUtil.format(template, item.getProperty(), item.getDirection().name())).collect(Collectors.joining(","));
    }
}
