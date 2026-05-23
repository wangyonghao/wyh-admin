
package top.wyhao.starter.web.config;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.*;
import com.fasterxml.jackson.datatype.jsr310.ser.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import top.wyhao.starter.core.enums.BaseEnum;
import top.wyhao.starter.core.util.GeneralPropertySourceFactory;
import top.wyhao.starter.web.json.serializer.BaseEnumDeserializer;
import top.wyhao.starter.web.json.serializer.BaseEnumSerializer;
import top.wyhao.starter.web.json.serializer.BigNumberSerializer;
import top.wyhao.starter.web.json.serializer.SimpleDeserializersWrapper;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 自动配置
 *


 * @since 1.0.0
 */
@AutoConfiguration
@PropertySource(value = "classpath:default-json-jackson.yml", factory = GeneralPropertySourceFactory.class)
public class JacksonAutoConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            JavaTimeModule javaTimeModule = this.javaTimeModule();
            SimpleModule baseEnumModule = this.baseEnumModule();
            SimpleModule bigNumberModule = this.bigNumberModule();

            builder.timeZone(TimeZone.getDefault());
            builder.modules(javaTimeModule, baseEnumModule, bigNumberModule);
        };
    }

    /**
     * 日期时间序列化及反序列化配置
     *
     * @return {@link JavaTimeModule}
     */
    private JavaTimeModule javaTimeModule() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // LocalDateTime 的序列化和反序列化处理
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        // LocalDate 的序列化和反序列化处理
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        // LocalTime 的序列化和反序列化处理
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN);
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
        // Instant 的序列化和反序列化处理
        javaTimeModule.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        javaTimeModule.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        // Duration 的序列化和反序列化处理
        javaTimeModule.addSerializer(Duration.class, DurationSerializer.INSTANCE);
        javaTimeModule.addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
        return javaTimeModule;
    }

    /**
     * 枚举序列化及反序列化配置
     *
     * @return {@link SimpleModule}
     */
    private SimpleModule baseEnumModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BaseEnum.class, BaseEnumSerializer.INSTANCE);
        SimpleDeserializersWrapper deserializers = new SimpleDeserializersWrapper();
        deserializers.addDeserializer(BaseEnum.class, BaseEnumDeserializer.INSTANCE);
        simpleModule.setDeserializers(deserializers);
        return simpleModule;
    }

    /**
     * 大数值序列化及反序列化配置
     *
     * @return {@link SimpleModule}
     */
    private SimpleModule bigNumberModule() {
        SimpleModule bigNumberModule = new SimpleModule();
        bigNumberModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        bigNumberModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        bigNumberModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
        return bigNumberModule;
    }
}
