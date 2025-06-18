package com.zhangyu.sca.module.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 签到信息表
 *
 * @TableName pay_sign_in_info
 */
@TableName(value = "pay_sign_in_info")
@Data
@Builder
@Accessors(chain = true)
public class SignInInfo implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.NONE)
    private Long Id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 签到月份
     */
    @TableField(value = "sign_in_month")
    private String signInMonth;

    /**
     * 签到天数
     */
    @TableField(value = "sign_in_days")
    private Integer signInDays;

    /**
     * 连续签到天数
     */
    @TableField(value = "continuous_sign_in_days")
    private Integer continuousSignInDays;

    /**
     * 签到日历
     */
    @TableField(value = "sign_in_calendar")
    private byte[] signInCalendar;

    /**
     * 补签日历
     */
    @TableField(value = "resign_in_calendar")
    private byte[] resignInCalendar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}