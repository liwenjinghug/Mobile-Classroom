package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.AttendanceQr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface AttendanceQrMapper {

    @Insert("INSERT INTO class_attendance_qr(task_id, token, ttl_seconds, expire_time, used, create_by, create_time) VALUES(#{taskId}, #{token}, #{ttlSeconds}, #{expireTime}, #{used}, #{createBy}, NOW())")
    int insertQr(AttendanceQr qr);

    @Select("SELECT * FROM class_attendance_qr WHERE token = #{token}")
    AttendanceQr selectByToken(String token);

    @Select("SELECT * FROM class_attendance_qr WHERE qr_id = #{qrId}")
    AttendanceQr selectById(Long qrId);

    @Update("UPDATE class_attendance_qr SET used = #{used} WHERE qr_id = #{qrId}")
    int updateUsed(AttendanceQr qr);
}

