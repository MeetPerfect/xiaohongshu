package com.kaiming.xiaohongshu.auth.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import com.kaiming.framework.common.util.JsonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * ClassName: JacksonConfig
 * Package: com.kaiming.xiaohongshu.auth.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/1 21:43
 * @Version 1.0
 */
@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        
        // 初始化ObjectMapper 对象，用于自定义Jackson的行为
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // 设置时区 
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 使用JavaTimeModule设置序列化和反序列化规则
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // 支持 YearMonth
        javaTimeModule.addSerializer(YearMonth.class, new YearMonthSerializer(DateTimeFormatter.ofPattern("yyyy-MM")));
        javaTimeModule.addDeserializer(YearMonth.class, new YearMonthDeserializer(DateTimeFormatter.ofPattern("yyyy-MM")));
        
        objectMapper.registerModule(javaTimeModule);
        // 初始化JsonUtils中的ObjectMapper
        JsonUtils.init(objectMapper);
        
        return objectMapper;


    }
}
