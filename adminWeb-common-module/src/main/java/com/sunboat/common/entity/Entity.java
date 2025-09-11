package com.sunboat.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 权限表，用于定义系统中的各种权限
 * </p>
 *
 * @author Sunboat
 * @since 2025-04-10
 */
@Getter
@Setter
@TableName("entity")
public class Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限唯一标识，自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


}
