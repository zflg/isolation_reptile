package isolation.reptile;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {

  public static final ObjectMapper MAPPER = new ObjectMapper();
  public static final ObjectMapper MAPPER_NON_NULL = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

  static {
    JavaTimeModule module = new JavaTimeModule();
    module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
    module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
    module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));
    module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME));

    MAPPER.registerModule(module);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    MAPPER_NON_NULL.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    MAPPER_NON_NULL.registerModule(module);
    MAPPER_NON_NULL.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER_NON_NULL.setSerializationInclusion(Include.NON_NULL);
  }

  private JsonUtils() {

  }

  public static String toString(Object obj) {
    return toString(obj, true);
  }

  public static String toString(Object obj, boolean writeNullValue) {
    if (obj == null) {
      return null;
    }
    if (obj.getClass() == String.class) {
      return (String) obj;
    }
    try {
      return writeNullValue ? MAPPER.writeValueAsString(obj) : MAPPER_NON_NULL.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      logger.error("json serialization error:" + obj, e);
      return null;
    }
  }


  public static String toStringPretty(Object obj) {
    return toStringPretty(obj, true);
  }

  public static String toStringPretty(Object obj, boolean writeNullValue) {
    if (obj == null) {
      return null;
    }
    if (obj.getClass() == String.class) {
      return (String) obj;
    }
    try {
      return writeNullValue ? MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
          : MAPPER_NON_NULL.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      logger.error("json serialization error:" + obj, e);
      return null;
    }
  }

  public static <T> T toBean(String json, Class<T> tClass) {
    try {
      return MAPPER.readValue(json, tClass);
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return null;
    }
  }

  public static <T> T toBean(String json, Class<T> tClass, DeserializationFeature... deserializationFeature) {
    try {
      for (DeserializationFeature feature : deserializationFeature) {
        MAPPER.disable(feature);
      }
      return MAPPER.readValue(json, tClass);
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return null;
    }
  }

  public static <E> List<E> toList(String json, Class<E> eClass) {
    try {
      return MAPPER
          .readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, eClass));
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return new ArrayList<>();
    }
  }

  public static <E> List<E> toList(String json, Class<E> eClass, DeserializationFeature... deserializationFeature) {
    try {
      for (DeserializationFeature feature : deserializationFeature) {
        MAPPER.disable(feature);
      }
      return MAPPER
          .readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, eClass));
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return new ArrayList<>();
    }
  }

  public static <E> List<E> toList(String json, TypeReference<List<E>> typeReference) {
    try {
      return MAPPER.readValue(json, typeReference);
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return new ArrayList<>();
    }
  }

  public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
    try {
      return MAPPER
          .readValue(json, MAPPER.getTypeFactory().constructMapType(Map.class, kClass, vClass));
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return null;
    }
  }

  public static <T> T nativeRead(String json, TypeReference<T> type) {
    try {
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return null;
    }
  }

  public static <T> T nativeRead(String json, JavaType type) {
    try {
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return null;
    }
  }

  public static <T> T nativeRead(String json, TypeReference<T> type, DeserializationFeature deserializationFeature) {
    try {
      MAPPER.disable(deserializationFeature);
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      logger.error("json analysis error:" + json, e);
      return null;
    }
  }
}
